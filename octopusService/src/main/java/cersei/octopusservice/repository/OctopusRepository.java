package cersei.octopusservice.repository;

import cersei.octopusservice.model.Octopus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OctopusRepository extends JpaRepository<Octopus, Integer> {
}
