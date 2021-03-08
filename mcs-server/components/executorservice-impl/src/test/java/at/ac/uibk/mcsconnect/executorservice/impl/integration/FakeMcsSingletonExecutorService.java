package at.ac.uibk.mcsconnect.executorservice.impl.integration;

import at.ac.uibk.mcsconnect.executorservice.api.McsSingletonExecutorService;

import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;

public class FakeMcsSingletonExecutorService implements McsSingletonExecutorService {

    /** Bad design led to this but I left it due lack of time */
    private ExecutorService executorService = mock(ExecutorService.class);

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
