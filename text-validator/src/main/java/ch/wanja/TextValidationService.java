package ch.wanja;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TextValidationService {

    public record ValidationRequest(long id, String content) {
    }

    public record ValidationResponse(long id, boolean valid) {
    }

    @Incoming("blogs-in")
    @Outgoing("validation-result")
    public Multi<ValidationResponse> validateTextMessages(Multi<ValidationRequest> requests) {
        System.out.println("---> TEST OUTPUT <----");
        return requests
                .onItem().transform(request -> {
                    boolean valid;
                    if (!request.content().contains("0")) {
                        valid = true;
                    } else {
                        valid = false;
                    }
                    System.out.println("Text-Validation: " + request.content() + " -> " + valid);
                    return new ValidationResponse(request.id(), valid);
                });
    }

    /*
     * @Incoming("blogs-in")
     * 
     * @Outgoing("validation-result")
     * public Multi<ValidationResponse>
     * validateTextMessages(Multi<ValidationRequest> requests) {
     * System.out.println("---------> validateTextMessages" + requests.toString());
     * 
     * return Multi.createFrom().items(
     * new ValidationResponse(1, true));
     * 
     * }
     */
}