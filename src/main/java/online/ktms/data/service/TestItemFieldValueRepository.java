package online.ktms.data.service;

import online.ktms.data.entity.TestItemFieldValue;
import online.ktms.data.entity.TestItemFieldValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestItemFieldValueRepository extends JpaRepository<TestItemFieldValue, TestItemFieldValueId>, JpaSpecificationExecutor<TestItemFieldValue> {

}
