package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.AuthSessionService;
import org.acme.core.ContactService;
import org.acme.core.UserService;
import org.acme.domain.Contact;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Path("/api/contacts")
public class ContactResource {

    @Inject
    AuthSessionService authSessionService;

    @Inject
    UserService userService;

    @Inject
    ContactService contactService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        UserContext userContext = authenticate(sessionId);
        List<ContactDto> contacts = contactService.listActiveByOwnerUserId(userContext.userId()).stream()
                .map(ContactDto::new)
                .toList();
        return Response.ok(contacts).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId, CreateContactRequest request) {
        if (request == null) {
            return badRequest("Dados do contato obrigatorios.");
        }

        UserContext userContext = authenticate(sessionId);

        try {
            Contact contact = contactService.create(
                    userContext.userId(),
                    request.firstName(),
                    request.lastName(),
                    request.birthDate(),
                    request.phoneNumbers(),
                    request.relationshipDegree()
            );
            return Response.status(Response.Status.CREATED)
                    .entity(new ContactDto(contact))
                    .build();
        } catch (IllegalArgumentException | NullPointerException e) {
            return badRequest(e.getMessage());
        }
    }

    @DELETE
    @Path("/{contactId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
                           @PathParam("contactId") Long contactId) {
        UserContext userContext = authenticate(sessionId);
        if (contactId == null) {
            return badRequest("Contato invalido.");
        }

        if (contactService.findActiveByIdAndOwnerUserId(contactId, userContext.userId()).isEmpty()) {
            throw new NotFoundException("Contato nao encontrado.");
        }

        contactService.softDelete(contactId, userContext.userId());
        return Response.noContent().build();
    }

    @GET
    @Path("/panel")
    @Produces(MediaType.TEXT_HTML)
    public Response panel(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId) {
        UserContext userContext = authenticate(sessionId);
        return htmlPanel(userContext.userId(), null, null, FormState.empty());
    }

    @POST
    @Path("/panel")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response createFromForm(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
                                   @FormParam("firstName") String firstName,
                                   @FormParam("lastName") String lastName,
                                   @FormParam("birthDate") String birthDate,
                                   @FormParam("phoneNumbers") String phoneNumbers,
                                   @FormParam("relationshipDegree") String relationshipDegree) {
        UserContext userContext = authenticate(sessionId);
        FormState formState = new FormState(firstName, lastName, birthDate, phoneNumbers, relationshipDegree);

        try {
            contactService.create(
                    userContext.userId(),
                    firstName,
                    lastName,
                    parseBirthDate(birthDate),
                    splitPhoneNumbers(phoneNumbers),
                    relationshipDegree
            );
            return htmlPanel(userContext.userId(), "Contato salvo com sucesso.", null, FormState.empty());
        } catch (IllegalArgumentException | NullPointerException e) {
            return htmlPanel(userContext.userId(), null, safeMessage(e), formState);
        }
    }

    @DELETE
    @Path("/panel/{contactId}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteFromPanel(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
                                    @PathParam("contactId") String rawContactId) {
        UserContext userContext = authenticate(sessionId);
        Long contactId = parseContactId(rawContactId);
        if (contactId == null) {
            return htmlPanel(userContext.userId(), null, "Contato invalido.", FormState.empty());
        }

        if (contactService.findActiveByIdAndOwnerUserId(contactId, userContext.userId()).isEmpty()) {
            return htmlPanel(userContext.userId(), null, "Contato nao encontrado.", FormState.empty());
        }

        contactService.softDelete(contactId, userContext.userId());
        return htmlPanel(userContext.userId(), "Contato excluido com sucesso.", null, FormState.empty());
    }

    private UserContext authenticate(String sessionId) {
        try {
            var session = authSessionService.findActiveSession(sessionId);
            if (session.isEmpty()) {
                throw new jakarta.ws.rs.NotAuthorizedException("Sessao invalida.");
            }

            var currentUser = session.get().user();
            var user = userService.findOrCreateByExternalId(
                    currentUser.subject(),
                    currentUser.username(),
                    currentUser.email()
            );
            return new UserContext(user.id);
        } catch (AuthSessionService.SessionUnavailableException e) {
            throw new jakarta.ws.rs.ServiceUnavailableException("Sessao indisponivel.");
        }
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(message == null || message.isBlank() ? "Dados invalidos." : message))
                .build();
    }

    private Response htmlPanel(Long userId, String successMessage, String errorMessage, FormState formState) {
        String html = renderPanel(contactService.listActiveByOwnerUserId(userId), successMessage, errorMessage, formState);
        return Response.ok(html, MediaType.TEXT_HTML).build();
    }

    private Long parseContactId(String rawContactId) {
        if (rawContactId == null || rawContactId.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(rawContactId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            throw new IllegalArgumentException("Data de nascimento obrigatoria.");
        }

        try {
            return LocalDate.parse(birthDate.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de nascimento obrigatoria.");
        }
    }

    private List<String> splitPhoneNumbers(String phoneNumbers) {
        if (phoneNumbers == null) {
            return List.of();
        }

        return phoneNumbers.lines()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String renderPanel(List<Contact> contacts, String successMessage, String errorMessage, FormState formState) {
        StringBuilder html = new StringBuilder();
        html.append("<section id=\"contacts-panel\" class=\"space-y-6\">");
        html.append("<div class=\"rounded-3xl border border-stone-200 bg-stone-50 p-6\">");
        html.append("<div class=\"flex flex-wrap items-start justify-between gap-4\">");
        html.append("<div>");
        html.append("<h2 class=\"text-xl font-semibold text-stone-900\">Contatos ativos</h2>");
        html.append("<p class=\"mt-3 text-sm leading-6 text-stone-600\">Sua agenda agora usa fragments renderizados no servidor e mostra apenas registros ativos do usuario autenticado.</p>");
        html.append("</div>");
        html.append("<div class=\"rounded-2xl border border-emerald-200 bg-white px-4 py-3 text-right shadow-sm\">");
        html.append("<p class=\"text-xs font-medium uppercase tracking-[0.2em] text-emerald-700\">Resumo</p>");
        html.append("<p class=\"mt-1 text-3xl font-semibold text-stone-900\">").append(contacts.size()).append("</p>");
        html.append("<p class=\"text-xs text-stone-500\">contatos cadastrados</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class=\"rounded-3xl border border-stone-200 bg-white p-6 shadow-sm\">");
        html.append("<div class=\"flex items-center justify-between gap-3\">");
        html.append("<div><p class=\"text-sm font-medium uppercase tracking-[0.2em] text-emerald-700\">Agenda</p>");
        html.append("<h2 class=\"mt-2 text-2xl font-semibold text-stone-900\">Lista principal</h2></div>");
        html.append("<button type=\"button\" class=\"rounded-2xl border border-emerald-300 px-4 py-2 text-sm font-medium text-emerald-800 transition hover:bg-emerald-100\" hx-get=\"/api/contacts/panel\" hx-target=\"#contacts-panel\" hx-swap=\"outerHTML\">Atualizar contatos</button>");
        html.append("</div>");
        appendAlert(html, successMessage, "border-emerald-200 bg-emerald-50 text-emerald-700", "mt-4");
        appendAlert(html, errorMessage, "border-red-200 bg-red-50 text-red-700", "mt-4");
        if (contacts.isEmpty()) {
            html.append("<p class=\"mt-4 rounded-2xl border border-dashed border-stone-300 bg-stone-50 px-4 py-6 text-sm text-stone-500\">Nenhum contato ativo cadastrado.</p>");
        } else {
            html.append("<ul class=\"mt-4 space-y-3\">");
            for (Contact contact : contacts) {
                html.append(renderContactItem(contact));
            }
            html.append("</ul>");
        }
        html.append("</div>");

        html.append("<div class=\"rounded-3xl border border-stone-200 bg-white p-5 shadow-sm\">");
        html.append("<p class=\"text-sm font-medium uppercase tracking-[0.2em] text-emerald-700\">Novo contato</p>");
        html.append("<form class=\"mt-4 space-y-4\" hx-post=\"/api/contacts/panel\" hx-target=\"#contacts-panel\" hx-swap=\"outerHTML\" hx-sync=\"this:replace\" hx-disabled-elt=\"find button[type='submit']\">");
        html.append("<div class=\"grid gap-4 sm:grid-cols-2\">");
        html.append("<label class=\"block text-sm text-stone-700\"><span class=\"mb-1 block font-medium\">Nome</span><input name=\"firstName\" required value=\"").append(escapeHtml(formState.firstName())).append("\" class=\"w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white\" /></label>");
        html.append("<label class=\"block text-sm text-stone-700\"><span class=\"mb-1 block font-medium\">Sobrenome</span><input name=\"lastName\" required value=\"").append(escapeHtml(formState.lastName())).append("\" class=\"w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white\" /></label>");
        html.append("</div>");
        html.append("<label class=\"block text-sm text-stone-700\"><span class=\"mb-1 block font-medium\">Data de nascimento</span><input name=\"birthDate\" type=\"date\" required value=\"").append(escapeHtml(formState.birthDate())).append("\" class=\"w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white\" /></label>");
        html.append("<label class=\"block text-sm text-stone-700\"><span class=\"mb-1 block font-medium\">Telefones</span><textarea name=\"phoneNumbers\" required rows=\"3\" placeholder=\"Um telefone por linha\" class=\"w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white\">").append(escapeHtml(formState.phoneNumbers())).append("</textarea></label>");
        html.append("<label class=\"block text-sm text-stone-700\"><span class=\"mb-1 block font-medium\">Grau de parentesco</span><input name=\"relationshipDegree\" value=\"").append(escapeHtml(formState.relationshipDegree())).append("\" class=\"w-full rounded-2xl border border-stone-300 bg-stone-50 px-4 py-3 outline-none transition focus:border-emerald-500 focus:bg-white\" /></label>");
        html.append("<button type=\"submit\" class=\"w-full rounded-2xl bg-emerald-700 px-4 py-3 font-medium text-white transition hover:bg-emerald-600\">Salvar contato</button>");
        html.append("</form>");
        html.append("</div>");
        html.append("</section>");
        return html.toString();
    }

    private String renderContactItem(Contact contact) {
        StringBuilder html = new StringBuilder();
        html.append("<li class=\"rounded-3xl border border-stone-200 bg-stone-50 p-5 shadow-sm\">");
        html.append("<div class=\"flex flex-wrap items-start justify-between gap-4\">");
        html.append("<div class=\"space-y-3\">");
        html.append("<div><h3 class=\"text-lg font-semibold text-stone-900\">").append(escapeHtml(contact.fullName())).append("</h3>");
        html.append("<p class=\"text-sm text-stone-500\">Nascimento: ").append(formatBirthDate(contact.birthDate)).append("</p></div>");
        if (contact.relationshipDegree != null) {
            html.append("<p class=\"text-sm text-stone-500\">Parentesco: ").append(escapeHtml(contact.relationshipDegree)).append("</p>");
        }
        html.append("<div class=\"flex flex-wrap gap-2\">");
        for (var phoneNumber : contact.phoneNumbers) {
            html.append("<span class=\"rounded-full bg-stone-100 px-3 py-1 text-xs font-medium text-stone-700\">")
                    .append(escapeHtml(phoneNumber.number))
                    .append("</span>");
        }
        html.append("</div></div>");
        html.append("<button type=\"button\" class=\"rounded-2xl border border-red-200 px-4 py-2 text-sm font-medium text-red-700 transition hover:bg-red-50\" hx-delete=\"/api/contacts/panel/")
                .append(contact.id)
                .append("\" hx-target=\"#contacts-panel\" hx-swap=\"outerHTML\">Excluir</button>");
        html.append("</div></li>");
        return html.toString();
    }

    private void appendAlert(StringBuilder html, String message, String classes, String spacing) {
        if (message == null || message.isBlank()) {
            return;
        }

        html.append("<p class=\"")
                .append(spacing)
                .append(" rounded-2xl border px-4 py-3 text-sm ")
                .append(classes)
                .append("\">")
                .append(escapeHtml(message))
                .append("</p>");
    }

    private String formatBirthDate(LocalDate birthDate) {
        return String.format("%02d/%02d/%04d", birthDate.getDayOfMonth(), birthDate.getMonthValue(), birthDate.getYear());
    }

    private String escapeHtml(String value) {
        return Optional.ofNullable(value)
                .orElse("")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? "Dados invalidos." : e.getMessage();
    }

    record UserContext(Long userId) {
    }

    public record CreateContactRequest(String firstName,
                                       String lastName,
                                       LocalDate birthDate,
                                       List<String> phoneNumbers,
                                       String relationshipDegree) {
    }

    public record ContactDto(Long id,
                             String firstName,
                             String lastName,
                             String fullName,
                             LocalDate birthDate,
                             List<String> phoneNumbers,
                             String relationshipDegree) {
        public ContactDto(Contact contact) {
            this(
                    contact.id,
                    contact.firstName,
                    contact.lastName,
                    contact.fullName(),
                    contact.birthDate,
                    contact.phoneNumbers.stream().map(phoneNumber -> phoneNumber.number).toList(),
                    contact.relationshipDegree
            );
        }
    }

    public record ErrorResponse(String message) {
    }

    record FormState(String firstName,
                     String lastName,
                     String birthDate,
                     String phoneNumbers,
                     String relationshipDegree) {
        static FormState empty() {
            return new FormState("", "", "", "", "");
        }
    }
}