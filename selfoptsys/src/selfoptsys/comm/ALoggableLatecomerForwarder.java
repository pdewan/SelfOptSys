package selfoptsys.comm;

import selfoptsys.*;
import selfoptsys.config.*;
import commonutils.scheduling.*;
import commonutils.basic2.*;

public class ALoggableLatecomerForwarder
	extends ASelfOptArchThread {

	CommandMessage[] m_msgs;
	MessageForwarder m_msgForwarder;
	
	public ALoggableLatecomerForwarder(
			int localUserIndex,
			int destUserIndex,
			MessageDest msgDest,
			CommandMessage[] msgs,
			LocalLogger logger
			) {
		
		m_msgs = msgs; 
		
		m_msgForwarder = AMessageForwarderFactorySelector.getFactory().createMessageForwarder(
				localUserIndex
				);
		
		for ( int i = 0; i < m_msgs.length; i++ ) {
    		m_msgForwarder.addDest(
    				destUserIndex,
    				msgDest
    				);
		}
	}
	
	public void run() {
		
	    int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
	    coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
	    
	    super.run();
	    
		for ( int i = 0; i < m_msgs.length; i++ ) {
			m_msgForwarder.sendMsg( m_msgs[i] );
		}
		
		/*
		 * TODO: Need to kill this thread when its done ... and remove dest ...
		 */
		
	}
	
}
