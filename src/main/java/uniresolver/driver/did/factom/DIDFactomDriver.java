package uniresolver.driver.did.factom;

import did.DIDDocument;
import did.DIDURL;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uniresolver.driver.did.factom.Constants.DID_FACTOM_METHOD_PATTERN;
import static uniresolver.driver.did.factom.Constants.FACTOMD_URL_KEY;
import static uniresolver.driver.did.factom.Constants.FACTOMD_URL_MAINNET;
import static uniresolver.driver.did.factom.Constants.FACTOMD_URL_TESTNET;
import static uniresolver.driver.did.factom.Constants.MAINNET_KEY;
import static uniresolver.driver.did.factom.Constants.TESTNET_KEY;

public class DIDFactomDriver implements Driver {

    private final Pattern DID_FACTOM_PATTERN = Pattern.compile(DID_FACTOM_METHOD_PATTERN);
    private static Logger log = LoggerFactory.getLogger(DIDFactomDriver.class);


    public DIDFactomDriver() {
        ClientFactory clientFactory = new ClientFactory();
        List<IdentityClient> clients = clientFactory.fromEnvironment((Map) properties());
        if (clients.isEmpty()) {
            log.warn("No Factom networks defined in environment. Using default mainnet and testnet values using OpenNode");
            clients.add(new IdentityClient.Builder().id(MAINNET_KEY).mode(IdentityClient.Mode.OFFLINE_SIGNING).property(FACTOMD_URL_KEY, FACTOMD_URL_MAINNET).build());
            clients.add(new IdentityClient.Builder().id(TESTNET_KEY).mode(IdentityClient.Mode.OFFLINE_SIGNING).property(FACTOMD_URL_KEY, FACTOMD_URL_TESTNET).build());
        }
        clients.forEach(client -> IdentityClient.Registry.put(client));
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
        String targetIdentifier = identifier;
        DIDURL didurl = DIDURL.fromString(identifier);
        Optional<String> networkId = Optional.of(MAINNET_KEY);

        String[] parts = didurl.getDidUrlString().split(":");
        // In case we have a network Id, split it up
        if (parts.length > 3 && parts[3].length() == 64) {
            networkId = Optional.ofNullable(parts[2].toLowerCase());
            targetIdentifier = identifier.replaceFirst("\\:" + networkId.get(), "");
        }


        try {
            IdentityResponse identityResponse = getClient(networkId).getIdentityResponse(targetIdentifier, EntryValidation.IGNORE_ERROR, Optional.empty(), Optional.empty());
            DIDDocument didDocument = getClient(networkId).factory().toDid(identifier, identityResponse);
            ResolveResult resolveResult = ResolveResult.build(didDocument);
            resolveResult.setMethodMetadata(createMethodMetadata(identifier, networkId, identityResponse, didDocument, start));
            resolveResult.setResolverMetadata(createResolverMetadata(identifier, identityResponse, didDocument, start));
            return resolveResult;
        } catch (RuleException e) {
            throw new ResolutionException(e);
        }
    }

    private Map<String, Object> createMethodMetadata(String identifier, Optional<String> networkId, IdentityResponse identityResponse, DIDDocument didDocument, Instant start) {
        Map<String, Object> methodMetadata = new HashMap<>();

        methodMetadata.put("network", networkId.orElse(MAINNET_KEY));
        methodMetadata.put("factomdNode", ((AbstractClient) getClient(networkId).lowLevelClient().getEntryApi().getFactomdClient()).getSettings().getServer().getURL());
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
        resolverMetadata.put("driverId", "Sphereon/driver-did-factom");
        resolverMetadata.put("vendor", "Factom Protocol"/*getClass().getPackage().getImplementationVendor()*/);
        resolverMetadata.put("version", "0.2.0-SNAPSHOT"/* getClass().getPackage().getImplementationVersion()*/);
        return resolverMetadata;
    }

    @Override
    public Map<String, Object> properties() {
        Map<String, Object> props = new HashMap<>();
        props.putAll(System.getenv());
        return props;
    }

    private IdentityClient getClient(Optional<String> id) {
        IdentityClient identityClient = IdentityClient.Registry.get(id);
        if (identityClient == null) {
            throw new RuntimeException("Could not get identity client for network with id " + id.orElse("<none>"));
        }
        return identityClient;
    }


}