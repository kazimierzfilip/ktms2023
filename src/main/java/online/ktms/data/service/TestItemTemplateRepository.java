package online.ktms.data.service;

import online.ktms.data.entity.TestItemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestItemTemplateRepository extends JpaRepository<TestItemTemplate, Long>, JpaSpecificationExecutor<TestItemTemplate> {

}
