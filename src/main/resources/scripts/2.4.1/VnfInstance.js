print('Parsing VnfInstance message for ETSI SOL003 v2.4.1');

var parsedMessage = JSON.parse(message);
outputs.put('vnfInstanceId', parsedMessage.id);

logger.debug('Message parsed successfully');
