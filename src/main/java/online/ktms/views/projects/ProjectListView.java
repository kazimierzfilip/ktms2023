package online.ktms.views.projects;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import online.ktms.data.entity.Project;
import online.ktms.data.service.ProjectService;
import online.ktms.views.CommonLayout;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.function.BiFunction;

@PageTitle("kTMS - Projects")
@Route(value = "projects", layout = CommonLayout.class)
@RouteAlias(value = "", layout = CommonLayout.class)
@PermitAll
@Uses(Icon.class)
public class ProjectListView extends VerticalLayout {

    private TextField searchField;
    private Grid<Project> projectGrid;

    public ProjectListView(ProjectService projectService) {
        setHeightFull();
        setWidth("50%");
        setPadding(true);
        GridListDataView<Project> dataView = createProjectGrid(projectService);
        createSearchField(dataView);

        add(searchField);
        add(projectGrid);
    }

    private GridListDataView<Project> createProjectGrid(ProjectService projectService) {
        projectGrid = new Grid<>(Project.class, false);
        projectGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        projectGrid.addColumn("code").setHeader("Code")
                .setWidth("150px").setFlexGrow(0);
        projectGrid.addColumn("name").setHeader("Name");

        projectGrid.addSelectionListener(selection -> {
            Optional<Project> optionalProject = selection.getFirstSelectedItem();
            if (optionalProject.isPresent()) {
                getUI().ifPresent(ui -> ui.navigate(ProjectView.class, new RouteParameters(new RouteParam("projectCode", optionalProject.get().getCode()))));
            }
        });

        return projectGrid.setItems(projectService.list(Pageable.unpaged())
                .stream().toList());
    }

    private void createSearchField(GridListDataView<Project> dataView) {
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> dataView.refreshAll());
        dataView.addFilter(projectFilter(searchField));
    }

    private static SerializablePredicate<Project> projectFilter(TextField searchField) {
        return project -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            BiFunction<String, String, Boolean> matches = (text, search) ->
                    search == null || search.isEmpty() || text.toLowerCase().contains(search.toLowerCase());

            boolean matchesCode = matches.apply(project.getCode(), searchTerm);
            boolean matchesName = matches.apply(project.getName(), searchTerm);

            return matchesCode || matchesName;
        };
    }

}
