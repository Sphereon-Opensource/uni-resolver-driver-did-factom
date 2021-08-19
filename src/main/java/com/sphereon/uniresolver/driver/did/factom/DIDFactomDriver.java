package com.sphereon.uniresolver.driver.did.factom;

import com.sphereon.factom.identity.did.DIDRuntimeException;
import com.sphereon.factom.identity.did.IdentityClient;
import com.sphereon.factom.identity.did.entry.EntryValidation;
import com.sphereon.factom.identity.did.entry.FactomIdentityEntry;
import com.sphereon.factom.identity.did.parse.RuleException;
import com.sphereon.factom.identity.did.response.BlockchainResponse;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.DIDURL;
import foundation.identity.did.parser.ParserException;
import lombok.extern.slf4j.Slf4j;
import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;
import org.blockchain_innovation.factom.client.impl.AbstractClient;
import org.springframework.stereotype.Service;
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

import static com.sphereon.uniresolver.driver.did.factom.Constants.DID_FACTOM_METHOD_PATTERN;
import static com.sphereon.uniresolver.driver.did.factom.Constants.FACTOMD_URL_MAINNET;
import static com.sphereon.uniresolver.driver.did.factom.Constants.FACTOMD_URL_TESTNET;
import static com.sphereon.uniresolver.driver.did.factom.Constants.MAINNET_KEY;
import static com.sphereon.uniresolver.driver.did.factom.Constants.SIGNING_MODE_KEY;
import static com.sphereon.uniresolver.driver.did.factom.Constants.TESTNET_KEY;
import static com.sphereon.uniresolver.driver.did.factom.Constants.URL_KEY;

@Slf4j
@Service
public class DIDFactomDriver implements Driver {

    private final Pattern DID_FACTOM_PATTERN = Pattern.compile(DID_FACTOM_METHOD_PATTERN);


    public DIDFactomDriver() {
        ClientFactory clientFactory = new ClientFactory();
        List<IdentityClient> clients = clientFactory.fromEnvironment((Map) properties());
        if (clients.isEmpty()) {
            addDefaultClient(clients);
        }
        clients.forEach(IdentityClient.Registry::put);
    }


    @Override
    public ResolveResult resolve(String identifier) throws ResolutionException {
        log.info("Resolving did: {}....", identifier);
        Instant start = Instant.now();

        // match
        Matcher matcher = DID_FACTOM_PATTERN.matcher(identifier);
        if (!matcher.find()) {
            return null;
        }
        String targetIdentifier = identifier;
        DIDURL didurl = null;
        try {
            didurl = DIDURL.fromString(identifier);
        } catch (ParserException e) {
            log.error(e.getMessage(), e);
            throw new ResolutionException(e);
            // TODO: 19/08/2021 Log throw antipattern. Waiting for move towards Spring boot and ControllerAdvice
        }
        Optional<String> networkId = Optional.of(MAINNET_KEY);

        String[] parts = didurl.getDidUrlString().split(":");
        // In case we have a network Id, split it up
        if (parts.length > 3 && parts[3].length() == 64) {
            networkId = Optional.ofNullable(parts[2].toLowerCase());
            targetIdentifier = identifier.replaceFirst("\\:" + networkId.get(), "");
        }


        try {
            IdentityClient client = getClient(networkId);
            List<FactomIdentityEntry<?>> allEntries = client.lowLevelClient()
                    .getAllEntriesByIdentifier(identifier, EntryValidation.IGNORE_ERROR, Optional.empty(), Optional.empty());
            if (allEntries == null) {
                throw new DIDRuntimeException.NotFoundException(String.format("'%s' not found on network %s", identifier, networkId.get()));
            } else
            if (allEntries.isEmpty()) {
                throw new DIDRuntimeException.NotFoundException(String.format("'%s' is pending on %s", identifier, networkId.get()));
            }
            BlockchainResponse<?> blockchainResponse = client.factory().toBlockchainResponse(identifier, allEntries);
            DIDDocument didDocument = client.factory().toDid(identifier, blockchainResponse);
            final ResolveResult result = ResolveResult
                    .build(didDocument,
                            createMethodMetadata(identifier, networkId, blockchainResponse, didDocument, start),
                            createResolverMetadata(identifier, blockchainResponse, didDocument, start));
            log.info("Resolved did: {}", identifier);
            return result;
        } catch (RuleException | ParserException e) {
            throw new ResolutionException(e);
        }
    }

    private Map<String, Object> createMethodMetadata(String identifier, Optional<String> networkId, BlockchainResponse<?> blockchainResponse, DIDDocument didDocument, Instant start) {
        Map<String, Object> methodMetadata = new HashMap<>();

        methodMetadata.put("network", networkId.orElse(MAINNET_KEY));
        methodMetadata.put("factomdNode", ((AbstractClient) getClient(networkId).lowLevelClient().getEntryApi().getFactomdClient()).getSettings().getServer().getURL());
        methodMetadata.put("chainCreationEntryHash", blockchainResponse.getMetadata().getCreation().getEntryHash());
        methodMetadata.put("chainCreationEntryTimestamp", blockchainResponse.getMetadata().getCreation().getEntryTimestamp());
        methodMetadata.put("chainCreationBlockHeight", blockchainResponse.getMetadata().getCreation().getBlockHeight());
        methodMetadata.put("chainCreationBlockTimestamp", blockchainResponse.getMetadata().getCreation().getBlockTimestamp());
        methodMetadata.put("currentEntryHash", blockchainResponse.getMetadata().getUpdate().getEntryHash());
        methodMetadata.put("currentEntryTimestamp", blockchainResponse.getMetadata().getUpdate().getEntryTimestamp());
        methodMetadata.put("currentBlockHeight", blockchainResponse.getMetadata().getUpdate().getBlockHeight());
        methodMetadata.put("currentBlockTimestamp", blockchainResponse.getMetadata().getUpdate().getBlockTimestamp());
        methodMetadata.put("resolvedFactomIdentity", blockchainResponse);


        return methodMetadata;
    }

    private Map<String, Object> createResolverMetadata(String identifier, BlockchainResponse<?> blockchainResponse, DIDDocument didDocument, Instant start) throws ParserException {
        Map<String, Object> resolverMetadata = new HashMap<>();
        resolverMetadata.put("startTime", start.toString());
        resolverMetadata.put("duration", Duration.between(start, Instant.now()).toMillis());
        resolverMetadata.put("method", "factom");
        resolverMetadata.put("didUrl", DIDURL.fromString(identifier).toJsonObject());
        resolverMetadata.put("driverId", "sphereon/uni-resolver-driver-did-factom");
        resolverMetadata.put("vendor", "Factom Protocol");
        resolverMetadata.put("version", "0.4.0");
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

    private String constructPropertyKey(String networkId, RpcSettings.SubSystem subsystem, String key) {
        return String.format("%s.%s.%s", networkId, subsystem.configKey(), key);
    }


    private void addDefaultClient(List<IdentityClient> clients) {
        log.warn("No Factom networks defined in environment. Using default mainnet and testnet values using Factom OpenNode API");
        clients.add(new IdentityClient.Builder().networkName(MAINNET_KEY)
                .property(constructPropertyKey(MAINNET_KEY, RpcSettings.SubSystem.FACTOMD, URL_KEY),
                        FACTOMD_URL_MAINNET)
                .property(constructPropertyKey(MAINNET_KEY, RpcSettings.SubSystem.WALLETD, SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .build());
        clients.add(new IdentityClient.Builder().networkName(TESTNET_KEY)
                .property(constructPropertyKey(TESTNET_KEY, RpcSettings.SubSystem.FACTOMD, URL_KEY),
                        FACTOMD_URL_TESTNET)
                .property(constructPropertyKey(TESTNET_KEY, RpcSettings.SubSystem.WALLETD, SIGNING_MODE_KEY),
                        SigningMode.OFFLINE.toString().toLowerCase())
                .build());
    }
}
