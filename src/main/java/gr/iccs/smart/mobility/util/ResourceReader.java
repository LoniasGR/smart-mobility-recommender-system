package gr.iccs.smart.mobility.util;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceReader {
    private static final Logger log = LoggerFactory.getLogger(ResourceReader.class);

    public InputStream readResource(String resourcePath) throws FileNotFoundException {

        try {
            log.debug("Reading resource: " + resourcePath);
            return getClass().getResourceAsStream(resourcePath);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                throw e;
            } else {
                throw new RuntimeException(e);
            }
        }

    }

}
