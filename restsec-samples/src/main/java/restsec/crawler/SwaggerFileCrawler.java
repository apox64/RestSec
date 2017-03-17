package restsec.crawler;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import restsec.AttackSet;

import java.io.FileReader;
import java.util.Iterator;

public class SwaggerFileCrawler implements Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerFileCrawler.class);

    private String swaggerFileLocation;
    private Boolean useAllHTTPMethods;

    public SwaggerFileCrawler(String swaggerFileLocation, Boolean useAllHTTPMethods) {
        this.swaggerFileLocation = swaggerFileLocation;
        this.useAllHTTPMethods = useAllHTTPMethods;
    }

    @Override
    public void crawl(){
        LOGGER.info("Parsing Swagger File from : "+swaggerFileLocation);
        parseSwaggerJSON(swaggerFileLocation, useAllHTTPMethods);
    }

    private void parseSwaggerJSON(String swaggerFile, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(swaggerFile));

            String version = (String) jsonObject.get("swagger");
            LOGGER.info("Swagger Version: \t" + version);
            String host = (String) jsonObject.get("host");
            LOGGER.info("Host: \t\t\t\t" + host);
            String basePath = (String) jsonObject.get("basePath");
            LOGGER.info("Basepath: \t\t\t" + basePath);
            JSONObject pathsObj = (JSONObject) jsonObject.get("paths");
            LOGGER.info("Paths: \t\t\t" + pathsObj.size() + " paths found.");

            AttackSet attackSet = new AttackSet();

            if (useAllHTTPMethods) {
                attackSet.setAttackSet(new AttackSet().createFullAttackSetForPathsObject(pathsObj));
                attackSet.writeAttackSetToFile(attackSet);
            } else {
                attackSet.setAttackSet(findAttackableEndpoints(pathsObj));
                attackSet.writeAttackSetToFile(attackSet);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject findAttackableEndpoints(JSONObject pathsObject) {
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
        LOGGER.info("" + attackCounter + " attackable endpoints found.");
        return attackSet;
    }

}
