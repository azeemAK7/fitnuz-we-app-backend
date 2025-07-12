package com.fitnuz.project.Payload.DTO;

import com.fitnuz.project.Model.User;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private Long addressId;

    @NotBlank
    @Size(min = 2,message = "buildingName must be minimum 3 character")
    private String buildingName;

    @NotBlank
    @Size(min = 2,message = "street must be minimum 3 character")
    private String street;

    @NotBlank
    @Size(min = 2,message = "city must be minimum 3 character")
    private String city;

    @NotBlank
    @Size(min = 3,message = "state must be minimum 3 character")
    private String state;

    @NotBlank
    @Size(min = 3,message = "country must be minimum 3 character")
    private String country;

    @NotBlank
    @Size(min = 6,message = "pincode must be minimum 6 character")
    private String pincode;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
}
