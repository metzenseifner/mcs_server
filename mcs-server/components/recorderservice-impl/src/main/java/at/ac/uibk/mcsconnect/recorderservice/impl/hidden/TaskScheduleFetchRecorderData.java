package at.ac.uibk.mcsconnect.recorderservice.impl.hidden;

import at.ac.uibk.mcsconnect.common.api.CancellableRunnable;
import at.ac.uibk.mcsconnect.recorderservice.api.Recorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static at.ac.uibk.mcsconnect.common.api.StrUtils.swapNonPrintable;

// Odd-ball


public class TaskScheduleFetchRecorderData<O> implements CancellableRunnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduleFetchRecorderData.class);

    private final Smp351 recorder;
    private final Consumer<Recorder> fetchFunction;
    private final ExecutorService executorService;

    public TaskScheduleFetchRecorderData(Smp351 recorder, Consumer<Recorder> fetchFunction, ExecutorService service) {
        this.recorder = recorder;
        this.fetchFunction = fetchFunction;
        this.executorService = service;
    }

    public void run() {
        LOGGER.debug(String.format("%s.run() called and scheduling function.", this));
        try {
            executorService.execute(() -> fetchFunction.accept(recorder));
        } catch (Exception e) {
            LOGGER.error(String.format("%s.run() caught exception while executing: %s", this, fetchFunction.toString()), e);
        }
    }

    public void cancel() {
        // TODO: implement me
    }

    @Override
    public String toString() {
        swapNonPrintable(""); // to solve java.lang.NoClassDefFoundError: at/ac/uibk/functional/utilities/StrUtil used in recorder toString
        return String.format("%s(%s)", this.getClass().getSimpleName(), recorder);
    }

}
