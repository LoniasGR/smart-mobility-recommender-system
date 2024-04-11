package gr.iccs.smart.mobility.vehicle;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadVehicleRequest extends RuntimeException {

    public BadVehicleRequest(String message) {
        super(message);
    }
}
