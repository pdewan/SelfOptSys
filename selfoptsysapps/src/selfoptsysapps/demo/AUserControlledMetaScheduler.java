package selfoptsysapps.demo;

import java.util.concurrent.*;
import util.models.AListenableVector;

import commonutils.config.SchedulingPolicy;
import selfoptsys.*;
import selfoptsys.comm.*;
import selfoptsys.sched.*;

public class AUserControlledMetaScheduler 
    extends AMetaScheduler
    implements UserControlledMetaScheduler {

    protected SchedulingPolicy m_schedulingPolicyToUse = SchedulingPolicy.UNDEFINED;
    
    protected AListenableVector<CommandMessageInfo> m_commandInfosQueue;
    
    public AUserControlledMetaScheduler(
            SchedulingLogger logger,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
            ) {
        super(
                logger,
                coreToUseForProcessingThread,
                coreToUseForTransmissionThread
                );
        
        m_commandInfosQueue = new AListenableVector<CommandMessageInfo>();
    }
    
    protected void scheduleMsg( 
            CommandMessage msg,
            BlockingQueue<Integer> waitForQ,
            BlockingQueue<Integer> signalForQ
            ) {
        CommandMessageInfo cmdMsgInfo = new ACommandMessageInfo( msg );
        m_commandInfosQueue.add( cmdMsgInfo );
    }
    
    public AListenableVector<CommandMessageInfo> getCommandInfosQueue() {
        return m_commandInfosQueue;
    }
    public void setSchedulingPolicyToUse(
            SchedulingPolicy newSchedulingPolicyToUse
            ) {
        m_schedulingPolicyToUse = newSchedulingPolicyToUse;
    }
    public SchedulingPolicy getSchedulingPolicyToUse() {
        return m_schedulingPolicyToUse;
    }
    
    public void performScheduleMsg( 
            CommandMessageInfo cmdMsgInfo
            ) {
        m_commandInfosQueue.remove( cmdMsgInfo );
        CommandMessage cmdMsg = cmdMsgInfo.getCommandMessage();
        cmdMsg.setSuggestedSchedulingPolicy( m_schedulingPolicyToUse );
        super.scheduleMsg(
                cmdMsg,
                null,
                null
                );
        
    }

}
