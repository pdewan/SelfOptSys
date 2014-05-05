package selfoptsys.comm;


public interface MessageForwarderFactory {

    MessageForwarder createMessageForwarder(
            int userIndex
            );
    
}
