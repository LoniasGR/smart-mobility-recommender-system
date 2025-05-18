package gr.iccs.smart.mobility.database;

import org.neo4j.driver.types.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gr.iccs.smart.mobility.location.LocationDTO;
import gr.iccs.smart.mobility.util.DBClientProvider;

@Service
public class DatabaseService {

    @Autowired
    private DBClientProvider dbClientProvider;

    private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

    public String clearDatabase() {
        var summary = dbClientProvider.getClient().query("MATCH (n) DETACH DELETE n").run();
        var counters = summary.counters();

        var info = String.format("%d nodes have been deleted \n%d relationships have been deleted",
                counters.nodesDeleted(), counters.relationshipsDeleted());

        log.info("\n" + info);
        return info;
    }

    public void addVehicleLocationIndex() {
        dbClientProvider.getClient()
                .query("CREATE POINT INDEX vehicle_location_index IF NOT EXISTS FOR (v:LandVehicle) ON (v.location)")
                .run();
    }

    public Double distance(Point p1, Point p2) {
        return calculateDistance(p1, p2);
    }

    public Double distance(LocationDTO loc1, LocationDTO loc2) {
        return calculateDistance(loc1.toPoint(), loc2.toPoint());
    }

    private Double calculateDistance(Point p1, Point p2) {
        var result = dbClientProvider.getClient()
                .query("RETURN point.distance($p1, $p2)")
                .bind(p1).to("p1")
                .bind(p2).to("p2")
                .fetch().one();
        return (Double) result.get().values().toArray()[0];
    }
}
