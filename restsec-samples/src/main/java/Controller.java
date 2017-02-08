
public class Controller {
    // Create Parser
    // Create one or more Scanners

    public static void main (String[] args) {
        Parser parser = new Parser();
        parser.parseSwaggerJSON("docs_swagger/swagger-sample.json", false);
        Scanner scanner = new Scanner("attackable/attackable.json","payloads/xss.json");
        scanner.scanAll();
    }

}
