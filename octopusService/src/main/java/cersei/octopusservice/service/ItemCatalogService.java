package cersei.octopusservice.service;

import cersei.octopusservice.dto.ItemDto;
import cersei.octopusservice.exception.ItemNotFoundException;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.repository.ItemRepository;
import cersei.octopusservice.service.useritem.ItemDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCatalogService {

    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;

    public Item requireById(int itemId) {
        log.info("ItemCatalog lookup id={} (source=db)", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    public ItemDto getById(int itemId) {
        return itemDtoMapper.toDto(requireById(itemId));
    }

    public List<ItemDto> getAll() {
        log.info("Getting all items, (source=db)");
        return itemRepository.findAllByOrderByIdAsc().stream()
                .map(itemDtoMapper::toDto)
                .toList();
    }

    public List<Item> findByTier(int tier) {
        log.debug("ItemCatalog findByTier tier={}", tier);
        return itemRepository.findByTierOrderByIdAsc(tier);
    }
}
