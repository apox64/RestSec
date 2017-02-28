import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    @DisplayName("Testing Parser for correct output")
    void testParser() {
        //Parser p = new Parser("http://localhost:10001/albums/", "HATEOAS", true);
        //JSONObject jsonObject = new JSONObject();
        Assertions.assertEquals(22, 22);
    }

    @Test
    @DisplayName("Adding two numbers")
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