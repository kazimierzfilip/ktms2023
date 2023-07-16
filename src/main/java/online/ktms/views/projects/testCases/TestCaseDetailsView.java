package online.ktms.views.projects.testCases;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import online.ktms.data.Service;
import online.ktms.data.TestItemFieldType;
import online.ktms.data.TestItemType;
import online.ktms.data.entity.TestItem;
import online.ktms.data.entity.TestItemFieldValue;
import online.ktms.data.service.TestItemFieldValueService;
import online.ktms.data.service.TestItemService;

import java.util.HashSet;
import java.util.Set;

public class TestCaseDetailsView extends Div {

    private final Header header;
    private final Body body;

    private TestItem openedItem;

    public TestCaseDetailsView() {
        VerticalLayout verticalLayout = new VerticalLayout();

        body = new Body();
        header = new Header(body);

        verticalLayout.add(header);
        verticalLayout.add(body);
        add(verticalLayout);
    }

    public void displayDetailsOf(TestItem testItem) {
        header.createHeader(testItem.getType());
        body.createBody(testItem);
        this.openedItem = testItem;
    }

    public boolean isInEditMode() {
        return body.isInEditMode();
    }

    public void discardChanges() {
        header.changeButtonToEditButton();
        body.discard();
    }

    public void saveChanges() {
        header.changeButtonToEditButton();
        body.save();
    }

    public TestItem getOpenedItem() {
        return openedItem;
    }

    public void clear() {
        header.clear();
        body.clear();
        this.openedItem = null;
    }

    private static class Header extends Div {
        private final Button editSaveButton = new Button("Edit", new Icon(VaadinIcon.EDIT));

        public Header(Body body) {
            editSaveButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            editSaveButton.addClickListener(event -> {
                if (editSaveButton.getText().equals("Edit")) {
                    body.editMode();
                    changeButtonToSaveButton();
                } else {
                    body.save();
                    changeButtonToEditButton();
                }
            });
        }

        private void changeButtonToSaveButton() {
            editSaveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editSaveButton.setIcon(new Icon(VaadinIcon.CHECK_SQUARE));
            editSaveButton.setText("Save");
        }

        private void changeButtonToEditButton() {
            editSaveButton.setIcon(new Icon(VaadinIcon.EDIT));
            editSaveButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editSaveButton.setText("Edit");
        }

        public void createHeader(@NotNull TestItemType type) {
            removeAll();
            changeButtonToEditButton();
            add(editSaveButton);
        }

        public void clear() {
            removeAll();
        }
    }

    private static class Body extends Div {

        private final MenuBar copyPathMenu = new MenuBar();
        private final Text path = new Text("");
        private final H3 code = new H3();
        private final TextField name = new TextField();
        private final Set<FieldAndValue> fields = new HashSet<>();
        private TestItem testItem;
        private boolean editMode = false;

        public Body() {
            copyPathMenu.addThemeVariants(MenuBarVariant.LUMO_ICON, MenuBarVariant.LUMO_SMALL);
            MenuItem copyPathButton = copyPathMenu.addItem("Copy path");
            MenuItem copyOptions = copyPathMenu.addItem(new Icon(VaadinIcon.CHEVRON_DOWN));
            SubMenu subMenu = copyOptions.getSubMenu();
            MenuItem copyPath = createCopyPathOption(subMenu, "Path", true);
            MenuItem copyName = createCopyPathOption(subMenu, "Name", true);
            MenuItem copyCode = createCopyPathOption(subMenu, "Code", true);
            MenuItem copyLink = createCopyPathOption(subMenu, "Link", true);
            MenuItem copyChildren = createCopyPathOption(subMenu, "With children", true);
            copyPathButton.addClickListener(e -> {
                if (e.getSource().getText().equals("Copy path")) {
                    String path = copyPath.isChecked() ? testItem.getPath() + ": " : "";
                    String name = copyName.isChecked() ? testItem.getName() + " " : "";
                    String code = copyCode.isChecked() ? testItem.getCode() + " " : "";

                    if (copyLink.isChecked()) {
                        getUI().get().getPage().fetchCurrentURL(url -> {
                            String urlString = url.toString();
                            String partUrl = urlString.substring(0, urlString.indexOf("/testCases") + "/testCases".length());
                            copyToClipboard(path + "[" + name + code + "|" + partUrl + "]");
                        });
                    } else {
                        copyToClipboard(path + name + code);
                    }
                    if (copyChildren.isChecked()) {

                    }
                }
            });

            name.addClassName("test-item-name-in-details");
            name.setWidthFull();
            name.setReadOnly(true);
            setWidthFull();
            setHeightFull();
        }

        private void copyToClipboard(String toCopy) {
            getUI().get().getPage().executeJs("var text = '" + toCopy + "'\n" +
                    "var textArea = document.createElement(\"textarea\");\n" +
                    "\n" +
                    "  // Place in the top-left corner of screen regardless of scroll position.\n" +
                    "  textArea.style.position = 'fixed';\n" +
                    "  textArea.style.top = 0;\n" +
                    "  textArea.style.left = 0;\n" +
                    "\n" +
                    "  // Ensure it has a small width and height. Setting to 1px / 1em\n" +
                    "  // doesn't work as this gives a negative w/h on some browsers.\n" +
                    "  textArea.style.width = '2em';\n" +
                    "  textArea.style.height = '2em';\n" +
                    "\n" +
                    "  // We don't need padding, reducing the size if it does flash render.\n" +
                    "  textArea.style.padding = 0;\n" +
                    "\n" +
                    "  // Clean up any borders.\n" +
                    "  textArea.style.border = 'none';\n" +
                    "  textArea.style.outline = 'none';\n" +
                    "  textArea.style.boxShadow = 'none';\n" +
                    "\n" +
                    "  // Avoid flash of the white box if rendered for any reason.\n" +
                    "  textArea.style.background = 'transparent';\n" +
                    "\n" +
                    "\n" +
                    "  textArea.value = text;\n" +
                    "\n" +
                    "  document.body.appendChild(textArea);\n" +
                    "  textArea.focus();\n" +
                    "  textArea.select();\n" +
                    "\n" +
                    "  try {\n" +
                    "    document.execCommand('copy');\n" +
                    "  } catch (err) {\n" +
                    "    console.log('Unable to copy');\n" +
                    "  }\n" +
                    "\n" +
                    "  document.body.removeChild(textArea);\n");
            Notification.show("Copied to clipboard", 2000, Notification.Position.MIDDLE);
        }

        private MenuItem createCopyPathOption(SubMenu subItems, String text, boolean defaultOption) {
            MenuItem item = subItems.addItem(text);
            item.setCheckable(true);
            item.setChecked(defaultOption);
            return item;
        }

        public void createBody(TestItem testItem) {
            this.testItem = Service.get(TestItemService.class).loadFieldValues(testItem);
            removeAll();

            add(copyPathMenu);

            path.setText(this.testItem.getPath());
            add(path);

            code.setText(this.testItem.getCode());
            add(code);

            name.setValue(this.testItem.getName());
            add(name);

            createFields(this.testItem);

            setToReadOnly(true);
        }

        private void createFields(TestItem testItem) {
            for (TestItemFieldValue fieldValue : testItem.getTestItemFieldValues()) {
                FieldAndValue fieldAndValue = new FieldAndValue(fieldValue);
                fields.add(fieldAndValue);
                add(fieldAndValue.getFieldComponent());
            }
        }

        public void editMode() {
            this.editMode = true;
            setToReadOnly(false);
        }

        public void save() {
            this.editMode = false;
            testItem = Service.get(TestItemService.class).updateName(testItem, name.getValue().trim());
            saveFieldsValues();
            setToReadOnly(true);

        }

        private void setToReadOnly(boolean readOnly) {
            name.setReadOnly(readOnly);
            setFieldsReadOnly(readOnly);
        }

        private void saveFieldsValues() {
            for (FieldAndValue field : fields) {
                if (field.getFieldComponent() instanceof HasValue<?, ?>) {
                    String value = ((HasValue<?, String>) field.getFieldComponent()).getValue();
                    field.setFieldValue(Service.get(TestItemFieldValueService.class).updateFieldValue(field.getFieldValue().getId(), value));
                }
            }
        }

        private void setFieldsReadOnly(boolean readOnly) {
            for (FieldAndValue field : fields) {
                if (field.getFieldComponent() instanceof HasValue<?, ?>) {
                    ((HasValue) field.getFieldComponent()).setReadOnly(readOnly);
                }
            }
        }

        public boolean isInEditMode() {
            return editMode;
        }

        public void discard() {
            this.editMode = false;
            setToReadOnly(true);
        }

        public void clear() {
            removeAll();
            testItem = null;
        }

        private static class FieldAndValue {
            @Getter
            @Setter
            private TestItemFieldValue fieldValue;

            @Getter
            private AbstractField fieldComponent;

            public FieldAndValue(TestItemFieldValue fieldValue) {
                this.fieldValue = fieldValue;
                this.fieldComponent = getFieldComponent(fieldValue);
                if (fieldComponent instanceof HasSize)
                    ((HasSize) fieldComponent).setWidthFull();
                if (fieldComponent instanceof HasLabel)
                    ((HasLabel) fieldComponent).setLabel(fieldValue.getTestItemField().getName());
                fieldComponent.setValue(fieldValue.getFieldValue());
                fieldComponent.setReadOnly(true);
            }

            private AbstractField getFieldComponent(TestItemFieldValue fieldValue) {
                if (fieldValue.getTestItemField().getType().equals(TestItemFieldType.MULTILINE_TEXT)) {
                    return new TextArea();
                } else if (fieldValue.getTestItemField().getType().equals(TestItemFieldType.SHORT_TEXT)) {
                    return new TextField();
                } else {
                    throw new RuntimeException("Field type not supported");
                }
            }
        }
    }

}
