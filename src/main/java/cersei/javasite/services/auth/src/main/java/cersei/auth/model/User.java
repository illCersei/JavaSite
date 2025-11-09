package cersei.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private String username;

    @Column(unique = true, nullable = false)
    private String password;

    private String email;
    private String role;
}
