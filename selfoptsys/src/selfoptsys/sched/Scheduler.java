package selfoptsys.sched;

import commonutils.basic2.*;

public interface Scheduler 
    extends SelfOptArchThread {

	final int MAX_QUEUED_MSGS = 100;
	
	final int NUM_PROC_THREADS = 1;
	final int NUM_TRANS_THREADS = 1;
	
	void scheduleMsgDelivery(
	        SchedulableEntity schedEntity
	        );
	void stopScheduler();
	
	boolean isInOrderMessageDeliveryRequired();
	void isInOrderMessageDeliveryRequired(
	        boolean inOrderMessageDeliveryRequired
	        );
	
	void resetScheduler();
	
}
