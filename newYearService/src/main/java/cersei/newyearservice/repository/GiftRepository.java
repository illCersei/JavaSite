package cersei.newyearservice.repository;

import cersei.newyearservice.model.Gift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GiftRepository extends JpaRepository<Gift, Long> {
}

