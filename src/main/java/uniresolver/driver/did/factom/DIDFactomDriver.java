package uniresolver.driver.did.factom;

import did.DIDDocument;
import did.DIDURL;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.impl.AbstractClient;
import org.blockchain_innovation.factom.identiy.did.IdentityClient;
import org.blockchain_innovation.factom.identiy.did.entry.EntryValidation;
import org.blockchain_innovation.factom.identiy.did.parse.RuleException;
import org.factomprotocol.identity.did.model.IdentityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DIDFactomDriver implements Driver {

    private static Logger log = LoggerFactory.getLogger(DIDFactomDriver.class);
    private IdentityClient identityClient;

    private IdentityClient getClient() {
        if (identityClient == null) {
            this.identityClient = new IdentityClient.Builder().mode(IdentityClient.Mode.OFFLINE_SIGNING).properties((Map) properties()).build();
        }
        return identityClient;
    }



    private final Pattern DID_FACTOM_PATTERN = Pattern.compile("^did:factom:.+");


    public DIDFactomDriver() {
        this.getPropertiesFromEnvironment();
    }


    private void getPropertiesFromEnvironment() {
        if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());
        try {
//			this.setWorkDomain(System.getenv("uniresolver_driver_did_factom_factomd_rpc"));

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }


    @Override
    public ResolveResult resolve(String identifier) throws ResolutionException {
        Instant start = Instant.now();
        if (log.isDebugEnabled()) log.debug("Resolving identifier " + identifier);

        // match
        Matcher matcher = DID_FACTOM_PATTERN.matcher(identifier);
        if (!matcher.find()) {
            return null;
        }

        try {
            IdentityResponse identityResponse = getClient().getIdentityResponse(identifier, EntryValidation.IGNORE_ERROR, Optional.empty(), Optional.empty());
            DIDDocument didDocument = getClient().factory().toDid(identifier, identityResponse);
            ResolveResult resolveResult = ResolveResult.build(didDocument);
            resolveResult.setMethodMetadata(createMethodMetadata(identifier, identityResponse, didDocument, start));
            resolveResult.setResolverMetadata(createResolverMetadata(identifier, identityResponse, didDocument, start));
            return resolveResult;
        } catch (RuleException e) {
            throw new ResolutionException(e);
        }
    }

    private Map<String, Object> createMethodMetadata(String identifier, IdentityResponse identityResponse, DIDDocument didDocument, Instant start) {
        Map<String, Object> methodMetadata = new HashMap<>();

        methodMetadata.put("factomdNode", ((AbstractClient) identityClient.lowLevelClient().getEntryApi().getFactomdClient()).getSettings().getServer().getURL());
        methodMetadata.put("chainCreationEntryHash", identityResponse.getMetadata().getCreation().getEntryHash());
        methodMetadata.put("chainCreationEntryTimestamp", identityResponse.getMetadata().getCreation().getEntryTimestamp());
        methodMetadata.put("chainCreationBlockHeight", identityResponse.getMetadata().getCreation().getBlockHeight());
        methodMetadata.put("chainCreationBlockTimestamp", identityResponse.getMetadata().getCreation().getBlockTimestamp());
        methodMetadata.put("currentEntryHash", identityResponse.getMetadata().getUpdate().getEntryHash());
        methodMetadata.put("currentEntryTimestamp", identityResponse.getMetadata().getUpdate().getEntryTimestamp());
        methodMetadata.put("currentBlockHeight", identityResponse.getMetadata().getUpdate().getBlockHeight());
        methodMetadata.put("currentBlockTimestamp", identityResponse.getMetadata().getUpdate().getBlockTimestamp());
        methodMetadata.put("resolvedFactomIdentity", identityResponse);


        return methodMetadata;
    }

    private Map<String, Object> createResolverMetadata(String identifier, IdentityResponse identityResponse, DIDDocument didDocument, Instant start) {
        Map<String, Object> resolverMetadata = new HashMap<>();
        resolverMetadata.put("startTime", start.toString());
        resolverMetadata.put("duration", Duration.between(start, Instant.now()).toMillis());
        resolverMetadata.put("method", "factom");
        resolverMetadata.put("didUrl", DIDURL.fromString(identifier).toJsonObject());
        resolverMetadata.put("driverId", "Sphereon-OpenSource/uniresolver-driver-did-factom");
        resolverMetadata.put("vendor", "Factom Protocol"/*getClass().getPackage().getImplementationVendor()*/);
        resolverMetadata.put("version", "0.2.0-SNAPSHOT"/* getClass().getPackage().getImplementationVersion()*/);
        return resolverMetadata;
    }

    @Override
    public Map<String, Object> properties() {
        Map<String, Object> props = new HashMap<>();
//        props.put("factomd.url", getEnvVar("MAINNET_FACTOMD_URL", "https://api.factomd.net/v2"));
        props.put("factomd.url", getEnvVar("TESTNET_FACTOMD_URL", "https://dev.factomd.net/v2"));
        return props;
    }

    private String getEnvVar(String envKey, String defaultValue) {
        String value = System.getenv(envKey);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }


}