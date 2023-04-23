package online.ktms.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import online.ktms.data.entity.User;
import online.ktms.security.AuthenticatedUser;
import online.ktms.views.projects.ProjectListView;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class CommonLayout extends AppLayout {

    private String theme = Lumo.LIGHT;
    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public CommonLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        Div layout = new Div();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);

        layout.add(createLogo());
        layout.add(createProjectsLink());
        if (accessChecker.hasAccess(ProjectListView.class)) {
            layout.add(createAdministrationLink());
        }
        layout.add(createSpacer());
        layout.add(createUserProfileMenu());
        layout.add(createThemeToggle());

        header.add(layout);
        return header;
    }

    private static Component createLogo() {
        H1 appName = new H1("kTMS");
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.XLARGE, FontSize.LARGE);
        return appName;
    }

    private Component createProjectsLink() {
        RouterLink link = new RouterLink();
        link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL,
                TextColor.BODY);
        link.setRoute(ProjectListView.class);

        Span text = new Span("Projects");
        text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

        link.add(text);
        return link;
    }

    private Component createAdministrationLink() {
        RouterLink link = new RouterLink();
        link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.MEDIUM,
                TextColor.BODY);
        link.setRoute(ProjectListView.class);

        Span text = new Span("Administration");
        text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);

        link.add(text);
        return link;
    }

    private Component createSpacer() {
        Div div = new Div();
        div.addClassNames(Flex.GROW);
        return div;
    }

    private Component createUserProfileMenu() {
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Settings");
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            return userMenu;
        } else {
            return new Anchor("login", "Sign in");
        }
    }

    private Icon createThemeToggle() {
        Icon themeToggle = new Icon(VaadinIcon.ADJUST);
        themeToggle.addClassNames(IconSize.SMALL);
        themeToggle.addClickListener(e -> {
            changeTheme();
        });
        themeToggle.getStyle().set("margin-left", "1rem");
        return themeToggle;
    }

    private void changeTheme() {
        var newTheme = theme.equals(Lumo.DARK) ? Lumo.LIGHT : Lumo.DARK;
        var js = MessageFormat.format("""
                    document.documentElement.setAttribute("theme", "{0}")
                """, newTheme);
        getElement().executeJs(js).then(r -> theme = newTheme);
    }

}
