package at.ac.uibk.mcsconnect.messagebus.api;

public interface Command<R> extends Message {
    R exec();
}
