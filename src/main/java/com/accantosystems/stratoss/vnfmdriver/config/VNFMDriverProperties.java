package com.accantosystems.stratoss.vnfmdriver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to the VNFM Driver.
 *
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "vnfmdriver")
public class VNFMDriverProperties {

    private final Topics topics = new Topics();
    private final Logging logging = new Logging();

    public Topics getTopics() {
        return topics;
    }

    public Logging getLogging() {
        return logging;
    }

    public static class Topics {
        private String lifecycleResponsesTopic = "lm_vnfc_lifecycle_execution_events";

        public String getLifecycleResponsesTopic() {
            return lifecycleResponsesTopic;
        }

        public void setLifecycleResponsesTopic(String lifecycleResponsesTopic) {
            this.lifecycleResponsesTopic = lifecycleResponsesTopic;
        }
    }

    public static class Logging {
        private int loggingRequestInterceptMaxBodySize = 10000000;

        public int getLoggingRequestInterceptMaxBodySize() {
            return loggingRequestInterceptMaxBodySize;
        }

        public void setLoggingRequestInterceptMaxBodySize(int loggingRequestInterceptMaxBodySize) {
            this.loggingRequestInterceptMaxBodySize = loggingRequestInterceptMaxBodySize;
        }
    }

}
