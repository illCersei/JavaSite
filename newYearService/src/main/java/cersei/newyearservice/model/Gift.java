package cersei.newyearservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "gifts")
@Data
public class Gift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "link")
    private String link;
}

