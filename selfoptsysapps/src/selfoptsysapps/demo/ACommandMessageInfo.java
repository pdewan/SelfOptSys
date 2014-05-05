package selfoptsysapps.demo;

import java.util.*;
import selfoptsys.comm.*;


public class ACommandMessageInfo 
    implements CommandMessageInfo {

    protected CommandMessage m_msg;
    protected String m_destinations;
    
    public ACommandMessageInfo(
            CommandMessage msg
            ) {
        m_msg = msg;

        if ( m_msg.getDestUserIndices() == null || m_msg.getDestUserIndices().size() == 0 ) {
            m_destinations = "ALL";
        }
        else {
            for ( Iterator<Integer> itr = m_msg.getDestUserIndices().iterator(); itr.hasNext(); ) {
                m_destinations += itr.next() + ", ";
            }
            m_destinations = m_destinations.substring( 0, m_destinations.length() - 2 );
        }
    }
    
    public MessageType getMessageType() {
        return m_msg.getMessageType();
    }
    
    public String getDestinations() {
        return m_destinations;
    }
    
    public boolean isForLatecomerOrNewMaster() {
        return m_msg.isMsgForLatecomerOrNewMaster();
    }
    
    public boolean preGetCommandMessage() {
        return false;
    }
    public CommandMessage getCommandMessage() {
        return m_msg;
    }
    
    public int getSourceUserIndex() {
        return m_msg.getSourceUserIndex();
    }
    
    public int getSeqId() {
        return m_msg.getSeqId();
    }
    
}
