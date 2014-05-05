package selfoptsys.comm;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import commonutils.basic.*;


public class ANonBlockingTCPMessageForwarder 
    extends ATCPMessageForwarder {
    
    public ANonBlockingTCPMessageForwarder(
            int userIndex
            ) {
        super(
                userIndex
                );
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
            
            Socket sock = m_sockets.get( userIndex );
            SocketChannel sChannel = sock.getChannel();
            sChannel.configureBlocking( false );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANonBlockingTCPMessageForwarder: Error while adding dest",
                    e
                    );
        }
    }
    
    public void sendMsg( 
            Message msg 
            ) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( bos );
            out.writeObject( msg );
            out.close();
            byte[] cmdMsgBytes = bos.toByteArray();
            
            byte[] cmdMsgLenBytes = MiscUtils.intToBigEndianByteArray( cmdMsgBytes.length );
            
            ByteBuffer buf = ByteBuffer.allocateDirect( 4 + cmdMsgBytes.length );
            buf.put( cmdMsgLenBytes );
            buf.put( cmdMsgBytes );
            buf.flip();
            
            sendMsg( buf );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANonBlockingTCPMessageForwarder: Error in sendMsg",
                    e
                    );
        }

    }
    
    /*
     * Need the synchronized keyword because it is possible that the method is called
     * concurrently. In that case, without synchronization, we may attempt to write 
     * to the same stream concurrently, which results in IO exceptions.
     */
    private synchronized void sendMsg( 
            ByteBuffer buf 
            ) {
        try {
            
            Iterator<Map.Entry<Integer, Socket>> itr = 
                m_sockets.entrySet().iterator();
            while ( itr.hasNext() ) {
                SocketChannel sChannel = itr.next().getValue().getChannel();
                int numBytesSent = 0;
                int numBytesToSend = buf.remaining();
                while ( numBytesSent < numBytesToSend ) {
                    numBytesSent += sChannel.write( buf );
                }
                buf.rewind();
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANonBlockingTCPMessageForwarder: Error in sendMsg",
                    e
                    );
        }
    }
    
}
