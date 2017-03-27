package restsec.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restsec.AttackSet;

import static io.restassured.RestAssured.get;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HATEOASCrawler implements Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HATEOASCrawler.class);
    private String entryPoint;

    public HATEOASCrawler(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    @Override
    public AttackSet crawl(String target) {
        LOGGER.info("Following HATEOAS links on : " + target);
        return discoverLinks(target);
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    private AttackSet discoverLinks(String entryResource) {
        AttackSet attackSet = new AttackSet();

        HashMap<String, Boolean> relevantURLs;
        relevantURLs = getLinksForResource(entryResource);
        relevantURLs.put(entryResource, true);

        while (relevantURLs.containsValue(false)) {
            for (Object o : relevantURLs.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (!Boolean.parseBoolean(pair.getValue().toString())) {
                    HashMap<String, Boolean> temp;
                        temp = getLinksForResource(pair.getKey().toString());
                    relevantURLs = mergeHashMapsSimple(relevantURLs, temp);
                }
            }
        }

        for (Object key : relevantURLs.entrySet()) {
            attackSet.put(key, relevantURLs.get(key));
        }

        return attackSet;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private HashMap<String, Boolean> getLinksForResource(String resource) {

        Pattern patternFullURL = Pattern.compile("https?://(www\\.)?[a-zA-Z0-9@:%._+-~#=]{2,256}/([a-zA-Z0-9@:%_+-.~#?&/=]*)");
        Pattern patternHostAndPortOnly = Pattern.compile("https?://(www\\.)?[a-zA-Z0-9@:%._+-~#=]{2,256}(:?\\d+)/");
        String responseBody;
        responseBody = get(resource).asString();

        Matcher matcherFullURL = patternFullURL.matcher(responseBody);
        Matcher matcherHostAndPortOnly = patternHostAndPortOnly.matcher(resource);

        HashMap<String, Boolean> sameOriginURLsOnly = new HashMap<>();

        HashMap<String, Boolean> allURLsInResponse = new HashMap<>();

        while (matcherFullURL.find()) {
            allURLsInResponse.put(matcherFullURL.group(), false);
        }

        //Return only the URLs that are on the same host as the given resource
        matcherHostAndPortOnly.find();
        String entryResourceDomainAndPortOnly = matcherHostAndPortOnly.group();

        for (Object o : allURLsInResponse.entrySet()) {
            Map.Entry url = (Map.Entry) o;
            Matcher m = patternHostAndPortOnly.matcher(url.getKey().toString());
            while (m.find()) {
                if (m.group().equals(entryResourceDomainAndPortOnly)) {
                    sameOriginURLsOnly.put(url.getKey().toString(), false);
                }
            }
        }

        sameOriginURLsOnly.put(resource, true);

        return sameOriginURLsOnly;
    }

    private HashMap<String, Boolean> mergeHashMapsSimple(HashMap<String, Boolean> map1, HashMap<String, Boolean> map2) {

        HashMap<String, Boolean> resultMap = new HashMap<>();

        resultMap.putAll(map1);
        resultMap.putAll(map2);

        return resultMap;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private HashMap<String, Boolean> mergeHashMaps(HashMap<String, Boolean> map1, HashMap<String, Boolean> map2) {

        HashMap<String, Boolean> smallMap, bigMap;

        if (map1.size() >= map2.size()) {
            bigMap = map1;
            smallMap = map2;
        } else {
            bigMap = map2;
            smallMap = map1;
        }

        HashMap<String, Boolean> resultMap = new HashMap<>();

        for (Object key : bigMap.entrySet()) {
            if (smallMap.containsKey(key.toString())) {
                resultMap.put(key.toString(), smallMap.get(key) || bigMap.get(key));
            } else {
                resultMap.put(key.toString(), bigMap.get(key));
            }
        }

        return resultMap;
    }

}
