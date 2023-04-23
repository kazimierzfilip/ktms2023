package online.ktms.data.service;

import online.ktms.data.entity.TestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

public interface TestItemRepository extends JpaRepository<TestItem, Long>, JpaSpecificationExecutor<TestItem> {

    Set<TestItem> findByProjectCode(String projectCode);

    TestItem findByCode(String code);

    Integer countByParentItemId(Long parentItemId);

    Integer countByProjectId(Long projectId);
}
