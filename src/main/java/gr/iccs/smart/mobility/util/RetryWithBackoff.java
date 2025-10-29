package gr.iccs.smart.mobility.util;

import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class RetryWithBackoff {
    private RetryWithBackoff() {
        throw new IllegalStateException("Utility class");
    }

    // Example for passing a Supplier
    public static <T> T retryWithBackoff(
            Supplier<T> supplier,
            int maxAttempts,
            long maximumInitialDelayMillis,
            double backoffFactor) {
        long delay = RandomGenerator.getDefault().nextLong(0, maximumInitialDelayMillis);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (attempt == maxAttempts)
                    throw e;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during backoff", ie);
                }
                delay = (long) (delay * backoffFactor);
            }
        }
        throw new RuntimeException("Retries exhausted");
    }
}
