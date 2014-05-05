package selfoptsysapps.demo;

import util.models.AListenableVector;


public interface UserControlledLoggerUI {

    AListenableVector<CommandMessageInfo> getProcessTaskInfosQueue();
    AListenableVector<CommandMessageInfo> getTransmitTaskInfosQueue();
    
    void performNextProcessTask();
    void performNextTransmitTask();
    
}
