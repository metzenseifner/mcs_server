package at.ac.uibk.mcsconnect.messagebus.impl;

//@Component(
//    name = "at.ac.uibk.mcsconnect.messagebus.impl.MessageBusImpl"
//)
//public class MessageBusImpl implements MessageBus {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusImpl.class);
//
//    private final Map<Command, Callable> COMMAND_HANDLERS;
//    private final Map<Event, List<Callable>> EVENT_HANDLERS;
//
//    @Activate
//    public MessageBusImpl() {
//        this.COMMAND_HANDLERS = new HashMap<>();
//        this.EVENT_HANDLERS = new HashMap<>();
//    }
//
//    @Override
//    public void handle(Message message, AbstractUnitOfWork abstractUnitOfWork) {
//
//    }
//
//    private Object handleCommand(
//            Command command,
//            List<Message> queue,
//            AbstractUnitOfWork uow
//    ) {
//        LOGGER.debug(String.format("Handling command %s", command));
//        try {
//            // TODO Input not typesafe
//            Object result = command.exec();
//            uow.collectNewEvents().stream().forEach(e -> queue.add(e));
//            return result;
//        } catch (Exception e) {
//            LOGGER.error(String.format("Error handling command %s", command));
//            throw e;
//        }
//    }
//
//    private void handleEvent(
//        Event event,
//        Queue<Message> queue,
//        AbstractUnitOfWork uow
//    ) {
//        for (Callable handler : EVENT_HANDLERS.get(event)) {
//            try {
//                LOGGER.debug(String.format("Handling event %s with handler %s", event, handler));
//            } catch (Exception e) {
//                LOGGER.error(String.format("Exception occurred while handling event %s", event));
//                continue;
//            }
//        }
//    }
//}
