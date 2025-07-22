package gr.iccs.smart.mobility.connection;

public record ConnectionDTO(Long id, Double distance, Double time, ReachableNode target) {
    public static ConnectionDTO fromConnection(Connection connection) {
        return new ConnectionDTO(
                connection.getId(),
                connection.getDistance(),
                connection.getTime(),
                connection.getTarget());
    }
}
