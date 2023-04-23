package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.ktms.data.TestItemFieldType;

@Entity
@Table(name = "test_item_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestItemField extends AbstractEntity {
    private String name;
    @Enumerated(EnumType.STRING)
    private TestItemFieldType type;
    @ManyToOne
    private TestItemTemplate testItemTemplate;
    private String defaultValue;
}
