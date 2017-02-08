package juiceshop;

import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.PreemptiveOAuth2HeaderScheme;
import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class OAuth2 {

    private String accessToken = "";

    @Test
    //TODO: Continue with juiceshop.OAuth2 here
    public void oAuth2() {
        given().
                auth().
                oauth2(accessToken).
            when().
                get("").
            then().
                statusCode(200);
    }

    /* ######################################################################################################################################################
    UNMODIFIED CODE FROM rest-assured examples
    ###################################################################################################################################################### */

    @Test
    public void oauth2_works_with_preemptive_header_signing() {
        accessToken = "accessToken";

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
