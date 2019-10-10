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
    private final PackageManagement packageManagement = new PackageManagement();

    public Topics getTopics() {
        return topics;
    }

    public Logging getLogging() {
        return logging;
    }

    public PackageManagement getPackageManagement() {
        return packageManagement;
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
