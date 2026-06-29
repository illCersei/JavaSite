package cersei.octopusservice.service;

import cersei.octopusservice.dto.UserOctopusDto;
import cersei.octopusservice.exception.OctopusNotFoundException;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.UserOctopus;
import cersei.octopusservice.model.UserOctopusEquipment;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.repository.UserOctopusEquipmentRepository;
import cersei.octopusservice.repository.UserOctopusRepository;
import cersei.octopusservice.service.useroctopus.utils.UserOctopusDtoAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEquipmentService {

    private final UserOctopusRepository userOctopusRepository;
    private final UserOctopusEquipmentRepository equipmentRepository;
    private final ItemCatalogService itemCatalogService;
    private final UserItemService userItemService;
    private final UserOctopusDtoAssembler assembler;

    @Transactional
    public UserOctopusDto equip(UUID userId, int userOctopusId, int itemId) {
        log.info(
                "Игрок {} надевает itemId={} на осьминога userOctopusId={}",
                userId,
                itemId,
                userOctopusId
        );

        UserOctopus userOctopus = requireOwnedOctopus(userId, userOctopusId);
        Item item = itemCatalogService.requireById(itemId);

        userItemService.consumeItems(userId, itemId, 1);

        equipmentRepository.findByUserOctopus_IdAndSlot(userOctopusId, item.getSlot())
                .ifPresentOrElse(
                        existing -> replaceEquipment(userId, userOctopusId, existing, item),
                        () -> createEquipment(userId, userOctopus, item)
                );

        log.info(
                "Игрок {} успешно экипировал itemId={} slot={} на userOctopusId={}",
                userId,
                itemId,
                item.getSlot(),
                userOctopusId
        );

        return assembler.toDto(userOctopus);
    }

    @Transactional
    public UserOctopusDto unequip(UUID userId, int userOctopusId, ItemSlot slot) {
        log.info(
                "Игрок {} снимает slot={} с userOctopusId={}",
                userId,
                slot,
                userOctopusId
        );

        UserOctopus userOctopus = requireOwnedOctopus(userId, userOctopusId);
        UserOctopusEquipment equipment = equipmentRepository
                .findByUserOctopus_IdAndSlot(userOctopusId, slot)
                .orElseThrow(() -> new IllegalArgumentException("Слот пуст: " + slot));

        int itemId = equipment.getItem().getId();
        userItemService.addItems(userId, itemId, 1);
        equipmentRepository.delete(equipment);

        log.info(
                "Игрок {} снял itemId={} slot={} с userOctopusId={}, предмет возвращён в stash",
                userId,
                itemId,
                slot,
                userOctopusId
        );

        return assembler.toDto(userOctopus);
    }

    private void replaceEquipment(
            UUID userId,
            int userOctopusId,
            UserOctopusEquipment existing,
            Item newItem
    ) {
        int oldItemId = existing.getItem().getId();
        log.info(
                "Слот {} на userOctopusId={} занят: заменяем itemId={} -> itemId={}",
                existing.getSlot(),
                userOctopusId,
                oldItemId,
                newItem.getId()
        );
        userItemService.addItems(userId, oldItemId, 1);
        existing.setItem(newItem);
        equipmentRepository.save(existing);
    }

    private void createEquipment(UUID userId, UserOctopus userOctopus, Item item) {
        UserOctopusEquipment equipment = new UserOctopusEquipment();
        equipment.setUserOctopus(userOctopus);
        equipment.setItem(item);
        equipment.setSlot(item.getSlot());
        equipmentRepository.save(equipment);
        log.info(
                "Создана экипировка userOctopusId={} slot={} itemId={} для игрока {}",
                userOctopus.getId(),
                item.getSlot(),
                item.getId(),
                userId
        );
    }

    private UserOctopus requireOwnedOctopus(UUID userId, int userOctopusId) {
        return userOctopusRepository.findByIdAndUserId(userOctopusId, userId)
                .orElseThrow(() -> new OctopusNotFoundException(userOctopusId));
    }
}
