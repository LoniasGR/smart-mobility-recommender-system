package gr.iccs.smart.mobility.scenario;

public record RandomScenario(Boolean randomize, Integer ports, Integer busStops, Integer cars, Integer scooters,
        Integer boats) {
    public RandomScenario {
        if (randomize == null) {
            randomize = false;
        }
        if (randomize) {
            if (ports == null) {
                ports = 11;
            }
            if (busStops == null) {
                busStops = 2;
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
    }
}
