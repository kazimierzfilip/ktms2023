package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_user_profile")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserProfile {
    @EmbeddedId
    private ProjectUserProfileId id;
    @ManyToOne
    @MapsId("project_id")
    private Project project;
    @ManyToOne
    @MapsId("user_id")
    private User user;
    @ManyToOne
    @MapsId("profile_id")
    private Profile profile;

    public ProjectUserProfile(Project project, User user, Profile profile) {
        this.id = new ProjectUserProfileId(project.getId(), user.getId(), profile.getId());
        this.project = project;
        this.user = user;
        this.profile = profile;
    }
}
