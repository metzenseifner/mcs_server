package at.ac.uibk.mcsconnect.functional.utility;

import at.ac.uibk.mcsconnect.functional.common.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private final Result<Properties> properties;

    public PropertyReader(String configFileName) {
        this.properties = readProperties(configFileName);
    }

    /**
     *
     * Note this does not solve errors with properties themselves.
     * To solve that, just use mapFailure(print some useful error)
     * @param configFileName
     * @return
     */
    private Result<Properties> readProperties(String configFileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return Result.of(properties);
        } catch (NullPointerException n) { // because getResourceAsStream is terribly written
            return Result.failure(String.format("File %s not found in classpath", configFileName));
        } catch (IOException i) {
            return Result.failure(String.format("IO Exception reading classpath resource %s", configFileName));
        } catch (Exception e) {
            return Result.failure(String.format("Exception reading classpath resource %s", configFileName));
        }
    }

    public Result<String> getAsString(String name) {
        return properties.map(props -> props.getProperty(name));
    }


}
