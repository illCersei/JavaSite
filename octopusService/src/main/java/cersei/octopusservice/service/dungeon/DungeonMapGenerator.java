package cersei.octopusservice.service.dungeon;

import cersei.octopusservice.model.DungeonRun;
import cersei.octopusservice.model.DungeonRunRoom;
import cersei.octopusservice.model.DungeonRunRoomLink;
import cersei.octopusservice.model.DungeonTemplate;
import cersei.octopusservice.model.utils.DungeonRoomStatus;
import cersei.octopusservice.model.utils.DungeonRoomType;
import cersei.octopusservice.service.enemy.EnemyCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Decides room layout, type and (for combat rooms) which enemy - nothing about loot. Loot is
// rolled lazily by DungeonLootResolver once a room is actually resolved (see DungeonService),
// so nothing here spoils a reward before the player has earned it.
@Component
@RequiredArgsConstructor
@Slf4j
public class DungeonMapGenerator {

    private static final int CHEST_WEIGHT = 30;
    private static final int ELITE_COMBAT_WEIGHT = 15;
    private static final int EVENT_WEIGHT = 10;
    private static final int SHOP_WEIGHT = 5;
    // Remainder of the 0..99 roll (40%) falls through to BATTLE.

    private final EnemyCatalogService enemyCatalogService;

    public GeneratedDungeonMap generate(DungeonRun run, DungeonTemplate template) {
        Random random = new Random(run.getRngSeed());
        int depthLayers = template.getDepthLayers();
        List<List<DungeonRunRoom>> layers = new ArrayList<>();

        for (int layer = 0; layer < depthLayers; layer++) {
            // Layer 0 gets exactly 1 room like the boss layer does: markStartRoom() below only
            // ever marks slot 0 of layer 0 AVAILABLE, and connectLayers() only links layer N to
            // N+1 (nothing links *into* layer 0) - so any extra layer-0 room would be LOCKED
            // forever with no way to ever reach it. A single fixed entry point is also just what
            // "start room" means for this map shape.
            int roomCount = (layer == 0 || layer == depthLayers - 1) ? 1 : 2 + random.nextInt(2);
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
            return room;
        }

        DungeonRoomType roomType = rollRoomType(random);
        room.setRoomType(roomType);
        if (roomType == DungeonRoomType.BATTLE || roomType == DungeonRoomType.ELITE_COMBAT) {
            room.setEnemyTemplateId(enemyCatalogService.pickMobForTier(template.getTier(), random));
        }
        return room;
    }

    private DungeonRoomType rollRoomType(Random random) {
        int roll = random.nextInt(100);
        if (roll < CHEST_WEIGHT) {
            return DungeonRoomType.CHEST;
        }
        roll -= CHEST_WEIGHT;
        if (roll < ELITE_COMBAT_WEIGHT) {
            return DungeonRoomType.ELITE_COMBAT;
        }
        roll -= ELITE_COMBAT_WEIGHT;
        if (roll < EVENT_WEIGHT) {
            return DungeonRoomType.EVENT;
        }
        roll -= EVENT_WEIGHT;
        if (roll < SHOP_WEIGHT) {
            return DungeonRoomType.SHOP;
        }
        return DungeonRoomType.BATTLE;
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
