package gr.iccs.smart.mobility.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

@Component
public class ResourceReader {

    @Autowired
    private ResourceLoader resourceLoader;

    public File readResource(String resourcePath) throws FileNotFoundException {
        Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
        try {
            File file = resource.getFile();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
