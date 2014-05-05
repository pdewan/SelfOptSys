package selfoptsys.comm;


public class ALoggableMessageForwarderFactorySelector {

    private static LoggableMessageForwarderFactory m_loggableMessageForwarderFactory = new ASerializedObjectTCPLoggableMessageForwarderFactory();
    
    public static void setFactory(
            LoggableMessageForwarderFactory factory
            ) {
        m_loggableMessageForwarderFactory = factory;
    }
    
    public static LoggableMessageForwarderFactory getFactory() {
        return m_loggableMessageForwarderFactory;
    }
    
}
