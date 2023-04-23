package online.ktms.views.projects.testCases;

import com.vaadin.componentfactory.explorer.ExplorerTreeGrid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import online.ktms.data.Service;
import online.ktms.data.TestItemType;
import online.ktms.data.entity.Project;
import online.ktms.data.entity.TestItem;
import online.ktms.data.entity.TestItemTemplate;
import online.ktms.data.service.ProjectService;
import online.ktms.data.service.TestItemService;
import online.ktms.views.NotFoundView;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TestCasesTreeView extends Div {

    ExplorerTreeGrid<TestItem> testItemTreeGrid = new ExplorerTreeGrid<>();
    TestItem draggingItem;
    List<TestItem> rootItems;
    Project project;
    ProjectService projectService;
    TestItemService testItemService;
    TestCaseDetailsView detailsView;
    private TestItem internalRedirect = null;

    public TestCasesTreeView(TestCaseDetailsView testCaseDetailsView) {
        this.projectService = Service.get(ProjectService.class);
        this.testItemService = Service.get(TestItemService.class);
        this.detailsView = testCaseDetailsView;
        add(createTreeView());
    }

    public void populateTree(Project project, String testItemId) {
        this.project = project;
        rootItems = getRootItemsFromDatabase();
        setTreeItems(rootItems);
        if (testItemId != null) {
            Optional<TestItem> optional = project.getTestItems().stream().filter(i -> testItemId.equals(i.getCode())).findFirst();
            if (optional.isPresent()) {
                clickTestItemInTree(testItemTreeGrid, optional.get());
                expandPath(optional.get());
            } else {
                optional = project.getTestItems().stream().filter(i -> testItemId.equals(i.getId().toString())).findFirst();
                if (optional.isPresent()) {
                    clickTestItemInTree(testItemTreeGrid, optional.get());
                    expandPath(optional.get());
                } else {
                    add(new NotFoundView());
                }
            }
        }
    }

    private void expandPath(TestItem testItem) {
        TestItem i = testItem.getParentItem();
        while (i != null) {
            testItemTreeGrid.expand(i);
            i = i.getParentItem();
        }
    }

    public Component createTreeView() {
        Div treeMenu = createTreeMenu();
        createTree();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setHeightFull();
        verticalLayout.add(treeMenu);
        verticalLayout.add(testItemTreeGrid);

        return verticalLayout;
    }

    private void createTree() {
        testItemTreeGrid.setHeightFull();

        testItemTreeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);

        testItemTreeGrid.setDetailsVisibleOnClick(false);
        testItemTreeGrid.setItemDetailsRenderer(createTestItemDetailsRenderer());

        addColumnWithCreateItemButton();
        testItemTreeGrid.addHierarchyColumn(
                testItem -> testItem.getCodeAndName() +
                        (testItem.getChildren().size() > 0 ? " (" + testItem.getChildTestCasesCount() + ")" : ""),
                testItem -> testItem.getType().equals(TestItemType.TEST_SUITE) ? "vaadin:folder-open-o" : null);

        testItemTreeGrid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
        testItemTreeGrid.setRowsDraggable(true);

        testItemTreeGrid.addDragStartListener(e -> draggingItem = e.getDraggedItems().get(0));

        testItemTreeGrid.addDropListener(e -> {
            TestItem targetItem = e.getDropTargetItem().orElse(null);
            if (draggingItem != null && targetItem != null && draggingItem != targetItem) {
                GridDropLocation dropLocation = e.getDropLocation();
                System.out.println(draggingItem.getCodeAndName() + " " + dropLocation + " " + targetItem.getCodeAndName());

                if (changingPlacesInTheSameSuite(dropLocation, draggingItem, targetItem)) {
                    System.out.println("change");
                    Integer draggingIndex = draggingItem.getOrderIndex();
                    draggingItem.setOrderIndex(targetItem.getOrderIndex());
                    targetItem.setOrderIndex(draggingIndex);
                } else if (droppingToSuite(dropLocation)) {
                    System.out.println("drop");
                    draggingItem.getParentItem().getChildren().sort(Comparator.comparingInt(TestItem::getOrderIndex));
                    for (int i = draggingItem.getParentItem().getChildren().indexOf(draggingItem) + 1, order = draggingItem.getOrderIndex(); i < draggingItem.getParentItem().getChildren().size(); i++, order++) {
                        draggingItem.getParentItem().getChildren().get(i).setOrderIndex(order);
                    }
                    draggingItem.setParentItem(targetItem);
                    draggingItem.setOrderIndex(targetItem.getChildren().size());
                } else if (droppingToSuiteAtIndex(dropLocation, draggingItem, targetItem)) {
                    System.out.println("drop at index");
                    draggingItem.getParentItem().getChildren().sort(Comparator.comparingInt(TestItem::getOrderIndex));
                    for (int i = draggingItem.getParentItem().getChildren().indexOf(draggingItem) + 1, order = draggingItem.getOrderIndex(); i < draggingItem.getParentItem().getChildren().size(); i++, order++) {
                        draggingItem.getParentItem().getChildren().get(i).setOrderIndex(order);
                    }
                    draggingItem.setParentItem(targetItem);
                    Integer orderToSet = dropLocation.equals(GridDropLocation.ABOVE) ?
                            targetItem.getOrderIndex() - 1 : targetItem.getOrderIndex() + 1;
                    orderToSet = orderToSet < 0 ? 0 : orderToSet;
                    draggingItem.setOrderIndex(orderToSet);
                    targetItem.getParentItem().getChildren().sort(Comparator.comparingInt(TestItem::getOrderIndex));
                    Integer indexToSet = targetItem.getChildren().indexOf(targetItem) + (dropLocation.equals(GridDropLocation.ABOVE) ? 0 : 1);
                    for (int i = indexToSet, order = orderToSet + 1; i < targetItem.getParentItem().getChildren().size(); i++, order++) {
                        targetItem.getParentItem().getChildren().get(i).setOrderIndex(order);
                    }
                }
                testItemService.update(draggingItem);
                testItemService.update(targetItem);
                rootItems = getRootItemsFromDatabase();
                setTreeItems(rootItems);
            }
        });
        testItemTreeGrid.addDragEndListener(e -> draggingItem = null);

        createTreeGridContextMenu();

        collapseAlsoInnerItems();

        openDetailsOnClick();
    }

    private boolean changingPlacesInTheSameSuite(GridDropLocation dropLocation, TestItem draggingItem, TestItem targetItem) {
        return (dropLocation.equals(GridDropLocation.ABOVE) || dropLocation.equals(GridDropLocation.BELOW))
                && draggingItem.getParentItem() == targetItem.getParentItem();
    }

    private static boolean droppingToSuite(GridDropLocation dropLocation) {
        return dropLocation == GridDropLocation.ON_TOP;
    }

    private boolean droppingToSuiteAtIndex(GridDropLocation dropLocation, TestItem draggingItem, TestItem targetItem) {
        return (dropLocation.equals(GridDropLocation.ABOVE) || dropLocation.equals(GridDropLocation.BELOW))
                && draggingItem.getParentItem() != targetItem.getParentItem();
    }

    private void addColumnWithCreateItemButton() {
        Grid.Column<TestItem> newItemButtonColumn = testItemTreeGrid.addComponentColumn(testItem -> {
            Button createButton = new Button(new Icon(VaadinIcon.FILE_ADD));
            createButton.addClassName("create-test-item");
            createButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            createButton.getElement().setAttribute("aria-label", "Add item");
            createButton.addClickListener(e -> {
                testItemTreeGrid.setDetailsVisible(testItem, !testItemTreeGrid.isDetailsVisible(testItem));
            });
            return createButton;
        });
        newItemButtonColumn.setFlexGrow(0)
                .setWidth("1.8rem")
                .addClassNames(LumoUtility.Padding.Right.NONE);
    }

    private Div createTreeMenu() {
        Div div = new Div();
        div.setWidthFull();
        TextField searchField = createSearchField();
        searchField.addKeyPressListener(Key.ENTER, key -> searchForItems(project, testItemTreeGrid, searchField));
        div.add(searchField);

        Button search = createSearchButton(project, testItemTreeGrid, searchField);
        div.add(search);

        div.add(createExpandAllButton());
        div.add(createCollapseAllButton());

        refreshTreeAfterSearchFieldClear(testItemTreeGrid, searchField);
        return div;
    }

    private List<TestItem> getRootItemsFromDatabase() {
        return projectService.getWithValues(project.getId()).get().getTestItems().stream()
                .filter(testItem -> testItem.getType().equals(TestItemType.TEST_SUITE) && testItem.getParentItem() == null)
                .sorted(Comparator.comparingInt(TestItem::getOrderIndex))
                .collect(Collectors.toList());
    }

    private void setTreeItems(List<TestItem> rootItems) {
        testItemTreeGrid.setItems(rootItems, TestItem::getChildrenOrdered);
    }

    private ComponentRenderer<TestItemFormLayout, TestItem> createTestItemDetailsRenderer() {
        return new ComponentRenderer<>(TestItemFormLayout::new, (form, testItem) -> {
            form.setTestItem(testItem, testItemTreeGrid, projectService, project, (t) -> {
                setTreeItems(getRootItemsFromDatabase());
                TestItem parent = testItem;
                int i = 0;
                while (parent.getParentItem() != null) {
                    parent = parent.getParentItem();
                    i++;
                }
                testItemTreeGrid.expandRecursively(Collections.singletonList(parent), i);
            });
        });
    }

    private static class TestItemFormLayout extends FormLayout {

        private final Label title;
        private final RadioButtonGroup<String> suiteOrTestCase;
        private final TextField name;
        private final Button create;
        private final Button cancel;


        public TestItemFormLayout() {
            title = new Label("Create new");
            title.addClassNames(LumoUtility.FontWeight.BOLD);

            suiteOrTestCase = new RadioButtonGroup<>();
            suiteOrTestCase.setItems(TestItemType.TEST_SUITE.getLabel(), TestItemType.TEST_CASE.getLabel());
            suiteOrTestCase.setValue(TestItemType.TEST_SUITE.getLabel());
            setColspan(suiteOrTestCase, 2);

            name = new TextField();
            name.setPlaceholder("Name");
            setColspan(name, 2);

            create = new Button("Create");
            create.addThemeVariants(ButtonVariant.LUMO_SMALL);
            cancel = new Button("Cancel");
            cancel.addThemeVariants(ButtonVariant.LUMO_SMALL);

            getStyle().set("background-color", "var(--_lumo-grid-selected-row-color)");
            addClassName(LumoUtility.Padding.SMALL);
            add(title);
            add(suiteOrTestCase);
            add(name);
            add(create);
            add(cancel);
        }

        public void setTestItem(TestItem testItem, TreeGrid<TestItem> testItemTreeGrid, ProjectService projectService, Project project, Consumer<TestItem> reloadTree) {

            create.addClickListener(event -> {
                TestItem parent;
                if (testItem.getType() == TestItemType.TEST_CASE) {
                    parent = testItem.getParentItem();
                } else {
                    parent = testItem;
                }
                TestItemType type = suiteOrTestCase.getValue().equals(TestItemType.TEST_SUITE.getLabel()) ?
                        TestItemType.TEST_SUITE : TestItemType.TEST_CASE;

                TestItemTemplate template = suiteOrTestCase.getValue().equals(TestItemType.TEST_SUITE.getLabel()) ?
                        project.getTestItemTemplates().stream().filter(t -> t.getName().equals("Test suite template")).findFirst().get() :
                        project.getTestItemTemplates().stream().filter(t -> t.getName().equals("Test case template")).findFirst().get();

                projectService.addTestItem(project, type, name.getValue().trim(), parent, template);

                testItemTreeGrid.setDetailsVisible(testItem, false);
                reloadTree.accept(parent);
            });

            cancel.addClickListener(event -> {
                name.setValue("");
                testItemTreeGrid.setDetailsVisible(testItem, false);
            });
        }
    }

    private Button createSearchButton(Project project, TreeGrid<TestItem> testItemTreeGrid, TextField searchField) {
        Button search = new Button("Search");
        search.addClickListener(event -> {
            searchForItems(project, testItemTreeGrid, searchField);
        });
        return search;
    }

    private void searchForItems(Project project, TreeGrid<TestItem> testItemTreeGrid, TextField searchField) {
        TestItem testItemToSelect;
        var matchedItem = project.getTestItems().stream()
                .filter(testItem -> testItem.getCode() != null && testItem.getCode().toLowerCase()
                        .contains(searchField.getValue().trim().toLowerCase()))
                .findFirst();
        List<TestItem> items;
        if (matchedItem.isPresent()) {
            TestItem testItem = matchedItem.get();
            testItemToSelect = testItem;
            while (testItem.getParentItem() != null) {
                testItem = testItem.getParentItem();
            }
            items = Collections.singletonList(testItem);
        } else {
            items = project.getTestItems().stream()
                    .filter(testItem -> testItem.getName().toLowerCase()
                            .contains(searchField.getValue().trim().toLowerCase()))
                    .toList();
            testItemToSelect = items.size() > 0 ? items.get(0) : null;
            items = items.stream().map(testItem -> {
                        while (testItem.getParentItem() != null) {
                            testItem = testItem.getParentItem();
                        }
                        return testItem;
                    })
                    .distinct()
                    .toList();
        }
        testItemTreeGrid.setItems(
                items,
                TestItem::getChildrenOrdered);
        testItemTreeGrid.expandRecursively(items, 1000);
        if (testItemToSelect != null) {
            clickTestItemInTree(testItemTreeGrid, testItemToSelect);
        }
    }

    private Button createCollapseAllButton() {
        Button collapse = new Button("Collapse All");
        collapse.addClickListener(event -> testItemTreeGrid.collapseRecursively(rootItems, 1000));
        return collapse;
    }

    private Button createExpandAllButton() {
        Button expand = new Button("Expand All");
        expand.addClickListener(event -> testItemTreeGrid.expandRecursively(rootItems, 1000));
        return expand;
    }

    private void createTreeGridContextMenu() {
        GridContextMenu<TestItem> menu = testItemTreeGrid.addContextMenu();

        GridMenuItem<TestItem> create = menu.addItem("Create", event -> {
            if (event.getItem().isPresent()) {
                testItemTreeGrid.setDetailsVisible(event.getItem().get(), !testItemTreeGrid.isDetailsVisible(event.getItem().get()));
            }
        });
        GridMenuItem<TestItem> delete = menu.addItem("Delete", event -> {
            if (event.getItem().isPresent()) {
                projectService.removeTestItem(event.getItem().get());

                detailsView.clear();

                project = projectService.getWithValues(project.getId()).get();
                rootItems = getRootItemsFromDatabase();
                List<TestItem> expandedItems = new ArrayList<>();
                for (TestItem testItem : project.getTestItems()) {
                    if (testItemTreeGrid.isExpanded(testItem)) {
                        expandedItems.add(testItem);
                    }
                }
                expandedItems.remove(event.getItem().get());
                setTreeItems(rootItems);
                testItemTreeGrid.expand(expandedItems);
            }
        });

        menu.addGridContextMenuOpenedListener(event -> {
            if (event.getItem().isEmpty()) {
                create.setEnabled(true);
                delete.setEnabled(false);
            } else if (event.getItem().get().getType().equals(TestItemType.TEST_SUITE)) {
                create.setEnabled(true);
                delete.setEnabled(true);
            } else {
                create.setEnabled(false);
                delete.setEnabled(true);
            }
        });
    }

    private TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setClearButtonVisible(true);
        return searchField;
    }

    private void collapseAlsoInnerItems() {
        testItemTreeGrid.addCollapseListener(e -> {
            if (e.getItems().size() == 1) {
                testItemTreeGrid.collapseRecursively(e.getItems(), 1000);
            }
        });
    }

    private void openDetailsOnClick() {
        testItemTreeGrid.addItemClickListener(e -> {
            if (detailsView.isInEditMode()) {
                ConfirmDialog dialog = new ConfirmDialog();
                dialog.setHeader("Unsaved changes");
                dialog.setText("There are unsaved changes. Do you want to discard or save them?");

                dialog.setCancelable(true);
                dialog.setCancelText("Don't leave");
                dialog.addCancelListener(event -> {
                    testItemTreeGrid.deselectAll();
                    testItemTreeGrid.select(detailsView.getOpenedItem());
                });

                dialog.setRejectable(true);
                dialog.setRejectText("Discard");
                dialog.addRejectListener(event -> {
                    detailsView.discardChanges();
                    if (e.getItem() != null) {
                        clickTestItemInTree(testItemTreeGrid, e.getItem());
                    }
                });

                dialog.setConfirmText("Save");
                dialog.addConfirmListener(event -> {
                    detailsView.saveChanges();
                    if (e.getItem() != null) {
                        clickTestItemInTree(testItemTreeGrid, e.getItem());
                    }
                });

                dialog.open();
            } else {
                if (e.getItem() != null) {
                    clickTestItemInTree(testItemTreeGrid, e.getItem());
                }
            }
        });
    }

    private void clickTestItemInTree(TreeGrid<TestItem> testItemTreeGrid, TestItem testItem) {
        testItemTreeGrid.select(testItem);
        detailsView.displayDetailsOf(testItem);
    }

    private void refreshTreeAfterSearchFieldClear(TreeGrid<TestItem> testItemTreeGrid, TextField searchField) {
        searchField.addValueChangeListener(event -> {
            if (event.getValue().isEmpty() && !event.getOldValue().isEmpty()) {
                testItemTreeGrid.setItems(rootItems, TestItem::getChildrenOrdered);
            }
        });
    }
}
