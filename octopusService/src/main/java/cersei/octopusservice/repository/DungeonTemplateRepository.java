package cersei.octopusservice.repository;

import cersei.octopusservice.model.DungeonTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DungeonTemplateRepository extends JpaRepository<DungeonTemplate, Integer> {
}