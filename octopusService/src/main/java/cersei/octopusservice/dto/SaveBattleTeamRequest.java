package cersei.octopusservice.dto;

import java.util.List;

public record SaveBattleTeamRequest(
        List<Integer> userOctopusIds
) {
    public SaveBattleTeamRequest {
        if (userOctopusIds == null || userOctopusIds.size() != 3) {
            throw new IllegalArgumentException("userOctopusIds должен содержать ровно 3 id");
        }
        long distinct = userOctopusIds.stream().distinct().count();
        if (distinct != 3) {
            throw new IllegalArgumentException("В команде не должно быть одинаковых осьминогов");
        }
        if (userOctopusIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Каждый userOctopusId должен быть больше 0");
        }
    }
}
