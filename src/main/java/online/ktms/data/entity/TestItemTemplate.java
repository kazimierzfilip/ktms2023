package online.ktms.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_item_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestItemTemplate extends AbstractEntity {

    private String name;

    @ManyToOne
    private Project project;

    @OneToMany(mappedBy = "testItemTemplate", fetch = FetchType.EAGER)
    private List<TestItemField> testItemFields = new ArrayList<>();


    public TestItemTemplate(String name) {
        this.name = name;
    }
}
