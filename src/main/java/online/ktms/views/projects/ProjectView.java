package online.ktms.views.projects;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import lombok.Setter;
import online.ktms.data.Service;
import online.ktms.data.entity.Project;
import online.ktms.data.service.ProjectService;
import online.ktms.views.CommonLayout;
import online.ktms.views.NotFoundView;
import online.ktms.views.projects.testCases.TestCasesView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParentLayout(CommonLayout.class)
@RoutePrefix(value = "projects/:projectCode")
@Route(value = "", layout = CommonLayout.class)
@PermitAll
@Uses(Icon.class)
public class ProjectView extends VerticalLayout implements BeforeEnterObserver, RouterLayout, HasDynamicTitle {
    private String projectCode = "";
    private RouterLink testCases;
    private RouterLink testExecution;

    @Override
    public String getPageTitle() {
        return "kTMS - " + projectCode;
    }

    public ProjectView() {
        setHeightFull();
        addClassNames(LumoUtility.Padding.NONE);

        RouteTabs routeTabs = new RouteTabs();
        routeTabs.setWidthFull();
        routeTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() != null) {
                routeTabs.setLastSelected(event.getSelectedTab());
            }
        });

        testCases = new RouterLink("Test Cases", NotFoundView.class);
        routeTabs.add(testCases);

        testExecution = new RouterLink("Test Execution", NotFoundView.class);
        routeTabs.add(testExecution);

        add(routeTabs);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        ProjectService projectService = Service.get(ProjectService.class);
        projectCode = event.getRouteParameters().get("projectCode").orElse("");
        if (!projectCode.isEmpty()) {
            Optional<Project> optionalProject = projectService.getWithValues(projectCode);
            if (optionalProject.isEmpty()) {
                removeAll();
                add(new NotFoundView());
            } else {
                populateView(projectCode);
            }
        } else {
            removeAll();
            add(new NotFoundView());
        }
        if (event.getNavigationTarget() == ProjectView.class) {
            event.forwardTo(TestCasesView.class, new RouteParameters("projectCode", projectCode));
        }
    }

    private void populateView(String projectCode) {
        testCases.setRoute(TestCasesView.class, new RouteParameters("projectCode", projectCode));
        testExecution.setRoute(TestExecutionView.class, new RouteParameters("projectCode", projectCode));
    }

    private static class RouteTabs extends Tabs implements BeforeEnterObserver {
        @Setter
        private Tab lastSelected;
        private final Map<RouterLink, Tab> routerLinkTabMap = new HashMap<>();

        public void add(RouterLink routerLink) {
            routerLink.setHighlightCondition(HighlightConditions.sameLocation());
            routerLink.setHighlightAction(
                    (link, shouldHighlight) -> {
                        if (shouldHighlight) setSelectedTab(routerLinkTabMap.get(routerLink));
                    }
            );
            routerLinkTabMap.put(routerLink, new Tab(routerLink));
            add(routerLinkTabMap.get(routerLink));
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
            // In case no tabs will match
            setSelectedTab(lastSelected);
        }
    }

}
