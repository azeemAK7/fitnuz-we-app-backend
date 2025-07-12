package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Exception.CustomException.GeneralAPIException;
import com.fitnuz.project.Exception.CustomException.ResourceNotFoundException;
import com.fitnuz.project.Model.Address;
import com.fitnuz.project.Model.User;
import com.fitnuz.project.Payload.DTO.AddressDto;
import com.fitnuz.project.Payload.Response.AddressResponse;
import com.fitnuz.project.Repository.AddressRepository;
import com.fitnuz.project.Repository.UserRepository;
import com.fitnuz.project.Service.Definations.AddressService;
import com.fitnuz.project.Util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Transactional
    @Override
    public AddressResponse createAddress(AddressDto addressDto) {
        Address address = modelMapper.map(addressDto,Address.class);

        User user = authUtil.getUser();
        address.setUser(user);
        user.getAddresses().add(address);

        Address savedAddress =  addressRepository.save(address);
        AddressResponse addressResponse = modelMapper.map(address, AddressResponse.class);
        addressResponse.setUserName(user.getUserName());
        return addressResponse;
    }

    @Override
    public List<AddressDto> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        if(addresses.isEmpty()){
            throw new GeneralAPIException("No Addresses Exists");
        }
        List<AddressDto> addressDtos = addresses.stream().map(address -> {
            return modelMapper.map(address,AddressDto.class);
        }).toList();
        return addressDtos;
    }

    @Override
    public List<AddressDto> getUserAddresses() {
        User user = authUtil.getUser();
        List<Address> addresses = user.getAddresses();
        if(addresses.isEmpty()){
            throw new GeneralAPIException("No Addresses Exists");
        }
        List<AddressDto> addressDtos = addresses.stream().map(address -> {
            return modelMapper.map(address,AddressDto.class);
        }).toList();
        return addressDtos;
    }

    @Override
    public AddressResponse getAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","addressId",addressId));
        AddressResponse addressResponse = modelMapper.map(address, AddressResponse.class);
        addressResponse.setUserName(address.getUser().getUserName());
        return addressResponse;
    }

    @Transactional
    @Override
    public String removeAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address","addressId",addressId));
        User user = authUtil.getUser();
        user.getAddresses().removeIf(add -> add.getAddressId().equals(addressId));
        userRepository.save(user);
        return "Address with addressId " + addressId +" removed";
    }

    @Transactional
    @Override
    public AddressResponse updateAddress(Long addressId, AddressDto addressDto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        address.setBuildingName(addressDto.getBuildingName());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setState(addressDto.getState());
        address.setCountry(addressDto.getCountry());
        address.setPincode(addressDto.getPincode());

        Address updatedAddress = addressRepository.save(address);

        AddressResponse addressResponse = modelMapper.map(updatedAddress, AddressResponse.class);
        addressResponse.setUserName(updatedAddress.getUser().getUserName());
        return addressResponse;
    }
}
