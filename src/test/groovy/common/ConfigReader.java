package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    public static final String TEST_CONFIG ="src/test/resources/test-config.properties";
    public static String getProperty(String propFile, String key) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propFile)) {
            properties.load(fis);
            return properties.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden der Konfigurationsdatei", e);
        }
    }
}