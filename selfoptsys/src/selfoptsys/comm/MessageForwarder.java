package selfoptsys.comm;

public interface MessageForwarder {
	
    void sendMsg( 
    		Message msg 
    		);
    
    void addDest( 
    		int userIndex, 
    		MessageDest dest 
    		);
    
    void removeDest( 
    		int userIndex 
    		);
    
    void resetMsgSender();

}
