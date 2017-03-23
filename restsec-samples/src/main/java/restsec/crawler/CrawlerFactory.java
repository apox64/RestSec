package restsec.crawler;

import restsec.config.Configuration;

public class CrawlerFactory {

    private Configuration config;

    public CrawlerFactory(Configuration configuration) {
        config = configuration;
    }

    public Crawler createCrawler() {

        switch (config.getCrawlerType()) {
            case HATEOAS:
                return new HATEOASCrawler(config.getHATEOASEntryPoint());
            case SWAGGER:
                return new SwaggerFileCrawler(config.getSwaggerFileLocation(), config.getBoolUseAllHTTPMethods());
            default:
                throw new IllegalStateException("Unknown Crawler Type");
        }
    }
}
