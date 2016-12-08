// Scanner needs input:
// - Attack Set (from SwaggerParser, etc.)
// - Payloads (from payloads/)

import org.json.simple.JSONObject;

import java.util.Set;

public class Scanner {

    public Scanner(Set<String> attackSet, JSONObject payloads) {
        // ...
    }

    public void scanAll(){
        //Total number of operations: Size of attackSet * number of payloads
    }

}
