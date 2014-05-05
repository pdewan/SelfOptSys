package selfoptsys.comm;

import commonutils.scheduling.*;


public interface MessageDestFactory {

    MessageDest createMessageDest(
            LocalMessageDest localMessageDest,
            WindowsThreadPriority windowsPriority,
            WindowsThreadPriority receiveThreadWindowsPriority
            );
    
}
