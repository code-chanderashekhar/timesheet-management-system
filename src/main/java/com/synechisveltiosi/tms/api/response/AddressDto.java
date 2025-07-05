package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.embed.PersonDetails.Address}
 */
public record AddressDto(String addressLine1, String addressLine2, String city, String state, String zipCode,
                         String country) implements Serializable {
  public AddressDto(PersonDetails.Address address) {
    this(address.getAddressLine1(), address.getAddressLine2(), address.getCity(), address.getState(),
         address.getZipCode(), address.getCountry());
  }
}