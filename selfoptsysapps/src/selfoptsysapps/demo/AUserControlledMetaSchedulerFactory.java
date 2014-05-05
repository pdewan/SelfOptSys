package selfoptsysapps.demo;

import selfoptsys.*;
import selfoptsys.sched.*;

public class AUserControlledMetaSchedulerFactory 
	implements MetaSchedulerFactory {

	public Scheduler createScheduler(
	        SchedulingLogger logger,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
			) {
		return new AUserControlledMetaScheduler(
		        logger,
		        coreToUseForProcessingThread,
	            coreToUseForTransmissionThread
	            );
	}
	
}
