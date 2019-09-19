package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_PASSWORD;
import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_USERNAME;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.sol003.lifecyclemanagement.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;

/**
 * Driver implementing the ETSI SOL003 Lifecycle Management interface
 * <p>
 * Endpoints expected to be found under the following structure
 *
 * <ul>
 *     <li>{apiRoot}/vnflcm/v1
 *     <li><ul>
 *         <li>/vnf_instances</li>
 *         <li><ul>
 *             <li>/{vnfInstanceId}
 *             <li><ul>
 *                 <li>/instantiate</li>
 *                 <li>/scale</li>
 *                 <li>/scale_to_level</li>
 *                 <li>/change_flavour</li>
 *                 <li>/operate</li>
 *                 <li>/heal</li>
 *                 <li>/change_ext_conn</li>
 *                 <li>/terminate</li>
 *             </ul></li>
 *         </ul></li>
 *     </ul></li>
 *     <li><ul>
 *         <li>/vnf_lcm_op_occs</li>
 *         <li><ul>
 *             <li>/{vnfLcmOpOccId}</li>
 *             <li><ul>
 *                 <li>/retry</li>
 *                 <li>/rollback</li>
 *                 <li>/fail</li>
 *                 <li>/cancel</li>
 *             </ul></li>
 *         </ul></li>
 *     </ul></li>
 *     <li><ul>
 *         <li>/subscriptions</li>
 *         <li><ul>
 *             <li>/{subscriptionId}</li>
 *         </ul></li>
 *     </ul></li>
 * </ul>
 */
@Service("VNFLifecycleManagementDriver")
public class VNFLifecycleManagementDriver {

    private final static Logger logger = LoggerFactory.getLogger(VNFLifecycleManagementDriver.class);

    private final static String API_CONTEXT_ROOT = "/vnflcm/v1";
    private final static String API_PREFIX_VNF_INSTANCES = "/vnf_instances";
    private final static String API_PREFIX_OP_OCCURRENCES = "/vnf_lcm_op_occs";
    private final static String API_PREFIX_SUBSCRIPTIONS = "/subscriptions";

    private final RestTemplate restTemplate;

    @Autowired
    public VNFLifecycleManagementDriver(RestTemplateBuilder restTemplateBuilder, SOL003ResponseErrorHandler sol003ResponseErrorHandler) {
        this.restTemplate = restTemplateBuilder.errorHandler(sol003ResponseErrorHandler)
                                               .build();
    }

    /**
     * Creates a new VNF instance record in the VNFM
     *
     * <ul>
     *     <li>Sends CreateVnfRequest message via HTTP POST to /vnf_instances</li>
     *     <li>Gets 201 Created response with a {@link VnfInstance} record as the response body</li>
     *     <li>Postcondition: VNF instance created in NOT_INSTANTIATED state</li>
     *     <li>Out of band {@link VnfIdentifierCreationNotification} should be received after this returns</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param createVnfRequest      request information
     * @return newly created {@link VnfInstance} record
     * @throws SOL003ResponseException if there are any errors creating the VNF instance
     */
    public VnfInstance createVnfInstance(final VNFMConnectionDetails vnfmConnectionDetails, final CreateVnfRequest createVnfRequest) throws SOL003ResponseException {
        final String url = vnfmConnectionDetails.getApiRoot() + API_CONTEXT_ROOT + API_PREFIX_VNF_INSTANCES;
        final HttpHeaders headers = getHttpHeaders(vnfmConnectionDetails);
        final HttpEntity<CreateVnfRequest> requestEntity = new HttpEntity<>(createVnfRequest, headers);

        final ResponseEntity<VnfInstance> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, VnfInstance.class);

        // "Location" header also includes URI of the created instance
        checkResponseEntityMatches(responseEntity, HttpStatus.CREATED, true);
        return responseEntity.getBody();
    }

    /**
     * Deletes a VNF instance record from the VNFM
     *
     * <ul>
     *     <li>Precondition: VNF instance in NOT_INSTANTIATED state</li>
     *     <li>Sends HTTP DELETE request to /vnf_instances/{vnfInstanceId}</li>
     *     <li>Gets 204 No Content response</li>
     *     <li>Postcondition: VNF instance resource removed</li>
     *     <li>Out of band {@link VnfIdentifierDeletionNotification} should be received after this returns</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier of the {@link VnfInstance} record to delete
     * @throws SOL003ResponseException if there are any errors deleting the VNF instance
     */
    public void deleteVnfInstance(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId) throws SOL003ResponseException {
        final String url = vnfmConnectionDetails.getApiRoot() + API_CONTEXT_ROOT + API_PREFIX_VNF_INSTANCES + "/{vnfInstanceId}";
        final HttpHeaders headers = getHttpHeaders(vnfmConnectionDetails);
        final HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        final Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("vnfInstanceId", vnfInstanceId);

        final ResponseEntity<Void> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class, uriVariables);

        checkResponseEntityMatches(responseEntity, HttpStatus.NO_CONTENT, false);
    }

    /*
       - Precondition: <<Precondition>>
       - Send <<RequestStructure>> via HTTP POST to /vnf_instances/{vnfInstanceId}/<<Task>>
       - Gets 202 Accepted response
       - Receive out of band VnfLcmOperationOccurrenceNotification (STARTING)
       - Granting exchange (NOTE: this *may* occur before the STARTING notification above)
       - Receive out of band VnfLcmOperationOccurrenceNotification (PROCESSING)
         - Optionally could perform HTTP GET on /vnf_lcm_op_occs/{vnfLcmOpOccId}
           - Gets 200 OK with response body of VnfLcmOpOcc:operationState=PROCESSING
       - Receive out of band VnfLcmOperationOccurrenceNotification (COMPLETED)
         - Optionally could perform HTTP GET on /vnf_lcm_op_occs/{vnfLcmOpOccId}
           - Gets 200 OK with response body of VnfLcmOpOcc:operationState=COMPLETED
       - Postcondition: <<Postcondition>>
     */

    /**
     * Submits an operation to the VNFM to instantiate an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance created and in NOT_INSTANTIATED state</li>
     *     <li>Sends an {@link InstantiateVnfRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/instantiate</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance in INSTANTIATED state</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param instantiateVnfRequest request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String instantiateVnf(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final InstantiateVnfRequest instantiateVnfRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "instantiate", instantiateVnfRequest);
    }

    /**
     * Submits an operation to the VNFM to scale an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link ScaleVnfRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/scale</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state and VNF was scaled</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param scaleVnfRequest       request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String scaleVnf(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final ScaleVnfRequest scaleVnfRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "scale", scaleVnfRequest);
    }

    /**
     * Submits an operation to the VNFM to scale an existing VNF instance to the specified level
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link ScaleVnfToLevelRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/scale_to_level</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state and VNF was scaled</li>
     * </ul>
     *
     * @param vnfmConnectionDetails  VNFM connection details
     * @param vnfInstanceId          Identifier for the {@link VnfInstance} to perform the operation on
     * @param scaleVnfToLevelRequest request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String scaleVnfToLevel(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final ScaleVnfToLevelRequest scaleVnfToLevelRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "scale_to_level", scaleVnfToLevelRequest);
    }

    /**
     * Submits an operation to the VNFM to change the deployment flavour of an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link ChangeVnfFlavourRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/change_flavour</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state and VNF deployment flavour changed</li>
     * </ul>
     *
     * @param vnfmConnectionDetails   VNFM connection details
     * @param vnfInstanceId           Identifier for the {@link VnfInstance} to perform the operation on
     * @param changeVnfFlavourRequest request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String changeVnfFlavour(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final ChangeVnfFlavourRequest changeVnfFlavourRequest)
            throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "change_flavour", changeVnfFlavourRequest);
    }

    /**
     * Submits an operation to the VNFM to start or stop (operate) an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends an {@link OperateVnfRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/operate</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state and VNF operational state changed</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param operateVnfRequest     request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String operateVnf(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final OperateVnfRequest operateVnfRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "operate", operateVnfRequest);
    }

    /**
     * Submits an operation to the VNFM to heal an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link HealVnfRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/heal</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param healVnfRequest        request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String healVnf(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final HealVnfRequest healVnfRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "heal", healVnfRequest);
    }

    /**
     * Submits an operation to the VNFM to change the external connectivity of an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link ChangeExtVnfConnectivityRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/change_ext_conn</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance still in INSTANTIATED state and external connectivity of the VNF is changed</li>
     * </ul>
     *
     * @param vnfmConnectionDetails           VNFM connection details
     * @param vnfInstanceId                   Identifier for the {@link VnfInstance} to perform the operation on
     * @param changeExtVnfConnectivityRequest request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String changeExtVnfConnectivity(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final ChangeExtVnfConnectivityRequest changeExtVnfConnectivityRequest)
            throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "change_ext_conn", changeExtVnfConnectivityRequest);
    }

    /**
     * Submits an operation to the VNFM to terminate an existing VNF instance
     *
     * <ul>
     *     <li>Precondition: VNF instance in INSTANTIATED state</li>
     *     <li>Sends a {@link TerminateVnfRequest} via HTTP POST to /vnf_instances/{vnfInstanceId}/terminate</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     *     <li>Postcondition: VNF instance in NOT_INSTANTIATED state</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param terminateVnfRequest   request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    public String terminateVnf(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final TerminateVnfRequest terminateVnfRequest) throws SOL003ResponseException {
        return callVnfLcmOperation(vnfmConnectionDetails, vnfInstanceId, "terminate", terminateVnfRequest);
    }

    /**
     * Submits an operation to the VNFM on an existing VNF instance
     *
     * <ul>
     *     <li>Sends &lt;&lt;RequestStructure&gt;&gt; via HTTP POST to /vnf_instances/{vnfInstanceId}/&lt;&lt;Task&gt;&gt;</li>
     *     <li>Gets 202 Accepted response with Location header to the {@link VnfLcmOpOcc} record</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfInstanceId         Identifier for the {@link VnfInstance} to perform the operation on
     * @param operationName         Name of the operation to perform (forms the URI)
     * @param operationRequest      request information
     * @return newly created {@link VnfLcmOpOcc} record identifier
     * @throws SOL003ResponseException if there are any errors creating the operation request
     */
    private String callVnfLcmOperation(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfInstanceId, final String operationName, final Object operationRequest)
            throws SOL003ResponseException {
        final String url = vnfmConnectionDetails.getApiRoot() + API_CONTEXT_ROOT + API_PREFIX_VNF_INSTANCES + "/" + vnfInstanceId + "/" + operationName;
        final HttpHeaders headers = getHttpHeaders(vnfmConnectionDetails);
        final HttpEntity<Object> requestEntity = new HttpEntity<>(operationRequest, headers);

        final ResponseEntity<VnfInstance> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, VnfInstance.class);

        checkResponseEntityMatches(responseEntity, HttpStatus.ACCEPTED, false);
        // "Location" header contains URI of the created VnfLcmOpOcc record
        final URI location = responseEntity.getHeaders().getLocation();
        if (location == null) {
            throw new SOL003ResponseException("No Location header found");
        }
        // Return the VnfLcmOpOccId, which is the last part of the path
        return location.getPath().substring(location.getPath().lastIndexOf("/") + 1);
    }

    /**
     * Performs a query to retrieve matching VNF lifecycle operation occurrence records
     *
     * <ul>
     *     <li>Sends HTTP GET request to /vnf_lcm_op_occs</li>
     *     <li>Gets 200 OK response with an array of {@link VnfLcmOpOcc} records as the response body</li>
     * </ul>
     * <p>
     * The following query parameters can be supplied to the request
     * <ul>
     *     <li>(attribute-based filtering) - e.g. ?operationState=PROCESSING</li>
     *     <li>all_fields</li>
     *     <li>fields=&lt;comma-separated list&gt;</li>
     *     <li>exclude_fields=&lt;comma-separated list&gt;</li>
     *     <li>exclude_default</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @return list of matching {@link VnfLcmOpOcc} records
     * @throws SOL003ResponseException if there are any errors performing the query
     */
    public List<VnfLcmOpOcc> queryAllLifecycleOperationOccurrences(final VNFMConnectionDetails vnfmConnectionDetails) throws SOL003ResponseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Retrieve a single VNF lifecycle operation occurrence record, found by its identifier
     *
     * <ul>
     *     <li>Sends HTTP GET request to /vnf_lcm_op_occs/{vnfLcmOpOccId}</li>
     *     <li>Gets 200 OK response with a {@link VnfLcmOpOcc} record as the response body</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param vnfLcmOpOccId         Identifier for the {@link VnfLcmOpOcc} record
     * @return matching {@link VnfLcmOpOcc} record
     * @throws SOL003ResponseException if there are any errors performing the query
     */
    public VnfLcmOpOcc queryLifecycleOperationOccurrence(final VNFMConnectionDetails vnfmConnectionDetails, final String vnfLcmOpOccId) throws SOL003ResponseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Creates a subscription for lifecycle change notifications
     *
     * <ul>
     *     <li>Sends LccnSubscriptionRequest message via HTTP POST to /subscriptions</li>
     *     <li>Optionally the VNFM may test the notification endpoint here</li>
     *     <li>Gets 201 Created response with a {@link LccnSubscription} record as the response body</li>
     * </ul>
     *
     * @param vnfmConnectionDetails   VNFM connection details
     * @param lccnSubscriptionRequest details of the requested subscription
     * @return newly created {@link LccnSubscription} record
     * @throws SOL003ResponseException if there are any errors creating the subscription
     */
    public LccnSubscription createLifecycleSubscription(final VNFMConnectionDetails vnfmConnectionDetails, final LccnSubscriptionRequest lccnSubscriptionRequest) throws SOL003ResponseException {
        final String url = vnfmConnectionDetails.getApiRoot() + API_CONTEXT_ROOT + API_PREFIX_SUBSCRIPTIONS;
        final HttpHeaders headers = getHttpHeaders(vnfmConnectionDetails);
        final HttpEntity<LccnSubscriptionRequest> requestEntity = new HttpEntity<>(lccnSubscriptionRequest, headers);

        final ResponseEntity<LccnSubscription> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, LccnSubscription.class);

        // "Location" header also includes URI of the created instance
        checkResponseEntityMatches(responseEntity, HttpStatus.CREATED, true);
        return responseEntity.getBody();
    }

    /**
     * Performs a query to retrieve matching lifecycle change notification subscriptions
     *
     * <ul>
     *     <li>Sends HTTP GET request to /subscriptions</li>
     *     <li>Gets 200 OK response with an array of {@link LccnSubscription} records as the response body</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @return list of matching {@link LccnSubscription} records
     * @throws SOL003ResponseException if there are any errors performing the query
     */
    public List<LccnSubscription> queryAllLifecycleSubscriptions(final VNFMConnectionDetails vnfmConnectionDetails) throws SOL003ResponseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Retrieve a single lifecycle change notification subscription, found by its identifier
     *
     * <ul>
     *     <li>Sends HTTP GET request to /subscriptions/{subscriptionId}</li>
     *     <li>Gets 200 OK response with a {@link LccnSubscription} record as the response body</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param subscriptionId        Identifier for the {@link LccnSubscription} record
     * @return matching {@link LccnSubscription} record
     * @throws SOL003ResponseException if there are any errors performing the query
     */
    public LccnSubscription queryLifecycleSubscription(final VNFMConnectionDetails vnfmConnectionDetails, final String subscriptionId) throws SOL003ResponseException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Deletes a lifecycle change notification subscription record from the VNFM
     *
     * <ul>
     *     <li>Sends HTTP DELETE request to /subscriptions/{subscriptionId}</li>
     *     <li>Gets 204 No Content response</li>
     * </ul>
     *
     * @param vnfmConnectionDetails VNFM connection details
     * @param subscriptionId        Identifier of the {@link LccnSubscription} record to delete
     * @throws SOL003ResponseException if there are any errors deleting the LccnSubscription
     */
    public void deleteLifecycleSubscription(final VNFMConnectionDetails vnfmConnectionDetails, final String subscriptionId) throws SOL003ResponseException {
        final String url = vnfmConnectionDetails.getApiRoot() + API_CONTEXT_ROOT + API_PREFIX_SUBSCRIPTIONS + "/{subscriptionId}";
        final HttpHeaders headers = getHttpHeaders(vnfmConnectionDetails);
        final HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        final Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("subscriptionId", subscriptionId);

        final ResponseEntity<Void> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class, uriVariables);

        checkResponseEntityMatches(responseEntity, HttpStatus.NO_CONTENT, false);
    }

    /**
     * Creates HTTP headers, populating the content type (as application/json) and any application authentication parameters
     *
     * @param vnfmConnectionDetails details of the VNFM connection
     * @return HTTP headers containing appropriate authentication parameters
     */
    private HttpHeaders getHttpHeaders(VNFMConnectionDetails vnfmConnectionDetails) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (vnfmConnectionDetails.getAuthenticationType() == VNFMConnectionDetails.AuthenticationType.BASIC) {
            headers.setBasicAuth(vnfmConnectionDetails.getAuthenticationProperties().get(BASIC_AUTHENTICATION_USERNAME),
                                 vnfmConnectionDetails.getAuthenticationProperties().get(BASIC_AUTHENTICATION_PASSWORD));
        } else if (vnfmConnectionDetails.getAuthenticationType() == VNFMConnectionDetails.AuthenticationType.OAUTH2) {
            // TODO Add OAuth2 authentication here
        }
        return headers;
    }

    /**
     * Utility method that checks if the HTTP status code matches the expected value and that it contains a response body (if desired)
     *
     * @param responseEntity       response to check
     * @param expectedStatusCode   HTTP status code to check against
     * @param containsResponseBody whether the response should contain a body
     */
    private void checkResponseEntityMatches(final ResponseEntity responseEntity, final HttpStatus expectedStatusCode, final boolean containsResponseBody) {
        // Check response code matches expected value (log a warning if incorrect 2xx status seen)
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getStatusCode() != expectedStatusCode) {
            // Be lenient on 2xx response codes
            logger.warn("Invalid status code [{}] received, was expecting [{}]", responseEntity.getStatusCode(), expectedStatusCode);
        } else if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new SOL003ResponseException(String.format("Invalid status code [%s] received", responseEntity.getStatusCode()));
        }
        // Check if the response body is populated (or not) as expected
        if (containsResponseBody && responseEntity.getBody() == null) {
            throw new SOL003ResponseException("No response body");
        } else if (!containsResponseBody && responseEntity.getBody() != null) {
            throw new SOL003ResponseException("No response body expected");
        }
    }

}
