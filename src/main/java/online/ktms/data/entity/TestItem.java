package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.ktms.data.TestItemType;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "test_items")
@NoArgsConstructor
@AllArgsConstructor
public class TestItem extends AbstractEntity {
    /**
     * C - test case
     * S - suite
     */
    @Enumerated
    private TestItemType type;

    private String code;

    private String name;

    @ManyToOne//(fetch = FetchType.LAZY)
    private Project project;

    @ManyToOne//(fetch = FetchType.LAZY)
    private TestItem parentItem;

    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "parentItem", fetch = FetchType.EAGER)
    private List<TestItem> children = new LinkedList<>();

    @ManyToOne
    private TestItemTemplate template;

    @OneToMany(mappedBy = "testItem")
    private Set<TestItemFieldValue> testItemFieldValues = new HashSet<>();


    public Optional<TestItemFieldValue> getField(String name) {
        return getTestItemFieldValues().stream().filter(testItemFieldValue -> testItemFieldValue.getTestItemField().getName().equals(name)).findFirst();
    }

    public String getCodeAndName() {
        return (getCode() != null ? getCode() + ": " : "") + getName();
    }

    public List<TestItem> getChildrenOrdered() {
        return getChildren() != null ? getChildren().stream()
                .sorted(Comparator.comparingInt(TestItem::getOrderIndex))
                .toList() : null;
    }

    public String getPath() {
        List<String> elements = new ArrayList<>();
        TestItem item = getParentItem();
        while (item != null) {
            elements.add(item.getName());
            item = item.getParentItem();
        }
        Collections.reverse(elements);
        return String.join(" > ", elements);
    }

    public Integer getChildTestCasesCount() {
        return (int) getChildren().stream().filter(i -> i.getType().equals(TestItemType.TEST_CASE)).count()
                + getChildren().stream().filter(i -> i.getType().equals(TestItemType.TEST_SUITE))
                .map(TestItem::getChildTestCasesCount).mapToInt(Integer::intValue).sum();
    }
}
