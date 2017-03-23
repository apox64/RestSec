package restsec.crawler;

import restsec.AttackSet;

public interface Crawler {

    void crawl();

    AttackSet crawl(String target);

}
