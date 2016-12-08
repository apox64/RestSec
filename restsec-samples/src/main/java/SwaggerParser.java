
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Swagger Documentations
//in the json Format, parses them and creates and attack set for the scanner.

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SwaggerParser {

    private String version;
    private String host;
    private String basePath;

    private static String filepath;

    private JSONObject pathsObj;
    private Set<String> attackSet = new HashSet<>();

    public SwaggerParser(String file) {
        filepath = file;
    }

    public static void main (String[] args){
        SwaggerParser test = new SwaggerParser("swagger-sample.json");
        test.parseSwaggerJSON(filepath);

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        test.printAttackSet();
    }

    // jsonObj > pathsObj > httpVerbObj

    @SuppressWarnings("unchecked")
    private void parseSwaggerJSON(String file) {
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
            System.out.println("\nPaths (unsorted):");
            Iterator<String> it = pathsObj.keySet().iterator();

            while(it.hasNext()){
                String currentPath = it.next();
                System.out.println(currentPath);
                createAttackSetForPath(pathsObj, currentPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> createAttackSetForPath(JSONObject object, String path) {
        JSONObject httpVerbObj = (JSONObject) object.get(path);
        Iterator<String> it = httpVerbObj.keySet().iterator();

        while (it.hasNext()){
            switch (it.next()) {
                case "post" :
                    //System.err.println("\n>>> POST found in " + path);
                    addToAttackSet(path + " : POST");
                    break;
                case "patch" :
                    //System.err.println("\n>>> PATCH found in " + path);
                    addToAttackSet(path + " : PATCH");
                    break;
                case "put" :
                    //System.err.println("\n>>> PUT found in " + path);
                    addToAttackSet(path + " : PUT");
                    break;
                case "delete" :
                    //System.err.println("\n>>> DELETE found in " + path);
                    addToAttackSet(path + " : DELETE");
                    break;
            }
        }

        return attackSet;
    }

    private void addToAttackSet(String string) {
        attackSet.add(string);
    }

    private void printAttackSet(){
        System.err.println("\n>>> POSSIBLE ATTACKS <<<");
        Iterator<String> it = attackSet.iterator();
        while (it.hasNext()) {
            System.err.println(it.next());
        }
        System.err.println(">>>>>>>>>>>> <<<<<<<<<<<");
    }

}
