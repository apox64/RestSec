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
        LOGGER.info("Swagger File: \t\t"+swaggerFileLocation);
        parseSwaggerJSON(swaggerFileLocation, useAllHTTPMethods);
    }

    @Override
    public AttackSet crawl(String target) {
        LOGGER.info("Swagger File: \t\t"+swaggerFileLocation);
        return parseSwaggerJSON(swaggerFileLocation, useAllHTTPMethods);
    }

    private AttackSet parseSwaggerJSON(String swaggerFile, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(swaggerFile));

            String version = (String) jsonObject.get("swagger");
            LOGGER.info("Swagger Version: \t" + version);
            String host = (String) jsonObject.get("host");
            LOGGER.info("Target: \t\t\t" + host);
            String basePath = (String) jsonObject.get("basePath");
            LOGGER.info("Basepath: \t\t\t" + basePath);
            JSONObject pathsObj = (JSONObject) jsonObject.get("paths");
            LOGGER.info("Paths: \t\t\t\t" + pathsObj.size() + " paths found.");

            AttackSet attackSet = new AttackSet();

            if (useAllHTTPMethods) {
                LOGGER.info("Creating AttackSet for *all* HTTP Methods ...");
                return attackSet.createFullAttackSet(pathsObj);
            } else {
                return findAttackableEndpoints(pathsObj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AttackSet();
    }

    @SuppressWarnings("unchecked")
    private AttackSet findAttackableEndpoints(JSONObject pathsObject) {
        Iterator<String> it = pathsObject.keySet().iterator();
//        JSONObject jsonObject = new JSONObject();
        AttackSet attackSet = new AttackSet();
        int attackCounter = 0;

        while (it.hasNext()) {
            String currentPath = it.next();
            LOGGER.info("Checking path: \t\t" + currentPath);
            JSONObject httpVerbsObject = (JSONObject) pathsObject.get(currentPath);
            JSONArray jsonArray = new JSONArray();

            for (String s : (Iterable<String>) httpVerbsObject.keySet()) {
                switch (s) {
                    case "get":
                        break;
                    case "post":
                        jsonArray.add("POST");
                        attackCounter++;
                        break;
                    case "patch":
                        jsonArray.add("PATCH");
                        attackCounter++;
                        break;
                    case "put":
                        jsonArray.add("PUT");
                        attackCounter++;
                        break;
                    case "delete":
                        jsonArray.add("DELETE");
                        attackCounter++;
                        break;
                }

            }

            if (jsonArray.size() != 0) {
                attackSet.put(currentPath, jsonArray);
            }

        }
        LOGGER.info("" + attackCounter + " attackable endpoints found.");
        return attackSet;
    }

}
