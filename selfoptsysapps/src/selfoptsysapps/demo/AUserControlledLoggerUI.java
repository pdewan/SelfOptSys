package selfoptsysapps.demo;

import util.models.AListenableVector;


public class AUserControlledLoggerUI 
    implements UserControlledLoggerUI {

    protected UserControlledLogger m_userControlledLogger;
    
    public AUserControlledLoggerUI(
            UserControlledLogger userControlledLogger
            ) {
        m_userControlledLogger = userControlledLogger;
    }
    
    public AListenableVector<CommandMessageInfo> getProcessTaskInfosQueue() {
        return m_userControlledLogger.getProcessTaskInfosQueue();
    }
    
    public AListenableVector<CommandMessageInfo> getTransmitTaskInfosQueue() {
        return m_userControlledLogger.getTransmitTaskInfosQueue();
    }
    
    public void performNextProcessTask() {
        m_userControlledLogger.performNextProcessTask();
    }
    
    public void performNextTransmitTask() {
        m_userControlledLogger.performNextTransmitTask();
    }
}
