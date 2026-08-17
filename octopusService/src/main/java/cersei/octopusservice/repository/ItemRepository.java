package cersei.octopusservice.repository;

import cersei.octopusservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByTierOrderByIdAsc(Integer tier);

    List<Item> findAllByOrderByIdAsc();
}
