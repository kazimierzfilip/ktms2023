package online.ktms.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.ktms.data.Status;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {

    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @JsonIgnore
    private String hashedPassword;

    @ManyToMany(mappedBy = "users", fetch = FetchType.EAGER)
    private Set<Group> groups = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Status status;

    public User(String username, String name, String hashedPassword, Status status) {
        this.username = username;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.status = status;
    }
}
