package selfoptsys.comm;

import commonutils.scheduling.*;


public interface LoggableMessageDestFactory {

    MessageDest createLoggableMessageDest(
            LocalMessageDest localMessageDest,
            boolean simulatingNetworkLatencies,
            WindowsThreadPriority windowsPriority
            );
    
}
