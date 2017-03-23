package restsec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    private static int vulnerabilityCounter = 0;
    private final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    private Configuration config;

    Evaluator(Configuration configuration) {
        config = configuration;
    }

    private void deleteOldLogFile() {

        if (config.getBoolDeleteOldResultsFile()) {
            try {
                boolean deleteStatus = Files.deleteIfExists(new File("src/main/resources/results/results.json").toPath());
                LOGGER.info("Old Logfile \"results.json\" deleted: " + deleteStatus);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    void evaluateJettyLogfile() {

        deleteOldLogFile();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(new Date()).replace("-", "_");

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("src/main/resources/jetty-logs/jetty-"+ s +".request.log"));
        } catch (FileNotFoundException e) {
            LOGGER.warn("No log found.");
            System.exit(0);
        }
        String line;

        try {
            if ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("GET //" + InetAddress.getLocalHost().getHostAddress() + ":5555/Cookie:")) {
                    LOGGER.info("Success! XSS Payload executed and called back! Content: ");

                    if (line.contains("token")) {
                        Pattern p = Pattern.compile("token=\\S*");
                        Matcher m = p.matcher(line);
                        //noinspection ResultOfMethodCallIgnored
                        m.find();
                        writeVulnerabilityToResultsFile("XSS", "unknown", "unknown", "Payload called back: "+m.group());
                    } else {
                        writeVulnerabilityToResultsFile("XSS", "unknown", "unknown", "Payload called back: no token found");
                    }
                }
            } else {
                LOGGER.warn("Jetty log is empty. Apparently no payload called back.");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeVulnerabilityToResultsFile(String vulnType, String endpoint, String payload, String comment) {

        File file = new File("src/main/resources/results/results.json");

        if (!file.isFile()) {
            try {
                FileWriter fileWriter = new FileWriter(file, false);
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
        JsonObject existingJsonObject = new JsonObject();

        try {
            existingJsonObject = (JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonObject newJsonObject = new JsonObject();

        newJsonObject.addProperty("VulnType", vulnType);
        newJsonObject.addProperty("Endpoint", endpoint);
        newJsonObject.addProperty("Payload", payload);
        newJsonObject.addProperty("Comment", comment);

        existingJsonObject.add(String.valueOf(vulnerabilityCounter), new Gson().toJsonTree(newJsonObject));

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            String jsonOutput = gsonBuilder.toJson(existingJsonObject);
            fileWriter.write(jsonOutput);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
