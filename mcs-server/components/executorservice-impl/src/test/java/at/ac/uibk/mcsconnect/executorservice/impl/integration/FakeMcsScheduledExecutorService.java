package at.ac.uibk.mcsconnect.executorservice.impl.integration;

import at.ac.uibk.mcsconnect.executorservice.api.McsScheduledExecutorService;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.mock;

public class FakeMcsScheduledExecutorService implements McsScheduledExecutorService {

    /** Bad design led to this but I left it due lack of time */
    private ScheduledExecutorService mcsScheduledExecutorService = mock(ScheduledExecutorService.class);

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return mcsScheduledExecutorService;
    }
}
