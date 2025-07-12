package com.fitnuz.project.Controllers;

import com.fitnuz.project.Payload.DTO.AddressDto;
import com.fitnuz.project.Payload.Response.AddressResponse;
import com.fitnuz.project.Service.Definations.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressDto addressDto){
        AddressResponse savedAddress = addressService.createAddress(addressDto);
        return new ResponseEntity<>(savedAddress, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getAllAddresses(){
        List<AddressDto> addresses = addressService.getAllAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<List<AddressDto>> getUserAddresses(){
        List<AddressDto> addresses = addressService.getUserAddresses();
        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable Long addressId){
        AddressResponse address = addressService.getAddress(addressId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> removeAddress(@PathVariable Long addressId){
        String status = addressService.removeAddress(addressId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@Valid @RequestBody AddressDto addressDto,@PathVariable Long addressId){
        AddressResponse address = addressService.updateAddress(addressId,addressDto);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }



}
