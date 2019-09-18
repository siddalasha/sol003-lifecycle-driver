package com.accantosystems.stratoss.vnfmdriver.driver;

import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_PASSWORD;
import static com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverConstants.BASIC_AUTHENTICATION_USERNAME;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.accantosystems.stratoss.vnfmdriver.model.VNFMConnectionDetails;
import com.accantosystems.stratoss.vnfmdriver.model.etsi.*;

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
 *                 <li>/terminate</li>
 *                 <li>/heal</li>
 *                 <li>/change_ext_conn</li>
 *                 <li>/operate</li>
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

    public final static String API_CONTEXT_ROOT = "/vnflcm/v1";
    public final static String API_PREFIX_VNF_INSTANCES = "/vnf_instances";
    public final static String API_PREFIX_OP_OCCURRENCES = "/vnf_lcm_op_occs";
    public final static String API_PREFIX_SUBSCRIPTIONS = "/subscriptions";

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

       Operation           Precondition                Task            RequestStructure                    Postcondition
       =========           ============                ====            ================                    =============

       Instantiate VNF     VNF instance created and    instantiate     InstantiateVnfRequest               VNF instance in
                           in NOT_INSTANTIATED                                                             INSTANTIATED state
                           state

       Scale VNF           VNF instance in             scale           ScaleVnfRequest                     VNF instance still in
                           INSTANTIATED state                                                              INSTANTIATED state and
                                                                                                           VNF was scaled

       Scale VNF to        VNF instance in             scale_to_level  ScaleVnfToLevelRequest              VNF instance still in
       Level               INSTANTIATED state                                                              INSTANTIATED state and
                                                                                                           VNF was scaled

       Change VNF flavor   VNF instance in             change_flavour  ChangeVnfFlavourRequest             VNF instance still in
                           INSTANTIATED state                                                              INSTANTIATED state and
                                                                                                           VNF deployment flavour
                                                                                                           changed

       Operate VNF         VNF instance in             operate         OperateVnfRequest                   VNF instance still in
                           INSTANTIATED state                                                              INSTANTIATED state and
                                                                                                           VNF operational state
                                                                                                           changed

       Heal VNF            VNF instance in             heal            HealVnfRequest                      VNF instance still in
                           INSTANTIATED state                                                              INSTANTIATED state

       Change external     VNF instance in             change_ext_conn ChangeExtVnfConnectivityRequest     VNF instance still in
       VNF connectivity    INSTANTIATED state                                                              INSTANTIATED state and
                                                                                                           external connectivity of the
                                                                                                           VNF is changed

       Terminate VNF       VNF instance in             terminate       TerminateVnfRequest                 VNF instance in
                           INSTANTIATED state                                                              NOT_INSTANTIATED state
     */

    /**
     * Performs a query to retrieve matching VNF lifecycle operation occurrence records
     *
     * <ul>
     *     <li>Sends HTTP GET request to /vnf_lcm_op_occs</li>
     *     <li>Gets 200 OK response with an array of {@link VnfLcmOpOcc} records as the response body</li>
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
        throw new UnsupportedOperationException("Not implemented yet");
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
        throw new UnsupportedOperationException("Not implemented yet");
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
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getStatusCode() != expectedStatusCode && (!containsResponseBody || responseEntity.getBody() != null)) {
            // Be lenient on 2xx response codes
            logger.warn("Invalid status code [{}] received, was expecting [{}]", responseEntity.getStatusCode(), expectedStatusCode);
        } else if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new SOL003ResponseException(String.format("Invalid status code [%s] received", responseEntity.getStatusCode()));
        } else if (containsResponseBody && responseEntity.getBody() == null) {
            throw new SOL003ResponseException("No response body");
        }
    }

}
