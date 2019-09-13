package com.accantosystems.stratoss.vnfmdriver.config;

import static springfox.documentation.builders.PathSelectors.regex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Springfox Swagger configuration.
 * <p>
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that case, you can use a
 * specific Spring profile for this class, so that only front-end developers have access to the Swagger view.
 */
@Configuration
@EnableSwagger2
@Import(springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class)
public class SwaggerConfiguration {

    @Value("${info.app.name:VNFM Driver}")
    private String appName;

    @Value("${info.app.description:ETSI SOL003 VNFM Driver}")
    private String appDescription;

    @Value("${info.app.version:0.0.1}")
    private String appVersion;

    @Value("${info.contact.name:Accanto Systems Ltd}")
    private String contactName;

    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    /**
     * Swagger Springfox configuration.
     *
     * @return the Swagger Springfox configuration
     */
    @Bean
    public Docket swaggerSpringfoxDocket() {
        log.debug("Starting Swagger");
        Contact contact = new Contact(contactName, null, null);

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(appName)
                .description(appDescription)
                .version(appVersion)
                .contact(contact)
                .build();

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo).forCodeGeneration(true).genericModelSubstitutes(ResponseEntity.class).select()
                                                      .paths(regex(DEFAULT_INCLUDE_PATTERN)).build();
    }
}
