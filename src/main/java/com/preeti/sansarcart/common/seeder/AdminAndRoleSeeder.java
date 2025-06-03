package com.preeti.sansarcart.common.seeder;

import com.preeti.sansarcart.entity.Role;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.repository.RoleRepository;
import com.preeti.sansarcart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminAndRoleSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Value("${app.admin.username}")
    private String adminUsername; // email


    @Value("${app.admin.password}")
    private String adminPassword;


    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdmin();
    }

    private void seedAdmin() {
        if (userRepository.findByEmail(adminUsername).isPresent())
            return;

        Role adminRole = roleRepository.findByAuthority(RoleType.ADMIN)
                .orElseThrow(() -> new ResourceNotFound("ADMIN role not found"));

        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setActive(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
    }
    private void seedRoles() {
        Set<RoleType> existing = roleRepository.findAll().stream()
                .map(Role::getAuthority)
                .collect(Collectors.toSet());

        List<Role> toAdd = Arrays.stream(RoleType.values())
                .filter(rt -> !existing.contains(rt))
                .map(Role::new)
                .toList();

        if (!toAdd.isEmpty()) roleRepository.saveAll(toAdd);
    }
}
