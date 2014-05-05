package selfoptsysapps.demo;

import util.models.AListenableVector;
import commonutils.config.*;


public class AUserControlledMetaSchedulerUI 
    implements UserControlledMetaSchedulerUI {
    
    protected UserControlledMetaScheduler m_userControlledMetaScheduler;
    
    public AUserControlledMetaSchedulerUI(
            UserControlledMetaScheduler userControlledMetaScheduler
            ) {
        m_userControlledMetaScheduler = userControlledMetaScheduler;
    }
    
    public void setSchedulingPolicyToUse(
            SchedulingPolicy newSchedulingPolicyToUse
            ) {
        m_userControlledMetaScheduler.setSchedulingPolicyToUse( newSchedulingPolicyToUse );
    }
    public SchedulingPolicy getSchedulingPolicyToUse() {
        return m_userControlledMetaScheduler.getSchedulingPolicyToUse();
    }
    
    public void performScheduleMsg( 
            CommandMessageInfo cmdMsgInfo
            ) {
        m_userControlledMetaScheduler.performScheduleMsg( cmdMsgInfo );
    }
    public AListenableVector<CommandMessageInfo> getCommandInfosQueue() {
        return m_userControlledMetaScheduler.getCommandInfosQueue();
    }

}
