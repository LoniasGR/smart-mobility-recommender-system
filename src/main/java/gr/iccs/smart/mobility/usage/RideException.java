package gr.iccs.smart.mobility.usage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RideException extends RuntimeException {
    public RideException(String message) {
        super(message);
    }
}
