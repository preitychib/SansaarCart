package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.enums.EntityType;
import com.preeti.sansarcart.payload.authentication.CustomerRegisterDto;
import com.preeti.sansarcart.entity.Customer;
import com.preeti.sansarcart.entity.Role;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.address.AddressDTO;
import com.preeti.sansarcart.payload.profile.CustomerProfileDTO;
import com.preeti.sansarcart.projection.UserListView;
import com.preeti.sansarcart.repository.AddressRepository;
import com.preeti.sansarcart.repository.user.CustomerRepository;
import com.preeti.sansarcart.repository.RoleRepository;
import com.preeti.sansarcart.repository.user.UserRepository;
import com.preeti.sansarcart.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.Util.getNullPropertyNames;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final ImageService imageService;

    public Customer signup(CustomerRegisterDto registerCustomerDto) {

        if (userRepository.findByEmail(registerCustomerDto.getEmail()).isPresent()) {
            throw new ValidationException("Email is already registered");
        }

        Role role = roleRepository.findByAuthority(RoleType.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFound("Role not found"));

        Customer customer = registerCustomerDto.toCustomer();
        customer.setPassword(passwordEncoder.encode(registerCustomerDto.getPassword()));
        customer.setActive(false);
        customer.setRoles(Set.of(role));

        return  customerRepository.save(customer);
    }


    public List<UserListView> getAllCustomers(int page, int size, String sortBy, String sortDir, String email) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (StringUtils.hasText(email)) {
            return customerRepository.findByEmailContainingIgnoreCase(email, pageable).getContent();
        } else {
            return customerRepository.findAllProjectedBy(pageable).getContent();
        }
    }

    public List<AddressDTO> getCustomerAddresses(UUID id) {
        List<Address> addresses = addressRepository.findAllByUserId(id);
        return addresses.stream()
                .map(AddressDTO::from)
                .collect(Collectors.toList());
    }

    public CustomerProfileDTO updateProfile(Customer customer, CustomerProfileDTO updatedProfile, MultipartFile imageFile) {

        if (updatedProfile != null ) {
            BeanUtils.copyProperties(updatedProfile, customer, getNullPropertyNames(updatedProfile));
            customerRepository.save(customer);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveEntityImage(EntityType.USER,customer.getId(),imageFile,null);
        }
        CustomerProfileDTO responseDto= CustomerProfileDTO.from(customer);
        responseDto.setProfileImage(imageService.getUserImagePath(customer.getId()));
        return responseDto;
    }
}


