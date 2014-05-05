package selfoptsys.comm;

import java.net.*;


public class ASerializedObjectTCPListeningThreadFactory 
    implements TCPListeningThreadFactory {

    public TCPListeningThread createThread(
            ServerSocket sock,
            MessageDest messageDest
            ) {
        return new ASerializedObjectTCPListeningThread(
                sock,
                messageDest
                );
    }
    
}
