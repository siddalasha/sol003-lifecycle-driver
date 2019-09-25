print('Generating CreateVnfRequest message for ETSI SOL003 v2.4.1');

var message = {};
message['vnfdId'] = 'xyz-xyz-xyz-xyz';
message.vnfInstanceName = executionRequest.getLifecycleName();
message.vnfInstanceDescription = executionRequest.getProperties().get('description');

logger.debug('Message generated successfully');

JSON.stringify(message);