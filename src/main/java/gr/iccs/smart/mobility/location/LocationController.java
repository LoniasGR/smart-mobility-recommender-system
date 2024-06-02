package gr.iccs.smart.mobility.location;

import gr.iccs.smart.mobility.geojson.FeatureCollection;
import gr.iccs.smart.mobility.geojson.GeoJSONUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
public class    LocationController {

    @PostMapping
    public IstanbulLocations.IstanbulLocationDescription whereIs(@RequestBody LocationDTO location) {
        return LocationDTO.istanbulLocation(location);
    }

    @GetMapping("/european-polygon")
    public FeatureCollection getIstanbulEuropeanPolugon() {
        FeatureCollection geoJSON = new FeatureCollection();
        for (var p : IstanbulLocations.europeanSidePolygon) {
            var f = GeoJSONUtils.createPointFeature(p.toPoint());
            geoJSON.getFeatures().add(f);
        }

        return geoJSON;
    }
}