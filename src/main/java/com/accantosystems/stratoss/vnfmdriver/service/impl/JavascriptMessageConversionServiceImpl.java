package com.accantosystems.stratoss.vnfmdriver.service.impl;

import javax.script.*;

import org.etsi.sol003.lifecyclemanagement.CreateVnfRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionException;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionService;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

@Service("JavascriptMessageConversionServiceImpl")
public class JavascriptMessageConversionServiceImpl implements MessageConversionService {

    private final static Logger logger = LoggerFactory.getLogger(JavascriptMessageConversionServiceImpl.class);

    @Override public String generateMessageFromRequest(final ExecutionRequest executionRequest, final String script) throws MessageConversionException {
        try {
            // Retrieve a Javascript engine (should be Nashorn in JRE 8+)
            final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByMimeType("application/javascript");
            logger.debug("Retrieved an instance of a [{}] script engine", scriptEngine);

            // Create a new bindings object and attach objects to be used by the scripts
            final Bindings bindings = scriptEngine.createBindings();
            bindings.put("executionRequest", executionRequest);
            bindings.put("logger", logger);

            logger.debug("Preparing to run script");
            final Object returnVal = scriptEngine.eval(script, bindings);
            logger.info("Script successfully run, returnVal is [{}]", returnVal);

            if (returnVal instanceof String) {
                return (String) returnVal;
            } else if (returnVal == null) {
                throw new MessageConversionException("Script did not return a value, expected a String");
            } else {
                throw new MessageConversionException(String.format("Script returned invalid object of type [%s], expected a String", returnVal.getClass().getSimpleName()));
            }
        } catch (ScriptException e) {
            throw new MessageConversionException("Exception caught executing a script", e);
        }
    }
}
