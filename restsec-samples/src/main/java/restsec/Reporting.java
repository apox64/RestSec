package restsec;

// Generate HTML report from results.json

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Reporting {

    private final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    public void generateReport() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(Reporting.class, "/reporting/");

        try {
            Template simpleTemplate = configuration.getTemplate("report.ftl");
            StringWriter stringWriter = new StringWriter();
            Map<String, Object> simpleMap = new HashMap<>();

            //populating the Map
            simpleMap = setGeneralScanValuesFromConfig(simpleMap);
            simpleMap = readResultsJSON(simpleMap);

            //Processing the Map and write it to file
            simpleMap.put("ReportGeneratedTime", new SimpleDateFormat("dd MMMM yyyy - HH:mm:ss", new Locale("en", "US")).format(Calendar.getInstance().getTime()));
            simpleTemplate.process(simpleMap, stringWriter);
            writeHTMLtoFile("report.html", stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map setGeneralScanValuesFromConfig(Map<String, Object> map) {
        restsec.config.Configuration config = new restsec.config.Configuration();
        map.put("TargetIP", config.getBaseURI());
        map.put("TargetPort", config.getPort());
        map.put("TargetPath", config.getBasePath());
        map.put("CrawlerType", config.getCrawlerType());
        map.put("ScannerType", config.getScannerType());
        return map;
    }

    private void writeHTMLtoFile(String path, String content) {
        try {
            PrintWriter printWriter = new PrintWriter(path, "UTF-8");
            printWriter.write(content);
            printWriter.close();
            LOGGER.info("HTML written to file: " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map readResultsJSON(Map<String, Object> map) {

        JsonParser parser = new JsonParser();
        JsonObject resultsObject = new JsonObject();

        try {
            resultsObject = (JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        map.put("counter_vulns", resultsObject.entrySet().size());
        LOGGER.info("counter_vulns : " + resultsObject.entrySet().size());

        for (int i = 1; i <= resultsObject.entrySet().size(); i++) {

            //create submap for each vuln, read the values from the JSON and put them in a fresh map
            HashMap<String, String> submap = new HashMap<>();

            JsonObject currObj = resultsObject.getAsJsonObject(Integer.toString(i));

            submap.put("VulnType", currObj.get("VulnType").getAsString());
            submap.put("Endpoint", currObj.get("Endpoint").getAsString());
            submap.put("Payload", currObj.get("Payload").getAsString());
            submap.put("Comment", currObj.get("Comment").getAsString());

            //put in into the main map
            map.put("vuln_" + Integer.toString(i), submap);
            LOGGER.info("vuln put to map");
        }

        return map;
    }

}
