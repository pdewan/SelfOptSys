package selfoptsysapps.demo;

import util.models.AListenableVector;


public interface UserControlledLogger {

    AListenableVector<CommandMessageInfo> getProcessTaskInfosQueue();
    AListenableVector<CommandMessageInfo> getTransmitTaskInfosQueue();
    
    void performNextProcessTask();
    void performNextTransmitTask();
    
}
