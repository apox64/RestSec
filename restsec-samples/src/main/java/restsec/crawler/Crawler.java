package restsec.crawler;

import restsec.AttackSet;

public interface Crawler {

    AttackSet crawl(String targetURL);

}
