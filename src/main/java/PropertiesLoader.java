import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesLoader {
    public static Properties load(String propFileName) throws IOException {
        final Properties properties = new Properties();
        try (final InputStream inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(propFileName)) {
            properties.load(inputStream);
        } catch (Exception e) {
        System.out.println("Exception: " + e);
    }
        return properties;
    }
}
