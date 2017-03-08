package restsec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.xpath.operations.Bool;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    /*
    evaluates the results
        reads the jetty server logs
        creates webpage (colors, etc.)
     */

    private static int vulnerabilityCounter = 0;

    public static void evaluateLogfile() {

        //JSONObject results = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(new Date()).replace("-", "_");

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("src/main/resources/jetty-logs/jetty-"+ s +".request.log"));
        } catch (FileNotFoundException e) {
            System.err.println("restsec.Evaluator: No log found.");
            System.exit(0);
        }
        String line;

        try {
            if ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("GET //0.0.0.0:5555/Cookie:")) {
                    System.out.print("restsec.Evaluator: Success! XSS Payload executed and called back! Content: ");

                    if (line.contains("token")) {
                        Pattern p = Pattern.compile("token=\\S*");
                        Matcher m = p.matcher(line);
                        //noinspection ResultOfMethodCallIgnored
                        m.find();
                        writeVulnerabilityToFile("XSS", "unknown", "unknown", "Payload called back: "+m.group());
                    } else {
                        writeVulnerabilityToFile("XSS", "unknown", "unknown", "Payload called back: no token found");
                    }
                }
            } else {
                System.err.println("restsec.Evaluator: Jetty log is empty ... Target seems to be resistant against used payloads. (Did your browser connect via the proxy?)");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeVulnerabilityToFile(String vulnType, String endpoint, String payload, String comment) {

        File f = new File("src/main/resources/results/results.json");

        if (!f.isFile()) {
            try {
                FileWriter fileWriter = new FileWriter(f, false);
                fileWriter.write("{}");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        vulnerabilityCounter++;

        JsonParser parser = new JsonParser();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        com.google.gson.JsonObject existingJsonObject = new com.google.gson.JsonObject();

        //read existing (father object)
        try {
            existingJsonObject = (com.google.gson.JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //create new (child object)
        Map<String, Object> config = new HashMap<>();
        config.put("javax.json.stream.JsonGenerator.prettyPrinting", Boolean.TRUE);
        JsonBuilderFactory factory = Json.createBuilderFactory(config);

        JsonObject newJsonObject = factory.createObjectBuilder()
                .add("Vulnerability Type", vulnType)
                .add("Endpoint", endpoint)
                .add("Payload", payload)
                .add("Comment", comment)
                .build();

        existingJsonObject.add(String.valueOf(vulnerabilityCounter), new Gson().toJsonTree(newJsonObject));

        try {
            FileWriter file = new FileWriter("src/main/resources/results/results.json", false);
            String jsonOutput = gsonBuilder.toJson(existingJsonObject);
            file.write(jsonOutput);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
