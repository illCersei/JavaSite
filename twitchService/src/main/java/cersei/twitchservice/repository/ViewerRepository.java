package cersei.twitchservice.repository;

import cersei.twitchservice.dto.ViewerDto;
import cersei.twitchservice.model.Viewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewerRepository extends JpaRepository<Viewer, Integer> {
    @Query("SELECT new cersei.twitchservice.dto.ViewerDto(CAST(v.dateTime AS LocalDate), MAX(v.viewers)) " +
            "FROM Viewer v " +
            "GROUP BY CAST(v.dateTime AS LocalDate) ORDER BY CAST(v.dateTime AS LocalDate)")
    List<ViewerDto> findMaxViewersGroupedByDay();
}
