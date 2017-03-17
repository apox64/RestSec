package restsec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import restsec.crawler.HATEOASCrawler;
import restsec.crawler.SwaggerFileCrawler;

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
    @DisplayName("HATEOAS Crawler for spring-hateoas-demo")
    void parserHATEOASSpringDemo() {
        HATEOASCrawler hateoasCrawler = new HATEOASCrawler("http://localhost:10001/albums/");
        hateoasCrawler.crawl();

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
    @DisplayName("Swagger Crawler for swagger-juiceshop-short.json, not all HTTP methods")
    void parserSwaggerJuiceShopShortNotAllHTTPMethods() throws URISyntaxException {
        String file = "src/main/resources/docs_swagger/swagger-juiceshop-short.json";
        SwaggerFileCrawler swaggerFileCrawler = new SwaggerFileCrawler(file, false);
        swaggerFileCrawler.crawl();

        JsonObject attackSetFromFile = new JsonObject();

        try {
            attackSetFromFile = (JsonObject) new JsonParser().parse(new FileReader("src/main/resources/attackable/attackset.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(attackSetFromFile.entrySet().size() == 1);
        Assertions.assertTrue(attackSetFromFile.get("/api/Products/1").toString().equals("[\"PUT\"]"));
    }

    @Test
    @DisplayName("Swagger Crawler for swagger-juiceshop-short.json, with all HTTP methods")
    void parserSwaggerJuiceShopShortWithAllHTTPMethods() {
        SwaggerFileCrawler swaggerFileCrawler = new SwaggerFileCrawler("src/main/resources/docs_swagger/swagger-juiceshop-short.json", true);
        swaggerFileCrawler.crawl();

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
    @DisplayName("Swagger Crawler for swagger-juiceshop.json, not all HTTP methods")
    void parserSwaggerJuiceShopFullNotAllHTTPMethods() {
        SwaggerFileCrawler swaggerFileCrawler = new SwaggerFileCrawler("src/main/resources/docs_swagger/swagger-juiceshop.json", false);
        swaggerFileCrawler.crawl();

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
    @DisplayName("Swagger Crawler for swagger-juiceshop.json, with all HTTP methods")
    void parserSwaggerJuiceShopFullWithAllHTTPMethods() {
        SwaggerFileCrawler swaggerFileCrawler = new SwaggerFileCrawler("src/main/resources/docs_swagger/swagger-juiceshop.json", true);
        swaggerFileCrawler.crawl();

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
    @DisplayName("Swagger Crawler for instagram-api-test.json, not all HTTP methods")
    void parserSwaggerInstagramAPINotAllHTTPMethods() {
        SwaggerFileCrawler swaggerFileCrawler = new SwaggerFileCrawler("src/main/resources/docs_swagger/swagger-instagram-api-test.json", false);
        swaggerFileCrawler.crawl();

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