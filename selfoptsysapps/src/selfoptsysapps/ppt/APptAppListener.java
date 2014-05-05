package selfoptsysapps.ppt;

import selfoptsys.config.*;
import commonutils.basic.*;
import commonutils.basic2.*;

import powerpoint.*;

public class APptAppListener extends EApplicationAdapter {

	private static final long serialVersionUID = -272472914731591205L;
	private boolean m_presStarted = false;
    private OperationMode m_operationMode;
    
    private APptLogger m_logger = null;

    private ConfigParamProcessor m_mainCpp;
    
    public APptAppListener( APptLogger l ) {
        m_mainCpp = AMainConfigParamProcessor.getInstance();
    	
        m_logger = l;
        m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
    }

    public void slideShowBegin(
            EApplicationSlideShowBeginEvent theEvent )
            throws java.io.IOException,
            com.linar.jintegra.AutomationException {}

    public void slideShowNextBuild( EApplicationSlideShowNextBuildEvent theEvent )
            throws java.io.IOException,
            com.linar.jintegra.AutomationException {
        
        if ( APptApp.skipNextPptEvent ) {
        	APptApp.skipNextPptEvent = false;
        	try {
        		APptApp.waitForPPTEvent.put( 0 );
        	}
        	catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "APptAppListener: Error in slideShowNextBuild",
                        e
                        );
        	}
        	return;
        }
        
        if ( m_operationMode != OperationMode.REPLAY ) {
            APptApp.skipNextInputCmd = true;
            m_logger.inNextBuild( theEvent );
        }
    }

    public void slideShowNextSlide(
            EApplicationSlideShowNextSlideEvent theEvent )
            throws java.io.IOException,
            com.linar.jintegra.AutomationException {
        
        if ( APptApp.skipNextPptEvent ) {
        	APptApp.skipNextPptEvent = false;
        	try {
        		APptApp.waitForPPTEvent.put( 0 );
        	}
        	catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "APptAppListener: Error in slideShowNextSlide",
                        e
                        );
        	}
        	return;
        }
        
        if ( m_operationMode != OperationMode.REPLAY ) {
            APptApp.skipNextInputCmd = true;
            if ( !m_presStarted ) {
                m_logger.inBeginSlideShowWith( theEvent );
            }
            else {
                m_logger.inShowSlide( theEvent );
            }
        }

        m_presStarted = true;
    }

    public void slideShowEnd(
            EApplicationSlideShowEndEvent theEvent )
            throws java.io.IOException,
            com.linar.jintegra.AutomationException {

        if ( APptApp.skipNextPptEvent ) {
        	APptApp.skipNextPptEvent = false;
        	try {
        		APptApp.waitForPPTEvent.put( 0 );
        	}
        	catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "APptAppListener: Error in slideShowEnd",
                        e
                        );
        	}
        	return;
        }
        
        if ( m_operationMode != OperationMode.REPLAY ) {
            APptApp.skipNextInputCmd = true;
            m_logger.inEndSlideShow( theEvent );
        }

        m_presStarted = false;
    }

}
