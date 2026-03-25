package dev.munebase.dynamickeybinds;

/**
 * Provider for accessing the global DynamicKeyRegistry instance.
 * Loader-specific implementations will provide the actual registry.
 */
public interface DynamicKeyRegistryProvider {
    /**
     * Sets the active provider used by {@link #getRegistry()}.
     *
     * @param provider loader-specific provider implementation
     */
    static void setRegistryProvider(DynamicKeyRegistryProvider provider) {
        Holder.INSTANCE = provider;
    }

    /**
     * Get the DynamicKeyRegistry instance.
     *
     * @return the registry instance
     */
    static DynamicKeyRegistry getRegistry() {
        DynamicKeyRegistryProvider provider = Holder.INSTANCE;
        if (provider == null) {
            throw new IllegalStateException("DynamicKeyRegistryProvider not initialized");
        }
        return provider.getRegistryInstance();
    }

    /**
     * Internal method to get the registry instance.
     * Implemented by loaders.
     *
     * @return the registry instance
     */
    DynamicKeyRegistry getRegistryInstance();

    /**
     * Internal class to hold the provider singleton.
     */
    class Holder {
        /** Active provider instance, set during loader initialization. */
        static DynamicKeyRegistryProvider INSTANCE = null;
    }
}
