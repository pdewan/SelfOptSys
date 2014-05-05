package selfoptsys.comm;


public class ATCPListeningThreadFactorySelector {

    private static TCPListeningThreadFactory m_factory = 
        new ASerializedObjectTCPListeningThreadFactory();
    
    public static void setFactory(
            TCPListeningThreadFactory newFactory
            ) {
        m_factory = newFactory;
    }
    
    public static TCPListeningThreadFactory getFactory() {
        return m_factory;
    }
    
}
