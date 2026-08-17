using Microsoft.EntityFrameworkCore;

namespace FightService.Infrastructure.Persistence;

public sealed class FightDbContext(DbContextOptions<FightDbContext> options) : DbContext(options)
{
    public DbSet<BattleSessionEntity> BattleSessions => Set<BattleSessionEntity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<BattleSessionEntity>(entity =>
        {
            entity.ToTable("battle_sessions");
            entity.HasKey(e => e.BattleId);
            entity.Property(e => e.BattleId).HasColumnName("battle_id");
            entity.Property(e => e.UserId).HasColumnName("user_id");
            entity.Property(e => e.Status).HasColumnName("status");
            entity.Property(e => e.StateJson).HasColumnName("state").HasColumnType("jsonb");
            entity.Property(e => e.CreatedAt).HasColumnName("created_at");
            entity.Property(e => e.UpdatedAt).HasColumnName("updated_at");
            entity.HasIndex(e => e.UserId).HasDatabaseName("idx_battle_sessions_user_id");
        });
    }
}
