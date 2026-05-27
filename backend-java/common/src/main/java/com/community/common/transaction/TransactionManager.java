package com.community.common.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private final List<Consumer<Void>> beforeCommitCallbacks = new ArrayList<>();
    private final List<Consumer<Void>> afterCommitCallbacks = new ArrayList<>();
    private final List<Consumer<Exception>> afterRollbackCallbacks = new ArrayList<>();

    public TransactionManager onBeforeCommit(Consumer<Void> callback) {
        beforeCommitCallbacks.add(callback);
        return this;
    }

    public TransactionManager onAfterCommit(Consumer<Void> callback) {
        afterCommitCallbacks.add(callback);
        return this;
    }

    public TransactionManager onAfterRollback(Consumer<Exception> callback) {
        afterRollbackCallbacks.add(callback);
        return this;
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(Runnable operation) {
        try {
            logger.debug("Starting transaction");
            operation.run();

            for (Consumer<Void> callback : beforeCommitCallbacks) {
                callback.accept(null);
            }

            logger.debug("Transaction committed successfully");

            for (Consumer<Void> callback : afterCommitCallbacks) {
                callback.accept(null);
            }
        } catch (Exception e) {
            logger.error("Transaction rolled back due to exception: {}", e.getMessage(), e);
            for (Consumer<Exception> callback : afterRollbackCallbacks) {
                callback.accept(e);
            }
            throw e;
        } finally {
            clearCallbacks();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public <T> T executeWithResult(TransactionCallback<T> callback) {
        try {
            logger.debug("Starting transaction with result");
            T result = callback.doInTransaction();

            for (Consumer<Void> beforeCallback : beforeCommitCallbacks) {
                beforeCallback.accept(null);
            }

            logger.debug("Transaction committed successfully with result");

            for (Consumer<Void> afterCallback : afterCommitCallbacks) {
                afterCallback.accept(null);
            }

            return result;
        } catch (Exception e) {
            logger.error("Transaction rolled back due to exception: {}", e.getMessage(), e);
            for (Consumer<Exception> rollbackCallback : afterRollbackCallbacks) {
                rollbackCallback.accept(e);
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        } finally {
            clearCallbacks();
        }
    }

    private void clearCallbacks() {
        beforeCommitCallbacks.clear();
        afterCommitCallbacks.clear();
        afterRollbackCallbacks.clear();
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction() throws Exception;
    }
}
