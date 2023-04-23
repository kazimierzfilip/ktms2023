package online.ktms.data.service;

import jakarta.transaction.Transactional;
import online.ktms.data.entity.TestItem;
import online.ktms.data.entity.TestItemFieldValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestItemService {

    private final TestItemRepository repository;
    private final TestItemFieldValueRepository testItemFieldValueRepository;

    public TestItemService(TestItemRepository repository, TestItemFieldValueRepository testItemFieldValueRepository) {
        this.repository = repository;
        this.testItemFieldValueRepository = testItemFieldValueRepository;
    }

    public Optional<TestItem> get(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public TestItem getWithValues(Long id) {
        Optional<TestItem> optionalTestItem = repository.findById(id);
        TestItem testItem = new TestItem();
        testItem.setId(optionalTestItem.get().getId());
        testItem.setVersion(optionalTestItem.get().getVersion());
        testItem.setCode(optionalTestItem.get().getCode());
        testItem.setName(optionalTestItem.get().getName());
        testItem.setProject(optionalTestItem.get().getProject());
        testItem.setParentItem(optionalTestItem.get().getParentItem());
        testItem.setChildren(optionalTestItem.get().getChildren().stream().collect(Collectors.toList()));
        testItem.setTemplate(optionalTestItem.get().getTemplate());
        testItem.setTestItemFieldValues(new HashSet<>(optionalTestItem.get().getTestItemFieldValues()));
        return testItem;
    }

    @Transactional
    public TestItem loadFieldValues(TestItem testItem) {
        Optional<TestItem> optionalTestItem = repository.findById(testItem.getId());
        testItem.setVersion(optionalTestItem.get().getVersion());
        testItem.setName(optionalTestItem.get().getName());
        testItem.setTestItemFieldValues(new HashSet<>(optionalTestItem.get().getTestItemFieldValues()));
        return testItem;
    }

    @Transactional
    public TestItem refresh(TestItem testItem) {
        Optional<TestItem> optionalTestItem = repository.findById(testItem.getId());
        testItem.setId(optionalTestItem.get().getId());
        testItem.setVersion(optionalTestItem.get().getVersion());
        testItem.setCode(optionalTestItem.get().getCode());
        testItem.setName(optionalTestItem.get().getName());
        testItem.setProject(optionalTestItem.get().getProject());
        testItem.setParentItem(optionalTestItem.get().getParentItem());
        testItem.setChildren(optionalTestItem.get().getChildren().stream().collect(Collectors.toList()));
        testItem.setTemplate(optionalTestItem.get().getTemplate());
        testItem.setTestItemFieldValues(new HashSet<>(optionalTestItem.get().getTestItemFieldValues()));
        return testItem;
    }

    public TestItem update(TestItem entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<TestItem> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<TestItem> list(Pageable pageable, Specification<TestItem> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public void updateFieldValue(TestItem testItem, String fieldName, String fieldValue) {
        Optional<TestItemFieldValue> field = testItem.getField(fieldName);
        if (field.isPresent()) {
            field.get().setFieldValue(fieldValue);
            testItemFieldValueRepository.save(field.get());
        } else {
            throw new RuntimeException("Field: " + fieldName + " not found.");
        }
    }

    public TestItem updateName(TestItem testItem, String name) {
        testItem = get(testItem.getId()).get();
        testItem.setName(name);
        return repository.save(testItem);
    }

    @Transactional
    public void reorderItems(TestItem first, TestItem second) {
        first = refresh(first);
        second = refresh(second);

        Integer firstOrderIndex = first.getOrderIndex();
        first.setOrderIndex(second.getOrderIndex());
        second.setOrderIndex(firstOrderIndex);

        repository.save(first);
        repository.save(second);
    }
}
