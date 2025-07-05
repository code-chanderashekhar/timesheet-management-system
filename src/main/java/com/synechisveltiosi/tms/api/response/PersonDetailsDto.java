package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.embed.PersonDetails}
 */
public record PersonDetailsDto(NameDto name, ContactDto contact, AddressDto address) implements Serializable {
  public PersonDetailsDto(PersonDetails personDetails) {
    this(new NameDto(personDetails.getName()), new ContactDto(personDetails.getContact()), new AddressDto(personDetails.getAddress()));
  }

}