package com.accantosystems.stratoss.vnfmdriver.driver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.accantosystems.stratoss.vnfmdriver.model.etsi.*;

@Service("VNFLifecycleManagementDriver")
public class VNFLifecycleManagementDriver {

    private final static Logger logger = LoggerFactory.getLogger(VNFLifecycleManagementDriver.class);

    public final static String API_CONTEXT_ROOT = "/vnflcm/v1";
    public final static String API_PREFIX_VNF_INSTANCES = "/vnf_instances";
    public final static String API_PREFIX_OP_OCCURRENCES = "/vnf_lcm_op_occs";
    public final static String API_PREFIX_SUBSCRIPTIONS = "/subscriptions";

    private final RestTemplate restTemplate;

    /*
     * Endpoint structure
     *
     * {apiRoot}/vnflcm/v1
     *                    /vnf_instances
     *                                  /{vnfInstanceId}
     *                                                  /instantiate
     *                                                  /scale
     *                                                  /scale_to_level
     *                                                  /change_flavour
     *                                                  /terminate
     *                                                  /heal
     *                                                  /change_ext_conn
     *                                                  /operate
     *                    /vnf_lcm_op_occs
     *                                    /{vnfLcmOpOccId}
     *                                                    /retry
     *                                                    /rollback
     *                                                    /fail
     *                                                    /cancel
     *                    /subscriptions
     *                                  /{subscriptionId}
     *
     */

    @Autowired
    public VNFLifecycleManagementDriver(RestTemplateBuilder restTemplateBuilder, SOL003ResponseErrorHandler sol003ResponseErrorHandler) {
        this.restTemplate = restTemplateBuilder.errorHandler(sol003ResponseErrorHandler)
                                               .build();
    }

    public VnfInstance createVnfInstance(CreateVnfRequest createVnfRequest) {
        // Sends CreateVnfRequest message via HTTP POST to /vnf_instances
        // Gets 201 Created response with a VnfInstance as the response body

        final String apiRoot = "http://localhost:8080";
        final String url = apiRoot + API_CONTEXT_ROOT + API_PREFIX_VNF_INSTANCES;
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // TODO Add authentication here
        final HttpEntity<CreateVnfRequest> requestEntity = new HttpEntity<>(createVnfRequest, headers);

        ResponseEntity<VnfInstance> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, VnfInstance.class);
        } catch (SOL003ResponseException e) {
            logger.error("SOL003-compliant exception", e);
            throw e;
        } catch (RestClientException e) {
            logger.error("General exception", e);
            throw e;
        }

        if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
            logger.error("Invalid status code [{}] received, was expecting [201 Created]", responseEntity.getStatusCode());
        }

        // Postcondition: VNF instance created in NOT_INSTANTIATED state
        return null;
        // Out of band VnfIdentifierCreationNotification should be received after this returns
    }

    public void deleteVnfInstance(String vnfInstanceId) {
        // Precondition: VNF instance in NOT_INSTANTIATED state
        // Sends HTTP DELETE request to /vnf_instances/{vnfInstanceId}
        // Gets 204 No Content response
        // Postcondition: VNF instance resource removed
        // Out of band VnfIdentifierDeletionNotification should be received after this returns
    }

    public void callGenericLifecycleOperation() {
        // Precondition: <<Precondition>>
        // Send <<RequestStructure>> via HTTP POST to /vnf_instances/{vnfInstanceId}/<<Task>>
        // Gets 202 Accepted response
        // Receive out of band VnfLcmOperationOccurrenceNotification (STARTING)
        // Granting exchange (NOTE: this *may* occur before the STARTING notification above)
        // Receive out of band VnfLcmOperationOccurrenceNotification (PROCESSING)
        //   Optionally could perform HTTP GET on /vnf_lcm_op_occs/{vnfLcmOpOccId}
        //     Gets 200 OK with response body of VnfLcmOpOcc:operationState=PROCESSING
        // Receive out of band VnfLcmOperationOccurrenceNotification (COMPLETED)
        //   Optionally could perform HTTP GET on /vnf_lcm_op_occs/{vnfLcmOpOccId}
        //     Gets 200 OK with response body of VnfLcmOpOcc:operationState=COMPLETED
        // Postcondition: <<Postcondition>>

        /*
            Operation           Precondition                Task            RequestStructure                    Postcondition

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
    }

    public List<VnfLcmOpOcc> queryAllLifecycleOperationOccurrences() {
        // Sends HTTP GET request to /vnf_lcm_op_occs
        // Gets 200 OK response with an array of VnfLcmOpOccs as the response body
        return null;
    }

    public VnfLcmOpOcc queryLifecycleOperationOccurrence(String vnfLcmOpOccId) {
        // Sends HTTP GET request to /vnf_lcm_op_occs/{vnfLcmOpOccId}
        // Gets 200 OK response with a VnfLcmOpOcc as the response body
        return null;
    }

    public LccnSubscription createLifecycleSubscription(LccnSubscriptionRequest lccnSubscriptionRequest) {
        // Sends LccnSubscriptionRequest message via HTTP POST to /subscriptions
        //    Optionally the VNFM may test the notification endpoint here
        // Gets 201 Created response with a LccnSubscription as the response body
        return null;
    }

    public List<LccnSubscription> queryAllLifecycleSubscriptions() {
        // Sends HTTP GET request to /subscriptions
        // Gets 200 OK response with an array of LccnSubscriptions as the response body
        return null;
    }

    public LccnSubscription queryLifecycleSubscription(String subscriptionId) {
        // Sends HTTP GET request to /subscriptions/{subscriptionId}
        // Gets 200 OK response with a LccnSubscription as the response body
        return null;
    }

    public void deleteLifecycleSubscription(String subscriptionId) {
        // Sends HTTP DELETE request to /subscriptions/{subscriptionId}
        // Gets 204 No Content response
    }

}
