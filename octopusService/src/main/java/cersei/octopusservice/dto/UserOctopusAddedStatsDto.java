package cersei.octopusservice.dto;

import cersei.octopusservice.utils.StatsForUpgrade;

public record UserOctopusAddedStatsDto(
        StatsForUpgrade stat,
        Integer currentStat,
        Integer leftFreePoints
){}
