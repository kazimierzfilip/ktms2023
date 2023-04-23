package online.ktms.views.projects;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import online.ktms.views.NotFoundView;

@Route(value = "testExecution", layout = ProjectView.class)
@PermitAll
@Uses(Icon.class)
public class TestExecutionView extends Div implements BeforeEnterObserver {

    private String projectCode;


    public TestExecutionView() {

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        projectCode = event.getRouteParameters().get("projectCode").orElse("");
    }
}
