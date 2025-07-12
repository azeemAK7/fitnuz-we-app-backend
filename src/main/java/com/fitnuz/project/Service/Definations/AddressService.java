package com.fitnuz.project.Service.Definations;

import com.fitnuz.project.Payload.DTO.AddressDto;
import com.fitnuz.project.Payload.Response.AddressResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AddressService {
    @Transactional
    AddressResponse createAddress(AddressDto addressDto);

    List<AddressDto> getAllAddresses();

    List<AddressDto> getUserAddresses();

    AddressResponse getAddress(Long addressId);

    @Transactional
    String removeAddress(Long addressId);

    @Transactional
    AddressResponse updateAddress(Long addressId, AddressDto addressDto);
}
