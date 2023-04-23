package online.ktms.views.projects.testCases;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import online.ktms.data.Service;
import online.ktms.data.entity.Project;
import online.ktms.data.service.ProjectService;
import online.ktms.views.projects.ProjectView;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Route(value = "testCases/:id?", layout = ProjectView.class)
@PermitAll
@Uses(Icon.class)
public class TestCasesView extends Div implements BeforeEnterObserver {

    private String projectCode;
    Project project;
    private String testItemId;

    private TestCasesTreeView testCasesTreeView;
    private TestCaseDetailsView testCaseDetailsView;

    public TestCasesView() {
        add(createView());
    }

    private SplitLayout createView() {
        setWidthFull();
        setHeightFull();
        this.testCaseDetailsView = new TestCaseDetailsView();
        this.testCasesTreeView = new TestCasesTreeView(testCaseDetailsView);

        SplitLayout splitLayout = new SplitLayout(testCasesTreeView, testCaseDetailsView);
        splitLayout.setSplitterPosition(30);
        splitLayout.setHeightFull();
        return splitLayout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        LoggerFactory.getLogger(TestCasesView.class).info("tc view before");
        projectCode = event.getRouteParameters().get("projectCode").orElse("");
        testItemId = event.getRouteParameters().get("id").orElse(null);
        if (!projectCode.isEmpty()) {
            populateView();
        } else {
            removeAll();
        }
    }

    private void populateView() {
        if (project == null || !project.getCode().equals(projectCode)) {
            Optional<Project> optionalProject = Service.get(ProjectService.class).getWithValues(projectCode);
            if (optionalProject.isEmpty()) {
                removeAll();
            } else {
                this.project = optionalProject.get();
            }
        }
        this.testCasesTreeView.populateTree(project, testItemId);
    }
}
