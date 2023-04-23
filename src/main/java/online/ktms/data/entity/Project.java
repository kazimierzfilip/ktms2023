package online.ktms.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import online.ktms.data.Status;
import online.ktms.data.TestItemType;

import java.util.*;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project extends AbstractEntity {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "project")
    private Set<ProjectGroupProfile> groupsAndProfiles = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private Set<ProjectUserProfile> usersAndProfiles = new HashSet<>();

    @OneToMany(mappedBy = "project")
    private List<TestItem> testItems = new LinkedList<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.EAGER)
    private List<TestItemTemplate> testItemTemplates = new ArrayList<>();

    @NotNull
    private Long lastTestCaseId = 0L;


    public Project(String code, String name, Status status) {
        this.code = code;
        this.name = name;
        this.status = status;
    }

}
