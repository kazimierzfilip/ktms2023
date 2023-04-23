package online.ktms.data.service;

import online.ktms.data.TestItemFieldType;
import online.ktms.data.entity.Project;
import online.ktms.data.entity.TestItemField;
import online.ktms.data.entity.TestItemTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestItemTemplateService {

    private final TestItemTemplateRepository repository;
    private final TestItemFieldRepository fieldRepository;

    public TestItemTemplateService(TestItemTemplateRepository repository, TestItemFieldRepository fieldRepository) {
        this.repository = repository;
        this.fieldRepository = fieldRepository;
    }

    public TestItemTemplate create(String name) {
        TestItemTemplate template = new TestItemTemplate(name);
        return repository.save(template);
    }

    public TestItemTemplate addField(TestItemTemplate template, String fieldName, TestItemFieldType fieldType, String defaultValue) {
        TestItemField field = fieldRepository.save(new TestItemField(fieldName, fieldType, template, defaultValue));
        template.getTestItemFields().add(field);
        return repository.save(template);
    }

    public TestItemTemplate assignToProject(TestItemTemplate template, Project project) {
        template.setProject(project);
        return repository.save(template);
    }
}
