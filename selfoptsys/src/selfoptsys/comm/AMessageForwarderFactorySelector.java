package selfoptsys.comm;


public class AMessageForwarderFactorySelector {

    private static MessageForwarderFactory m_messageForwarderFactory = new ASerializedObjectTCPMessageForwarderFactory();
    
    public static void setFactory(
            MessageForwarderFactory factory
            ) {
        m_messageForwarderFactory = factory;
    }
    
    public static MessageForwarderFactory getFactory() {
        return m_messageForwarderFactory;
    }
    
}
