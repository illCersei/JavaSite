package cersei.octopusservice.dto;

public record RenameOctopusRequest(
        String nickname
) {
    public RenameOctopusRequest {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("nickname не может быть пустым");
        }
        nickname = nickname.trim();
        if (nickname.length() > 32) {
            throw new IllegalArgumentException("nickname не может быть длиннее 32 символов");
        }
    }
}
