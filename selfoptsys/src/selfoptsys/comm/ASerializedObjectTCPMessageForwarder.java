package selfoptsys.comm;

import java.io.*;
import java.net.*;
import java.util.*;
import commonutils.basic.*;


public class ASerializedObjectTCPMessageForwarder 
    extends ATCPMessageForwarder {

    protected Map<Integer, ObjectOutputStream> m_ooss;
    
    public ASerializedObjectTCPMessageForwarder(
            int userIndex
            ) {
        super(
                userIndex
                );
        
        m_ooss = new Hashtable<Integer, ObjectOutputStream>();
    }
    
    public void addDest(
            int userIndex,
            MessageDest msgDest
            ) {
        try {
            super.addDest(
                    userIndex,
                    msgDest
                    );
            
            if ( m_ooss.get( userIndex ) == null ) {
                Socket sock = m_sockets.get( userIndex );
                ObjectOutputStream oos = new ObjectOutputStream( sock.getOutputStream() );
                m_ooss.put( userIndex, oos );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASerializedObjectTCPMessageForwarder: Error while adding a dest",
                    e
                    );
        }
    }
    
    public void removeDest(
            int userIndex
            ) {
        try { 
            super.removeDest( 
                    userIndex 
                    );
            
            if ( m_msgDests.get( userIndex ) == null ) {
                m_ooss.remove( userIndex );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASerializedObjectTCPMessageForwarder: Error while removing a dest",
                    e
                    );
        }
        
    }
    
    /*
     * Need the synchronized keyword because it is possible that the method is called
     * concurrently. In that case, without synchronization, we may attempt to write 
     * to the same stream concurrently, which results in IO exceptions.
     */
    public synchronized void sendMsg( 
            Message msg 
            ) {
        try {
            Iterator<Map.Entry<Integer, ObjectOutputStream>> itr = 
                m_ooss.entrySet().iterator();
            while ( itr.hasNext() ) {
                ObjectOutputStream oos = itr.next().getValue();
                oos.writeObject( msg );
                oos.flush();
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASerializedObjectTCPMessageForwarder: Error in sendMsg",
                    e
                    );
        }
    }
    
}
