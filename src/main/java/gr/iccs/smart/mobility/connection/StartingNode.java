package gr.iccs.smart.mobility.connection;

import org.neo4j.driver.types.Point;

public interface StartingNode {

    public void addConnection(Connection connection);

    public String getOrsProfile();

    public Point getLocation();
}
