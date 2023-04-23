package online.ktms.data.service;

import online.ktms.data.entity.*;
import org.springframework.stereotype.Service;

@Service
public class ProjectUserProfileService {

    private final ProjectUserProfileRepository repository;

    public ProjectUserProfileService(ProjectUserProfileRepository repository) {
        this.repository = repository;
    }

    public ProjectUserProfile create(Project project, User user, Profile profile) {
        ProjectUserProfile projectUserProfile = new ProjectUserProfile(project, user, profile);
        return repository.save(projectUserProfile);
    }
}
