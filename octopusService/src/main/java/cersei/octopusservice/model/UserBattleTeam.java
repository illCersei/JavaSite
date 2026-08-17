package cersei.octopusservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_battle_team")
@Getter
@Setter
@NoArgsConstructor
public class UserBattleTeam {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "user_octopus_ids", columnDefinition = "integer[]", nullable = false)
    private List<Integer> userOctopusIds;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}