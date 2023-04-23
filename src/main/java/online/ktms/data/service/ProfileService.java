package online.ktms.data.service;

import online.ktms.data.Privilege;
import online.ktms.data.entity.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }


    public Profile createProfile(String name, Privilege... privileges) {
        Profile profile = new Profile(name, Arrays.stream(privileges).collect(Collectors.toSet()));
        return repository.save(profile);
    }
}
