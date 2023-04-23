package online.ktms.data.service;

import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import jakarta.transaction.Transactional;
import online.ktms.data.Status;
import online.ktms.data.TestItemType;
import online.ktms.data.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TestItemRepository testItemRepository;
    private final TestItemService testItemService;
    private final ProjectGroupProfileService projectGroupProfileService;
    private final ProjectUserProfileService projectUserProfileService;
    private final TestItemFieldValueRepository testItemFieldValueRepository;
    private final TestItemTemplateRepository testItemTemplateRepository;
    private final TestItemTemplateService testItemTemplateService;

    public ProjectService(ProjectRepository projectRepository, TestItemRepository testItemRepository, TestItemService testItemService, ProjectGroupProfileService projectGroupProfileService, ProjectUserProfileService projectUserProfileService, TestItemFieldValueRepository testItemFieldValueRepository, TestItemTemplateRepository testItemTemplateRepository, TestItemTemplateService testItemTemplateService) {
        this.projectRepository = projectRepository;
        this.testItemRepository = testItemRepository;
        this.testItemService = testItemService;
        this.projectGroupProfileService = projectGroupProfileService;
        this.projectUserProfileService = projectUserProfileService;
        this.testItemFieldValueRepository = testItemFieldValueRepository;
        this.testItemTemplateRepository = testItemTemplateRepository;
        this.testItemTemplateService = testItemTemplateService;
    }

    public Page<Project> list(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Transactional
    public TestItem addTestItem(Project project, TestItemType type, String name, TestItem parent, TestItemTemplate template) {
        project = projectRepository.findById(project.getId()).get();

        TestItem testItem = new TestItem();
        testItem.setProject(project);
        testItem.setType(type);
        testItem.setName(name);
        Integer orderIndex;
        if (parent != null) {
            if (parent.getType().equals(TestItemType.TEST_CASE)) {
                orderIndex = parent.getOrderIndex() + 1;
                parent = parent.getParentItem();
                moveTestCaseOrdersToEndFrom(project, parent, orderIndex);
            } else {
                orderIndex = testItemRepository.countByParentItemId(parent.getId());
            }
        } else {
            orderIndex = testItemRepository.countByProjectId(project.getId());
        }
        testItem.setParentItem(parent);
        testItem.setOrderIndex(orderIndex);
        testItem.setTemplate(template);
        if (type.equals(TestItemType.TEST_CASE)) {
            testItem.setCode(project.getCode() + "-" + (project.getLastTestCaseId() + 1));
            project.setLastTestCaseId(project.getLastTestCaseId() + 1);
        }
        if (parent != null) {
            parent.getChildren().add(testItem);
        }
        testItemService.update(testItem);
        for (TestItemField testItemField : template.getTestItemFields()) {
            TestItemFieldValue testItemFieldValue = new TestItemFieldValue();
            testItemFieldValue.setId(new TestItemFieldValueId(testItem.getId(), testItemField.getId()));
            testItemFieldValue.setTestItem(testItem);
            testItemFieldValue.setTestItemField(testItemField);
            testItemFieldValue.setFieldValue(testItemField.getDefaultValue());
            testItemFieldValueRepository.save(testItemFieldValue);
            testItem.getTestItemFieldValues().add(testItemFieldValue);
        }
        project.getTestItems().add(testItem);
        projectRepository.save(project);
        return testItem;
    }

    @Transactional
    public void moveItemToSuite(Project project, TestItem item, TestItem suite) {
        project = refresh(project);
        item = testItemService.refresh(item);
        suite = testItemService.refresh(suite);

        System.out.println("rewrite parent");
        rewriteTestCaseOrdersAfterRemoving(project, item.getParentItem(), item.getOrderIndex());
        System.out.println("set props");
        item.setParentItem(suite);
        item.setOrderIndex(suite.getChildren().size());

        System.out.println("Save");
        testItemRepository.save(item);
        testItemRepository.save(suite);
    }

    @Transactional
    public void moveItemToSuiteAtLocation(Project project, TestItem item, TestItem suite, TestItem relativeToItem, GridDropLocation dropLocation) {
        project = refresh(project);
        item = testItemService.refresh(item);
        if (suite != null)
            suite = testItemService.refresh(suite);
        relativeToItem = testItemService.refresh(relativeToItem);

        rewriteTestCaseOrdersAfterRemoving(project, item.getParentItem(), item.getOrderIndex());

        int orderToSet = dropLocation.equals(GridDropLocation.ABOVE) ?
                relativeToItem.getOrderIndex() - 1 : relativeToItem.getOrderIndex() + 1;
        orderToSet = Math.max(orderToSet, 0);
        moveTestCaseOrdersToEndFrom(project, suite, orderToSet);
        item.setOrderIndex(orderToSet);

        item.setParentItem(suite);

        testItemRepository.save(item);
        if (suite != null)
            testItemRepository.save(suite);
    }

    private Project refresh(Project project) {
        return getWithValues(project.getId()).get();
    }

    private void moveTestCaseOrdersToEndFrom(Project project, TestItem parent, Integer orderIndex) {
        List<TestItem> testItems;
        if (parent == null) {
            testItems = project.getTestItems().stream()
                    .filter(testItem -> testItem.getType().equals(TestItemType.TEST_SUITE) && testItem.getParentItem() == null)
                    .collect(Collectors.toList());
        } else {
            testItems = parent.getChildren();
        }
        testItems.sort(Comparator.comparingInt(TestItem::getOrderIndex));
        for (int i = orderIndex, order = orderIndex + 1; i < testItems.size(); i++, order++) {
            testItems.get(i).setOrderIndex(order);
            testItemService.update(testItems.get(i));
        }
    }

    private void rewriteTestCaseOrdersAfterRemoving(Project project, TestItem parent, Integer orderIndex) {
        List<TestItem> testItems;
        if (parent == null) {
            testItems = project.getTestItems().stream()
                    .filter(testItem -> testItem.getType().equals(TestItemType.TEST_SUITE) && testItem.getParentItem() == null)
                    .collect(Collectors.toList());
        } else {
            testItems = parent.getChildren();
        }
        testItems.sort(Comparator.comparingInt(TestItem::getOrderIndex));
        for (int i = orderIndex + 1, order = orderIndex; i < testItems.size(); i++, order++) {
            testItems.get(i).setOrderIndex(order);
            testItemService.update(testItems.get(i));
        }
    }

    @Transactional
    public Optional<Project> getWithValues(Long id) {
        Optional<Project> optionalProject = projectRepository.findById(id);
        if (optionalProject.isPresent()) {
            Project project = new Project();
            project.setId(optionalProject.get().getId());
            project.setVersion(optionalProject.get().getVersion());
            project.setCode(optionalProject.get().getCode());
            project.setName(optionalProject.get().getName());
            project.setStatus(optionalProject.get().getStatus());
            project.setGroupsAndProfiles(optionalProject.get().getGroupsAndProfiles().stream().collect(Collectors.toSet()));
            project.setUsersAndProfiles(optionalProject.get().getUsersAndProfiles().stream().collect(Collectors.toSet()));
            project.setTestItems(optionalProject.get().getTestItems().stream().collect(Collectors.toList()));
            project.setTestItemTemplates(optionalProject.get().getTestItemTemplates().stream().collect(Collectors.toList()));
            project.setLastTestCaseId(optionalProject.get().getLastTestCaseId());
            return Optional.of(project);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<Project> getWithValues(String code) {
        Optional<Project> optionalProject = projectRepository.findByCode(code);
        if (optionalProject.isPresent()) {
            Project project = new Project();
            project.setId(optionalProject.get().getId());
            project.setVersion(optionalProject.get().getVersion());
            project.setCode(optionalProject.get().getCode());
            project.setName(optionalProject.get().getName());
            project.setStatus(optionalProject.get().getStatus());
            project.setGroupsAndProfiles(optionalProject.get().getGroupsAndProfiles().stream().collect(Collectors.toSet()));
            project.setUsersAndProfiles(optionalProject.get().getUsersAndProfiles().stream().collect(Collectors.toSet()));
            project.setTestItems(optionalProject.get().getTestItems().stream().collect(Collectors.toList()));
            project.setTestItemTemplates(optionalProject.get().getTestItemTemplates().stream().collect(Collectors.toList()));
            project.setLastTestCaseId(optionalProject.get().getLastTestCaseId());
            return Optional.of(project);
        } else {
            return Optional.empty();
        }
    }

    public Project createProject(String code, String name) {
        Project project = new Project(code, name, Status.ACTIVE);
        return projectRepository.save(project);
    }

    public Project closeProject(Project project) {
        project.setStatus(Status.INACTIVE);
        return projectRepository.save(project);
    }

    public void addGroupAccess(Project project, Group group, Profile profile) {
        ProjectGroupProfile projectGroupProfile = projectGroupProfileService.create(project, group, profile);
        project.getGroupsAndProfiles().add(projectGroupProfile);
    }

    public void addUserAccess(Project project, User user, Profile profile) {
        ProjectUserProfile projectUserProfile = projectUserProfileService.create(project, user, profile);
        project.getUsersAndProfiles().add(projectUserProfile);
    }

    public void addTestItemTemplate(Project project, TestItemTemplate template) {
        project.getTestItemTemplates().add(template);
        testItemTemplateService.assignToProject(template, project);
    }

    @Transactional
    public void removeTestItem(TestItem testItem) {
        testItem = testItemService.refresh(testItem);
        Project project = getWithValues(testItem.getProject().getId()).get();

        if (testItem.getParentItem() != null) {
            testItem.getParentItem().getChildren().remove(testItem);
            testItem.setParentItem(null);
        }

        testItem.getTestItemFieldValues().forEach(this::removeTestItemFieldValue);
        testItem.setTestItemFieldValues(null);

        testItem.getChildren().forEach(this::removeTestItem);

        project.getTestItems().remove(testItem);

        testItem.setTemplate(null);

        testItem.setProject(null);

        testItemRepository.save(testItem);
        testItemRepository.delete(testItem);
    }

    private void removeTestItemFieldValue(TestItemFieldValue testItemFieldValue) {
        testItemFieldValueRepository.delete(testItemFieldValue);
    }
}
