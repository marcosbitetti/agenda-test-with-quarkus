package org.acme.core;

import java.time.LocalDate;
import java.util.List;

public record ContactWriteInput(String firstName, String lastName, LocalDate birthDate,
        List<String> phoneNumbers, String relationshipDegree) {
}