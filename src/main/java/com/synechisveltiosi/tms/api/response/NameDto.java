package com.synechisveltiosi.tms.api.response;

import com.synechisveltiosi.tms.model.embed.PersonDetails;

import java.io.Serializable;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.embed.PersonDetails.Name}
 */
public record NameDto(String firstName, String lastName) implements Serializable {
    public NameDto(PersonDetails.Name name) {
        this(name.getFirstName(), name.getLastName());
    }
}