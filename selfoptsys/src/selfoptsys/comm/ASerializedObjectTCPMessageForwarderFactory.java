package selfoptsys.comm;


public class ASerializedObjectTCPMessageForwarderFactory 
    implements MessageForwarderFactory {

    public MessageForwarder createMessageForwarder(
            int userIndex
            ) {
        return new ASerializedObjectTCPMessageForwarder(
                userIndex
                );
    }
    
}
