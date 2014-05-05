package selfoptsys;

import java.util.*;
import selfoptsys.perf.*;
import commonutils.config.*;

public class ASessionRegistryUserInfo {
	
	private int m_userIndex;
	private Logger m_userLogger;
	private int m_masterUserIndex;
	private boolean m_isInputtingCommands;
	private boolean m_runningUIAsMaster;
	private SchedulingPolicy m_schedulingPolicy;
	private HashMap<PerformanceParameterType, Double> m_observedTransCosts;
	private boolean m_fakeUser;

	public ASessionRegistryUserInfo(
			int userIndex
			) {
		m_userIndex = userIndex;
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public void setUserLogger(
			Logger newLogger
			) {
		m_userLogger = newLogger;
	}
	public Logger getUserLogger() {
		return m_userLogger;
	}

	public void setMasterUserIndex(
			int newMasterUserIndex
			) {
		m_masterUserIndex = newMasterUserIndex;
	}
	public int getMasterUserIndex() {
		return m_masterUserIndex;
	}
	
	public void setInputsCommands(
			boolean newInputsCommands
			) {
		m_isInputtingCommands = newInputsCommands;
	}
	public boolean isInputtingCommands() {
		return m_isInputtingCommands;
	}
	
	public boolean isMaster() {
		return m_userIndex == m_masterUserIndex;
	}
	
	public void setRunningUIAsMaster(
			boolean runningUIAsMaster
			) {
		m_runningUIAsMaster = runningUIAsMaster;
	}
	public boolean getRunningUIAsMaster() {
		return m_runningUIAsMaster;
	}

    public void setSchedulingPolicy(
            SchedulingPolicy schedulingPolicy
            ) {
        m_schedulingPolicy = schedulingPolicy;
    }
    public SchedulingPolicy getSchedulingPolicy() {
        return m_schedulingPolicy;
    }
    
    public void setObservedTransCosts(
            HashMap<PerformanceParameterType, Double> observedTransCosts
            ) {
        m_observedTransCosts = observedTransCosts;
    }
    public HashMap<PerformanceParameterType, Double> getAllObservedTransCosts() {
        return m_observedTransCosts;
    }
    public double getObservedTransCost(
            PerformanceParameterType costType
            ) {
        return m_observedTransCosts.get( costType );
    }
    
    public void isFakeUser(
            boolean fakeUser
            ) {
        m_fakeUser = fakeUser;
    }
    public boolean isFakeUser() {
        return m_fakeUser;
    }
    
    public static ASessionRegistryUserInfo copy(
            ASessionRegistryUserInfo other
            ) {
        ASessionRegistryUserInfo copy = new ASessionRegistryUserInfo( other.getUserIndex() );
        copy.setInputsCommands( other.isInputtingCommands() );
        copy.setMasterUserIndex( other.getMasterUserIndex() );
        copy.setRunningUIAsMaster( other.getRunningUIAsMaster() );
        copy.setSchedulingPolicy( other.getSchedulingPolicy() );
        copy.setUserLogger( other.getUserLogger() );
        copy.isFakeUser( other.isFakeUser() );
        
        if ( other.getAllObservedTransCosts() != null ) {
            HashMap<PerformanceParameterType, Double> observedTransCosts = new HashMap<PerformanceParameterType, Double>();
            Iterator<Map.Entry<PerformanceParameterType, Double>> observedTransCostItr = other.getAllObservedTransCosts().entrySet().iterator();
            while ( observedTransCostItr.hasNext() ) {
                Map.Entry<PerformanceParameterType, Double> entry = observedTransCostItr.next();
                observedTransCosts.put(
                        entry.getKey(),
                        entry.getValue()
                        );
            }
            copy.setObservedTransCosts( observedTransCosts );
        }

        
        return copy;
    }
}

