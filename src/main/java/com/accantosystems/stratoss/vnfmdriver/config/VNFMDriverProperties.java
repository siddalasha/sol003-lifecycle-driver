package com.accantosystems.stratoss.vnfmdriver.config;

import java.time.Duration;

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

    private final Async async = new Async();
    private final Topics topics = new Topics();
    private final Logging logging = new Logging();
    private final PackageManagement packageManagement = new PackageManagement();
    private Duration executionResponseDelay = Duration.ofSeconds(5);
    private Duration lccnPollingDelay = Duration.ofSeconds(10);

    public Async getAsync() {
        return async;
    }

    public Topics getTopics() {
        return topics;
    }

    public Logging getLogging() {
        return logging;
    }

    public PackageManagement getPackageManagement() {
        return packageManagement;
    }

    public Duration getExecutionResponseDelay() {
        return executionResponseDelay;
    }

    public void setExecutionResponseDelay(Duration executionResponseDelay) {
        this.executionResponseDelay = executionResponseDelay;
    }

    public Duration getLccnPollingDelay() {
        return lccnPollingDelay;
    }

    public void setLccnPollingDelay(Duration lccnPollingDelay) {
        this.lccnPollingDelay = lccnPollingDelay;
    }

    public static class Async {
        private int corePoolSize = 4;
        private int maxPoolSize = 32;
        private int queueCapacity = 10000;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
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

    public static class PackageManagement {
        private String packageRepositoryUrl;

        public String getPackageRepositoryUrl() {
            return packageRepositoryUrl;
        }

        public void setPackageRepositoryUrl(String packageRepositoryUrl) {
            this.packageRepositoryUrl = packageRepositoryUrl;
        }
    }

}
