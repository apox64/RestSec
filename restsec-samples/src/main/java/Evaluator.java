import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    /*
    evaluates the results
        reads the jetty server logs
        creates webpage (colors, etc.)
     */

    public void evaluateLogfile() {

        JSONObject results = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(new Date()).replace("-", "_");

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("restsec-samples/src/main/resources/jetty-logs/jetty-"+s.toString()+".request.log"));
        } catch (FileNotFoundException e) {
            System.err.println("Evaluator: No log found.");
            System.exit(0);
        }
        String line;

        JSONArray xssResult = new JSONArray();

        try {
            if ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("GET //0.0.0.0:5555/Cookie:")) {
                    System.out.print("Evaluator: Success! XSS Payload executed and called back! Content: ");
                    xssResult.add("vulnerable");
                    if (line.contains("token")) {
                        Pattern p = Pattern.compile("token=\\S*");
                        Matcher m = p.matcher(line);
                        m.find();
                        System.out.println(m.group(0));
                        xssResult.add(m.group(0));
                    } else {
                        System.out.println("empty. (No token was found. Is a user logged in?)");
                        xssResult.add("no token");
                    }

                }
            } else {
                System.err.println("Evaluator: Jetty log is empty ... Target is resistant against used payloads. (Did your browser connect via the proxy?)");
                xssResult.add("not vulnerable");
            }

            results.put("XSS", xssResult);

        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println("Writing results to file ...");
        writeEvaluationToFile(results);

    }

    private void writeEvaluationToFile(JSONObject results){
        try {

            FileWriter file = new FileWriter("restsec-samples/src/main/resources/results/results.json");

            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            String output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);

            file.write(output);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Parser: AttackSet written to File.");

    }

}
