package cersei.octopusservice.service;

import cersei.octopusservice.dto.UserItemStackDto;
import cersei.octopusservice.exception.InsufficientItemQuantityException;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.UserItemStack;
import cersei.octopusservice.repository.UserItemStackRepository;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserItemService {

    private final UserItemStackRepository userItemStackRepository;
    private final ItemCatalogService itemCatalogService;
    private final ItemDtoMapper itemDtoMapper;

    @Transactional(readOnly = true)
    public List<UserItemStackDto> listInventory(UUID userId) {
        List<UserItemStackDto> items = userItemStackRepository.findByUserIdOrderByItem_IdAsc(userId).stream()
                .filter(stack -> stack.getQuantity() > 0)
                .map(this::toDto)
                .toList();
        log.info("Игрок {} запросил инвентарь предметов, строк={}", userId, items.size());
        return items;
    }

    @Transactional
    public int addItems(UUID userId, int itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity должен быть больше 0");
        }
        if (itemId <= 0) {
            throw new IllegalArgumentException("itemId должен быть больше 0");
        }
        Item item = itemCatalogService.requireById(itemId);
        UserItemStack row = userItemStackRepository.findByUserIdAndItem_Id(userId, itemId)
                .orElseGet(() -> newStack(userId, item));
        int added = quantity;
        row.setQuantity(row.getQuantity() + quantity);
        row.setUpdatedAt(Instant.now());
        userItemStackRepository.save(row);
        log.info(
                "Игрок {} получил предмет itemId={} name={} +{} -> quantity={}",
                userId,
                itemId,
                item.getName(),
                added,
                row.getQuantity()
        );
        return row.getQuantity();
    }

    @Transactional
    public int consumeItems(UUID userId, int itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity должен быть больше 0");
        }
        if (itemId <= 0) {
            throw new IllegalArgumentException("itemId должен быть больше 0");
        }
        UserItemStack row = userItemStackRepository.findByUserIdAndItem_Id(userId, itemId)
                .orElseThrow(() -> new InsufficientItemQuantityException(itemId, quantity, 0));
        if (row.getQuantity() < quantity) {
            throw new InsufficientItemQuantityException(itemId, quantity, row.getQuantity());
        }
        row.setQuantity(row.getQuantity() - quantity);
        row.setUpdatedAt(Instant.now());
        userItemStackRepository.save(row);
        log.info(
                "Игрок {} потратил предмет itemId={} -{} -> quantity={}",
                userId,
                itemId,
                quantity,
                row.getQuantity()
        );
        return row.getQuantity();
    }

    private UserItemStack newStack(UUID userId, Item item) {
        UserItemStack stack = new UserItemStack();
        stack.setUserId(userId);
        stack.setItem(item);
        stack.setQuantity(0);
        stack.setUpdatedAt(Instant.now());
        return stack;
    }

    private UserItemStackDto toDto(UserItemStack stack) {
        return new UserItemStackDto(itemDtoMapper.toDto(stack.getItem()), stack.getQuantity());
    }
}
