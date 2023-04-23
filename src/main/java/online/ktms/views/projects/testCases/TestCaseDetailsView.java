package online.ktms.views.projects.testCases;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        System.out.println("Header created");
        body.createBody(testItem);
        System.out.println("body created");
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

        private final Text path = new Text("");
        private final H3 code = new H3();
        private final TextField name = new TextField();
        private final Set<FieldAndValue> fields = new HashSet<>();
        private TestItem testItem;
        private boolean editMode = false;

        public Body() {
            name.addClassName("test-item-name-in-details");
            name.setWidthFull();
            name.setReadOnly(true);
            setWidthFull();
            setHeightFull();
        }

        public void createBody(TestItem testItem) {
            this.testItem = Service.get(TestItemService.class).loadFieldValues(testItem);
            removeAll();

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
