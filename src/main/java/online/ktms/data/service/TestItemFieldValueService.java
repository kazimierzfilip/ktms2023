package online.ktms.data.service;

import jakarta.transaction.Transactional;
import online.ktms.data.entity.TestItem;
import online.ktms.data.entity.TestItemFieldValue;
import online.ktms.data.entity.TestItemFieldValueId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class TestItemFieldValueService {

    private final TestItemFieldValueRepository repository;

    public TestItemFieldValueService(TestItemFieldValueRepository repository) {
        this.repository = repository;
    }

    public TestItemFieldValue update(TestItemFieldValue entity) {
        return repository.save(entity);
    }

    public TestItemFieldValue updateFieldValue(TestItemFieldValueId fieldId, String value) {
        TestItemFieldValue field = repository.findById(fieldId).get();
        field.setFieldValue(value);
        return repository.save(field);
    }

    public Page<TestItemFieldValue> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<TestItemFieldValue> list(Pageable pageable, Specification<TestItemFieldValue> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
