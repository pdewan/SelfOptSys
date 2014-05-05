package commonutils.basic;

import java.io.*;
import java.util.logging.*;

public class AMyConsoleHandler extends StreamHandler {

    public AMyConsoleHandler( OutputStream outputStream ) {
        setOutputStream( outputStream );
    }

    public void publish( LogRecord record ) {
        super.publish( record );
        flush();
    }
 
    public void close() {
        flush();
    }
}

//public class AMyConsoleHandler 
//    extends ConsoleHandler
//{
//    public AMyConsoleHandler(
//            OutputStream outputStream
//            ) {
//        super();
//        
//        if ( !outputStream.equals( System.err ) ) {
//            setOutputStream( outputStream );
//        }
//        
//    }
//    
//}
