package gr.iccs.smart.mobility.location;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class    LocationController {

    @PostMapping
    public IstanbulLocations.IstanbulLocationDescription whereIs(@RequestBody LocationDTO location) {
        return LocationDTO.istanbulLocation(location);
    }
}