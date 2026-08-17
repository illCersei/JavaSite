package cersei.octopusservice.controller;

import cersei.octopusservice.dto.EquipItemRequest;
import cersei.octopusservice.dto.ItemDto;
import cersei.octopusservice.dto.UnequipItemRequest;
import cersei.octopusservice.dto.UserItemStackDto;
import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.service.ItemCatalogService;
import cersei.octopusservice.service.UserEquipmentService;
import cersei.octopusservice.service.UserItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Инвентарь предметов и экипировка")
public class UserItemController {

    private final UserItemService userItemService;
    private final UserEquipmentService userEquipmentService;
    private final ItemCatalogService itemCatalogService;

    @GetMapping
    public List<UserItemStackDto> myItems(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userItemService.listInventory(userId);
    }

    @GetMapping("/catalog")
    public List<ItemDto> itemCatalog() {
        return itemCatalogService.getAll();
    }

    @PostMapping("/equip")
    public UserOctopusDto equip(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody EquipItemRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userEquipmentService.equip(userId, request.userOctopusId(), request.itemId());
    }

    @PostMapping("/unequip")
    public UserOctopusDto unequip(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UnequipItemRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("uuid"));
        return userEquipmentService.unequip(userId, request.userOctopusId(), request.slot());
    }
}
