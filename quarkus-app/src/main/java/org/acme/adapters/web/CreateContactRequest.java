package org.acme.adapters.web;

import java.time.LocalDate;
import java.util.List;

public record CreateContactRequest(String firstName, String lastName, LocalDate birthDate,
        List<String> phoneNumbers, String relationshipDegree) {
}