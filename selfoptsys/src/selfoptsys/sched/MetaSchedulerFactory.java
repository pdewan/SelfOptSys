package selfoptsys.sched;

import selfoptsys.*;

public interface MetaSchedulerFactory {

	Scheduler createScheduler(
			SchedulingLogger logger,
            int coreToUseForProcessingThread,
            int coreToUseForTransmissionThread
			);
	
}
