package selfoptsysapps.demo;

import selfoptsys.comm.*;


public interface CommandMessageInfo {

    MessageType getMessageType();
    String getDestinations();
    boolean isForLatecomerOrNewMaster();
    int getSourceUserIndex();
    int getSeqId();
    
    CommandMessage getCommandMessage();
    
}
