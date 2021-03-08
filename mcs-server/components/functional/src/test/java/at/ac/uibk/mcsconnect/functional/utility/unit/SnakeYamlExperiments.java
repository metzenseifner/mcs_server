package at.ac.uibk.mcsconnect.functional.utility.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Note on nomenclature:
 *
 * token: "token"
 * map token: "token:"
 * list token: "- token"
 * list of map tokens: "- token:"
 */
public class SnakeYamlExperiments {

    private Yaml yamlParser;

    @BeforeEach
    public void setupYamlParser() {
        yamlParser = new Yaml(new SafeConstructor());
    }

    @Test
    @DisplayName("Read single token into string")
    public void canParseSingleTokenAsString() {
        String testData = "---\nobj1";
        Yaml yaml = new Yaml(new SafeConstructor());

        String sut = yaml.load(testData);

        assertThat(sut).isEqualTo("obj1");
    }

    @Test
    @DisplayName("Fail on read single token into map")
    public void cannotParseSingleTokenAsMap() {
        String testData = "---\nobj1";
        Yaml yaml = new Yaml(new SafeConstructor());

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            Map<String, String> sut1 = yaml.load(testData);
        });
        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            Map<String, Object> sut2 = yaml.load(testData);
        });
    }

    @Test
    @DisplayName("Read multiple LF-separated tokens into string")
    public void canParseMultipleLFSeparatedTokensIntoString() {
        String testData = "---\nobj1\nobj2";
        Yaml yaml = new Yaml(new SafeConstructor());

        String sut = yaml.load(testData);

        assertThat(sut).isEqualTo("obj1 obj2");
    }

    @Test
    @DisplayName("Fail on read LF-separated map token followed by non-map token into string")
    public void cannotParseMultipleLFSeparatedMixedTypeTokensIntoString() {
        String testData = "---\nobj1:\nobj2";
        Yaml yaml = new Yaml(new SafeConstructor());

        assertThatExceptionOfType(Exception.class).isThrownBy( () -> {
            // some syntax exception
            String sut = yaml.load(testData);
        });
    }

    @Test
    @DisplayName("Fail to read LF-separated map tokens into string because a LinkedHashMap cannot be cast into a String")
    public void cannotParseMultipleLFSeparatedMapTokensIntoString() {
        String testData = "---\nobj1:\nobj2:";
        Yaml yaml = new Yaml(new SafeConstructor());

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            String sut = yaml.load(testData);
        });
    }

    @Test
    @DisplayName("Read LF-separated map tokens into Map<String, String>")
    public void canParseMultipleLFSeparatedMapTokensIntoMapOfStringToString() {
        String testData = "---\nobj1:\nobj2:";
        Yaml yaml = new Yaml(new SafeConstructor());

        Map<String, String> sut = yaml.load(testData);

        assertThat(sut.toString()).isEqualTo("{obj1=null, obj2=null}");
    }

    @Test
    @DisplayName("Fails to read LF-separated tokens into Map<String, String>. " +
            "Helps confirm that output is either String or Map<String, String>, but cannot be both.")
    public void cannotParseMultipleLFSeparatedTokensIntoMapOfStringToString() {
        String testData = "---\nobj1\nobj2";
        Yaml yaml = new Yaml(new SafeConstructor());

        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    Map<String, String> sut = yaml.load(testData);
                });
    }

    @Test
    @DisplayName("Fail to parse multiple LF-separated list tokens into string")
    public void cannotParseMultpleLFSeparatedListTokensIntoString() {
        String testData = "---\n- obj1\n- obj2";
        assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
            String sut = yamlParser.load(testData);
        });
    }

    @Test
    @DisplayName("Can parse multiple LF-separated list tokens into List<String>")
    public void canParseMultpleLFSeparatedListTokensIntoList() {
        String testData = "---\n- obj1\n- obj2";
        List<String> sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("[obj1, obj2]");
    }

    @Test
    @DisplayName("Can parse multiple LF-separated list tokens into Object")
    public void canParseMultpleLFSeparatedListTokensIntoObject() {
        String testData = "---\n- obj1\n- obj2";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("[obj1, obj2]");
    }

    @Test
    @DisplayName("Can parse multiple LF-dash-separated map tokens into Object")
    public void canParseMultipleLFSeparatedMapTokensIntoObject() {
        String testData = "---\n- obj1:\n- obj2:";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("[{obj1=null}, {obj2=null}]");
    }


    @Test
    @DisplayName("Can parse multiple LF-separated tokens into Object")
    public void canParseMultipleLFSeparatedTokensIntoObject() {
        String testData = "---\n- obj1\n- obj2";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("[obj1, obj2]");
    }

    @Test
    @DisplayName("Can parse multiple LF-separated tokens into Object then into List")
    public void canParseMultipleLFSeparatedTokensIntoObjectIntoList() {
        String testData = "---\n- obj1\n- obj2";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("[obj1, obj2]");
        assertThat(((List<String>) sut).toString()).isEqualTo("[obj1, obj2]");
    }

    @Test
    @DisplayName("Can parse multiple LF-separated map tokens into Object")
    public void canParseMultipleLFSeparatedMapTokensIntoList() {
        String testData = "---\nobj1:\nobj2:";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("{obj1=null, obj2=null}");
    }

    @Test
    @DisplayName("Can parse multiple LF-separated map tokens into Object into Map")
    public void canParseMultipleLFSeparatedMapTokensIntoObjectIntoMap() {
        String testData = "---\nobj1:\nobj2:";
        Object sut = yamlParser.load(testData);
        assertThat(sut.toString()).isEqualTo("{obj1=null, obj2=null}");
        assertThat(((Map<String, String>) sut).toString()).isEqualTo("{obj1=null, obj2=null}");
    }

}
