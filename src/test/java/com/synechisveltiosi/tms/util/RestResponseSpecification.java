package com.synechisveltiosi.tms.util;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.notNullValue;

public class RestResponseSpecification {
    public ResponseSpecification getErrorResponseSpec() {
        return new ResponseSpecBuilder()
                .expectBody("title", notNullValue())
                .expectBody("detail", notNullValue())
                .expectBody("instance", notNullValue())
                .expectBody("method", notNullValue())
                .expectBody("timestamp", notNullValue())
                .build();
    }
}
