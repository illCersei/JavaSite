package cersei.octopusservice.service.enemy;

import cersei.octopusservice.dto.CombatStatsDto;
import cersei.octopusservice.dto.SkillDto;
import cersei.octopusservice.dto.SkillEffectDto;
import cersei.octopusservice.dto.fight.EnemyTemplateDto;
import cersei.octopusservice.exception.FightServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EnemyCatalogService {

    private final ObjectMapper objectMapper;
    private Map<String, EnemyTemplateDto> byId = Map.of();

    public EnemyCatalogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void load() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("enemy-templates.json").getInputStream());
        Map<String, EnemyTemplateDto> map = new HashMap<>();
        for (JsonNode node : root) {
            EnemyTemplateDto dto = parse(node);
            map.put(dto.id(), dto);
        }
        this.byId = Map.copyOf(map);
        log.info("EnemyCatalog loaded templates={}", byId.size());
    }

    public EnemyTemplateDto requireById(String id) {
        EnemyTemplateDto dto = byId.get(id);
        if (dto == null) {
            throw new FightServiceException("Unknown enemy template: " + id);
        }
        return dto;
    }

    public List<EnemyTemplateDto> findByTier(int tier) {
        return byId.values().stream().filter(e -> e.tier() == tier).toList();
    }

    public String pickMobForTier(int tier, java.util.Random random) {
        List<EnemyTemplateDto> pool = findByTier(tier).stream()
                .filter(e -> !e.id().contains("king"))
                .toList();
        if (pool.isEmpty()) {
            throw new FightServiceException("No mob templates for tier " + tier);
        }
        return pool.get(random.nextInt(pool.size())).id();
    }

    public String pickBossForTier(int tier) {
        return findByTier(tier).stream()
                .filter(e -> e.id().contains("king") || e.id().contains("boss"))
                .map(EnemyTemplateDto::id)
                .findFirst()
                .orElseThrow(() -> new FightServiceException("No boss template for tier " + tier));
    }

    private EnemyTemplateDto parse(JsonNode node) {
        JsonNode stats = node.get("stats");
        List<SkillDto> skills = parseSkills(node.get("skills"));
        return new EnemyTemplateDto(
                node.get("id").asText(),
                node.get("name").asText(),
                node.get("tier").asInt(),
                new CombatStatsDto(
                        stats.get("hp").asInt(),
                        stats.get("attack").asInt(),
                        stats.get("magicPower").asInt(),
                        stats.get("armor").asInt(),
                        stats.get("magicResist").asInt(),
                        stats.get("speed").asInt()
                ),
                skills
        );
    }

    private List<SkillDto> parseSkills(JsonNode skillsNode) {
        if (skillsNode == null || !skillsNode.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(skillsNode.spliterator(), false)
                .map(skill -> {
                    List<SkillEffectDto> effects = java.util.stream.StreamSupport
                            .stream(skill.get("effects").spliterator(), false)
                            .map(effect -> new SkillEffectDto(
                                    effect.get("id").asLong(),
                                    effect.get("effectType").asText(),
                                    effect.get("elementType").asText(),
                                    effect.get("baseValue").asInt(),
                                    textOrNull(effect.get("scalingStat")),
                                    intOrNull(effect.get("scalingRatioBps")),
                                    intOrNull(effect.get("durationMs")),
                                    intOrNull(effect.get("tickMs")),
                                    textOrNull(effect.get("stackingRule"))
                            ))
                            .toList();
                    return new SkillDto(
                            skill.get("id").asInt(),
                            skill.get("name").asText(),
                            skill.get("description").asText(null),
                            skill.get("elementType").asText(),
                            skill.get("cooldownMs").asInt(),
                            skill.get("manaCost").asInt(),
                            effects
                    );
                })
                .toList();
    }

    private static String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static Integer intOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asInt();
    }
}