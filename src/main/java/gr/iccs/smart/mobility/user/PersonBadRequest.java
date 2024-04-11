package gr.iccs.smart.mobility.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PersonBadRequest extends RuntimeException {
    public PersonBadRequest(String message) {
        super(message);
    }

}
