package restsec.crawler;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.Iterator;

public class SwaggerParser extends Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerParser.class);

    private String swaggerFileLocation;
    private Boolean useAllHTTPMethods;

    public SwaggerParser(String swaggerFileLocation, Boolean useAllHTTPMethods) {
        this.swaggerFileLocation = swaggerFileLocation;
        this.useAllHTTPMethods = useAllHTTPMethods;
    }

    public void crawl(){
        LOGGER.info("Parsing Swagger File from : "+swaggerFileLocation);
        parseSwaggerJSON(swaggerFileLocation, useAllHTTPMethods);
    }

    private void parseSwaggerJSON(String swaggerFile, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(swaggerFile));

            String version = (String) jsonObject.get("swagger");
            String host = (String) jsonObject.get("host");
            String basePath = (String) jsonObject.get("basePath");
            JSONObject pathsObj = (JSONObject) jsonObject.get("paths");

            LOGGER.info("Swagger Version: \t" + version);
            LOGGER.info("Host: \t\t\t\t" + host);
            LOGGER.info("Basepath: \t\t\t" + basePath);

            if (useAllHTTPMethods) {
                super.writeAttackSetToFile(super.createCompleteHTTPMethodAttackSetJSON(pathsObj));
            } else {
                super.writeAttackSetToFile(createAttackSetJSONSwagger(pathsObj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject createAttackSetJSONSwagger(JSONObject pathsObject) {
        Iterator<String> it = pathsObject.keySet().iterator();
        JSONObject attackSet = new JSONObject();
        int attackCounter = 0;

        while (it.hasNext()) {
            String currentPath = it.next();
            LOGGER.info("Checking path: " + currentPath);
            JSONObject httpVerbsObject = (JSONObject) pathsObject.get(currentPath);
            JSONArray array = new JSONArray();

            for (String s : (Iterable<String>) httpVerbsObject.keySet()) {
                switch (s) {
                    case "get":
                        break;
                    case "post":
                        array.add("POST");
                        attackCounter++;
                        break;
                    case "patch":
                        array.add("PATCH");
                        attackCounter++;
                        break;
                    case "put":
                        array.add("PUT");
                        attackCounter++;
                        break;
                    case "delete":
                        array.add("DELETE");
                        attackCounter++;
                        break;
                }

            }

            if (array.size() != 0) {
                attackSet.put(currentPath, array);
            }

        }
        LOGGER.info("" + attackCounter + " possible attack points found.");
        return attackSet;
    }

}
