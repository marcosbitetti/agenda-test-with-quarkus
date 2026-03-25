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

import java.time.LocalDate;
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

    @PUT
    @Path("/{contactId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@CookieParam(AuthSessionService.COOKIE_NAME) String sessionId,
                           @PathParam("contactId") Long contactId,
                           CreateContactRequest request) {
        if (contactId == null || request == null) {
            return badRequest("Dados do contato obrigatorios.");
        }

        UserContext userContext = authenticate(sessionId);

        try {
            Optional<Contact> updated = contactService.update(
                    userContext.userId(),
                    contactId,
                    request.firstName(),
                    request.lastName(),
                    request.birthDate(),
                    request.phoneNumbers(),
                    request.relationshipDegree()
            );
            if (updated.isEmpty()) {
                throw new NotFoundException("Contato nao encontrado.");
            }

            return Response.ok(new ContactDto(updated.get())).build();
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
}