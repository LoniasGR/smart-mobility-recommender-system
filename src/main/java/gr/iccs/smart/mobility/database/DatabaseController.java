package gr.iccs.smart.mobility.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/admin")
public class DatabaseController {
    @Autowired
    private DatabaseService databaseService;

    @PostMapping("delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> clearDatabase() {
        var info = databaseService.clearDatabase();
        return new ResponseEntity<String>(info, HttpStatus.OK);
    }
}