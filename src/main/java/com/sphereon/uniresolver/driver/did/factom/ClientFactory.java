package com.sphereon.uniresolver.driver.did.factom;

import com.sphereon.factom.identity.did.IdentityClient;
import org.blockchain_innovation.factom.client.api.SigningMode;
import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.client.api.settings.RpcSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.sphereon.uniresolver.driver.did.factom.ClientFactory.Env.ENABLED;
import static com.sphereon.uniresolver.driver.did.factom.ClientFactory.Env.FACTOMD_URL;
import static com.sphereon.uniresolver.driver.did.factom.ClientFactory.Env.MODE;
import static com.sphereon.uniresolver.driver.did.factom.ClientFactory.Env.NETWORK_ID;
import static com.sphereon.uniresolver.driver.did.factom.ClientFactory.Env.WALLETD_URL;
import static com.sphereon.uniresolver.driver.did.factom.Constants.MAINNET_KEY;

public class ClientFactory {
    public enum Env {
        ENABLED, FACTOMD_URL, WALLETD_URL, NETWORK_ID, MODE;

        public String key(int id) {
            if (id < 1 || id > 9) {
                throw new RuntimeException("Invalid value for id specified " + id + " for creating environment key " + name());
            }
            return String.format("NODE%d_%s", id, name());
        }
    }


    public List<IdentityClient> fromEnvironment(Properties properties) {
        return fromEnvironment(toMap(properties));
    }

    public List<IdentityClient> fromEnvironment(Map<String, String> environment) {
        List<IdentityClient> clients = new ArrayList<>();
        if (environment == null) {
            return clients;
        }
        for (int nr = 1; nr < 10; nr++) {
            fromEnvironment(environment, nr).ifPresent(clients::add);
        }
        return clients;
    }

    public Optional<IdentityClient> fromEnvironment(Properties properties, int nr) {
        return fromEnvironment(toMap(properties), nr);
    }


    public Optional<IdentityClient> fromEnvironment(Map<String, String> environment, int nr) {
        String enabled = Optional.ofNullable(environment.get(ENABLED.key(nr))).orElse("false");
        if (!Boolean.parseBoolean(enabled)) {
            return Optional.empty();
        }
        String factomdUrl = environment.get(FACTOMD_URL.key(nr));
        if (StringUtils.isEmpty(factomdUrl)) {
            return Optional.empty();
        }
        String walletdUrl = environment.get(WALLETD_URL.key(nr));
        String id = environment.get(NETWORK_ID.key(nr));
        if (StringUtils.isEmpty(id)) {
            id = MAINNET_KEY;
        }
        String mode = environment.get(MODE.key(nr));

        IdentityClient.Builder builder = new IdentityClient.Builder()
                .networkName(id)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.FACTOMD, Constants.URL_KEY), factomdUrl)
                .property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.SIGNING_MODE_KEY),
                        StringUtils.isEmpty(mode) ? SigningMode.OFFLINE.toString() : SigningMode.fromModeString(mode).toString()
                );

        if (StringUtils.isNotEmpty(walletdUrl)) {
            builder.property(constructPropertyKey(id, RpcSettings.SubSystem.WALLETD, Constants.URL_KEY), walletdUrl);
        }
        return Optional.of(builder.build());
    }

    private Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        properties.forEach((key, value) -> map.put((String) key, (String) value));
        return map;
    }

    private String constructPropertyKey(String networkId, RpcSettings.SubSystem subsystem, String key) {
        return String.format("%s.%s.%s", networkId, subsystem.configKey(), key);
    }
}
