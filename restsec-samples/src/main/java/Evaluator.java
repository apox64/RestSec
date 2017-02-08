import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {

    /*
    evaluates the results
        reads the jetty server logs
        creates webpage (colors, etc.)
     */

    private void evaluateLogfile() throws IOException {
        BufferedReader bufRead = new BufferedReader(new FileReader("logs/jetty-request.log"));
        String line;

        if ((line = bufRead.readLine()) != null)
        {
            if (line.contains("token")) {
                System.out.println(">>> Success! Log contains token.");
                Pattern p = Pattern.compile("token=\\S*");
                Matcher m = p.matcher(line);
                m.find();
                System.out.println(">>> Token found: "+m.group(0));

            } else {
                System.out.println(">>> No token was found. Is a user logged in?");
            }
        } else {
            Assert.fail(">>> Jetty log seems to be empty ... \n- Did you refresh the page manually to execute the payload?\n" +
                    "- Did your browser connect via the proxy?");
        }
    }

}
