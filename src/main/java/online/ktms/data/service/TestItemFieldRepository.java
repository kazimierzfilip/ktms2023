package online.ktms.data.service;

import online.ktms.data.entity.TestItemField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestItemFieldRepository extends JpaRepository<TestItemField, Long>, JpaSpecificationExecutor<TestItemField> {

}
