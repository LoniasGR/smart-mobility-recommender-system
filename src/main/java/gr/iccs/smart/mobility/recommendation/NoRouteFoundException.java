package gr.iccs.smart.mobility.recommendation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoRouteFoundException extends RuntimeException {
    public NoRouteFoundException(String message) {
        super(message);
    }
}
