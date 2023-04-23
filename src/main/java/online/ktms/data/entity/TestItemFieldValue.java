package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "test_item_field_values")
@Getter
@Setter
public class TestItemFieldValue {

    @EmbeddedId
    private TestItemFieldValueId id;
    @Version
    private int version = 0;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("test_item_id")
    private TestItem testItem;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("test_item_field_id")
    private TestItemField testItemField;
    @Column(columnDefinition = "TEXT")
    private String fieldValue;


    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractEntity)) {
            return false; // null or other class
        }
        AbstractEntity other = (AbstractEntity) obj;

        if (getId() != null) {
            return getId().equals(other.getId());
        }
        return super.equals(other);
    }
}
