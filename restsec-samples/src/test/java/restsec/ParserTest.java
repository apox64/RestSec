package restsec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;

class ParserTest {
    @BeforeEach
    void setUp() {
        //empty
    }

    @AfterEach
    void tearDown() {
        //empty
    }

    @Test
    @DisplayName("HATEOAS Parser for spring-hateoas-demo")
    void parserHATEOASSpringDemo() {
        Thread parserThread = new Thread(new restsec.Parser("http://localhost:10001/albums/"));
        parserThread.start();

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Assertions.assertEquals(9, attackSetFromFile.entrySet().size(), "Are all HATEOAS Links even offered? (Some might somtimes show up and sometimes not.)");
        Assertions.assertTrue(attackSetFromFile.entrySet().size() >= 6);
        Assertions.assertTrue(attackSetFromFile.entrySet().size() <= 9);

    }

    @Test
    @DisplayName("Swagger Parser for swagger-juiceshop-short.json, not all HTTP methods")
    void parserSwaggerJuiceShopShortNotAllHTTPMethods() throws URISyntaxException {

        String file = "docs_swagger/swagger-juiceshop-short.json";
        Parser parser = createParserForFile(file, false);
        Thread parserThread = createAndStartThread(parser);

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 1);
        Assertions.assertTrue(attackSetFromFile.get("/api/Products/1").toString().equals("[\"PUT\"]"));
    }

    private Thread createAndStartThread(Parser parser) {
        Thread parserThread = new Thread(parser);
        parserThread.start();
        return parserThread;
    }

    private Parser createParserForFile(String file, boolean useAllPossibleHTTPMethodsForAttack) throws URISyntaxException {
        return new Parser(getPath(file), useAllPossibleHTTPMethodsForAttack);
    }

    private String getPath(String file) throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(file).toURI()).getAbsolutePath();
    }

    @Test
    @DisplayName("Swagger Parser for swagger-juiceshop-short.json, with all HTTP methods")
    void parserSwaggerJuiceShopShortWithAllHTTPMethods() {
        Thread parserThread = new Thread(new restsec.Parser("src/main/resources/docs_swagger/swagger-juiceshop-short.json", true));
        parserThread.start();

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 2);
        Assertions.assertTrue(attackSetFromFile.get("/api/Products/1").toString().equals("[\"POST\",\"PATCH\",\"PUT\",\"DELETE\"]"));
    }

    @Test
    @DisplayName("Swagger Parser for swagger-juiceshop.json, not all HTTP methods")
    void parserSwaggerJuiceShopFullNotAllHTTPMethods() {

        Thread parserThread = new Thread(new restsec.Parser("src/main/resources/docs_swagger/swagger-juiceshop.json", false));
        parserThread.start();

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 6);
        Assertions.assertTrue(attackSetFromFile.get("/stores/order").toString().equals("[\"PUT\"]"));
        Assertions.assertTrue(attackSetFromFile.get("/rest/user/login").toString().equals("[\"POST\"]"));
        Assertions.assertTrue(attackSetFromFile.get("/api/Products/1").toString().equals("[\"PUT\"]"));
    }

    @Test
    @DisplayName("Swagger Parser for swagger-juiceshop.json, with all HTTP methods")
    void parserSwaggerJuiceShopFullWithAllHTTPMethods() {
        Thread parserThread = new Thread(new restsec.Parser("src/main/resources/docs_swagger/swagger-juiceshop.json", true));
        parserThread.start();

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 9);
        Assertions.assertTrue(attackSetFromFile.get("/stores/order").toString().equals("[\"POST\",\"PATCH\",\"PUT\",\"DELETE\"]"));
        Assertions.assertTrue(attackSetFromFile.get("/rest/user/login").toString().equals("[\"POST\",\"PATCH\",\"PUT\",\"DELETE\"]"));
        Assertions.assertTrue(attackSetFromFile.get("/api/Products/1").toString().equals("[\"POST\",\"PATCH\",\"PUT\",\"DELETE\"]"));
    }

    @Test
    @DisplayName("Swagger Parser for instagram-api-test.json, not all HTTP methods")
    void parserSwaggerInstagramAPINotAllHTTPMethods() {
        Thread parserThread = new Thread(new restsec.Parser("src/main/resources/docs_swagger/swagger-instagram-api-test.json", false));
        parserThread.start();

        try {
            parserThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 3);
        Assertions.assertTrue(attackSetFromFile.get("/media/{media-id}/comments").toString().equals("[\"POST\",\"DELETE\"]"));
    }

}