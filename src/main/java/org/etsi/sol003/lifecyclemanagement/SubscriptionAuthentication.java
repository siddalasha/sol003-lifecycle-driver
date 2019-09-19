package org.etsi.sol003.lifecyclemanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Represents authorization requirements.
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Represents authorization requirements.")
public class SubscriptionAuthentication {

    @ApiModelProperty(name = "Authentication Type", required = true, notes = "Defines the type of Authentication / Authorization to use when sending a notification.")
    private AuthType authType;
    @ApiModelProperty(name = "Basic Authentication Parameters", notes = "Parameters for authentication/authorization using BASIC. Shall be present if authType is \"BASIC\" and the contained information has not been provisioned out of band. Shall be absent otherwise.")
    private BasicParameters paramsBasic;
    @ApiModelProperty(name = "OAuth2 Client Credential Parameters", notes = "Parameters for authentication/authorization using OAUTH2_CLIENT_CREDENTIALS. Shall be present if authType is \"OAUTH2_CLIENT_CREDENTIALS\" and the contained information has not been provisioned out of band. Shall be absent otherwise.")
    private OAuth2ClientCredentialParameters paramsOauth2ClientCredentials;

    public enum AuthType {
        /**
         * In every POST request that sends a notification, use HTTP Basic authentication with the client credentials.
         */
        BASIC,
        /**
         * In every POST request that sends a notification, use an OAuth 2.0 Bearer token, obtained using the client credentials grant type
         */
        OAUTH2_CLIENT_CREDENTIALS
    }

    /**
     * Parameters for authentication/authorization using BASIC.
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "Parameters for authentication/authorization using BASIC.")
    public static class BasicParameters {

        @ApiModelProperty(name = "Username", notes = "Username to be used in HTTP Basic authentication. Shall be present if it has not been provisioned out of band.")
        private String username;
        @ApiModelProperty(name = "Password", notes = "Password to be used in HTTP Basic authentication. Shall be present if it has not been provisioned out of band.")
        private String password;

    }

    /**
     * Parameters for authentication/authorization using OAUTH2_CLIENT_CREDENTIALS.
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "Parameters for authentication/authorization using OAUTH2_CLIENT_CREDENTIALS.")
    public static class OAuth2ClientCredentialParameters {

        @ApiModelProperty(name = "Client Id", notes = "Client identifier to be used in the access token request of the OAuth 2.0 client credentials grant type. Shall be present if it has not been provisioned out of band.")
        private String clientId;
        @ApiModelProperty(name = "Client Password", notes = "Client password to be used in the access token request of the OAuth 2.0 client credentials grant type. Shall be present if it has not been provisioned out of band.")
        private String clientPassword;
        @ApiModelProperty(name = "Token Endpoint", dataType = "URI", notes = "The token endpoint from which the access token can be obtained. Shall be present if it has not been provisioned out of band.")
        private String tokenEndpoint;

    }

}
