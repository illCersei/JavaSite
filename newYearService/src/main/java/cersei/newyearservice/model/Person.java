package cersei.newyearservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "persons")
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gift> gifts = new ArrayList<>();
}

