package com.synechisveltiosi.tms.api.response;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.synechisveltiosi.tms.model.entity.Project}
 */
public record ProjectDto(UUID id, String name, String description) implements Serializable {
}