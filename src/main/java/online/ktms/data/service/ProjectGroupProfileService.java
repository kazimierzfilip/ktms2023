package online.ktms.data.service;

import online.ktms.data.entity.Group;
import online.ktms.data.entity.Profile;
import online.ktms.data.entity.Project;
import online.ktms.data.entity.ProjectGroupProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectGroupProfileService {

    private final ProjectGroupProfileRepository repository;

    public ProjectGroupProfileService(ProjectGroupProfileRepository repository) {
        this.repository = repository;
    }

    public ProjectGroupProfile create(Project project, Group group, Profile profile) {
        ProjectGroupProfile projectGroupProfile = new ProjectGroupProfile(project, group, profile);
        return repository.save(projectGroupProfile);
    }
}
