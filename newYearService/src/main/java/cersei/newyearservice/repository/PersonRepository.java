package cersei.newyearservice.repository;

import cersei.newyearservice.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.gifts")
    List<Person> findAllWithGifts();
}

