package com.accantosystems.stratoss.vnfmdriver.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.accantosystems.stratoss.vnfmdriver.model.alm.ExecutionRequest;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionException;
import com.accantosystems.stratoss.vnfmdriver.service.MessageConversionService;

@Service("JavascriptMessageConversionServiceImpl")
public class JavascriptMessageConversionServiceImpl implements MessageConversionService {

    private final static Logger logger = LoggerFactory.getLogger(JavascriptMessageConversionServiceImpl.class);
    private static final String DEFAULT_ETSI_SOL003_VERSION = "2.4.1";

    @Override public String generateMessageFromRequest(final String messageType, final ExecutionRequest executionRequest) throws MessageConversionException {
        final String script = getScriptFromExecutionRequest(executionRequest, messageType);
        final ScriptEngine scriptEngine = getScriptEngine();

        try {
            // Create a new bindings object and attach objects to be used by the scripts
            final Bindings bindings = scriptEngine.createBindings();
            bindings.put("executionRequest", executionRequest);
            bindings.put("logger", logger);

            final Object returnVal = scriptEngine.eval(script, bindings);
            logger.info("Message conversion script successfully run, returnVal is\n{}", returnVal);
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

    @Override public Map<String, String> extractPropertiesFromMessage(String messageType, ExecutionRequest executionRequest, String message) throws MessageConversionException {
        final String script = getScriptFromExecutionRequest(executionRequest, messageType);
        final ScriptEngine scriptEngine = getScriptEngine();

        try {
            // Create a new bindings object and attach objects to be used by the scripts
            final Bindings bindings = scriptEngine.createBindings();
            bindings.put("message", message);
            bindings.put("logger", logger);
            final Map<String, String> outputs = new HashMap<>();
            bindings.put("outputs", outputs);

            scriptEngine.eval(script, bindings);
            logger.info("Message conversion script successfully run, outputs are\n{}", outputs);
            return outputs;
        } catch (ScriptException e) {
            throw new MessageConversionException("Exception caught executing a script", e);
        }
    }

    private ScriptEngine getScriptEngine() {
        // Retrieve a Javascript engine (should be Nashorn in JRE 8+)
        final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByMimeType("application/javascript");
        logger.debug("Retrieved an instance of a [{}] script engine", scriptEngine);
        return scriptEngine;
    }

    private String getScriptFromExecutionRequest(final ExecutionRequest executionRequest, final String scriptName) {
        final String fullScriptName = scriptName + ".js";

        // Attempt to extract the script from the executionRequest passed in
        if (!StringUtils.isEmpty(executionRequest.getLifecycleScripts())) {
            // lifecycleScripts should contain (if not empty) a Base64 encoded Zip file of all scripts concerning the VNFM driver
            byte[] decodedByteArray = Base64.getDecoder().decode(executionRequest.getLifecycleScripts());

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decodedByteArray))) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    logger.trace("Found zip entry: {}", entry);
                    if (fullScriptName.equalsIgnoreCase(entry.getName())) {
                        logger.debug("Found script called [{}], extracting...", entry.getName());
                        final String script = IOUtils.toString(zis, Charset.defaultCharset());
                        logger.debug("Script content is\n{}", script);
                        return script;
                    }
                    // Get the next entry for the loop
                    entry = zis.getNextEntry();
                }
            } catch (IOException e) {
                logger.error("Exception raised reading lifecycle scripts", e);
            }
        }

        // If we can't find it in the zip file, try searching in out default locations
        final String interfaceVersion = executionRequest.getProperties().getOrDefault("interfaceVersion", DEFAULT_ETSI_SOL003_VERSION);
        try (InputStream inputStream = JavascriptMessageConversionServiceImpl.class.getResourceAsStream("/scripts/" + interfaceVersion + "/" + fullScriptName)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Exception raised looking up default lifecycle script", e);
        }

        throw new IllegalArgumentException(String.format("Unable to find a script called [%s]", fullScriptName));
    }

}
