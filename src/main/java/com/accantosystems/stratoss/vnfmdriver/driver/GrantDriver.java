package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.*;

import java.time.Duration;
import java.util.Arrays;

import org.etsi.sol003.granting.Grant;
import org.etsi.sol003.granting.GrantRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties.Authentication;
import com.accantosystems.stratoss.vnfmdriver.model.AuthenticationType;
import com.accantosystems.stratoss.vnfmdriver.service.GrantRejectedException;
import com.accantosystems.stratoss.vnfmdriver.utils.DynamicSslCertificateHttpRequestFactory;

/**
 * Driver implementing the ETSI SOL003 Grant interface
 */
@Service("GrantDriver")
public class GrantDriver {

    private final static Logger logger = LoggerFactory.getLogger(GrantDriver.class);

    private final static String API_CONTEXT_ROOT = "/grant/v1";
    private final static String API_PATH_GRANTS = "/grants";

    private final VNFMDriverProperties vnfmDriverProperties;
    private final RestTemplate authenticatedRestTemplate;

    public GrantDriver(VNFMDriverProperties vnfmDriverProperties, RestTemplateBuilder restTemplateBuilder, GrantResponseErrorHandler grantResponseErrorHandler) {
        this.vnfmDriverProperties = vnfmDriverProperties;
        this.authenticatedRestTemplate = getAuthenticatedRestTemplate(vnfmDriverProperties, restTemplateBuilder, grantResponseErrorHandler);
    }

    /**
     * Requests a grant for a particular VNF lifecycle operation.
     *
     * <ul>
     * <li>Sends GrantRequest message via HTTP POST to /grants</li>
     * <li>If grant provider supports the synchronous path, should receive 201 Created response with a {@link Grant} resource as the response body</li>
     * <li>If grant provider supports the asynchronous path, should receive 202 Accepted response with no response body and Location header to poll for grant response</li>
     * </ul>
     * 
     * @param grantRequest
     *            the request for permission from the NFVO to perform a particular VNF lifecycle operation.
     * @return
     * @throws GrantRejectedException
     *             if the grant request was rejected
     * @throws SOL003ResponseException
     *             if there are any errors creating the Grant request
     */
    public Grant requestGrant(GrantRequest grantRequest) throws GrantRejectedException, SOL003ResponseException {

        final String url = vnfmDriverProperties.getGrant().getProvider().getUrl() + API_CONTEXT_ROOT + API_PATH_GRANTS;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<GrantRequest> requestEntity = new HttpEntity<>(grantRequest, headers);

        final ResponseEntity<Grant> responseEntity = authenticatedRestTemplate.exchange(url, HttpMethod.POST, requestEntity, Grant.class);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            // synchronous response - should find grant resource in body
            if (responseEntity.getBody() == null) {
                throw new SOL003ResponseException("No response body");
            }
            return responseEntity.getBody();
        } else if (HttpStatus.ACCEPTED.equals(responseEntity.getStatusCode())) {
            // asynchronous response - need to poll for grant resource, no body expected
            if (responseEntity.getBody() != null) {
                throw new SOL003ResponseException("No response body expected");
            }
            return null;
        } else {
            throw new SOL003ResponseException(String.format("Invalid status code [%s] received", responseEntity.getStatusCode()));
        }
    }

    /**
     * Reads a grant.
     *
     * <ul>
     * <li>Sends HTTP GET request to /grants/grantId</li>
     * <li>If grant has been accepted, should receive 200 OK response with a {@link Grant} resource as the response body</li>
     * <li>If grant decision is still pending, should receive 202 Accepted response with no response body</li>
     * </ul>
     * 
     * @param grantRequest
     *            the request for permission from the NFVO to perform a particular VNF lifecycle operation.
     * @return
     * @throws GrantRejectedException
     *             if the grant request was rejected
     * @throws SOL003ResponseException
     *             if there are any errors returning the Grant resource
     */
    public Grant getGrant(String grantId) throws GrantRejectedException, SOL003ResponseException {
        final String url = vnfmDriverProperties.getGrant().getProvider().getUrl() + API_CONTEXT_ROOT + API_PATH_GRANTS + "/{grantId}";

        final ResponseEntity<Grant> responseEntity = authenticatedRestTemplate.getForEntity(url, Grant.class, grantId);

        if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            // grant was accepted and grant resource is available and should be found in body
            if (responseEntity.getBody() == null) {
                throw new SOL003ResponseException("No response body");
            }
            return responseEntity.getBody();
        } else if (HttpStatus.ACCEPTED.equals(responseEntity.getStatusCode())) {
            // grant not yet accepted nor rejected - should continue to poll until grant resource available
            if (responseEntity.getBody() != null) {
                throw new SOL003ResponseException("No response body expected");
            }
            return null;
        } else {
            throw new SOL003ResponseException(String.format("Invalid status code [%s] received", responseEntity.getStatusCode()));
        }

    }

    private RestTemplate getAuthenticatedRestTemplate(VNFMDriverProperties vnfmDriverProperties, RestTemplateBuilder restTemplateBuilder, GrantResponseErrorHandler grantResponseErrorHandler) {
        RestTemplateBuilder customRestTemplateBuilder = configureRestTemplateBuilder(restTemplateBuilder, grantResponseErrorHandler);

        Authentication authenticationProperties = vnfmDriverProperties.getGrant().getProvider().getAuthentication();
        final String authenticationTypeString = authenticationProperties.getAuthenticationType();
        final AuthenticationType authenticationType = AuthenticationType.valueOfIgnoreCase(authenticationTypeString);
        if (authenticationType == null) {
            throw new IllegalArgumentException(String.format("Invalid authentication type specified [%s]", authenticationTypeString));
        }

        RestTemplate authenticatedRestTemplate;
        switch (authenticationType) {
        case BASIC:
            String username = checkProperty(authenticationProperties.getUsername(), AUTHENTICATION_USERNAME);
            String password = checkProperty(authenticationProperties.getPassword(), AUTHENTICATION_PASSWORD);

            authenticatedRestTemplate = getBasicAuthenticatedRestTemplate(customRestTemplateBuilder, username, password);
            break;
        case OAUTH2:
            String accessTokenUri = checkProperty(authenticationProperties.getAccessTokenUri(), AUTHENTICATION_ACCESS_TOKEN_URI);
            String clientId = checkProperty(authenticationProperties.getClientId(), AUTHENTICATION_CLIENT_ID);
            String clientSecret = checkProperty(authenticationProperties.getClientSecret(), AUTHENTICATION_CLIENT_SECRET);

            authenticatedRestTemplate = getOAuth2RestTemplate(customRestTemplateBuilder, authenticationProperties, accessTokenUri, clientId, clientSecret);

            break;
        case COOKIE:
            throw new UnsupportedOperationException("Attempting to use Cookie-based authentication which is unsupported for the grant provider.");
        default:
            authenticatedRestTemplate = getUnauthenticatedRestTemplate(customRestTemplateBuilder);
        }
        return authenticatedRestTemplate;
    }

    private RestTemplate getUnauthenticatedRestTemplate(RestTemplateBuilder customRestTemplateBuilder) {
        logger.info("Configuring unauthenticated RestTemplate.");
        return customRestTemplateBuilder.build();
    }

    private RestTemplate getBasicAuthenticatedRestTemplate(RestTemplateBuilder customRestTemplateBuilder, String username, String password) {
        logger.info("Configuring Basic Authentication RestTemplate.");
        return customRestTemplateBuilder.basicAuthentication(username, password)
                .build();
    }

    private RestTemplate getOAuth2RestTemplate(RestTemplateBuilder customRestTemplateBuilder, Authentication authenticationProperties, String accessTokenUri, String clientId, String clientSecret) {
        final ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
        resourceDetails.setAccessTokenUri(accessTokenUri);
        resourceDetails.setClientId(clientId);
        resourceDetails.setClientSecret(clientSecret);
        resourceDetails.setGrantType("client_credentials");
        if (StringUtils.hasText(authenticationProperties.getScope())) {
            resourceDetails.setScope(Arrays.asList(authenticationProperties.getScope().split(",")));
        }

        logger.info("Configuring OAuth2 authenticated RestTemplate.");
        return customRestTemplateBuilder.configure(new OAuth2RestTemplate(resourceDetails));
    }

    private RestTemplateBuilder configureRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder, GrantResponseErrorHandler grantResponseErrorHandler) {
        RestTemplateBuilder customRestTemplateBuilder = restTemplateBuilder.errorHandler(grantResponseErrorHandler)
                .requestFactory(DynamicSslCertificateHttpRequestFactory.class)
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30));
        logger.info("Initialising RestTemplate configuration");
        return customRestTemplateBuilder;
    }

    private String checkProperty(String property, String propertyName) {
        if (StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException(String.format("Must specify a property value for [%s]", propertyName));
        }
        return propertyName;
    }
}
