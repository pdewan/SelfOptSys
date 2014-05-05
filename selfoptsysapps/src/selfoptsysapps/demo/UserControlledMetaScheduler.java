package selfoptsysapps.demo;

import util.models.AListenableVector;
import commonutils.config.*;


public interface UserControlledMetaScheduler {

    void setSchedulingPolicyToUse(
            SchedulingPolicy newSchedulingPolicyToUse
            );
    SchedulingPolicy getSchedulingPolicyToUse();
    
    void performScheduleMsg( 
            CommandMessageInfo cmdMsgInfo
            );
    
    AListenableVector<CommandMessageInfo> getCommandInfosQueue();

}
