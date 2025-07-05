package com.synechisveltiosi.tms.model.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PersonDetails {

    @Embedded
    private Name name;
    @Embedded
    private Address address;
    @Embedded
    private Contact contact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Name {
        String firstName;
        String lastName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Address {
        String addressLine1;
        String addressLine2;
        String city;
        String state;
        String zipCode;
        String country;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class Contact {
        @Column(name = "contact_number")
        private String contactNumber;
        @Column(name = "email_address")
        private String emailAddress;
    }
}