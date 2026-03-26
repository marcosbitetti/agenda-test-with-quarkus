package org.acme.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgendaMessagesTest {

    @Test
    public void getsMessageFromPtBrBundle() {
        assertEquals("Informe login ou e-mail e senha.", AgendaMessages.get(MessageKey.AUTH_LOGIN_REQUIRED));
    }

    @Test
    public void fallsBackToBaseBundleWhenLocaleIsUnknown() {
        assertEquals("Contato nao encontrado.", AgendaMessages.get("en-US", MessageKey.CONTACT_NOT_FOUND));
    }

    @Test
    public void formatsParameterizedMessage() {
        assertEquals("arquivo.html nao encontrada", AgendaMessages.format(MessageKey.PAGE_NOT_FOUND, "arquivo.html"));
    }
}
