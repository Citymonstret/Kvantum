package xyz.kvantum.server.api.memguard;

/**
 * A {@link LeakageProne} object is prone to memory leaks,
 * this can be used together with utility classes to prevent
 * long-term memory leakage
 */
@FunctionalInterface
public interface LeakageProne
{

    /**
     * Clean up memory leakage
     */
    void cleanUp();

}
