package gr.iccs.smart.mobility.scenario;

import java.util.Map;

public record RandomScenario(Boolean randomize, Integer ports, Integer cars, Integer scooters, Integer boats) {
    public RandomScenario {
        if (randomize == null) {
            randomize = false;
        }
        if (ports == null) {
            ports = 11;
        }
        if (cars == null) {
            cars = 5;
        }
        if (scooters == null) {
            scooters = 5;
        }
        if (boats == null) {
            boats = 10;
        }
    }

    public RandomScenario(Map<String, String> allParams) {

        this(allParams.get("randomize") == null ? null : Boolean.parseBoolean(allParams.get("randomize")),
                allParams.get("random_ports") == null ? null : Integer.parseInt(allParams.get("random_ports")),
                allParams.get("random_cars") == null ? null : Integer.parseInt(allParams.get("random_cars")),
                allParams.get("random_scooters") == null ? null : Integer.parseInt(allParams.get("random_scooters")),
                allParams.get("random_boats") == null ? null : Integer.parseInt(allParams.get("random_boats")));
    }

}
