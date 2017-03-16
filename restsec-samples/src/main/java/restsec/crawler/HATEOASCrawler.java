package restsec.crawler;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.get;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HATEOASCrawler extends Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HATEOASCrawler.class);
    private String entryPoint;

    public HATEOASCrawler(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public void crawl() {
        LOGGER.info("Following HATEOAS links on : "+entryPoint);
        discoverLinksForHATEOAS(entryPoint);
    }

    private void discoverLinksForHATEOAS(String entryResource) {
        JSONObject attackSet = new JSONObject();
        //System.out.println("restsec.Parser: Starting endpoint discovery for HATEOAS on "+entryResource+" ...");

        //HashMap that gets filled for each call of getHATEOASLinks()
        //String endpoint, Boolean isVisited
        HashMap<String, Boolean> relevantURLs = new HashMap<>();

        //init
        try {
            relevantURLs = getHATEOASLinksForResource(entryResource);
        } catch (ConnectException e) {
            LOGGER.warn("Could not establish connection to target.");
            System.exit(0);
        }

        relevantURLs.put(entryResource, true);

        while (relevantURLs.containsValue(false)) {
            for (Object o : relevantURLs.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (!Boolean.parseBoolean(pair.getValue().toString())) {
                    HashMap<String, Boolean> temp = null;
                    try {
                        temp = getHATEOASLinksForResource(pair.getKey().toString());
                    } catch (ConnectException ce) {
                        LOGGER.warn("Could not establish connection to target.");
                        System.exit(0);
                    }
                    relevantURLs = mergeHashMaps(relevantURLs, temp);
                }
            }
        }

        for (Object key : relevantURLs.keySet()) {
            //noinspection unchecked,SuspiciousMethodCalls
            attackSet.put(key, relevantURLs.get(key));
        }

        super.writeAttackSetToFile(createCompleteHTTPMethodAttackSetJSON(attackSet));

    }

    private HashMap<String, Boolean> getHATEOASLinksForResource(String resource) throws ConnectException {

        Pattern patternFullURL = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}/([-a-zA-Z0-9@:%_+.~#?&/=]*)");
        Pattern patternHostAndPortOnly = Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}(:?\\d+)*/");
        String responseBody;
        responseBody = get(resource).asString();

        Matcher matcherFullURL = patternFullURL.matcher(responseBody);
        Matcher matcherHostAndPortOnly = patternHostAndPortOnly.matcher(resource);

        HashMap<String, Boolean> relevantURLsOnly = new HashMap<>();

        //TODO: Printing here
        //System.out.print("Looking for all HATEOAS Links on " + resource + " ... ");

        //Matching all URLs
        HashMap<String, Boolean> allURLsInResponse = new HashMap<>();
        while (matcherFullURL.find()) {
            allURLsInResponse.put(matcherFullURL.group(), false);
        }

        //Return only the URLs that are on the same host as the given resource
        //noinspection ResultOfMethodCallIgnored
        matcherHostAndPortOnly.find();
        String entryResourceDomainAndPortOnly = matcherHostAndPortOnly.group();

        for (Object o : allURLsInResponse.entrySet()) {
            Map.Entry url = (Map.Entry) o;
            Matcher m = patternHostAndPortOnly.matcher(url.getKey().toString());
            while (m.find()) {
                if (m.group().equals(entryResourceDomainAndPortOnly)) {
                    relevantURLsOnly.put(url.getKey().toString(), false);
                }
            }
        }

        //TODO: Printing here
        //System.out.println(relevantURLsOnly.size() + " found.");

        relevantURLsOnly.put(resource, true);
        //System.out.println(resource + " changed to isVisited : " + relevantURLsOnly.get(resource));

        //Return the result
        return relevantURLsOnly;
    }

    HashMap<String, Boolean> mergeHashMaps(HashMap<String, Boolean> map1, HashMap<String, Boolean> map2) {

        //find bigger one
        HashMap<String, Boolean> smallMap, bigMap;

        if (map1.size() >= map2.size()) {
            bigMap = map1;
            smallMap = map2;
        } else {
            bigMap = map2;
            smallMap = map1;
        }

        HashMap<String, Boolean> resultMap = new HashMap<>();

        for (Object key : bigMap.keySet()) {
            if (smallMap.containsKey(key.toString())) {
                //noinspection SuspiciousMethodCalls,SuspiciousMethodCalls
                resultMap.put(key.toString(), smallMap.get(key) || bigMap.get(key));
            } else {
                //noinspection SuspiciousMethodCalls
                resultMap.put(key.toString(), bigMap.get(key));
            }
        }

        return resultMap;
    }

}
