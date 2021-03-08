package at.ac.uibk.mcsconnect.executorservice.api;

import java.util.concurrent.ExecutorService;

public interface McsSingletonExecutorService {
    ExecutorService getExecutorService();
}
