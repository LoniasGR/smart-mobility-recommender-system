package gr.iccs.smart.mobility.scenario;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ScenarioException extends RuntimeException{
    public ScenarioException(String message) {
        super(message);
    }
}
