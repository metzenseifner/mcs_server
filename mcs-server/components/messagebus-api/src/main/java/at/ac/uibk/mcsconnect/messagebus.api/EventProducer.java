package at.ac.uibk.mcsconnect.messagebus.api;

import java.util.LinkedList;
import java.util.List;

public abstract class EventProducer<E> {
    private List<E> events = new LinkedList<>();
    private void publish(E e) {
        events.add(e);
    }
}
