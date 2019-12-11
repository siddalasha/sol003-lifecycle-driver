package com.accantosystems.stratoss.vnfmdriver.service;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.accantosystems.stratoss.vnfmdriver.driver.SOL003ResponseErrorHandler;
import com.accantosystems.stratoss.vnfmdriver.driver.VNFMResponseErrorHandler;
import com.accantosystems.stratoss.vnfmdriver.model.AuthenticationType;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;
import com.accantosystems.stratoss.vnfmdriver.security.CookieAuthenticatedRestTemplate;
import com.accantosystems.stratoss.vnfmdriver.security.CookieCredentials;
import com.accantosystems.stratoss.vnfmdriver.utils.DynamicSslCertificateHttpRequestFactory;

@Service("AuthenticatedRestTemplateService")
public class AuthenticatedRestTemplateService {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticatedRestTemplateService.class);

    private final RestTemplateBuilder restTemplateBuilder;
    private final Map<ResourceManagerDeploymentLocation, RestTemplate> cachedRestTemplates = new ConcurrentHashMap<>();

    @Autowired
    public AuthenticatedRestTemplateService(RestTemplateBuilder restTemplateBuilder, VNFMResponseErrorHandler vnfmResponseErrorHandler) {
        logger.info("Initialising RestTemplate configuration");
        this.restTemplateBuilder = restTemplateBuilder.errorHandler(vnfmResponseErrorHandler)
                                                      .requestFactory(DynamicSslCertificateHttpRequestFactory.class)
                                                      .setConnectTimeout(Duration.ofSeconds(10))
                                                      .setReadTimeout(Duration.ofSeconds(30));
    }

    public RestTemplate getRestTemplate(ResourceManagerDeploymentLocation deploymentLocation) {
        if (cachedRestTemplates.containsKey(deploymentLocation)) {
            return cachedRestTemplates.get(deploymentLocation);
        }

        // Double-check we haven't got a cached entry of the same "name", but different properties. If so, remove it.
        cachedRestTemplates.keySet()
                           .stream()
                           .filter(dl -> Objects.equals(dl.getName(), deploymentLocation.getName()))
                           .findFirst()
                           .ifPresent(cachedRestTemplates::remove);

        checkProperty(deploymentLocation, VNFM_SERVER_URL);

        final String authenticationTypeString = deploymentLocation.getProperties().getOrDefault(AUTHENTICATION_TYPE, AuthenticationType.NONE.toString());
        final AuthenticationType authenticationType = AuthenticationType.valueOfIgnoreCase(authenticationTypeString);
        if (authenticationType == null) {
            throw new IllegalArgumentException(String.format("Invalid authentication type specified [%s]", authenticationTypeString));
        }

        final RestTemplate restTemplate;
        switch (authenticationType) {
            case BASIC:
                checkProperty(deploymentLocation, AUTHENTICATION_USERNAME);
                checkProperty(deploymentLocation, AUTHENTICATION_PASSWORD);
                restTemplate = getBasicAuthenticatedRestTemplate(deploymentLocation);
                break;
            case OAUTH2:
                checkProperty(deploymentLocation, AUTHENTICATION_ACCESS_TOKEN_URI);
                checkProperty(deploymentLocation, AUTHENTICATION_CLIENT_ID);
                checkProperty(deploymentLocation, AUTHENTICATION_CLIENT_SECRET);
                restTemplate = getOAuth2RestTemplate(deploymentLocation);
                break;
            case COOKIE:
                checkProperty(deploymentLocation, AUTHENTICATION_URL);
                checkProperty(deploymentLocation, AUTHENTICATION_USERNAME);
                checkProperty(deploymentLocation, AUTHENTICATION_PASSWORD);
                restTemplate = getCookieAuthenticatedRestTemplate(deploymentLocation);
                break;
            default:
                restTemplate = getUnauthenticatedRestTemplate();
        }

        cachedRestTemplates.put(deploymentLocation, restTemplate);
        return restTemplate;
    }

    private void checkProperty(ResourceManagerDeploymentLocation deploymentLocation, String propertyName) {
        if (StringUtils.isEmpty(deploymentLocation.getProperties().get(propertyName))) {
            throw new IllegalArgumentException(String.format("Deployment Location properties must specify a value for [%s]", propertyName));
        }
    }

    private RestTemplate getUnauthenticatedRestTemplate() {
        logger.info("Configuring unauthenticated RestTemplate.");
        return restTemplateBuilder.build();
    }

    private RestTemplate getBasicAuthenticatedRestTemplate(final ResourceManagerDeploymentLocation deploymentLocation) {
        logger.info("Configuring Basic Authentication RestTemplate.");
        return restTemplateBuilder.basicAuthentication(deploymentLocation.getProperties().get(AUTHENTICATION_USERNAME),
                                                       deploymentLocation.getProperties().get(AUTHENTICATION_PASSWORD))
                                  .build();
    }

    private RestTemplate getOAuth2RestTemplate(final ResourceManagerDeploymentLocation deploymentLocation) {
        final ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
        resourceDetails.setAccessTokenUri(deploymentLocation.getProperties().get(AUTHENTICATION_ACCESS_TOKEN_URI));
        resourceDetails.setClientId(deploymentLocation.getProperties().get(AUTHENTICATION_CLIENT_ID));
        resourceDetails.setClientSecret(deploymentLocation.getProperties().get(AUTHENTICATION_CLIENT_SECRET));
        resourceDetails.setGrantType(deploymentLocation.getProperties().getOrDefault(AUTHENTICATION_GRANT_TYPE, "client_credentials"));
        if (StringUtils.hasText(deploymentLocation.getProperties().get(AUTHENTICATION_SCOPE))) {
            resourceDetails.setScope(Arrays.asList(deploymentLocation.getProperties().get(AUTHENTICATION_SCOPE).split(",")));
        }

        logger.info("Configuring OAuth2 authenticated RestTemplate.");
        return restTemplateBuilder.configure(new OAuth2RestTemplate(resourceDetails));
    }

    private RestTemplate getCookieAuthenticatedRestTemplate(final ResourceManagerDeploymentLocation deploymentLocation) {
        CookieCredentials cookieCredentials = new CookieCredentials();
        cookieCredentials.setAuthenticationUrl(deploymentLocation.getProperties().get(AUTHENTICATION_URL));
        cookieCredentials.setUsernameTokenName(deploymentLocation.getProperties().getOrDefault(AUTHENTICATION_USERNAME_TOKEN_NAME, "IDToken1"));
        cookieCredentials.setPasswordTokenName(deploymentLocation.getProperties().getOrDefault(AUTHENTICATION_PASSWORD_TOKEN_NAME, "IDToken2"));
        cookieCredentials.setUsername(deploymentLocation.getProperties().get(AUTHENTICATION_USERNAME));
        cookieCredentials.setPassword(deploymentLocation.getProperties().get(AUTHENTICATION_PASSWORD));

        logger.info("Configuring Cookie authenticated RestTemplate.");
        return restTemplateBuilder.configure(new CookieAuthenticatedRestTemplate(cookieCredentials));
    }

}
