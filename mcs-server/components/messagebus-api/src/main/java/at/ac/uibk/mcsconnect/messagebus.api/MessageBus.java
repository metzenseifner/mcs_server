package at.ac.uibk.mcsconnect.messagebus.api;

import java.util.List;
import java.util.Map;

/**
 * Dispatcher for {@link Message}.
 */
//public class MessageBus {
//
//    private Map<Class<?>, List<EventHandler>> map;
//
//    public <T> void register(Class<? extends T> typeFilter, EventHandler<T> handler) {
//        map.get(typeFilter).add(handler);
//    }
//
//    //safe since we used a generic method to add
//    @SuppressWarnings("unchecked");
//    public void handleEvent(Event<?> event) {
//            for ( EventHandler handler : map.get(event.getClass()) ) {
//                handler.onDataChanged(event);
//            }
//        }
//
//    void handle(Message message, AbstractUnitOfWork abstractUnitOfWork);
//
//    //void register();
//    //void dispatch(Event<?> event);
//    //void dispatch(Command<?> command);
//}
