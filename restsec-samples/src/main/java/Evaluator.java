import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

        try {
            if ((line = bufferedReader.readLine()) != null)
            {
                if (line.contains("GET //0.0.0.0:5555/Cookie:")) {
                    System.out.print("Evaluator: Success! XSS Payload executed and called back! Content: ");
                    if (line.contains("token")) {
                        Pattern p = Pattern.compile("token=\\S*");
                        Matcher m = p.matcher(line);
                        m.find();
                        System.out.println(m.group(0));
                    } else {
                        System.out.println("empty. (No token was found. Is a user logged in?)");
                    }
                }
            } else {
                System.err.println("Evaluator: Jetty log is empty ... Target is resistant against used payloads. (Did your browser connect via the proxy?)");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
