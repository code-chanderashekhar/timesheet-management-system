package com.synechisveltiosi.tms.controller;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.MediaType;

public class RestRequestSpecification {
    public RequestSpecification getBasicRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(MediaType.APPLICATION_JSON_VALUE)
                .setAccept(MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
