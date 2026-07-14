package cersei.octopusservice.service.dungeon;

import cersei.octopusservice.model.DungeonRun;
import cersei.octopusservice.model.DungeonRunRoom;
import cersei.octopusservice.model.DungeonRunRoomLink;
import cersei.octopusservice.model.DungeonTemplate;
import cersei.octopusservice.model.Item;
import cersei.octopusservice.model.utils.DungeonRoomStatus;
import cersei.octopusservice.model.utils.DungeonRoomType;
import cersei.octopusservice.service.enemy.EnemyCatalogService;
import cersei.octopusservice.service.loot.LootCoinCalculator;
import cersei.octopusservice.service.loot.LootTierRoller;
import cersei.octopusservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DungeonMapGenerator {

    private final EnemyCatalogService enemyCatalogService;
    private final LootTierRoller lootTierRoller;
    private final LootCoinCalculator lootCoinCalculator;
    private final ItemRepository itemRepository;

    public GeneratedDungeonMap generate(DungeonRun run, DungeonTemplate template) {
        Random random = new Random(run.getRngSeed());
        int depthLayers = template.getDepthLayers();
        List<List<DungeonRunRoom>> layers = new ArrayList<>();

        for (int layer = 0; layer < depthLayers; layer++) {
            int roomCount = layer == depthLayers - 1 ? 1 : 2 + random.nextInt(2);
            List<DungeonRunRoom> layerRooms = new ArrayList<>();
            for (int slot = 0; slot < roomCount; slot++) {
                layerRooms.add(createRoom(run, template, layer, slot, random, layer == depthLayers - 1));
            }
            layers.add(layerRooms);
        }

        List<DungeonRunRoomLink> links = connectLayers(run, layers, random);
        markStartRoom(layers.get(0));

        log.info(
                "DungeonMap generated runId={} layers={} totalRooms={} links={}",
                run.getId(),
                layers.size(),
                layers.stream().mapToInt(List::size).sum(),
                links.size()
        );

        return new GeneratedDungeonMap(flatten(layers), links);
    }

    private DungeonRunRoom createRoom(
            DungeonRun run,
            DungeonTemplate template,
            int layer,
            int slot,
            Random random,
            boolean bossLayer
    ) {
        DungeonRunRoom room = new DungeonRunRoom();
        room.setDungeonRun(run);
        room.setLayerIndex(layer);
        room.setSlotIndex(slot);
        room.setRoomStatus(DungeonRoomStatus.LOCKED);

        if (bossLayer) {
            room.setRoomType(DungeonRoomType.BOSS);
            room.setEnemyTemplateId(enemyCatalogService.pickBossForTier(template.getTier()));
            room.setLootCoinsMinor(lootCoinCalculator.coinsForTier(template.getTier()) * 2);
            rollItemLoot(room, template.getTier());
            return room;
        }

        boolean treasure = random.nextInt(100) < 35;
        if (treasure) {
            room.setRoomType(DungeonRoomType.CHEST);
            room.setLootCoinsMinor(lootCoinCalculator.coinsForTier(template.getTier()));
            rollItemLoot(room, template.getTier());
        } else {
            room.setRoomType(DungeonRoomType.BATTLE);
            room.setEnemyTemplateId(enemyCatalogService.pickMobForTier(template.getTier(), random));
            room.setLootCoinsMinor(lootCoinCalculator.coinsForTier(template.getTier()));
            rollItemLoot(room, template.getTier());
        }
        return room;
    }

    private void rollItemLoot(DungeonRunRoom room, int tier) {
        int rolledTier = lootTierRoller.rollTier();
        List<Item> pool = itemRepository.findByTierOrderByIdAsc(rolledTier);
        if (pool.isEmpty()) {
            return;
        }
        Item item = pool.get(new Random(room.getDungeonRun().getRngSeed() + room.getLayerIndex()).nextInt(pool.size()));
        room.setLootItem(item);
        room.setLootQuantity(1);
    }

    private List<DungeonRunRoomLink> connectLayers(
            DungeonRun run,
            List<List<DungeonRunRoom>> layers,
            Random random
    ) {
        List<DungeonRunRoomLink> links = new ArrayList<>();
        for (int layer = 0; layer < layers.size() - 1; layer++) {
            List<DungeonRunRoom> current = layers.get(layer);
            List<DungeonRunRoom> next = layers.get(layer + 1);
            Set<DungeonRunRoom> nextWithIncoming = new HashSet<>();

            for (DungeonRunRoom from : current) {
                int linkCount = 1 + random.nextInt(Math.min(2, next.size()));
                List<DungeonRunRoom> shuffled = new ArrayList<>(next);
                java.util.Collections.shuffle(shuffled, random);
                for (int i = 0; i < linkCount; i++) {
                    DungeonRunRoom to = shuffled.get(i);
                    links.add(createLink(run, from, to));
                    nextWithIncoming.add(to);
                }
            }

            for (DungeonRunRoom to : next) {
                if (!nextWithIncoming.contains(to)) {
                    DungeonRunRoom from = current.get(random.nextInt(current.size()));
                    links.add(createLink(run, from, to));
                }
            }
        }
        return links;
    }

    private DungeonRunRoomLink createLink(DungeonRun run, DungeonRunRoom from, DungeonRunRoom to) {
        DungeonRunRoomLink link = new DungeonRunRoomLink();
        link.setDungeonRunId(run.getId());
        link.setFromRoom(from);
        link.setToRoom(to);
        return link;
    }

    private void markStartRoom(List<DungeonRunRoom> firstLayer) {
        firstLayer.get(0).setRoomStatus(DungeonRoomStatus.AVAILABLE);
    }

    private List<DungeonRunRoom> flatten(List<List<DungeonRunRoom>> layers) {
        return layers.stream().flatMap(List::stream).toList();
    }

    public record GeneratedDungeonMap(
            List<DungeonRunRoom> rooms,
            List<DungeonRunRoomLink> links
    ) {
    }
}