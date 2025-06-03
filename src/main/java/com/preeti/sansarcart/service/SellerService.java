package com.preeti.sansarcart.service;


import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.entity.Role;
import com.preeti.sansarcart.entity.Seller;
import com.preeti.sansarcart.enums.AddressLabelType;
import com.preeti.sansarcart.enums.EntityType;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.address.AddressDTO;
import com.preeti.sansarcart.payload.authentication.SellerRegisterDto;
import com.preeti.sansarcart.payload.profile.SellerProfileDTO;
import com.preeti.sansarcart.projection.SellerListView;
import com.preeti.sansarcart.repository.AddressRepository;
import com.preeti.sansarcart.repository.RoleRepository;
import com.preeti.sansarcart.repository.user.SellerRepository;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.preeti.sansarcart.common.Util.getNullPropertyNames;


@Service
@RequiredArgsConstructor
public class SellerService {

    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public SellerRegisterDto signup(SellerRegisterDto sellerRegisterDto) {

        validateUniqueCompanyName(
                sellerRegisterDto.getCompanyName(),
                sellerRegisterDto.getGstNumber(),
                sellerRegisterDto.getCompanyContactNumber()
        );
        if (userRepository.findByEmail(sellerRegisterDto.getEmail()).isPresent()) {
            throw new ValidationException("Email is already registered");
        }

        Role role = roleRepository.findByAuthority(RoleType.SELLER)
                .orElseThrow(() -> new ResourceNotFound("Role not found"));

        Seller seller = sellerRegisterDto.toSeller();
        seller.setPassword(passwordEncoder.encode(sellerRegisterDto.getPassword()));
        seller.setActive(false);
        seller.setRoles(Set.of(role));
        seller=sellerRepository.save(seller);
        Address address = sellerRegisterDto.getAddress().toSellerAddress(seller);
        addressRepository.save(address);
        sellerRegisterDto.setAddress(AddressDTO.from(address));
        return  sellerRegisterDto;
    }

    public void validateUniqueCompanyName(String companyName, String gstNumber, String contactNumber) {
        Optional<Seller> sellerConflict = sellerRepository
                .findFirstByCompanyNameIgnoreCaseOrGstNumberIgnoreCaseOrCompanyContactNumber
                        (companyName, gstNumber, contactNumber);
        if (sellerConflict.isPresent()) {
            throw new ValidationException("Company name, GST number, or contact number already exists");

        }
    }

    public List<SellerListView> getAllSellers(int page, int size, String sortBy, String sortDir, String email) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (StringUtils.hasText(email)) {
            return sellerRepository.findByEmailContainingIgnoreCase(email, pageable).getContent();
        } else {
            return sellerRepository.findAllProjectedBy(pageable).getContent();
        }
    }


    public Address getSellerCompanyAddress(UUID sellerId) {
        return addressRepository.findFirstByUserIdAndLabel(sellerId, AddressLabelType.SELLER_COMPANY)
                .orElseThrow(() -> new ValidationException("No company address found for this seller"));
    }

    public SellerProfileDTO updateProfile(Seller seller, SellerProfileDTO updatedProfile, MultipartFile imageFile) {

        Address address = getSellerCompanyAddress(seller.getId());

        if (updatedProfile != null ) {
            BeanUtils.copyProperties(updatedProfile, seller, getNullPropertyNames(updatedProfile));
            if (updatedProfile.getAddress() != null) {
                Address patchAddress = updatedProfile.getAddress().toSellerAddress(seller);
                BeanUtils.copyProperties(patchAddress, address, getNullPropertyNames(patchAddress));
                addressRepository.save(address);
            }
            sellerRepository.save(seller);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.saveEntityImage(EntityType.USER,seller.getId(),imageFile,null);
        }

        SellerProfileDTO responseDto = SellerProfileDTO.from(seller);
        if (address != null) {
            responseDto.setAddress(AddressDTO.from(address));
        }
        responseDto.setProfileImage(imageService.getUserImagePath(seller.getId()));
        return responseDto;

    }


}
