package online.ktms;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import online.ktms.data.Privilege;
import online.ktms.data.TestItemFieldType;
import online.ktms.data.TestItemType;
import online.ktms.data.entity.*;
import online.ktms.data.service.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "ktms")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Component
    public class DataLoader implements ApplicationRunner {

        @Autowired
        private ProjectService projectService;
        @Autowired
        private ProfileService profileService;
        @Autowired
        private GroupService groupService;
        @Autowired
        private UserService userService;
        @Autowired
        private ProjectRepository projectRepository;
        @Autowired
        private TestItemService testItemService;
        @Autowired
        private TestItemTemplateService testItemTemplateService;
        @Autowired
        private TestItemFieldRepository testItemFieldRepository;

        private TestItemTemplate testSuiteTemplate;
        private TestItemTemplate testCaseTemplate;
        private Project ktmsProject;

        public void run(ApplicationArguments args) {

            Profile adminProfile = profileService.createProfile("Admin", Privilege.EDIT_PROJECT_SETTINGS);
            Profile testLeadProfile = profileService.createProfile("Test Lead", Privilege.EDIT_PROJECT_SETTINGS);
            Profile testerProfile = profileService.createProfile("Tester");
            Profile viewerProfile = profileService.createProfile("Viewer");

            ktmsProject = projectService.createProject("KTMS", "kTMS project");
            Project projectA = projectService.createProject("A", "Project A");
            Project projectB = projectService.createProject("B", "Project B");
            Project inactiveProject = projectService.createProject("INACTV", "Inactive project");
            inactiveProject = projectService.closeProject(inactiveProject);

            Group administrators = groupService.create("Administrators");
            Group teamA = groupService.create("Team A");
            Group teamB = groupService.create("Team B");

            projectService.addGroupAccess(ktmsProject, administrators, adminProfile);
            projectService.addGroupAccess(projectA, teamA, testerProfile);
            projectService.addGroupAccess(projectB, teamB, testerProfile);

            User admin = userService.create("Emma Admin", "admin", "admin");
            administrators = groupService.addUser(administrators, admin);

            User userA = userService.create("John tester A", "usera", "usera");
            teamA = groupService.addUser(teamA, userA);

            User userB = userService.create("John tester B", "userb", "userb");
            teamB = groupService.addUser(teamB, userB);

            User viewer = userService.create("John Viewer", "viewer", "viewer");
            User inactive = userService.create("John Inactive", "inactive", "inactive");

            projectService.addUserAccess(ktmsProject, viewer, viewerProfile);

            testSuiteTemplate = testItemTemplateService.create("Test suite template");
            testSuiteTemplate = testItemTemplateService.addField(testSuiteTemplate, "Description", TestItemFieldType.MULTILINE_TEXT, "");
            projectService.addTestItemTemplate(ktmsProject, testSuiteTemplate);

            testCaseTemplate = testItemTemplateService.create("Test case template");
            testCaseTemplate = testItemTemplateService.addField(testCaseTemplate, "Description", TestItemFieldType.MULTILINE_TEXT, "Test feature: ");
            testCaseTemplate = testItemTemplateService.addField(testCaseTemplate, "Preconditions", TestItemFieldType.MULTILINE_TEXT, "Active user with privileges exists.\n");
            projectService.addTestItemTemplate(ktmsProject, testCaseTemplate);

            addSuite("Users", null);
            addSuite("User groups", null);
            addSuite("Profiles", null);
            addSuite("Privileges", null);
            addSuite("Projects Admin", null);
            TestItem projectList = addSuite("Project list", null);
            addTestCase("List of all projects should be visible", projectList);
            addTestCase("List is scrollable", projectList);
            TestItem projectFilter = addTestCase("Projects can be filtered by code and name", projectList);
            updateDescriptionField(projectFilter, "Start typing, list should update based on inserted text (search in code or name).");
            addTestCase("Clicking on project opens project page", projectList);
            addTestCase("Multiple projects can be opened in different browser tabs", projectList);
            TestItem project = addSuite("Project", null);
            TestItem testCases = addSuite("Test cases", project);
            TestItem tree = addSuite("Tree", testCases);
            TestItem treeContent = addSuite("Content", tree);
            addTestCase("All test cases in project are listed in a tree on the left side", treeContent);
            addTestCase("Items are displayed in created order", treeContent);
            addTestCase("List is scrollable", projectList);
            TestItem treeNavigation = addSuite("Navigation", tree);
            addTestCase("Tree list width can be changed using a split toggle", treeNavigation);
            addTestCase("Test suite tree item can be expanded using [+] button and collapsed back with [-]", treeNavigation);
            addTestCase("Order of items can be changed using drag & drop feature", treeNavigation);
            addTestCase("Dragging item on test suite moves this item to the bottom of suite items", treeNavigation);
            addTestCase("Clicking on test suite or test case name in a tree opens it's details", treeNavigation);
            TestItem createTestItem = addTestCase("Creating test items", tree);
            updateDescriptionField(createTestItem, "- Hover on test item to see 'Create' button icon\n- Click 'Create' button icon to open form\n- Click 'Cancel' to give up creating new item\n- Choose item type: test case or test suite\n- Enter item name\n- Click 'Create' button\n\n-Repeat for test suite and test case\n- Create item under root test suite, inner test suite\n- Create item under test case (should be added below)\n- Reload page and check if item in fact created");
            TestItem details = addSuite("Details", testCases);
            addTestCase("Item name and fields are displayed in readonly mode", details);
            addTestCase("Edit button is displayed", details);
            TestItem editTestItem = addTestCase("Editing test cases or test suites", details);
            updateDescriptionField(editTestItem, "After clicking Edit button:\n- Name and other fields become editable and can be changed\n- Edit button changes to Save button\n- After clicking Save values should be saved and details should be again in readonly mode\n- Reload page and check if values are in fact changed");
            TestItem leavingEditedItemDialog = addTestCase("Leaving unsaved work", details);
            updateDescriptionField(leavingEditedItemDialog, "- Open test suite or test case\n- Click edit button\n- Click other item in a tree\n\n- Dialog should appear\n- Clicking 'Don't leave' goes back to unsaved work\n- Clicking 'Discard' ignores any changes made and opens clicked item\n- Clicking 'Save' saves item and opens clicked item");

            for (int a = 0; a < 1; a++) {
                Project perfProject = projectService.createProject("Perf" + a, "Project with large number of test cases and suites");
                TestItemTemplate perfSuiteTemplate = testItemTemplateService.create("Test suite template");
                perfSuiteTemplate = testItemTemplateService.addField(perfSuiteTemplate, "Description", TestItemFieldType.MULTILINE_TEXT, "");
                projectService.addTestItemTemplate(ktmsProject, perfSuiteTemplate);
                TestItemTemplate perfCaseTemplate = testItemTemplateService.create("Test case template");
                perfCaseTemplate = testItemTemplateService.addField(perfCaseTemplate, "Description", TestItemFieldType.MULTILINE_TEXT, "Test feature: ");
                perfCaseTemplate = testItemTemplateService.addField(perfCaseTemplate, "Preconditions", TestItemFieldType.MULTILINE_TEXT, "Active user with privileges exists.\n");
                projectService.addTestItemTemplate(ktmsProject, perfCaseTemplate);
                for (int i = 0; i < 10; i++) {
                    TestItem suite = projectService.addTestItem(perfProject, TestItemType.TEST_SUITE, i + "Suite2 " + RandomStringUtils.randomAlphanumeric(30), null, perfSuiteTemplate);
                    createSuitesRecursively(perfProject, perfSuiteTemplate, perfCaseTemplate, suite, 10);
                    //testItemService.updateFieldValue(suite, "Description", RandomStringUtils.randomAlphanumeric(200));
                    //testItemService.updateFieldValue(testCase, "Description", RandomStringUtils.randomAlphanumeric(1500));
                    //testItemService.updateFieldValue(testCase, "Preconditions", RandomStringUtils.randomAlphanumeric(1500));
                }
            }
        }

        private void updateDescriptionField(TestItem editTestItem, String fieldValue) {
            testItemService.updateFieldValue(editTestItem, "Description", fieldValue);
        }

        private TestItem addTestCase(String name, TestItem testItem) {
            return projectService.addTestItem(ktmsProject, TestItemType.TEST_CASE, name, testItem, testCaseTemplate);
        }

        private TestItem addSuite(String name, TestItem parent) {
            return projectService.addTestItem(ktmsProject, TestItemType.TEST_SUITE, name, parent, testSuiteTemplate);
        }

        private void createSuitesRecursively(Project project, TestItemTemplate suiteTemplate, TestItemTemplate caseTemplate, TestItem parent, int max) {
            if (max == 0) return;
            TestItem suite = projectService.addTestItem(project, TestItemType.TEST_SUITE, max + " Suite " + RandomStringUtils.randomAlphanumeric(30), parent, suiteTemplate);
            for (int i = 0; i < 15; i++) {
                TestItem testCase = projectService.addTestItem(project, TestItemType.TEST_CASE, "Case " + i, suite, caseTemplate);
            }
            createSuitesRecursively(project, suiteTemplate, caseTemplate, suite, max - 1);
        }
    }
}
