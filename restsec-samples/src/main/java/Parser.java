
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Documentations
//in the json Format, parses them and creates and attack set for the scanner.
//
// Swagger (yes)
// Spring (working on it)
// ...

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
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
    private String entryRessourceHATEOAS = "";

    public Parser(String pathToSwaggerJSONFile, boolean useAllPossibleHTTPMethodsForAttack) {
        this.pathToSwaggerJSONFile = pathToSwaggerJSONFile;
        this.useAllPossibleHTTPMethodsForAttack = useAllPossibleHTTPMethodsForAttack;
    }

    public Parser(String entryRessourceHATEOAS) throws IOException {
        this.entryRessourceHATEOAS = entryRessourceHATEOAS;
        //loadProperties();
    }

    public void run() {
        if (pathToSwaggerJSONFile.isEmpty()) {
            //run HATEOAS Parsing
            discoverLinksForHATEOAS(this.entryRessourceHATEOAS);
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

    //TODO: call this function recursively on itself until no new entries in global list appear (check this after each iteration)
    private void discoverLinksForHATEOAS(String entryRessource){
        JSONObject pathsObj = new JSONObject();
        System.out.println("Parser: Starting endpoint discovery for HATEOAS on "+entryRessource+" ...");

        Pattern regexPatternFullURL = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\/([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
        Pattern regexPatternDomainPortOnly = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}(:?\\d+)*\\/");

        String responseBody = get(entryRessource).asString();

        //Find all URLs in Response (unique)
        Set<String> allUniqueURLs = new HashSet<String>();
        Matcher m = regexPatternFullURL.matcher(responseBody);
        while (m.find()) {
            allUniqueURLs.add(m.group());
        }

        System.out.println(allUniqueURLs.size() + " unique URLs found ...");
        int c = 1;
        for(String match : allUniqueURLs) {
            System.out.println(c + " : " + match);
            c++;
        }

        //Find all relevant URLs that are located on the entryRessource domain (unique)
        Matcher ma = regexPatternDomainPortOnly.matcher(entryRessource);
        ma.find();
        String entryRessourceDomainAndPortOnly = ma.group();

        Set<String> allRessourceURLs = new HashSet<String>();
        int count = 0;
        for(String s : allUniqueURLs) {
            Matcher mat = regexPatternDomainPortOnly.matcher(s);
            while (mat.find()) {
                if (mat.group().equals(entryRessourceDomainAndPortOnly)) {
                    //System.out.println("HATEOAS Link found: " + mat.group());
                    allRessourceURLs.add(mat.group());
                    count++;
                }
            }
        }

        if (count == 0) {
            System.err.println("Parser: No HATEOAS Link found for the entry URL you entered. Are you sure the given entry point follows HATEOAS?");
        } else {
            System.out.println(count + " HATEOAS Links found under "+entryRessource);
            if (count == allUniqueURLs.size()) System.out.println("Only HATEOAS Links were found. No links to external Websites.");
        }

        //TODO : Currently ending program here
        //TODO : 1. create global list with endpoint links
        //TODO : 2. Fill list by calling this function recursively (until all links have been followed once)
        System.exit(0);

        writeAttackSetToFile(createAttackSetJSON(pathsObj));

        //call entryRessource and check if there is any link on the same domain/host (if not, abort "You sure you use HATEOAS?")
        //find all links in response from entryRessource on the same domain/host
        //check if found ressource is valid (by calling it)
        //perform checks if the entry is already in the attackset or not (if not, add it)
        //all methods have to be tested since there is not documentation (creating the attackset)
        //done.
    }

    //Supporting Methods:

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
