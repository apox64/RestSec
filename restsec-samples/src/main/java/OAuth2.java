import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.OAuthSignature;
import io.restassured.authentication.PreemptiveOAuth2HeaderScheme;
import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class OAuth2 {

    /*
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
    */

    @Test
    //TODO: Continue with OAuth2 here
    public void oAuth2() {
        //RestAssured.oauth2(accessToken, OAuthSignature);
        //final RequestSpecification oauthTest = RestAssured.given().auth().oauth2("", OAuthSignature.valueOf(""));
    }

    /* ######################################################################################################################################################
    UNMODIFIED CODE FROM rest-assured examples
    ###################################################################################################################################################### */

    /*
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
    */

    @Test
    public void oauth2_works_with_preemptive_header_signing() {
        final String accessToken = "accessToken";

        given().
                auth().preemptive().oauth2(accessToken).
                filter(new Filter() {
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        assertThat(requestSpec.getHeaders().getValue("Authorization"), equalTo("Bearer "+accessToken));
                        return new ResponseBuilder().setBody("ok").setStatusCode(200).build();
                    }
                }).
                when().
                get("/somewhere").
                then().
                statusCode(200);
    }

    @Test
    public void oauth2_works_with_non_preemptive_header_signing() {
        final String accessToken = "accessToken";

        given().
                auth().oauth2(accessToken).
                filter(new Filter() {
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        AuthenticationScheme scheme = requestSpec.getAuthenticationScheme();
                        assertThat(scheme, instanceOf(PreemptiveOAuth2HeaderScheme.class));
                        assertThat(((PreemptiveOAuth2HeaderScheme) scheme).getAccessToken(), equalTo(accessToken));
                        return new ResponseBuilder().setBody("ok").setStatusCode(200).build();
                    }
                }).
                when().
                get("/somewhere").
                then().
                statusCode(200);
    }

    /* ######################################################################################################################################################
    END
    ###################################################################################################################################################### */
}
