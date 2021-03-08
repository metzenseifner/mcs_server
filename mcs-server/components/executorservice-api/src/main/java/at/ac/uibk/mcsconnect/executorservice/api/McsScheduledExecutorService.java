package at.ac.uibk.mcsconnect.executorservice.api;

import java.util.concurrent.ScheduledExecutorService;

public interface McsScheduledExecutorService {
    ScheduledExecutorService getScheduledExecutorService();
}
