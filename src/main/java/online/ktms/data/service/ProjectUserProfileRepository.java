package online.ktms.data.service;

import online.ktms.data.entity.ProjectUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectUserProfileRepository extends JpaRepository<ProjectUserProfile, Long>, JpaSpecificationExecutor<ProjectUserProfile> {

}
