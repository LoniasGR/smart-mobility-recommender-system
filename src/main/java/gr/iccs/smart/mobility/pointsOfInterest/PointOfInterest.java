package gr.iccs.smart.mobility.pointsOfInterest;

import org.neo4j.driver.types.Point;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node
abstract public class PointOfInterest {
    @Id
    private final UUID id;

    private Point location;

    public PointOfInterest(UUID id, Point location) {
        this.id = id;
        this.location = location;
    }


    /*
     **************************************************************************
     * GETTERS & SETTERS
     **************************************************************************
     */

    public UUID getId() {
        return id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

}
