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
    private Duration lcmOpOccPollingDelay = Duration.ofSeconds(10);

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

    public Duration getLcmOpOccPollingDelay() {
        return lcmOpOccPollingDelay;
    }

    public void setLcmOpOccPollingDelay(Duration lcmOpOccPollingDelay) {
        this.lcmOpOccPollingDelay = lcmOpOccPollingDelay;
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
        private String lcmOpOccPollingTopic = "lcm_op_occ_polling_requests";

        public String getLifecycleResponsesTopic() {
            return lifecycleResponsesTopic;
        }

        public void setLifecycleResponsesTopic(String lifecycleResponsesTopic) {
            this.lifecycleResponsesTopic = lifecycleResponsesTopic;
        }

        public String getLcmOpOccPollingTopic() {
            return lcmOpOccPollingTopic;
        }

        public void setLcmOpOccPollingTopic(String lcmOpOccPollingTopic) {
            this.lcmOpOccPollingTopic = lcmOpOccPollingTopic;
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
        private String imageArtifactFilter;

        public String getPackageRepositoryUrl() {
            return packageRepositoryUrl;
        }

        public void setPackageRepositoryUrl(String packageRepositoryUrl) {
            this.packageRepositoryUrl = packageRepositoryUrl;
        }

        public String getImageArtifactFilter() {
            return imageArtifactFilter;
        }

        public void setImageArtifactFilter(String imageArtifactFilter) {
            this.imageArtifactFilter = imageArtifactFilter;
        }

    }

}
