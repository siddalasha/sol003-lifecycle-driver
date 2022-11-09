package com.accantosystems.stratoss.vnfmdriver.config;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Springfox Swagger configuration.
 * <p>
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that case, you can use a
 * specific Spring profile for this class, so that only front-end developers have access to the Swagger view.
 */
@Configuration
public class SwaggerConfiguration {

    @Value("${info.app.name:VNFM Driver}")
    private String appName;

    @Value("${info.app.description:ETSI SOL003 VNFM Driver}")
    private String appDescription;

    @Value("${info.app.version:0.0.1}")
    private String appVersion;

    @Value("${info.contact.name:Accanto Systems Ltd}")
    private String contactName;

    //@Value("${swagger.server-url}")
    private String serverUrl;

    /**
     * Swagger Springfox configuration.
     *
     * @return the Swagger Springfox configuration
     */
    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations());;
        };
    }
        
}
    