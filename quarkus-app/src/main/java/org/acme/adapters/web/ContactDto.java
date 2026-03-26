package org.acme.adapters.web;

import java.time.LocalDate;
import java.util.List;

public record ContactDto(Long id, String firstName, String lastName, String fullName, LocalDate birthDate,
        List<String> phoneNumbers, String relationshipDegree) {
}