package gr.iccs.smart.mobility.pointsofinterest;

import java.util.List;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Node;

import gr.iccs.smart.mobility.connection.Connection;

@Node
public class BusStop extends PointOfInterest {
    public BusStop(String id, String name, Point location, List<Connection> connections) {
        super(id, name, location, connections);
    }

    @Override
    public String getOrsProfile() {
        return "foot-walking";
    }

}
