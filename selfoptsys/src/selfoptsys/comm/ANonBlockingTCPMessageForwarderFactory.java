package selfoptsys.comm;


public class ANonBlockingTCPMessageForwarderFactory 
    implements MessageForwarderFactory {

    public MessageForwarder createMessageForwarder(
            int userIndex
            ){
        return new ANonBlockingTCPMessageForwarder(
                userIndex
                );
    }
    
}
