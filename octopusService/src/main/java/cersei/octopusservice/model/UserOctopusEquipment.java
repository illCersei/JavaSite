package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.ItemSlot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_octopus_equipment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_octopus_equipment_slot",
                columnNames = {"user_octopus_id", "slot"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UserOctopusEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_octopus_id", nullable = false)
    private UserOctopus userOctopus;

    /**
     * Item template referenced from the user's stash (movement between octopuses = update this row).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ItemSlot slot;
}
