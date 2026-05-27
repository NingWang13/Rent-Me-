package com.community.common.dataconsistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class DataConsistencyChecker {

    private static final Logger logger = LoggerFactory.getLogger(DataConsistencyChecker.class);

    private final List<ConsistencyCheck> checks = new ArrayList<>();

    public void registerCheck(String name, Supplier<Boolean> checkLogic) {
        checks.add(new ConsistencyCheck(name, checkLogic));
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void runAllChecks() {
        logger.info("Starting data consistency checks");
        Map<String, Boolean> results = new HashMap<>();

        for (ConsistencyCheck check : checks) {
            try {
                boolean passed = check.execute();
                results.put(check.getName(), passed);
                if (!passed) {
                    logger.warn("Consistency check failed: {}", check.getName());
                }
            } catch (Exception e) {
                results.put(check.getName(), false);
                logger.error("Consistency check error: {} - {}", check.getName(), e.getMessage());
            }
        }

        long failedCount = results.values().stream().filter(r -> !r).count();
        logger.info("Data consistency checks completed. Passed: {}, Failed: {}",
            results.size() - failedCount, failedCount);
    }

    public Map<String, Boolean> getCheckResults() {
        Map<String, Boolean> results = new HashMap<>();
        for (ConsistencyCheck check : checks) {
            try {
                results.put(check.getName(), check.execute());
            } catch (Exception e) {
                results.put(check.getName(), false);
            }
        }
        return results;
    }

    private static class ConsistencyCheck {
        private final String name;
        private final Supplier<Boolean> checkLogic;

        ConsistencyCheck(String name, Supplier<Boolean> checkLogic) {
            this.name = name;
            this.checkLogic = checkLogic;
        }

        String getName() {
            return name;
        }

        boolean execute() {
            return checkLogic.get();
        }
    }
}
