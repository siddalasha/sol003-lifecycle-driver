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
import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;
import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails.AuthenticationType;
import com.accantosystems.stratoss.vnfmdriver.model.alm.ResourceManagerDeploymentLocation;
import com.accantosystems.stratoss.vnfmdriver.security.CookieCredentials;
import com.accantosystems.stratoss.vnfmdriver.security.CookieAuthenticatedRestTemplate;

@Service("AuthenticatedRestTemplateService")
public class AuthenticatedRestTemplateService {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticatedRestTemplateService.class);

    private final RestTemplateBuilder restTemplateBuilder;
    private final Map<ResourceManagerDeploymentLocation, RestTemplate> cachedRestTemplates = new ConcurrentHashMap<>();

    @Autowired
    public AuthenticatedRestTemplateService(RestTemplateBuilder restTemplateBuilder, SOL003ResponseErrorHandler sol003ResponseErrorHandler) {
        logger.info("Initialising RestTemplate configuration");
        this.restTemplateBuilder = restTemplateBuilder.errorHandler(sol003ResponseErrorHandler)
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

        final RestTemplate restTemplate;
        final VNFMConnectionDetails connectionDetails = getConnectionDetails(deploymentLocation);
        switch (connectionDetails.getAuthenticationType()) {
            case BASIC:
                restTemplate = getBasicAuthenticatedRestTemplate(connectionDetails);
                break;
            case OAUTH2:
                restTemplate = getOAuth2RestTemplate(connectionDetails);
                break;
            case SESSION:
                restTemplate = getSessionBasedRestTemplate(connectionDetails);
                break;
            default:
                restTemplate = getUnauthenticatedRestTemplate();
        }

        cachedRestTemplates.put(deploymentLocation, restTemplate);
        return restTemplate;
    }

    private VNFMConnectionDetails getConnectionDetails(ResourceManagerDeploymentLocation deploymentLocation) {
        final String vnfmServerUrl = deploymentLocation.getProperties().get(VNFM_SERVER_URL);
        if (StringUtils.isEmpty(vnfmServerUrl)) {
            throw new IllegalArgumentException(String.format("Deployment Location must specify a value for [%s]", VNFM_SERVER_URL));
        }
        final String authenticationTypeString = deploymentLocation.getProperties().getOrDefault(AUTHENTICATION_TYPE, AuthenticationType.NONE.toString());
        final AuthenticationType authenticationType = AuthenticationType.valueOfIgnoreCase(authenticationTypeString);
        if (authenticationType == null) {
            throw new IllegalArgumentException(String.format("Invalid authentication type specified [%s]", authenticationTypeString));
        }

        final VNFMConnectionDetails vnfmConnectionDetails = new VNFMConnectionDetails(vnfmServerUrl, authenticationType);

        switch(authenticationType) {
            case BASIC:
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_USERNAME, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_PASSWORD, true);
                break;
            case OAUTH2:
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_ACCESS_TOKEN_URI, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_CLIENT_ID, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_CLIENT_SECRET, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_GRANT_TYPE, false);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_SCOPE, false);
                break;
            case SESSION:
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_URL, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_USERNAME_TOKEN_NAME, false);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_PASSWORD_TOKEN_NAME, false);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_USERNAME, true);
                copyProperty(vnfmConnectionDetails, deploymentLocation, AUTHENTICATION_PASSWORD, true);
                break;
        }

        return vnfmConnectionDetails;
    }

    private void copyProperty(VNFMConnectionDetails vnfmConnectionDetails, ResourceManagerDeploymentLocation deploymentLocation, String propertyName, boolean throwIfEmpty) {
        final String propertyValue = deploymentLocation.getProperties().get(propertyName);
        if (throwIfEmpty && StringUtils.isEmpty(propertyValue)) {
            throw new IllegalArgumentException(String.format("Deployment Location properties must specify a value for [%s]", propertyName));
        } else if (StringUtils.hasText(propertyValue)) {
            vnfmConnectionDetails.getAuthenticationProperties().put(propertyName, propertyValue);
        }
    }

    private RestTemplate getUnauthenticatedRestTemplate() {
        logger.info("Configuring unauthenticated RestTemplate.");
        return restTemplateBuilder.build();
    }

    private RestTemplate getBasicAuthenticatedRestTemplate(final VNFMConnectionDetails connectionDetails) {
        logger.info("Configuring Basic Authentication RestTemplate.");
        return restTemplateBuilder.basicAuthentication(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_USERNAME),
                                                       connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_PASSWORD))
                                  .build();
    }

    private RestTemplate getOAuth2RestTemplate(final VNFMConnectionDetails connectionDetails) {
        final ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
        resourceDetails.setAccessTokenUri(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_ACCESS_TOKEN_URI));
        resourceDetails.setClientId(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_CLIENT_ID));
        resourceDetails.setClientSecret(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_CLIENT_SECRET));
        resourceDetails.setGrantType(connectionDetails.getAuthenticationProperties().getOrDefault(AUTHENTICATION_GRANT_TYPE, "client_credentials"));
        if (StringUtils.hasText(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_SCOPE))) {
            resourceDetails.setScope(Arrays.asList(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_SCOPE).split(",")));
        }

        logger.info("Configuring OAuth2 authenticated RestTemplate.");
        return restTemplateBuilder.configure(new OAuth2RestTemplate(resourceDetails));
    }

    private RestTemplate getSessionBasedRestTemplate(final VNFMConnectionDetails connectionDetails) {
        CookieCredentials cookieCredentials = new CookieCredentials();
        cookieCredentials.setAuthenticationUrl(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_URL));
        cookieCredentials.setUsernameTokenName(connectionDetails.getAuthenticationProperties().getOrDefault(AUTHENTICATION_USERNAME_TOKEN_NAME, "IDToken1"));
        cookieCredentials.setPasswordTokenName(connectionDetails.getAuthenticationProperties().getOrDefault(AUTHENTICATION_PASSWORD_TOKEN_NAME, "IDToken2"));
        cookieCredentials.setUsername(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_USERNAME));
        cookieCredentials.setPassword(connectionDetails.getAuthenticationProperties().get(AUTHENTICATION_PASSWORD));

        logger.info("Configuring Cookie authenticated RestTemplate.");
        return restTemplateBuilder.configure(new CookieAuthenticatedRestTemplate(cookieCredentials));
    }

}
