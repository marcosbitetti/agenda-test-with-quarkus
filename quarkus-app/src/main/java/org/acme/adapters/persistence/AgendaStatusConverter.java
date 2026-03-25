package org.acme.adapters.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.acme.domain.IAgendaEntity;

@Converter(autoApply = false)
public class AgendaStatusConverter implements AttributeConverter<IAgendaEntity.Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(IAgendaEntity.Status status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> 1;
            case DELETED -> 0;
        };
    }

    @Override
    public IAgendaEntity.Status convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case 1 -> IAgendaEntity.Status.ACTIVE;
            case 0 -> IAgendaEntity.Status.DELETED;
            default -> throw new IllegalArgumentException("Status invalido no banco: " + value);
        };
    }
}
