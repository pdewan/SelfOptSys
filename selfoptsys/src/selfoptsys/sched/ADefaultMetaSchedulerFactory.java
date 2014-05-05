package selfoptsys.sched;

import selfoptsys.SchedulingLogger;

public class ADefaultMetaSchedulerFactory 
	implements MetaSchedulerFactory {

	public Scheduler createScheduler(
	        SchedulingLogger logger,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
			) {
		return new AMetaScheduler(
		        logger,
		        coreToUseForProcessingThread,
		        coreToUseForTransmissionThread
	            );
	}
	
}
