package restsec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.config.Configuration;

import java.io.*;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    private static int vulnerabilityCounter = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    static void deleteOldResultsFile() {
        File file = new File("src/main/resources/results/results.json");
        try {
            LOGGER.info("Existing results file deleted: "+Files.deleteIfExists(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void evaluateJettyLogfile() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(new Date()).replace("-", "_");

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("target/jetty-logs/jetty-"+ s +".request.log"));
        } catch (FileNotFoundException e) {
            LOGGER.warn("No log found.");
            System.exit(0);
        }
        String line;

        try {

            if ((bufferedReader.readLine()) == null) {
                LOGGER.warn("Jetty log is empty. Apparently no payload called back.");
            } else {
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("GET //" + InetAddress.getLocalHost().getHostAddress() + ":5555/Cookie:")) {

                        Pattern pattern = Pattern.compile("(?:Cookie.*)=(\\S+)");
                        Matcher matcher = pattern.matcher(line);

                        String content = "";

                        if (matcher.find()) {
                            content = matcher.group(1);
                        }

                        LOGGER.info("Success! XSS Payload executed and called back! Content: " + content);

                        if (line.contains("token")) {
                            Pattern p = Pattern.compile("token=\\S*");
                            Matcher m = p.matcher(line);
                            //noinspection ResultOfMethodCallIgnored
                            m.find();
                            writeVulnerabilityToResultsFile("XSS", "unknown", "unknown", "Payload called back: " + m.group());
                        } else {
                            writeVulnerabilityToResultsFile("XSS", "unknown", "unknown", "Payload called back: no token found");
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeVulnerabilityToResultsFile(String vulnType, String endpoint, String payload, String comment) {

        LoggerFactory.getLogger(Evaluator.class).info("Writing found vulnerability to file: " + vulnType);

        File file = new File("src/main/resources/results/results.json");

        if (!file.isFile()) {
            try (BufferedWriter br = new BufferedWriter(new FileWriter(file, false))) {
                br.write("{}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vulnerabilityCounter++;

        JsonParser parser = new JsonParser();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        JsonObject existingJsonObject = new JsonObject();

        try (FileReader fr = new FileReader(file)) {
            existingJsonObject = (JsonObject) parser.parse(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject newJsonObject = new JsonObject();

        newJsonObject.addProperty("VulnType", vulnType);
        newJsonObject.addProperty("Endpoint", endpoint);
        newJsonObject.addProperty("Payload", payload);
        newJsonObject.addProperty("Comment", comment);

        existingJsonObject.add(String.valueOf(vulnerabilityCounter), new Gson().toJsonTree(newJsonObject));
        String jsonOutput = gsonBuilder.toJson(existingJsonObject);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write(jsonOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
