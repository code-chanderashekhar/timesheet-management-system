package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.embed.PersonDetails.Contact}
 */
public record ContactDto(String contactNumber, String emailAddress) implements Serializable {
    public ContactDto(PersonDetails.Contact contact) {
        this(contact.getContactNumber(), contact.getEmailAddress());
    }
}