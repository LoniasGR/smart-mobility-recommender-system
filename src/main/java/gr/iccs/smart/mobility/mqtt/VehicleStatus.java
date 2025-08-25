package gr.iccs.smart.mobility.mqtt;

public record VehicleStatus(Integer service, Integer status, Double latitude, Double longitude, Double altitude) {
}
