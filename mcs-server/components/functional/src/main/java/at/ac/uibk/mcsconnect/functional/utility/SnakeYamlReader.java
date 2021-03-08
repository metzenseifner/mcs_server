package at.ac.uibk.mcsconnect.functional.utility;

import at.ac.uibk.mcsconnect.functional.common.Result;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.Properties;

/** Second attempt at functional yaml reader */
public class SnakeYamlReader<T> {

    // new Yaml(new Constructor(RoomsDTO.class));
    private final Result<Yaml> yamlParser;

    private SnakeYamlReader(String filePath, Class<?> clz) {
        this.yamlParser = Result.of(new Yaml(new Constructor(clz)));
    }

}
