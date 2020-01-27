package com.accantosystems.stratoss.vnfmdriver.driver.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.etsi.sol003.packagemanagement.VnfPkgInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.accantosystems.stratoss.vnfmdriver.config.VNFMDriverProperties;
import com.accantosystems.stratoss.vnfmdriver.driver.AbstractVNFPackageRepositoryDriver;
import com.accantosystems.stratoss.vnfmdriver.driver.VNFPackageNotFoundException;
import com.accantosystems.stratoss.vnfmdriver.service.AuthenticatedRestTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonatype.nexus.ComponentInformation;
import com.sonatype.nexus.PaginatedResults;

public class NexusVNFPackageRepositoryDriver extends AbstractVNFPackageRepositoryDriver {

    private static final String COMPONENT_SEARCH_URL = "/service/rest/v1/search";
    private static final String VNF_PKG_INFO_SUFFIX = ".pkgInfo";
    private static final Logger logger = LoggerFactory.getLogger(NexusVNFPackageRepositoryDriver.class);

    private final AuthenticatedRestTemplateService authenticatedRestTemplateService;
    private final ObjectMapper objectMapper;
    private final Map<String, VnfPkgInfo> localVnfPkgInfoCache = new ConcurrentHashMap<>();

    @Autowired
    public NexusVNFPackageRepositoryDriver(VNFMDriverProperties vnfmDriverProperties, AuthenticatedRestTemplateService authenticatedRestTemplateService, ObjectMapper objectMapper) {
        super(vnfmDriverProperties);
        this.authenticatedRestTemplateService = authenticatedRestTemplateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<VnfPkgInfo> queryAllVnfPkgInfos(String groupName) {
        return queryVnfPkgInfos(groupName, null);
    }

    @Override
    public VnfPkgInfo getVnfPkgInfo(String vnfPackageId) throws VNFPackageNotFoundException {
        List<VnfPkgInfo> vnfPkgInfoList = queryVnfPkgInfos(null, vnfPackageId);
        if (vnfPkgInfoList.size() == 1) {
            return vnfPkgInfoList.get(0);
        } else if (vnfPkgInfoList.size() > 1) {
            throw new VNFPackageNotFoundException(String.format("Too many matches found when searching for package information for VNF package [%s]", vnfPackageId));
        } else {
            throw new VNFPackageNotFoundException(String.format("Cannot find package information for VNF package [%s]", vnfPackageId));
        }
    }

    private List<VnfPkgInfo> queryVnfPkgInfos(String groupName, String vnfPackageId) {
        final MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
        queryParameters.set("repository", vnfmDriverProperties.getPackageManagement().getRepositoryName());
        if (StringUtils.hasText(groupName)) {
            queryParameters.set("group", groupName);
        }
        if (StringUtils.hasText(vnfPackageId)) {
            queryParameters.set("keyword", "*" + vnfPackageId + "*");
        }

        // Get a list of components from Nexus
        List<ComponentInformation> componentList = getPaginatedResultsAsList(COMPONENT_SEARCH_URL, queryParameters, ComponentInformation.class);

        // Reduce list to only contain unique VnfPkgInfo assets (and populate local cache, keyed by MD5 sum)
        return componentList.stream()
                            .flatMap(c -> c.getAssets().stream())
                            .filter(a -> a.getPath().endsWith(VNF_PKG_INFO_SUFFIX))
                            .map(a -> {
                                // TODO What about if there's no md5 checksum?
                                if (!localVnfPkgInfoCache.containsKey(a.getChecksum().getMd5())) {
                                    localVnfPkgInfoCache.put(a.getChecksum().getMd5(), getVnfPkgInfoFromUrl(a.getDownloadUrl()));
                                }
                                return localVnfPkgInfoCache.get(a.getChecksum().getMd5());
                            })
                            .collect(Collectors.toList());
    }

    private RestTemplate getRestTemplate() {
        return authenticatedRestTemplateService.getRestTemplate(vnfmDriverProperties.getPackageManagement().getPackageRepositoryUrl(),
                                                                vnfmDriverProperties.getPackageManagement().getAuthenticationProperties());
    }

    private VnfPkgInfo getVnfPkgInfoFromUrl(String downloadUrl) {
        logger.debug("Downloading VnfPkgInfo: {}", downloadUrl);
        final ResponseEntity<String> responseEntity = getRestTemplate().exchange(downloadUrl, HttpMethod.GET, null, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String vnfPkgInfoString = responseEntity.getBody();
            logger.debug("Got response\n{}", vnfPkgInfoString);
            VnfPkgInfo vnfPkgInfo = null;
            try {
                vnfPkgInfo = objectMapper.readValue(vnfPkgInfoString, VnfPkgInfo.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return vnfPkgInfo;
        }
        return null;
    }

    private <T> List<T> getPaginatedResultsAsList(final String baseUrl, final MultiValueMap<String, String> queryParameters, Class<T> returnClass) {
        final List<T> paginatedResults = new ArrayList<>();
        String continuationToken = null;
        int pageCount = 0;

        do {
            final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(vnfmDriverProperties.getPackageManagement().getPackageRepositoryUrl() + baseUrl).queryParams(queryParameters);
            if (continuationToken != null) {
                uriBuilder.queryParam("continuationToken", continuationToken);
            }

            logger.debug("Making paginated call to {}", uriBuilder.toUriString());
            ResponseEntity<PaginatedResults<T>> responseEntity = getRestTemplate()
                    .exchange(uriBuilder.toUriString(), HttpMethod.GET, null, ParameterizedTypeReference.forType(ResolvableType.forClassWithGenerics(PaginatedResults.class, returnClass).getType()));

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                pageCount++;
                paginatedResults.addAll(responseEntity.getBody().getItems());
                continuationToken = responseEntity.getBody().getContinuationToken();
            } else {
                // FIXME Something wrong
                throw new RuntimeException("Something bad happened");
            }
        } while (continuationToken != null);

        logger.debug("Got paginated response with {} element(s) over {} page(s)", paginatedResults.size(), pageCount);
        return paginatedResults;
    }

}
