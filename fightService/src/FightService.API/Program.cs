using System.Security.Claims;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using FightService.Application;
using FightService.Application.Contracts;
using FightService.Domain.Exceptions;
using FightService.Domain.ValueObjects;
using FightService.Infrastructure.Persistence;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenApi();

builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    options.SerializerOptions.PropertyNameCaseInsensitive = true;
    options.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});

string connectionString = builder.Configuration.GetConnectionString("FightDb")
    ?? throw new InvalidOperationException("ConnectionStrings:FightDb is not configured");
builder.Services.AddDbContext<FightDbContext>(options => options.UseNpgsql(connectionString));

builder.Services.AddScoped<IBattleSessionRepository, BattleSessionRepository>();
builder.Services.AddSingleton<BattleEngine>();

string? jwtSecret = builder.Configuration["Jwt:Secret"];
if (string.IsNullOrWhiteSpace(jwtSecret))
{
    throw new InvalidOperationException(
        "Jwt:Secret is not configured - set the Jwt__Secret env var to the same JWT_SECRET the rest of the backend uses");
}

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = false,
            ValidateAudience = false,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret)),
            ValidAlgorithms = [SecurityAlgorithms.HmacSha256],
            NameClaimType = "sub"
        };
    });
builder.Services.AddAuthorization();

string[] allowedOrigins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() ?? [];
const string corsPolicyName = "fight-service-cors";
builder.Services.AddCors(options => options.AddPolicy(corsPolicyName, policy => policy
    .SetIsOriginAllowed(origin =>
        allowedOrigins.Contains(origin) ||
        (Uri.TryCreate(origin, UriKind.Absolute, out Uri? originUri)
            && originUri.Scheme == Uri.UriSchemeHttp
            && originUri.Host == "localhost"))
    .AllowAnyMethod()
    .AllowAnyHeader()
    .AllowCredentials()));

var app = builder.Build();

using (IServiceScope scope = app.Services.CreateScope())
{
    scope.ServiceProvider.GetRequiredService<FightDbContext>().Database.Migrate();
}

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.Use(async (context, next) =>
{
    try
    {
        await next();
    }
    catch (DomainException ex)
    {
        context.Response.StatusCode = StatusCodes.Status400BadRequest;
        await context.Response.WriteAsJsonAsync(new { error = ex.Message });
    }
});

app.UseCors(corsPolicyName);
app.UseAuthentication();
app.UseAuthorization();

app.MapPost("/fight/start", async (
        FightStartRequest request,
        IBattleSessionRepository repository,
        BattleEngine engine,
        ClaimsPrincipal user,
        CancellationToken cancellationToken) =>
    {
        Guid userId = RequireUserId(user);
        if (request.Context.UserId != userId)
        {
            return Results.Forbid();
        }

        var session = engine.StartBattle(request);
        await repository.SaveAsync(session, cancellationToken);
        return Results.Ok(BattleMapper.ToStateDto(session));
    })
    .RequireAuthorization();

app.MapPost("/fight/{battleId}/action", async (
        string battleId,
        FightActionRequest request,
        IBattleSessionRepository repository,
        BattleEngine engine,
        ClaimsPrincipal user,
        CancellationToken cancellationToken) =>
    {
        Guid userId = RequireUserId(user);
        var session = await repository.LoadForUpdateAsync(BattleId.Create(battleId), cancellationToken);
        if (session is null)
        {
            return Results.NotFound();
        }
        if (session.UserId != userId)
        {
            return Results.Forbid();
        }

        engine.SubmitAction(session, request.ActorId, request.SkillId, request.TargetIds);
        await repository.SaveAsync(session, cancellationToken);
        return Results.Ok(BattleMapper.ToStateDto(session));
    })
    .RequireAuthorization();

app.MapGet("/fight/{battleId}/state", async (
        string battleId,
        IBattleSessionRepository repository,
        ClaimsPrincipal user,
        CancellationToken cancellationToken) =>
    {
        Guid userId = RequireUserId(user);
        var session = await repository.LoadAsync(BattleId.Create(battleId), cancellationToken);
        if (session is null)
        {
            return Results.NotFound();
        }
        if (session.UserId != userId)
        {
            return Results.Forbid();
        }

        return Results.Ok(BattleMapper.ToStateDto(session));
    })
    .RequireAuthorization();

app.Run();

static Guid RequireUserId(ClaimsPrincipal user)
{
    string? uuid = user.FindFirstValue("uuid");
    if (uuid is null || !Guid.TryParse(uuid, out Guid userId))
    {
        throw new DomainException("JWT is missing a valid 'uuid' claim");
    }
    return userId;
}
