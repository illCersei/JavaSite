package cersei.octopusservice.repository;

import cersei.octopusservice.model.Octopus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OctopusCatalogRepository extends JpaRepository<Octopus, Integer> {
}
