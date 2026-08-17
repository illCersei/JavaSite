package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_item_stack",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_item_stack",
                columnNames = {"user_id", "item_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UserItemStack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}

