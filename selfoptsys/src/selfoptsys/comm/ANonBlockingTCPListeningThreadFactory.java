package selfoptsys.comm;

import java.net.*;


public class ANonBlockingTCPListeningThreadFactory 
    implements TCPListeningThreadFactory {

    public TCPListeningThread createThread(
            ServerSocket sock,
            MessageDest messageDest
            ) {
        return new ANonBlockingTCPListeningThread(
                sock,
                messageDest
                );
    }
    
}
