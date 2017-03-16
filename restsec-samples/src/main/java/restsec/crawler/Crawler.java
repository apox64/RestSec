package restsec.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import restsec.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    Configuration configuration;

    public Crawler() {
        configuration = new Configuration();
    }

    public void crawl() {

        switch (configuration.getDocumentationType()) {
            case "Swagger":
                SwaggerParser swaggerParser = new SwaggerParser(configuration.getSwaggerFileLocation(), configuration.getBoolUseAllHTTPMethods());
                swaggerParser.crawl();
                break;
            case "HATEOAS":
                HATEOASCrawler hateoasCrawler = new HATEOASCrawler(configuration.getHATEOASEntryPoint());
                hateoasCrawler.crawl();
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    JSONObject createCompleteHTTPMethodAttackSetJSON(JSONObject pathsObject) {
        JSONObject attackSet = new JSONObject();

        for (String currentPath : (Set<String>) pathsObject.keySet()) {
            JSONArray array = new JSONArray();
            array.add("POST");
            array.add("PATCH");
            array.add("PUT");
            array.add("DELETE");
            attackSet.put(currentPath, array);
        }

        LOGGER.info("attackSet created for bruteforcing (size: " + pathsObject.size() * 4 + ")");
        return attackSet;
    }

    void writeAttackSetToFile(JSONObject attackSet) {
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        printAttackSet(attackSet);

        try {

            FileWriter file = new FileWriter("src/main/resources/attackable/attackset.json");

            String output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attackSet);

            file.write(output);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("AttackSet written to File.");

    }

    private void printAttackSet(JSONObject attackSet) {
        System.out.println("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            System.out.println(key + " : " + attackSet.get(key));
        }
        System.out.println("------------------");
    }

}
