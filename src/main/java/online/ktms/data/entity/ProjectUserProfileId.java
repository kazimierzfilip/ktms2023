package online.ktms.data.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserProfileId {
    private Long project_id;
    private Long user_id;
    private Long profile_id;
}
