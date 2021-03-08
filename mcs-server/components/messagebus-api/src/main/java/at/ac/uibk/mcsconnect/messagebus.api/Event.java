//package at.ac.uibk.mcsconnect.messagebus.api;
//
//import java.util.Set;
//
//public abstract class Event<EventType> implements Message {
//    abstract EventType getEventType(); // default None
//    abstract String getName(); // debug
//    abstract Set<EventCategory> getCategories();
//    abstract T getData();
//
//    // protected boolean handled = false;
//
//    public boolean isInCategory(EventCategory category) {
//        return getCategories().stream().filter(cat -> cat.equals(category)).findFirst().isPresent();
//    }
//
//    @Override
//    public String toString() {
//        return getName();
//    }
//}
