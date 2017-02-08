
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Documentations
//in the json Format, parses them and creates and attack set for the scanner.
//
// Swagger (yes)
// Spring (no)
// ...

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.Iterator;

public class Parser {

    private String version;
    private String host;
    private String basePath;

    //private static String filepath = "docs_swagger/swagger-sample.json";

    private JSONObject pathsObj;

    //public Parser(String filename) {
        //filepath = filename;
    //}

    /*
    public static void main (String[] args){
        Parser swaggerParser = new Parser(filepath);
        swaggerParser.parseSwaggerJSON(filepath);

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    @SuppressWarnings("unchecked")
    public void parseSwaggerJSON(String file, boolean bruteforce) {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObj = (JSONObject) jsonParser.parse(new FileReader(getClass().getClassLoader().getResource(file).getFile()));

            version = (String) jsonObj.get("swagger");
            host = (String) jsonObj.get("host");
            basePath = (String) jsonObj.get("basePath");

            pathsObj = (JSONObject) jsonObj.get("paths");

            System.out.println("Swagger Version: \t" + version);
            System.out.println("Host: \t\t\t\t" + host);
            System.out.println("Basepath: \t\t\t" + basePath);

            if (bruteforce) {
                writeAttackSetToFile(createBruteForceAttackSetJSON(pathsObj));
            } else {
                writeAttackSetToFile(createAttackSetJSON(pathsObj));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSpringJSON(String file, boolean bruteforce){
    //TODO: Not yet implemented. Future release?
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
                        //attackSet.put(currentPath, new JSONObject().put(currentPath, "POST")).toString();
                        //temp.put(currentPath, "POST");
                        attackCounter++;
                        break;
                    case "patch":
                        array.add("PATCH");
                        //attackSet.put(currentPath, new JSONObject().put(currentPath, "PATCH"));
                        //temp.put(currentPath,"PATCH");
                        attackCounter++;
                        break;
                    case "put":
                        array.add("PUT");
                        //attackSet.put(currentPath, new JSONObject().put(currentPath, "PUT"));
                        //temp.put(currentPath,"PUT");
                        attackCounter++;
                        break;
                    case "delete":
                        array.add("DELETE");
                        //attackSet.put(currentPath, new JSONObject().put(currentPath, "DELETE"));
                        //temp.put(currentPath,"DELETE");
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
    private JSONObject createBruteForceAttackSetJSON(JSONObject pathsObject) {
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

}
