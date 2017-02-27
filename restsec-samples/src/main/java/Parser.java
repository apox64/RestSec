
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Documentations
//in the json Format, parses them and creates and attack set for the scanner.
//
// Swagger (yes)
// Spring (working on it)
// ...

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.get;

public class Parser implements Runnable {

    private String entryPointOrDocumentationFile = "";
    private boolean useAllPossibleHTTPMethodsForAttack = false;
    private String docType = "";

    public Parser(String entryPointOrDocumentationFile, String docType, boolean useAllPossibleHTTPMethodsForAttack) {
        this.entryPointOrDocumentationFile = entryPointOrDocumentationFile;
        this.useAllPossibleHTTPMethodsForAttack = useAllPossibleHTTPMethodsForAttack;
        this.docType = docType;
    }

    public void run() {
        switch (docType) {
            case "HATEOAS" :
                //run HATEOAS Parsing
                discoverLinksForHATEOAS(this.entryPointOrDocumentationFile);
                break;
            case "Swagger" :
                //run Swagger Parsing
                parseSwaggerJSON(this.entryPointOrDocumentationFile, this.useAllPossibleHTTPMethodsForAttack);
                break;
            default :
                System.err.println("Could not recognize specified documentation type.");
                System.exit(0);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void parseSwaggerJSON(String file, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(new FileReader(getClass().getClassLoader().getResource(file).getFile()));

            String version = (String) jsonObj.get("swagger");
            String host = (String) jsonObj.get("host");
            String basePath = (String) jsonObj.get("basePath");
            JSONObject pathsObj = (JSONObject) jsonObj.get("paths");

            System.out.println("Swagger Version: \t" + version);
            System.out.println("Host: \t\t\t\t" + host);
            System.out.println("Basepath: \t\t\t" + basePath);

            if (useAllHTTPMethods) {
                writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(pathsObj));
            } else {
                writeAttackSetToFile(createAttackSetJSONSwagger(pathsObj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void discoverLinksForHATEOAS(String entryResource){
        JSONObject attackSet = new JSONObject();
        //System.out.println("Parser: Starting endpoint discovery for HATEOAS on "+entryResource+" ...");

        //HashMap that gets filled for each call of getHATEOASLinks()
        //String endpoint, Boolean isVisited
        HashMap<String, Boolean> relevantURLs;

        //init
        relevantURLs = getHATEOASLinks(entryResource);
        relevantURLs.put(entryResource, true);

        //print
        Iterator it = relevantURLs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            //System.out.println("  - " + pair.getKey() + " : " + pair.getValue());
        }

        while (relevantURLs.containsValue(false)) {
            Iterator iterator = relevantURLs.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                if (!Boolean.parseBoolean(pair.getValue().toString())) {
                    HashMap<String, Boolean> temp = getHATEOASLinks(pair.getKey().toString());
                    relevantURLs = mergeHashMaps(relevantURLs, temp);
                }
            }
        }

        for (Object key : relevantURLs.keySet()) {
            attackSet.put(key, relevantURLs.get(key));
        }

            writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(attackSet));

    }

    private HashMap<String, Boolean> getHATEOASLinks(String resource) {

        Pattern patternFullURL = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\/([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
        Pattern patternHostAndPortOnly = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}(:?\\d+)*\\/");
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
        matcherHostAndPortOnly.find();
        String entryResourceDomainAndPortOnly = matcherHostAndPortOnly.group();

        Iterator it = allURLsInResponse.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry url = (Map.Entry)it.next();
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

        if (map1.size() >= map2.size()){
            bigMap = map1;
            smallMap = map2;
        } else {
            bigMap = map2;
            smallMap = map1;
        }

        HashMap<String, Boolean> resultMap = new HashMap<>();

        for (Object key : bigMap.keySet()) {
            if (smallMap.containsKey(key.toString())) {
                resultMap.put(key.toString(), smallMap.get(key) || bigMap.get(key));
            } else {
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
            System.out.println("Parser: Checking path: " + currentPath);
            JSONObject httpVerbsObject = (JSONObject) pathsObject.get(currentPath);
            JSONArray array = new JSONArray();

            Iterator<String> iter = httpVerbsObject.keySet().iterator();
            while (iter.hasNext()) {
                switch (iter.next()) {
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
        System.out.println("Parser: "+attackCounter + " possible attack points found.");
        return attackSet;
    }

    @SuppressWarnings("unchecked")
    private JSONObject createCompleteHTTPMethodAttackSetJSON(JSONObject pathsObject) {
        JSONObject attackSet = new JSONObject();
        Iterator<String> iterator = pathsObject.keySet().iterator();

        while (iterator.hasNext()) {
            String currentPath = iterator.next();
            JSONArray array = new JSONArray();
            array.add("POST");
            array.add("PATCH");
            array.add("PUT");
            array.add("DELETE");
            attackSet.put(currentPath, array);
        }

        System.out.println("Parser: attackSet created for bruteforcing (size: "+pathsObject.size() * 4+")");
        return attackSet;
    }

    private void writeAttackSetToFile(JSONObject attackSet){
        printAttackSet(attackSet);

        try {

            FileWriter file = new FileWriter("restsec-samples/src/main/resources/attackable/attackable.json");
            file.write(attackSet.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Parser: AttackSet written to File.");

    }

    private void printAttackSet(JSONObject attackSet){
        System.out.println("--- Attack Set ---");
        for (Object key : attackSet.keySet()) {
            System.out.println(key + " : " + attackSet.get(key));
        }
        System.out.println("------------------");
    }

}
