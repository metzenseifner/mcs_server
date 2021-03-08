package at.ac.uibk.mcsconnect.common.api.observerpattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Abstraction of the Observed in the Observer Design Pattern.
 *
 * To implement this with maximum flexibility, create an interface
 * to describe the Observer. The subject can then extend this abstract
 * class like MySubject extends Observed<SomeObserverInterface>
 *
 * Threads: CopyOnWriteArrayList was chosen as the implementation because it
 * is thread-safe and requires no locking. It works here, because
 * the list of observers is not expected to changed often, but will
 * be traversed often.
 *
 * @param <T>
 */
@JsonIgnoreProperties
public abstract class Observed<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observed.class);

    private List<T> observerRegistry = new CopyOnWriteArrayList();

    /**
     * Add observer to the list of observers.
     *
     * @param observer
     */
    public T attachObserver(T observer) {
        LOGGER.debug("Attaching observer: {}", observer);
        observerRegistry.add(observer);
        return observer;
    }

    /**
     * Remove observer from the list of observers.
     *
     * @param observer
     */
    public void detachObserver(T observer) {
        LOGGER.debug("Detaching observer: {}", observer);
        observerRegistry.remove(observer);
    }

    public void notifyObservers(Consumer<? super T> method) {
        if (this.observerRegistry.isEmpty()) {
            LOGGER.debug("There are no observers to notify.");
            return;
        }
        LOGGER.debug("Notifying observers: {}", observerRegistry.toString());
        this.observerRegistry.stream().forEach(method);
    }
}
