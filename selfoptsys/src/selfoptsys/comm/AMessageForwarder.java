package selfoptsys.comm;

import java.util.*;

public abstract class AMessageForwarder
	implements MessageForwarder {

	protected Map<Integer, MessageDest> m_msgDests;
	
    protected int m_userIndex;
    
    public AMessageForwarder(
            int userIndex
            ) {
        m_userIndex = userIndex;
		m_msgDests = new Hashtable<Integer, MessageDest>();
    }

    public abstract void sendMsg( Message msg );
    
    public synchronized void resetMsgSender() {
    	m_msgDests.clear();
    }
    
    public synchronized void addDest(
            int userIndex,
            MessageDest dest
            ) {
    	m_msgDests.put( userIndex, dest );
    }
    
    public synchronized void removeDest(
    		int userIndex
            ) {
        m_msgDests.remove( userIndex );
    }
    
}
