package selfoptsys.comm;

import java.net.*;


public interface TCPListeningThreadFactory {

    TCPListeningThread createThread(
            ServerSocket sock,
            MessageDest messageDest
            );
    
}
