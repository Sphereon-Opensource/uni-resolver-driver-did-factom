package uniresolver.driver.did.factom;

import org.blockchain_innovation.factom.client.api.ops.StringUtils;
import org.blockchain_innovation.factom.identiy.did.IdentityClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static uniresolver.driver.did.factom.ClientFactory.Env.ENABLED;
import static uniresolver.driver.did.factom.ClientFactory.Env.MODE;
import static uniresolver.driver.did.factom.ClientFactory.Env.NETWORK_ID;
import static uniresolver.driver.did.factom.ClientFactory.Env.FACTOMD_URL;
import static uniresolver.driver.did.factom.ClientFactory.Env.WALLETD_URL;

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
            id = "mainnet";
        }
        String mode = environment.get(MODE.key(nr));

        IdentityClient.Builder builder = new IdentityClient.Builder()
                .id(id)
                .property("factomd.url", factomdUrl)
                .mode(StringUtils.isEmpty(mode) ? IdentityClient.Mode.OFFLINE_SIGNING : IdentityClient.Mode.valueOf(mode.toUpperCase()));

        if (StringUtils.isNotEmpty(walletdUrl)) {
            builder.property("walletd.url", walletdUrl);
        }
        return Optional.of(builder.build());
    }

    private Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        properties.forEach((key, value) -> map.put((String) key, (String) value));
        return map;
    }

}
