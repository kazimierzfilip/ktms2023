package online.ktms.data.service;

import jakarta.transaction.Transactional;
import online.ktms.data.entity.Group;
import online.ktms.data.entity.Project;
import online.ktms.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group create(String name) {
        Group group = new Group(name);
        return groupRepository.save(group);
    }

    public Group addUser(Group group, User user) {
        group.getUsers().add(user);
        user.getGroups().add(group);
        userRepository.save(user);
        return groupRepository.save(group);
    }
}
