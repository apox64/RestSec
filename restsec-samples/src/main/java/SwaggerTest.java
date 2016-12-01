
//IDEA: Scanning REST APIs is complicated without documentation. This class reads Swagger Documentations
// either as xml or json, parses them and starts security scans on them.

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

public class SwaggerTest {

    public static void main (String[] args){
        SwaggerTest test = new SwaggerTest();
        test.swaggerJSONParser();
    }

    @SuppressWarnings("unchecked")
    private void swaggerJSONParser() {
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(getClass().getClassLoader().getResource("swagger.json").getFile()));

            String version = (String) jsonObject.get("swagger");
            String host = (String) jsonObject.get("host");
            String basepath = (String) jsonObject.get("basePath");

            JSONObject pathsObj = (JSONObject) jsonObject.get("paths");
            Set<String> pathsSet = pathsObj.keySet();

            System.out.println("Swagger Version: \t" + version);
            System.out.println("Host: \t\t\t\t" + host);
            System.out.println("Basepath: \t\t\t" + basepath);
            System.out.println("\nPaths (unsorted):");
            Iterator<String> it = pathsSet.iterator();
            while(it.hasNext()){
                System.out.println(it.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
