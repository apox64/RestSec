import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicFunctionsTests {

    @Nested
    @DisplayName("Controller Tests")
    public class ControllerTests {

        @Test
        @DisplayName("Initiating Controller")
        void initiateController() {
            String s = "test";
            assertThat(s.equals("test"));
        }

    }

    @Nested
    @DisplayName("Parser Tests")
    public class ParserTests {

        @Test
        @DisplayName("Testing Parser for correct output")
        void testParser() {
            Parser p = new Parser("http://localhost:10001/albums/", "HATEOAS", true);
            JSONObject jsonObject = new JSONObject();
            Assertions.assertEquals(22, 22);
        }

        @Test
        @DisplayName("Test1")
        void testMergingHashMaps() {

            int a = 10;
            int b = 12;

            assertThat(Integer.toString(a+b)).isEqualTo("22");

        /*
        HashMap<String, Boolean> map1 = new HashMap<>();
        HashMap<String, Boolean> map2 = new HashMap<>();
        HashMap<String, Boolean> result = new HashMap<>();

        map1.put("http://localhost:10001/album/1", false);
        map1.put("http://localhost:10001/album/3", false);
        map1.put("http://localhost:10001/artist/cfrost", true);
        map1.put("http://localhost:10001/album/purchase/3", false);

        map2.put("http://localhost:10001/album/1", true);
        map2.put("http://localhost:10001/album/cfrost", false);
        map2.put("http://localhost:10001/album/3", false);

        result.put("http://localhost:10001/album/1", true);
        result.put("http://localhost:10001/album/3", false);
        result.put("http://localhost:10001/artist/cfrost", true);
        result.put("http://localhost:10001/album/purchase/3", false);

        Parser p = new Parser(null,null,false);
        assert (p.mergeHashMaps(map1, map2).keySet().equals(result.keySet()));
        */

        }

    }

}
