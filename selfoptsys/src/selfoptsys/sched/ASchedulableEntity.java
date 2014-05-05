package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;

import selfoptsys.comm.*;


public class ASchedulableEntity 
	implements SchedulableEntity {

	CommandMessage m_msg;
	List<BlockingQueue<Integer>> m_waitForQList;
	List<BlockingQueue<Integer>> m_signalDoneQList;
	
	public ASchedulableEntity(
			CommandMessage msg,
			List<BlockingQueue<Integer>> waitForQList,
			List<BlockingQueue<Integer>> signalDoneQList
			) {
		m_msg = msg;
		m_waitForQList = waitForQList;
		m_signalDoneQList = signalDoneQList;
	}
	
	public CommandMessage getMsg() {
		return m_msg;
	}
	
	public List<BlockingQueue<Integer>> getWaitForQList() {
		return m_waitForQList;
	}

	public List<BlockingQueue<Integer>> getSignalDoneQList() {
		return m_signalDoneQList;
	}
	
}
