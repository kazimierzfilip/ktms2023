package online.ktms.data.service;

import online.ktms.data.entity.ProjectGroupProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectGroupProfileRepository extends JpaRepository<ProjectGroupProfile, Long>, JpaSpecificationExecutor<ProjectGroupProfile> {

}
