package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_group_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGroupProfile {
    @EmbeddedId
    private ProjectGroupProfileId id;
    @ManyToOne
    @MapsId("project_id")
    private Project project;
    @ManyToOne
    @MapsId("group_id")
    private Group group;
    @ManyToOne
    @MapsId("profile_id")
    private Profile profile;

    public ProjectGroupProfile(Project project, Group group, Profile profile) {
        this.id = new ProjectGroupProfileId(project.getId(), group.getId(), profile.getId());
        this.project = project;
        this.group = group;
        this.profile = profile;
    }
}
