package restsec.scanner;

import restsec.AttackSet;

public interface Scanner {

    void scan(String targetURL, AttackSet attackSet);

}
