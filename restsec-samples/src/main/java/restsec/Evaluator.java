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
    private final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    private Configuration config;

    Evaluator(Configuration configuration) {
        config = configuration;
    }

    void deleteOldResultsFile() {

        /*
        File fileToDelete = new File("src/main/resources/results/results.json");

        if(fileToDelete.delete()){
            System.out.println(fileToDelete.getName() + " is deleted!");
        }else{
            System.out.println("Delete operation failed.");
        }

        if (fileToDelete.exists()) {
            boolean bool = fileToDelete.delete();
            LOGGER.info(fileToDelete + " deleted? : " + bool);
        }
        */

        String fileName = "src/main/resources/results/results.json";
        File file = new File(fileName);

        System.out.println(file.canRead() + " " + file.canWrite()+" "+file.canExecute());

        try {
            System.out.println("deletedIfExists?: "+Files.deleteIfExists(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("delete result:" + file.delete());


        /*
        try {
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            FileLock lock = channel.lock();
            try {
                lock = channel.tryLock();
                System.out.print("file is not locked");
            } catch (OverlappingFileLockException e) {
                System.out.print("file is locked");
            } finally {
                lock.release();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        */

//        if (config.getBoolDeleteOldResultsFile()) {
//            try {
//                boolean deleteStatus = Files.deleteIfExists(fileToDelete.toPath());
//                LOGGER.info("Old Logfile \"results.json\" deleted: " + deleteStatus);
//            } catch (IOException ioe) {
//                ioe.printStackTrace();
//            }
//        }
    }

    void evaluateJettyLogfile() {

        if (config.getBoolDeleteOldResultsFile()) {
            deleteOldResultsFile();
        }

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

        // try with ressources
        if (!file.isFile()) {
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file, false));
                bufferedWriter.write("{}");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        vulnerabilityCounter++;

        JsonParser parser = new JsonParser();
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
        JsonObject existingJsonObject = new JsonObject();

        try {
            existingJsonObject = (JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject newJsonObject = new JsonObject();

        newJsonObject.addProperty("VulnType", vulnType);
        newJsonObject.addProperty("Endpoint", endpoint);
        newJsonObject.addProperty("Payload", payload);
        newJsonObject.addProperty("Comment", comment);

        existingJsonObject.add(String.valueOf(vulnerabilityCounter), new Gson().toJsonTree(newJsonObject));

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, false));
            String jsonOutput = gsonBuilder.toJson(existingJsonObject);
            bw.write(jsonOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
