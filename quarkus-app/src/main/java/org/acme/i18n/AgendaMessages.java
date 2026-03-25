package org.acme.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class AgendaMessages {

    public static final String PT_BR = "pt-BR";

    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static final Locale BASE_LOCALE = Locale.ROOT;
    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(PT_BR);

    private AgendaMessages() {
    }

    public static String get(MessageKey key) {
        return get(PT_BR, key);
    }

    public static String get(String localeTag, MessageKey key) {
        try {
            return bundleFor(localeTag).getString(key.name());
        } catch (MissingResourceException missingResourceException) {
            try {
                return bundleFor(BASE_LOCALE).getString(key.name());
            } catch (MissingResourceException fallbackException) {
                throw new IllegalArgumentException("Mensagem nao configurada para a chave: " + key, fallbackException);
            }
        }
    }

    public static String format(MessageKey key, Object... args) {
        return format(PT_BR, key, args);
    }

    public static String format(String localeTag, MessageKey key, Object... args) {
        return String.format(resolveLocale(localeTag), get(localeTag, key), args);
    }

    private static ResourceBundle bundleFor(String localeTag) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, resolveLocale(localeTag));
    }

    private static ResourceBundle bundleFor(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
    }

    private static Locale resolveLocale(String localeTag) {
        if (localeTag == null || localeTag.isBlank()) {
            return DEFAULT_LOCALE;
        }
        Locale locale = Locale.forLanguageTag(localeTag);
        return locale.getLanguage().isBlank() ? DEFAULT_LOCALE : locale;
    }
}