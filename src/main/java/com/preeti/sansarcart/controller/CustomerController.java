package com.preeti.sansarcart.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.entity.Customer;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.address.AddressUpdateDTO;
import com.preeti.sansarcart.payload.authentication.ResetPasswordDto;
import com.preeti.sansarcart.payload.address.AddressDTO;
import com.preeti.sansarcart.payload.profile.CustomerProfileDTO;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.service.*;
import com.preeti.sansarcart.service.image.ImageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("customer")
public class CustomerController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final AddressService addressService;
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;
    private final Validator validator;

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetCustomerPassword (@Valid @RequestBody ResetPasswordDto dto) {
        log.info("Customer: Resetting password for user");
        dto.validatePasswords();
        User currentUser = authenticationService.getCurrentUser();
        userService.updateUserPassword(currentUser, dto.getPassword());
        authenticationService.sendPasswordUpdateMail(currentUser.getEmail());
        log.info("Customer: Password updated successfully for user with email: {}", currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success(i18n("customer.password.updated"), null));
    }

    // image
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> getCurrentCustomerProfile() {
        log.info("Customer: Fetching profile for current customer");
        Customer currentCustomer = (Customer) authenticationService.getCurrentUser();
        CustomerProfileDTO customerProfileDTO = CustomerProfileDTO.from(currentCustomer);
        customerProfileDTO.setProfileImage(imageService.getUserImagePath(currentCustomer.getId()));
        log.info("Customer: Profile fetched successfully for customer with ID: {}", currentCustomer.getId());
        return ResponseEntity.ok(ApiResponse.success(i18n("customer.profile.fetched"), customerProfileDTO));
    }

    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CustomerProfileDTO>> updateProfile(
            @RequestPart(value = "profile", required = false) String data,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        log.info("Customer: Updating profile with data: {}", data);
        CustomerProfileDTO updatedProfile = null;
        try {
            if (data != null && !data.isBlank()) {
                updatedProfile = objectMapper.readValue(data, CustomerProfileDTO.class);
                Set<ConstraintViolation<CustomerProfileDTO>> violations = validator.validate(updatedProfile);
                if (!violations.isEmpty()) {
                    List<String> errorMessages = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toList());

                    throw new ListValidationException(errorMessages);
                }
            }
            Customer currentCustomer = (Customer) authenticationService.getCurrentUser();
            CustomerProfileDTO updatedCustomer = customerService.updateProfile(currentCustomer, updatedProfile, imageFile);
            log.info("Customer: Profile updated successfully for customer with ID: {}", currentCustomer.getId());
            return ResponseEntity.ok(ApiResponse.success(i18n("customer.profile.update.success"),updatedCustomer));
        } catch (JsonProcessingException e) {
            log.error("Customer: Error processing profile update data", e);
            throw new ValidationException(e.getMessage());
        }
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteCustomerAddress(@PathVariable UUID addressId) {
        log.info("Customer: Deleting address with ID: {}", addressId);
        User currentUser = authenticationService.getCurrentUser();
        addressService.deleteAddressByIdAndUser(addressId, currentUser.getId());
        log.info("Customer: Address with ID: {} deleted successfully", addressId);
        return ResponseEntity.ok(ApiResponse.success(i18n("customer.address.deleted"), null));
    }

    @PatchMapping("/address/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateCustomerAddress(@PathVariable UUID addressId,
                                                                         @Valid @RequestBody AddressUpdateDTO patchDTO) {
        log.info("Customer: Updating address with ID: {}", addressId);
        User currentUser = authenticationService.getCurrentUser();
        Address address = addressService.getAddressByIdAndUser(addressId, currentUser.getId());
        Address updatedAddress = patchDTO.patchAddress(address);
        addressService.saveAddress(updatedAddress);
        log.info("Customer: Address with ID: {} updated successfully", addressId);
        return ResponseEntity.ok(ApiResponse.success(i18n("customer.address.updated"), AddressDTO.from(updatedAddress)));
    }

    @PostMapping("/address/add")
    public ResponseEntity<ApiResponse<AddressDTO>> addCustomerAddress(@Valid @RequestBody AddressDTO addressDTO) {
        log.info("Customer: Adding new address for customer");
        User customer = authenticationService.getCurrentUser();
        Address address = addressDTO.toUserAddress(customer);
        addressService.saveAddress(address);
        log.info("Customer: New address added successfully for customer with ID: {}", customer.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(i18n("customer.address.added"), AddressDTO.from(address)));
    }

    @GetMapping("/address/all")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getMyAddresses() {
        log.info("Customer: Fetching all addresses for current customer");
        Customer customer = (Customer) authenticationService.getCurrentUser();
        List<AddressDTO> addresses = customerService.getCustomerAddresses(customer.getId());
        log.info("Customer: Fetched all addresses for customer with ID: {}", customer.getId());
        return ResponseEntity.ok(ApiResponse.success(i18n("customer.address.all.fetched"), addresses));
    }

}
