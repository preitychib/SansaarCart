package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.Address;
import com.preeti.sansarcart.exception.custom.ResourceNotFound;
import com.preeti.sansarcart.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public void deleteAddressByIdAndUser(UUID addressId, UUID userId) {
        Address address = getAddressByIdAndUser(addressId,userId);
        addressRepository.delete(address);
    }

    public Address getAddressByIdAndUser(UUID addressId, UUID userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFound("Address not found"));
    }

    public void saveAddress(Address address){
        addressRepository.save(address);
    }
}

