package org.acme.adapters.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.core.AuthSessionService;
import org.acme.core.ContactService;
import org.acme.core.UserService;
import org.acme.domain.Contact;
import org.acme.i18n.AgendaMessages;
import org.acme.i18n.MessageKey;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Path("/api/contacts")
public class ContactResource {

    private static final Logger LOG = Logger.getLogger(ContactResource.class);

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
                .map(ContactDto::new).toList();
        LOG.debug("contacts.list.completed userId=" + userContext.userId() + " count=" + contacts.size());
        return Response.ok(contacts).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
            CreateContactRequest request) {
        if (request == null) {
            return badRequest(AgendaMessages.get(MessageKey.CONTACT_DATA_REQUIRED));
        }

        UserContext userContext = authenticate(sessionId);
        LOG.debug("contacts.create.received userId=" + userContext.userId());

        try {
            Contact contact = contactService.create(userContext.userId(), request.firstName(), request.lastName(),
                    request.birthDate(), request.phoneNumbers(), request.relationshipDegree());
            return Response.status(Response.Status.CREATED).entity(new ContactDto(contact)).build();
        } catch (IllegalArgumentException | NullPointerException e) {
            return badRequest(e.getMessage());
        }
    }

    @PUT
    @Path("/{contactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
            @PathParam("contactId") Long contactId, CreateContactRequest request) {
        if (contactId == null || request == null) {
            return badRequest(AgendaMessages.get(MessageKey.CONTACT_DATA_REQUIRED));
        }

        UserContext userContext = authenticate(sessionId);
        LOG.debug("contacts.update.received userId=" + userContext.userId() + " contactId=" + contactId);

        try {
            Optional<Contact> updated = contactService.update(userContext.userId(), contactId, request.firstName(),
                    request.lastName(), request.birthDate(), request.phoneNumbers(), request.relationshipDegree());
            return updated.map(c -> Response.ok(new ContactDto(c)).build())
                    .orElseThrow(() -> new NotFoundException(AgendaMessages.get(MessageKey.CONTACT_NOT_FOUND)));
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
        LOG.debug("contacts.delete.received userId=" + userContext.userId() + " contactId=" + contactId);
        if (contactId == null) {
            return badRequest(AgendaMessages.get(MessageKey.CONTACT_INVALID));
        }

        contactService.findActiveByIdAndOwnerUserId(contactId, userContext.userId())
                .orElseThrow(() -> new NotFoundException(AgendaMessages.get(MessageKey.CONTACT_NOT_FOUND)));

        contactService.softDelete(contactId, userContext.userId());
        return Response.noContent().build();
    }

    private UserContext authenticate(String sessionId) {
        try {
            var session = authSessionService.findActiveSession(sessionId)
                    .orElseThrow(() -> new jakarta.ws.rs.NotAuthorizedException(
                            AgendaMessages.get(MessageKey.AUTH_SESSION_INVALID)));

            var currentUser = session.user();
            var user = userService.findOrCreateByExternalId(currentUser.subject(), currentUser.username(),
                    currentUser.email());
                return new UserContext(user.getId());
        } catch (AuthSessionService.SessionUnavailableException e) {
            throw new jakarta.ws.rs.ServiceUnavailableException(
                    AgendaMessages.get(MessageKey.AUTH_SESSION_UNAVAILABLE));
        }
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiErrorResponse.current(
                        message == null || message.isBlank() ? AgendaMessages.get(MessageKey.INVALID_DATA) : message,
                        Response.Status.BAD_REQUEST.getStatusCode()))
                .build();
    }

    record UserContext(Long userId) {
    }

    public record CreateContactRequest(String firstName, String lastName, LocalDate birthDate,
            List<String> phoneNumbers, String relationshipDegree) {
    }

    public record ContactDto(Long id, String firstName, String lastName, String fullName, LocalDate birthDate,
            List<String> phoneNumbers, String relationshipDegree) {
        public ContactDto(Contact contact) {
            this(contact.getId(), contact.getFirstName(), contact.getLastName(), contact.fullName(),
                    contact.getBirthDate(), contact.getPhoneNumbers().stream().map(phoneNumber -> phoneNumber.getNumber()).toList(),
                    contact.getRelationshipDegree());
        }
    }
}
