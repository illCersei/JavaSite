package cersei.twitchservice.repository;

import cersei.twitchservice.model.Games;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//PagingAndSortingRepository нужно сделать
@Repository
public interface GamesRepository extends JpaRepository<Games, Long> {
}
