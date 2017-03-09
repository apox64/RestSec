package restsec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.get;

class Parser implements Runnable {

    private String entryPointOrDocumentationFile = "";
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private boolean useAllPossibleHTTPMethodsForAttack = false;
    private String whichParser = "";
    private static final Logger logger = Logger.getLogger(Parser.class);

    Parser(String documentationFileSwagger, boolean useAllPossibleHTTPMethodsForAttack) {
        this.entryPointOrDocumentationFile = documentationFileSwagger;
        this.useAllPossibleHTTPMethodsForAttack = useAllPossibleHTTPMethodsForAttack;
        whichParser = "Swagger";
    }

    Parser(String entryPointHATEOAS) {
        this.entryPointOrDocumentationFile = entryPointHATEOAS;
        whichParser = "HATEOAS";
    }

    public void run() {
        switch (whichParser) {
            case "Swagger":
                parseSwaggerJSON(this.entryPointOrDocumentationFile, useAllPossibleHTTPMethodsForAttack);
                break;
            case "HATEOAS":
                discoverLinksForHATEOAS(this.entryPointOrDocumentationFile);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void parseSwaggerJSON(String swaggerFile, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(swaggerFile));

            String version = (String) jsonObject.get("swagger");
            String host = (String) jsonObject.get("host");
            String basePath = (String) jsonObject.get("basePath");
            JSONObject pathsObj = (JSONObject) jsonObject.get("paths");

            logger.info("Swagger Version: \t" + version);
            logger.info("Host: \t\t\t\t" + host);
            logger.info("Basepath: \t\t\t" + basePath);

            if (useAllHTTPMethods) {
                writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(pathsObj));
            } else {
                writeAttackSetToFile(createAttackSetJSONSwagger(pathsObj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void discoverLinksForHATEOAS(String entryResource) {
        JSONObject attackSet = new JSONObject();
        //System.out.println("restsec.Parser: Starting endpoint discovery for HATEOAS on "+entryResource+" ...");

        //HashMap that gets filled for each call of getHATEOASLinks()
        //String endpoint, Boolean isVisited
        HashMap<String, Boolean> relevantURLs;

        //init
        relevantURLs = getHATEOASLinks(entryResource);
        relevantURLs.put(entryResource, true);

        //print
        for (Object o1 : relevantURLs.entrySet()) {
            Map.Entry pair = (Map.Entry) o1;
            //System.out.println("  - " + pair.getKey() + " : " + pair.getValue());
        }

        while (relevantURLs.containsValue(false)) {
            for (Object o : relevantURLs.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (!Boolean.parseBoolean(pair.getValue().toString())) {
                    HashMap<String, Boolean> temp = getHATEOASLinks(pair.getKey().toString());
                    relevantURLs = mergeHashMaps(relevantURLs, temp);
                }
            }
        }

        for (Object key : relevantURLs.keySet()) {
            //noinspection unchecked,SuspiciousMethodCalls
            attackSet.put(key, relevantURLs.get(key));
        }

        writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(attackSet));

    }

    // Supporting Methods

    private HashMap<String, Boolean> getHATEOASLinks(String resource) {

        Pattern patternFullURL = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}/([-a-zA-Z0-9@:%_+.~#?&/=]*)");
        Pattern patternHostAndPortOnly = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}(:?\\d+)*/");
        String responseBody = get(resource).asString();
        Matcher matcherFullURL = patternFullURL.matcher(responseBody);
        Matcher matcherHostAndPortOnly = patternHostAndPortOnly.matcher(resource);

        HashMap<String, Boolean> relevantURLsOnly = new HashMap<>();

        //TODO: Printing here
        //System.out.print("Looking for all HATEOAS Links on " + resource + " ... ");

        //Matching all URLs
        HashMap<String, Boolean> allURLsInResponse = new HashMap<>();
        while (matcherFullURL.find()) {
            allURLsInResponse.put(matcherFullURL.group(), false);
        }

        //Return only the URLs that are on the same host as the given resource
        //noinspection ResultOfMethodCallIgnored
        matcherHostAndPortOnly.find();
        String entryResourceDomainAndPortOnly = matcherHostAndPortOnly.group();

        for (Object o : allURLsInResponse.entrySet()) {
            Map.Entry url = (Map.Entry) o;
            Matcher m = patternHostAndPortOnly.matcher(url.getKey().toString());
            while (m.find()) {
                if (m.group().equals(entryResourceDomainAndPortOnly)) {
                    relevantURLsOnly.put(url.getKey().toString(), false);
                }
            }
        }

        //TODO: Printing here
        //System.out.println(relevantURLsOnly.size() + " found.");

        relevantURLsOnly.put(resource, true);
        //System.out.println(resource + " changed to isVisited : " + relevantURLsOnly.get(resource));

        //Return the result
        return relevantURLsOnly;
    }

    //Supporting Methods:

    private HashMap<String, Boolean> mergeHashMaps(HashMap<String, Boolean> map1, HashMap<String, Boolean> map2) {

        //find bigger one
        HashMap<String, Boolean> smallMap, bigMap;

        if (map1.size() >= map2.size()) {
            bigMap = map1;
            smallMap = map2;
        } else {
            bigMap = map2;
            smallMap = map1;
        }

        HashMap<String, Boolean> resultMap = new HashMap<>();

        for (Object key : bigMap.keySet()) {
            if (smallMap.containsKey(key.toString())) {
                //noinspection SuspiciousMethodCalls,SuspiciousMethodCalls
                resultMap.put(key.toString(), smallMap.get(key) || bigMap.get(key));
            } else {
                //noinspection SuspiciousMethodCalls
                resultMap.put(key.toString(), bigMap.get(key));
            }
        }

        return resultMap;
    }

    @SuppressWarnings("unchecked")
    private JSONObject createAttackSetJSONSwagger(JSONObject pathsObject) {
        Iterator<String> it = pathsObject.keySet().iterator();
        JSONObject attackSet = new JSONObject();
        int attackCounter = 0;

        while (it.hasNext()) {
            String currentPath = it.next();
            logger.info("Checking path: " + currentPath);
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
        logger.info("" + attackCounter + " possible attack points found.");
        return attackSet;
    }

    @SuppressWarnings("unchecked")
    private JSONObject createCompleteHTTPMethodAttackSetJSON(JSONObject pathsObject) {
        JSONObject attackSet = new JSONObject();

        for (String currentPath : (Iterable<String>) pathsObject.keySet()) {
            JSONArray array = new JSONArray();
            array.add("POST");
            array.add("PATCH");
            array.add("PUT");
            array.add("DELETE");
            attackSet.put(currentPath, array);
        }

        logger.info("attackSet created for bruteforcing (size: " + pathsObject.size() * 4 + ")");
        return attackSet;
    }

    private void writeAttackSetToFile(JSONObject attackSet) {
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

        logger.info("AttackSet written to File.");

    }

    private void printAttackSet(JSONObject attackSet) {
        System.out.println("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            System.out.println(key + " : " + attackSet.get(key));
        }
        System.out.println("------------------");
    }

}
