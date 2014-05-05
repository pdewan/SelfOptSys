package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;

import selfoptsys.comm.*;


public interface SchedulableEntity {

	public CommandMessage getMsg();
	
	public List<BlockingQueue<Integer>> getWaitForQList();

	public List<BlockingQueue<Integer>> getSignalDoneQList();
	
}
