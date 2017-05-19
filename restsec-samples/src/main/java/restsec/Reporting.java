package restsec;

// Generate HTML report from results.json
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.scanner.ScannerUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

class Reporting {

    private final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    void generateReport() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(Reporting.class, "/reporting/");

        // check if results file even exists, if not, create one
        File file = new File("src/main/resources/results/results.json");
        if (!file.isFile()) {
            try (BufferedWriter br = new BufferedWriter(new FileWriter(file, false))) {
                br.write("{}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Template template = configuration.getTemplate("report.ftl");
            StringWriter stringWriter = new StringWriter();
            Map<String, Object> map = new HashMap<>();

            //populating the Map
            map = setGeneralScanValuesFromConfig(map);

            Map newMap = ScannerUtils.getPackageStatisticsAsMap();
            map.put("packageStatistics", newMap);

            map = readVulnerabilityResultsJSON(map);
            map = readSecurityHeadersResultsJSON(map);

            //Processing the Map and write it to file
            map.put("ReportGeneratedTime", new SimpleDateFormat("dd MMMM yyyy - HH:mm:ss", new Locale("en", "US")).format(Calendar.getInstance().getTime()));

            //Scripts (thus also Payloads) are getting escaped by the Template Engine
            template.process(map, stringWriter);
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

    private Map readVulnerabilityResultsJSON(Map<String, Object> map) {

        JsonParser parser = new JsonParser();
        JsonObject resultsObject = new JsonObject();

        try {
            resultsObject = (JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        HashMap vulnerabilitesMap = new HashMap();

        int vulnCounter = 0;

        for (int i = 1; i <= resultsObject.entrySet().size(); i++) {

            //create submap for each vuln, read the values from the JSON and put them in a fresh map
            HashMap<String, String> submap = new HashMap<>();

            JsonObject currObj = resultsObject.getAsJsonObject(Integer.toString(i));

            if (!currObj.get("VulnType").getAsString().equals("Insecure HTTP Header") &&
                    !currObj.get("VulnType").getAsString().equals("Secure HTTP Header")) {
                submap.put("VulnType", currObj.get("VulnType").getAsString());
                submap.put("Endpoint", currObj.get("Endpoint").getAsString());
                submap.put("Payload", currObj.get("Payload").getAsString().replace("<", "&lt;").replace(">", "&gt;"));
                submap.put("Comment", currObj.get("Comment").getAsString());

                vulnerabilitesMap.put("vuln_" + Integer.toString(i), submap);
                LOGGER.info("vuln (xss, sqli, etc.) put to map");
                vulnCounter++;
            }
        }

        LOGGER.info("counter_vulns : " + vulnCounter);
        map.put("counter_vulns", vulnCounter);
        map.put("vulnerabilites", vulnerabilitesMap);
        return map;
    }

    private Map readSecurityHeadersResultsJSON(Map<String, Object> map) {
        JsonParser parser = new JsonParser();
        JsonObject resultsObject = new JsonObject();
        HashMap securityHeadersMap = new HashMap();

        try {
            resultsObject = (JsonObject) parser.parse(new FileReader("src/main/resources/results/results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int headerCounter = 0;
        int insecureCounter = 0;

        for (int i = 1; i <= resultsObject.entrySet().size(); i++) {
            //create submap for each vuln, read the values from the JSON and put them in a fresh map
            HashMap<String, String> submap = new HashMap<>();
            JsonObject currObj = resultsObject.getAsJsonObject(Integer.toString(i));

            if (currObj.get("VulnType").getAsString().equals("Insecure HTTP Header") ||
                    currObj.get("VulnType").getAsString().equals("Secure HTTP Header")) {
                submap.put("VulnType", currObj.get("VulnType").getAsString());
                submap.put("Endpoint", currObj.get("Endpoint").getAsString());
                submap.put("Header", currObj.get("Payload").getAsString());
                submap.put("Comment", currObj.get("Comment").getAsString());

                if (currObj.get("VulnType").getAsString().equals("Insecure HTTP Header")) {
                    insecureCounter++;
                }

                securityHeadersMap.put("headers_" + Integer.toString(i), submap);
                LOGGER.info("http security header put to map");
                headerCounter++;
            }
        }

        LOGGER.info("counter_headers : " + headerCounter);
        map.put("counter_headers", headerCounter);
        LOGGER.info("counter_insecure_headers : " + insecureCounter);
        map.put("counter_insecure_headers", insecureCounter);
        map.put("securityHeaders", securityHeadersMap);
        return map;
    }

}
