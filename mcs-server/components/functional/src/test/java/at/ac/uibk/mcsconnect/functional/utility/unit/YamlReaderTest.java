package at.ac.uibk.mcsconnect.functional.utility.unit;

import at.ac.uibk.mcsconnect.functional.common.Result;
import at.ac.uibk.mcsconnect.functional.utility.YamlData;
import at.ac.uibk.mcsconnect.functional.utility.YamlReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.*;

public class YamlReaderTest {

    private static Logger LOGGER = LoggerFactory.getLogger(YamlReaderTest.class);

    @Test
    void canParseListOfElementsOfMultipleTypesButNotToken() {
        String testData = "---\n- dummies:\n- dummy1\n- dummy2";

        YamlReader reader = YamlReader.stringYamlReader(testData);

        Result<List<String>> sut = reader.getAsList();
        if (sut.isFailure()) LOGGER.error(() -> sut.failureValue().getMessage());
        assertThat(sut.successValue().toString()).isEqualTo("[{dummies=null}, dummy1, dummy2]");
    }

    @Test
    void canParseTokens() {
        String testData = "---\nelem1\nelem2\nelem3";
        YamlReader reader = YamlReader.stringYamlReader(testData);
        Result<String> sut = reader.getAsString();
        sut.forEachOrFail(s -> assertThat(s).isEqualTo("elem1 elem2 elem3")).forEach(YamlReaderTest::logError);
    }

    @Test
    void canParseMapRecursively() {
        String testData = "---\nroot:\n  sub1:\n    subsub1:\n  sub2:";
        YamlReader reader = YamlReader.stringYamlReader(testData);
        //Result<Map<String, Object>> sut = reader.getAsMap();
        //sut.forEachOrFail(s -> assertThat(s.toString()).isEqualTo("{root={sub1={subsub1=null}, sub2=null}}")).forEach(YamlReaderTest::logError);
    }




//    class Dummy {
//
//    }
    //Function<String, Result<Dummy>> stringToDummy = s -> Result.of(new Dummy());
    //static Result<List<Dummy>> getAsDummyList(String key, YamlReader yamlReader) {
    //    Result<List<String>> rList = yamlReader.getAsStringList(key);
    //}

//    @Test
//    @DisplayName("Fail to parse LF-separated tokens and cast to list")
//    public void cannotParseLFSeparatedTokensAsList() {
//        String testData = "---\nobj1\nobj2";
//        YamlReader reader = YamlReader.stringYamlReader(testData);
//        Result<List<String>> sut = reader.getAsList();
//
//        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
//            sut.forEachOrThrow(c -> System.out.println(c));
//        });
//    }


//    @Test
//    @DisplayName("Read LF-separated list tokens and cast to List<String>")
//    public void canParseLFSeparatedListTokensAsList() {
//        String testData = "---\n- obj1\n- obj2";
//        YamlReader reader = YamlReader.stringYamlReader(testData);
//        Result<List<Object>> sut = reader.getAsList();
//        assertThat(sut.successValue().toString()).isEqualTo("[obj1, obj2]");
//    }
//
//    @Test
//    @DisplayName("Read LF-separated map tokens and cast to Map<String, String>")
//    public void canParseLFSeparatedMapTokensAsMap() {
//        String testData = "---\nobj1:\nobj2:";
//        YamlReader reader = YamlReader.stringYamlReader(testData);
//        Result<Map<String, Object>> sut = reader.getAsMap();
//        assertThat(sut.successValue().toString()).isEqualTo("{obj1=null, obj2=null}");
//    }
//
//    @Test
//    @DisplayName("Read LF-separated list of map tokens and cast to List<Map<String, String>>")
//    public void canParseLFSeparatedListOfMapTokensAsListOfMap() {
//        String testData = "---\n- obj1:\n- obj2:";
//        YamlReader reader = YamlReader.stringYamlReader(testData);
//        Result<List<Object>> sut = reader.getAsList();
//        for (Object s : sut.successValue()) {
//            Result<Map<String, Object>> inner = YamlReader.objectYamlReader(s).getAsMap();
//            System.out.println(inner.successValue());
//        }
//        //sut.map(s -> YamlReader.stringYamlReader(s).getAsMap());
//        assertThat(sut.successValue().toString()).isEqualTo("[{obj1=null}, {obj2=null}]");
//    }
//
//    @Test
//    @DisplayName("Read LF-separated list tokens into List<String>")
//    public void canParseLFSeparatedListTokensIntoListOfString() {
//        String testData = "---\n- obj1\n- obj2";
//        YamlReader reader = YamlReader.stringYamlReader(testData);
//        Result<List<String>> sut = reader.getAsListOf(String.class);
//        assertThat(sut.successValue().toString()).isEqualTo("[obj1, obj2]");
//    }

    //@Test
    //@DisplayName("Read LF-separated list of map tokens into List<Map<String, Object>>")
    //public void testsOmething() {
    //    String testData = "---\n- obj1:\n- obj2:";
    //    YamlReader reader = YamlReader.stringYamlReader(testData);
    //    TypeReference<List<String>> x = new TypeReference<List<String>>() {};
    //    Result<List<Map<String, Object>>> sut = reader.getAsListOf(Map.class);
    //    assertThat(sut.successValue().toString()).isEqualTo("[obj1, obj2]");
    //}

    private static void logError(String msg) {
        fail(msg);
        LOGGER.error(() -> msg);
    }
}
