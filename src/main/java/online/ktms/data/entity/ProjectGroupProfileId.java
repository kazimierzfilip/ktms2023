package online.ktms.data.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGroupProfileId {
    private Long project_id;
    private Long group_id;
    private Long profile_id;
}
