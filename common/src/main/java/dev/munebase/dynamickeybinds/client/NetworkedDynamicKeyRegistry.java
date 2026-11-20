package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistryImpl;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import dev.munebase.dynamickeybinds.util.KeyMappingUtil;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-side dynamic key registry that always uses server networking for add/remove operations.
 */
public final class NetworkedDynamicKeyRegistry implements ServerSynchronizedDynamicKeyRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private final DynamicKeyRegistryImpl localRegistry = new DynamicKeyRegistryImpl();
    private final ClientRegistryNetworkBridge networkBridge;
    private final AtomicBoolean suppressNetwork = new AtomicBoolean(false);

    public NetworkedDynamicKeyRegistry(ClientRegistryNetworkBridge networkBridge) {
        this.networkBridge = networkBridge;
    }

    @Override
    public KeyMapping registerDynamicKey(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        return registerDynamicKey(id, keyCode, category, action, DisplaySpec.empty());
    }

    @Override
    public KeyMapping registerDynamicKey(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        KeyMapping keyMapping = localRegistry.registerDynamicKey(id, keyCode, category, action, displaySpec);
        RuntimeKeyMappingManager.registerRuntimeKey(keyMapping, LOGGER);

        if (!suppressNetwork.get()) {
            networkBridge.sendAdd(id, keyCode, category, action, displaySpec);
        }

        return keyMapping;
    }

    @Override
    public void unregisterDynamicKey(KeyMapping keyBinding) {
        if (keyBinding == null) {
            return;
        }

        Optional<String> id = localRegistry.getKeyBindId(keyBinding);
        localRegistry.unregisterDynamicKey(keyBinding);
        RuntimeKeyMappingManager.unregisterRuntimeKey(keyBinding, LOGGER);

        if (!suppressNetwork.get() && id.isPresent()) {
            networkBridge.sendRemove(id.get());
        }
    }

    @Override
    public Collection<KeyMapping> getAllDynamicKeys() {
        return localRegistry.getAllDynamicKeys();
    }

    @Override
    public KeyMapping getKeyBindById(String id) {
        return localRegistry.getKeyBindById(id);
    }

    @Override
    public Optional<DynamicKeybindAction> getKeyBindAction(KeyMapping keyBinding) {
        return localRegistry.getKeyBindAction(keyBinding);
    }

    @Override
    public Optional<String> getKeyBindId(KeyMapping keyBinding) {
        return localRegistry.getKeyBindId(keyBinding);
    }

    @Override
    public void applyServerSnapshot(List<StoredKeybind> serverKeybinds, Logger logger) {
        suppressNetwork.set(true);
        try {
            Map<String, StoredKeybind> incoming = new LinkedHashMap<>();
            for (StoredKeybind entry : serverKeybinds) {
                incoming.put(KeyMappingUtil.normalizeId(entry.id()), entry);
            }

            // Remove keys the server no longer has, or whose category changed (KeyMapping category is immutable)
            for (KeyMapping keyMapping : new ArrayList<>(localRegistry.getAllDynamicKeys())) {
                StoredKeybind entry = localRegistry.getKeyBindId(keyMapping).map(incoming::get).orElse(null);
                if (entry == null || !keyMapping.getCategory().equals(DynamicKeyMapping.categoryKey(entry.category()))) {
                    localRegistry.unregisterDynamicKey(keyMapping);
                    RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, logger);
                }
            }

            // Update survivors in place so other mods' references (e.g. TooManyKeybinds buttons) stay valid
            boolean keysChanged = false;
            for (Map.Entry<String, StoredKeybind> e : incoming.entrySet()) {
                String cleanId = e.getKey();
                StoredKeybind entry = e.getValue();
                try {
                    if (localRegistry.getKeyBindById(cleanId) != null) {
                        keysChanged |= localRegistry.updateDynamicKey(cleanId, entry.keyCode(), entry.action(), entry.displaySpec());
                    } else {
                        KeyMapping keyMapping = localRegistry.registerDynamicKey(cleanId, entry.keyCode(), entry.category(), entry.action(), entry.displaySpec());
                        RuntimeKeyMappingManager.registerRuntimeKey(keyMapping, logger);
                    }
                } catch (IllegalArgumentException ex) {
                    logger.error("Failed to apply synced keybind: {}", cleanId, ex);
                }
            }
            if (keysChanged) {
                KeyMapping.resetMapping();
            }
        } finally {
            suppressNetwork.set(false);
        }
    }

    @Override
    public void clearLocalState(Logger logger) {
        List<KeyMapping> toRemove = new ArrayList<>(localRegistry.getAllDynamicKeys());
        for (KeyMapping keyMapping : toRemove) {
            localRegistry.unregisterDynamicKey(keyMapping);
            RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, logger);
        }
    }
}
