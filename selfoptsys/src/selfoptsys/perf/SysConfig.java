package selfoptsys.perf;

import java.util.*;
import commonutils.config.*;
import selfoptsys.network.*;


public interface SysConfig {

    void setSystemConfigurationVersion(
            int newSystemConfigurationVersion
            );
    int getSystemConfigurationVersion();
    
    void setUserIndices(
            List<Integer> newUserIndices
            );
    List<Integer> getUserIndices();
    
    void setMasterUserIndices(
            List<Integer> newMasterUserIndices
            );
    List<Integer> getMasterUserIndices();
    
    void setInputtingUserIndices(
            Vector<Integer> newInputtingUserIndices
            );
    Vector<Integer> getInputtingUserIndices();
    
    void setMastersRunningUI(
            Vector<Integer> newMastersRunningUI
            );
    Vector<Integer> getMastersRunningUI();
    
    void setNetworkOverlays(
            HashMap<Integer, Overlay> newNetworkOverlays
            );
    HashMap<Integer, Overlay> getNetworkOverlays();
    
    void setSchedulingPolicies(
            HashMap<Integer, SchedulingPolicy> schedulingPolicies
            );
    HashMap<Integer, SchedulingPolicy> getSchedulingPolicies();
    
}
