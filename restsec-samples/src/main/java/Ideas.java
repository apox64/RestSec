
public class Ideas {

    /* from REST Security Cheat Sheet
    1. Protect priviledged areas (https://example.com/admin/exportAllData)
    2. The session token or API key should be sent along as a cookie or body parameter
    3. CSRF Token
    4. A data contextual check needs to be done, server side, with each request.
    5. the client will specify the Content-Type (e.g. application/xml or application/json) of the incoming data. The server should never assume the Content-Type;
    (Always require Content-Type header. if Content-Type header != expected content, then reject with a 406 Not Acceptable response)

    */


    // Swagger Support (Parse Swagger and test with extracted params)


/* #################################################################################################################
    @Ignore("juice-shop doesn't authenticate via HTTP header.")
    @Test
    //@DisplayName("authenticating: basic, base64, HTTP header")
    public void authBasicBase64HTTPHeader() {
        RestAssured.given().
                auth().preemptive().basic(email,password).
                when().
                post("/user/login").
                then().
                statusCode(200);
    }

   #################################################################################################################

        @Ignore("Not implemented yet.")
    @Test
    //@DisplayName("getting cookies")
    public void showCookies() {
        Response response = when().get("/user/login");
        // Get all cookies as simple name-value pairs
        Map<String, String> allCookies = response.getCookies();
        // Get a single cookie value:
        //String cookieValue = response.getCookie("cookieName");
    }

   #################################################################################################################

        @Ignore("Not implemented yet.")
    @Test
    //@DisplayName("looking for CSRF protection cookie")
    public void csrfProtectionTokenReturned() {

        given().
                auth().form("John", "Doe", formAuthConfig().withAutoDetectionOfCsrf()).
        when().
                get("/formAuth").
        then().
                statusCode(200);
    given().
    auth().form("John", "Doe", springSecurity().withCsrfFieldName("_csrf")).
    when().
    get("/formAuth").
    then().
    statusCode(200);

}

    #################################################################################################################
    OAUTH1 (LEGACY)

    @Ignore("Legacy: OAuth1 - Header Signing")
    @Test
    public void POST_OAuth1_Header_Signing() {
        //RestAssured.oauth(consumerKey, consumerSecret, accessToken, secretToken);
        given().
                auth().oauth("key", "secret", "accesskey", "accesssecret").
                when().
                get("http://term.ie/oauth/example/echo_api.php?works=true").
                then().
                body("html.body", equalTo("works=true"));
    }

    @Ignore("Legacy: OAuth1 - Query Signing")
    @Test
    public void POST_OAuth1_Query_Signing() {
        given().
                auth().oauth("key", "secret", "accesskey", "accesssecret", OAuthSignature.QUERY_STRING).
                when().
                get("http://term.ie/oauth/example/echo_api.php?works=true").
                then().
                body("html.body", equalTo("works=true"));
    }

    #################################################################################################################
    CODE FROM REST-ASSURED

    @Test
    public void oauth1_works_with_header_signing() {
        given().
                auth().oauth("key", "secret", "accesskey", "accesssecret").
                when().
                get("http://term.ie/oauth/example/echo_api.php?works=true").
                then().
                body("html.body", equalTo("works=true"));
    }

    @Test
    public void oauth1_works_with_query_signing() {
        given().
                auth().oauth("key", "secret", "accesskey", "accesssecret", OAuthSignature.QUERY_STRING).
                when().
                get("http://term.ie/oauth/example/echo_api.php?works=true").
                then().
                body("html.body", equalTo("works=true"));
    }

     #################################################################################################################

#################################################################################################################
XSS: Open Port on Server and let Payload POST to Server (ex. Cookies) as Proof-Of-Concept
#################################################################################################################

// Simple XXS ServerSocket Listener
        try (
                ServerSocket serverSocket = new ServerSocket(9876);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out =
                        new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        }


*/
}
