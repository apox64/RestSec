
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Documentations
//in the json Format, parses them and creates and attack set for the scanner.
//
// Swagger (yes)
// Spring (working on it)
// ...

import org.apache.xerces.impl.xpath.regex.Match;
import org.apache.xpath.operations.Bool;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;

public class Parser implements Runnable {

    private String baseURL = "http://127.0.0.1:80";

    private String version;
    private String host;
    private String basePath;

    private JSONObject pathsObj;

    private String pathToSwaggerJSONFile = "";
    private boolean useAllPossibleHTTPMethodsForAttack = false;
    private String entryResourceHATEOAS = "";

    public Parser(String pathToSwaggerJSONFile, boolean useAllPossibleHTTPMethodsForAttack) {
        this.pathToSwaggerJSONFile = pathToSwaggerJSONFile;
        this.useAllPossibleHTTPMethodsForAttack = useAllPossibleHTTPMethodsForAttack;
    }

    public Parser(String entryResourceHATEOAS) throws IOException {
        this.entryResourceHATEOAS = entryResourceHATEOAS;
        //loadProperties();
    }

    public void run() {
        if (pathToSwaggerJSONFile.isEmpty()) {
            //run HATEOAS Parsing
            discoverLinksForHATEOAS(this.entryResourceHATEOAS);
        } else {
            //run Swagger Parsing
            parseSwaggerJSON(this.pathToSwaggerJSONFile, this.useAllPossibleHTTPMethodsForAttack);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseSwaggerJSON(String file, boolean useAllHTTPMethods) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(new FileReader(getClass().getClassLoader().getResource(file).getFile()));

            this.version = (String) jsonObj.get("swagger");
            this.host = (String) jsonObj.get("host");
            this.basePath = (String) jsonObj.get("basePath");
            this.pathsObj = (JSONObject) jsonObj.get("paths");

            System.out.println("Swagger Version: \t" + version);
            System.out.println("Host: \t\t\t\t" + host);
            System.out.println("Basepath: \t\t\t" + basePath);

            if (useAllHTTPMethods) {
                writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(pathsObj));
            } else {
                writeAttackSetToFile(createAttackSetJSON(pathsObj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void discoverLinksForHATEOAS(String entryResource){
        JSONObject pathsObj = new JSONObject();
        System.out.println("Parser: Starting endpoint discovery for HATEOAS on "+entryResource+" ...");

        //HashMap that gets filled for each call of getHATEOASLinks()
        //String endpoint, Boolean isVisited
        HashMap<String, Boolean> relevantURLs;

        //init
        relevantURLs = getHATEOASLinks(entryResource);
        System.out.println("Parser: Adding entryResource ...");
        relevantURLs.put(entryResource, true);
        //System.out.println("Parser: HATEOAS Links found for entryPoint: " + relevantURLs.size());

        //print
        Iterator it = relevantURLs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("  - " + pair.getKey() + " : " + pair.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }

        //TODO: CONTINUE HERE
        /*
        while contains value == false
            get one key that contains value == false
            put that into getHATEOASLinks()
            merge result and relevantURLs
            repeat until all values are true
         */

        while (relevantURLs.containsValue(false)) {
            Iterator iterator = relevantURLs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)iterator.next();
                //draft: relevantURLs.putAll(getHATEOASLinks(pair.getKey()), );
            }

        }


        System.out.println("Parser: Creating AttackSet for : " + relevantURLs.size() + " relevant URLs.");
        // TODO: CURRENTLY ENDING PROGRAM HERE (TESTING)
        System.exit(0);

        /*

        //Adding the entryResource itself
        allRelevantURLs.add(entryResource);
        numberOfRelevantURLs++;

        if (numberOfRelevantURLs == 1) {
            System.err.println("Parser: No HATEOAS Link found for the entry URL you entered. Are you sure the given entry point follows HATEOAS?");
            System.exit(0);
        } else {
            System.out.println((numberOfAllURLs-numberOfRelevantURLs) + " external URLs removed.");
            System.out.println(numberOfRelevantURLs + " HATEOAS Links found under " + entryResource);
            if (numberOfRelevantURLs == allURLsInResponse.size()+1) {
                System.out.println("Only HATEOAS Links were found. No links to external Websites.");
            }
        }

        */

        writeAttackSetToFile(createAttackSetJSON(pathsObj));

    }

    private HashMap<String, Boolean> getHATEOASLinks(String resource) {

        Pattern patternFullURL = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\/([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
        Pattern patternHostAndPortOnly = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}(:?\\d+)*\\/");
        String responseBody = get(resource).asString();
        Matcher matcherFullURL = patternFullURL.matcher(responseBody);
        Matcher matcherHostAndPortOnly = patternHostAndPortOnly.matcher(resource);

        HashMap<String, Boolean> relevantURLsOnly = new HashMap<>();

        System.out.print("Getting all HATEOAS Links on " + resource + " ... ");

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

        System.out.println(relevantURLsOnly.size() + " found.");

        relevantURLsOnly.put(resource, true);
        System.out.println(resource + " changed to isVisited : " + relevantURLsOnly.get(resource));

        //Return the result
        return relevantURLsOnly;
    }


    //Supporting Methods:

    private ArrayList removeDuplicates(ArrayList<String> arrayList) {
        ArrayList<String> result = new ArrayList<>();
        HashSet<String> hashSet = new HashSet<>();
        for (String s : arrayList) {
            hashSet.add(s);
        }
        
        for (String s : hashSet) {
            result.add(s);
        }
        
        System.out.println(arrayList.size() - result.size() + " duplicate elements removed.");
        return result;
    }

    @SuppressWarnings("unchecked")
    private JSONObject createAttackSetJSON(JSONObject pathsObject) {
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
        System.out.println("Parser: attackSet: " + attackSet.toString());

        try {

            FileWriter file = new FileWriter("restsec-samples/src/main/resources/attackable/attackable.json");
            file.write(attackSet.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    private void loadProperties() throws IOException {
        System.out.println("Controller: Loading properties ... ");
        Properties properties = new Properties();

        try(InputStream stream = Scanner.class.getClassLoader().getResourceAsStream("config.properties")){
            properties.load(stream);
        }

        // Load config
        RestAssured.baseURI = properties.getProperty("base-uri");
        System.out.println("baseURI : "+RestAssured.baseURI);
        RestAssured.port = Integer.parseInt(properties.getProperty("port"));
        System.out.println("port : "+RestAssured.port);
        RestAssured.basePath = properties.getProperty("base-path");
        System.out.println("basePath : "+RestAssured.basePath);

        this.baseURL = properties.getProperty("base-uri") + ":" + properties.getProperty("port");// + properties.getProperty("base-path");

        if (!properties.getProperty("proxy_ip").equals("")) {
            RestAssured.proxy(properties.getProperty("proxy_ip"), Integer.parseInt(properties.getProperty("proxy_port")));
            System.out.println("proxy : "+RestAssured.proxy);
        } else {
            System.out.println("proxy : no proxy set in config");
        }

        System.out.println("Done.");

    }
    */

}
