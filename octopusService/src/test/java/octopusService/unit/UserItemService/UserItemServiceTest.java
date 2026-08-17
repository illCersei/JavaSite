package octopusService.unit.UserItemService;

import cersei.octopusservice.exception.InsufficientItemQuantityException;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.UserItemStack;
import cersei.octopusservice.model.utils.ItemSlot;
import cersei.octopusservice.repository.UserItemStackRepository;
import cersei.octopusservice.service.ItemCatalogService;
import cersei.octopusservice.service.UserItemService;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserItemServiceTest {

    @Mock
    private UserItemStackRepository userItemStackRepository;

    @Mock
    private ItemCatalogService itemCatalogService;

    private UserItemService userItemService;

    private final UUID userId = UUID.randomUUID();
    private Item item;

    @BeforeEach
    void setUp() {
        ItemDtoMapper itemDtoMapper = new ItemDtoMapper();
        ReflectionTestUtils.setField(itemDtoMapper, "itemIconUrlTemplate", "item-%d.png");

        userItemService = new UserItemService(
                userItemStackRepository,
                itemCatalogService,
                itemDtoMapper
        );

        item = new Item();
        item.setId(1);
        item.setName("Coral Spear");
        item.setSlot(ItemSlot.WEAPON);
        item.setTier(1);
        item.setAttackStat(6);
        item.setMagicPowerStat(0);
        item.setArmorStat(2);
        item.setMagicResistStat(1);
        item.setSpeedStat(1);
    }

    @Test
    void when_AddItems_ToNewStack_CreatesRow() {
        when(itemCatalogService.requireById(1)).thenReturn(item);
        when(userItemStackRepository.findByUserIdAndItem_Id(userId, 1)).thenReturn(Optional.empty());
        when(userItemStackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int quantityAfter = userItemService.addItems(userId, 1, 2);

        assertEquals(2, quantityAfter);

        ArgumentCaptor<UserItemStack> captor = ArgumentCaptor.forClass(UserItemStack.class);
        verify(userItemStackRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getQuantity());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void when_ConsumeItems_WithInsufficientQuantity_Throws() {
        UserItemStack stack = new UserItemStack();
        stack.setUserId(userId);
        stack.setItem(item);
        stack.setQuantity(1);

        when(userItemStackRepository.findByUserIdAndItem_Id(userId, 1)).thenReturn(Optional.of(stack));

        assertThrows(
                InsufficientItemQuantityException.class,
                () -> userItemService.consumeItems(userId, 1, 2)
        );
    }

    @Test
    void when_ConsumeItems_DecrementsQuantity() {
        UserItemStack stack = new UserItemStack();
        stack.setUserId(userId);
        stack.setItem(item);
        stack.setQuantity(3);

        when(userItemStackRepository.findByUserIdAndItem_Id(userId, 1)).thenReturn(Optional.of(stack));
        when(userItemStackRepository.save(stack)).thenReturn(stack);

        int quantityAfter = userItemService.consumeItems(userId, 1, 2);

        assertEquals(1, quantityAfter);
        verify(userItemStackRepository).save(stack);
    }
}
