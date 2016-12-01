
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Swagger Documentations
// in the json Format, parses them and creates and attack set for the scanner.

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SwaggerTest {

    private String version;
    private String host;
    private String basePath;

    private String file = "swagger-sample.json";

    private JSONObject pathsObj;
    private Set<String> attackSet = new HashSet<>();
    
    public static void main (String[] args){
        SwaggerTest test = new SwaggerTest();
        test.parseSwaggerJSON();

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        test.printAttackSet();
    }

    // jsonObj > pathsObj > methodsObj

    @SuppressWarnings("unchecked")
    private void parseSwaggerJSON() {
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
        JSONObject methodObj = (JSONObject) object.get(path);
        Iterator<String> it = methodObj.keySet().iterator();

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

    public void addToAttackSet(String string) {
        attackSet.add(string);
    }

    public void printAttackSet(){
        System.err.println("\n>>> POSSIBLE ATTACKS <<<");
        Iterator<String> it = attackSet.iterator();
        while (it.hasNext()) {
            System.err.println(it.next());
        }
        System.err.println(">>>>>>>>>>>> <<<<<<<<<<<");
    }

}
