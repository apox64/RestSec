package restsec.crawler;

import restsec.config.Configuration;
import restsec.config.CrawlerType;

public class CrawlerFactory {

    private Configuration config;

    public CrawlerFactory(Configuration configuration) {
        config = configuration;
    }

    public Crawler createCrawler() {

//      CrawlerType crawlerType = config.getCrawlerType();
        CrawlerType crawlerType = null;

        //TODO: Inline config.get...();
        switch (crawlerType) {
            case HATEOAS:
                return new HATEOASCrawler(config.getHATEOASEntryPoint());
            case SWAGGER:
                return new SwaggerFileCrawler(config.getSwaggerFileLocation(), config.getBoolUseAllHTTPMethods());
            default:
                throw new IllegalStateException("Unknown Crawler Type");
        }
    }
}
