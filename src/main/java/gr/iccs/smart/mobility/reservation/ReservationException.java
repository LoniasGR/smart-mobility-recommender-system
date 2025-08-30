package gr.iccs.smart.mobility.reservation;

import org.springframework.http.HttpStatus;

public class ReservationException extends RuntimeException {

    private final HttpStatus status;

    public ReservationException(String message, HttpStatus status) {
        super(message);
        this.status = status;

    }

    public HttpStatus getStatus() {
        return status;
    }

}
