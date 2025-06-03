package com.preeti.sansarcart.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.exception.custom.ListValidationException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.address.AddressUpdateDTO;
import com.preeti.sansarcart.payload.authentication.ResetPasswordDto;
import com.preeti.sansarcart.payload.address.AddressDTO;
import com.preeti.sansarcart.payload.profile.SellerProfileDTO;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.service.AddressService;
import com.preeti.sansarcart.service.AuthenticationService;
import com.preeti.sansarcart.service.SellerService;
import com.preeti.sansarcart.service.UserService;
import com.preeti.sansarcart.service.image.ImageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("seller")
public class SellerController {

    private final UserService userService;
    private final SellerService sellerService;
    private final AuthenticationService authenticationService;
    private final AddressService addressService;
    private final ImageService imageService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetSellerPassword (@Valid @RequestBody ResetPasswordDto dto) {
        dto.validatePasswords();
        User currentUser= authenticationService.getCurrentUser();
        userService.updateUserPassword(currentUser,dto.getPassword());
        authenticationService.sendPasswordUpdateMail(currentUser.getEmail());
        return ResponseEntity.ok(ApiResponse
                .success(i18n("password.updated.success"),null));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> getCurrentSellerProfile(){
        Seller currentSeller = (Seller) authenticationService.getCurrentUser();
        Address address=sellerService.getSellerCompanyAddress(currentSeller.getId());
        SellerProfileDTO sellerProfile= SellerProfileDTO.from(currentSeller);
        sellerProfile.setAddress(AddressDTO.from(address));
        sellerProfile.setProfileImage(imageService.getUserImagePath(currentSeller.getId()));
        return ResponseEntity.ok(ApiResponse
                .success(i18n("seller.profile.fetched.success"),sellerProfile)
        );
    }


    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SellerProfileDTO>> updateProfile(
            @RequestPart(value = "profile",required = false) String data,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        SellerProfileDTO updatedProfile = null;
        try {
            if (data != null && !data.isBlank()) {
                updatedProfile = objectMapper.readValue(data, SellerProfileDTO.class);
                Set<ConstraintViolation<SellerProfileDTO>> violations = validator.validate(updatedProfile);
                if (!violations.isEmpty()) {
                    List<String> errorMessages = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toList());

                    throw new ListValidationException(errorMessages);
                }
            }
            Seller currentSeller = (Seller) authenticationService.getCurrentUser();
             updatedProfile= sellerService.updateProfile(currentSeller, updatedProfile, imageFile);
            return ResponseEntity.ok(ApiResponse.success(i18n("seller.profile.updated.success"),updatedProfile));
        } catch (JsonProcessingException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @PatchMapping("/address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateSellerAddress(@Valid @RequestBody AddressUpdateDTO patchDTO) {
        User currentUser = authenticationService.getCurrentUser();
        Address address= sellerService.getSellerCompanyAddress(currentUser.getId());
        Address updatedAddress= patchDTO.patchAddress(address);
        addressService.saveAddress(updatedAddress);
        return ResponseEntity.ok(ApiResponse.success(i18n("seller.address.updated.success"),AddressDTO.from(updatedAddress)));
    }

}
