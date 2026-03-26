package org.acme.adapters.persistence;

import org.acme.domain.IAgendaEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgendaStatusConverterTest {

    private final AgendaStatusConverter converter = new AgendaStatusConverter();

    @Test
    public void convertActiveStatusToDatabase() {
        assertEquals(1, converter.convertToDatabaseColumn(IAgendaEntity.Status.ACTIVE));
    }

    @Test
    public void convertDeletedStatusToDatabase() {
        assertEquals(0, converter.convertToDatabaseColumn(IAgendaEntity.Status.DELETED));
    }

    @Test
    public void convertNullStatusToDatabase() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void convertDatabaseValueToActiveStatus() {
        assertEquals(IAgendaEntity.Status.ACTIVE, converter.convertToEntityAttribute(1));
    }

    @Test
    public void convertDatabaseValueToDeletedStatus() {
        assertEquals(IAgendaEntity.Status.DELETED, converter.convertToEntityAttribute(0));
    }

    @Test
    public void rejectInvalidDatabaseValue() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute(9));

        assertEquals("Status invalido no banco: 9", error.getMessage());
    }
}
