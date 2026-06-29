package cersei.octopusservice.dto;

public record UserItemStackDto(
        ItemDto item,
        int quantity
) {
}
