namespace FightService.Application.Contracts;

public sealed record FightActionRequest(string ActorId, int SkillId, IReadOnlyList<string> TargetIds);
