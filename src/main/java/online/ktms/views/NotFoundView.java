package online.ktms.views;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.security.PermitAll;

@PageTitle("kTMS - Not Found")
@Route(value = "/not-found", layout = CommonLayout.class)
@AnonymousAllowed
@Uses(Icon.class)
public class NotFoundView extends VerticalLayout {
    public NotFoundView() {
        add(new H2("Not Found"));
    }
}
