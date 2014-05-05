package selfoptsys.perf;

import java.io.*;
import java.util.*;
import selfoptsys.network.*;
import commonutils.config.*;


public class ASysConfig 
    implements SysConfig, Serializable {

    private static final long serialVersionUID = -3910957581593179919L;
    
    protected int m_systemConfigurationVersion;
    protected List<Integer> m_userIndices;
    protected List<Integer> m_masterUserIndices;
    protected Vector<Integer> m_inputtingUserIndices;
    protected Vector<Integer> m_mastersRunningUI;
    protected HashMap<Integer, Overlay> m_networkOverlays;
    protected HashMap<Integer, SchedulingPolicy> m_schedulingPolicies;
    
    public ASysConfig(
            int systemConfigurationVersion,
            List<Integer> userIndices,
            List<Integer> masterUserIndices,
            Vector<Integer> inputtingUserIndices,
            Vector<Integer> mastersRunningUI,
            HashMap<Integer, Overlay> networkOverlays,
            HashMap<Integer, SchedulingPolicy> schedulingPolicies
            ) {
        m_systemConfigurationVersion = systemConfigurationVersion;
        m_userIndices = userIndices;
        m_masterUserIndices = masterUserIndices;
        m_inputtingUserIndices = inputtingUserIndices;
        m_mastersRunningUI = mastersRunningUI;
        m_networkOverlays = networkOverlays;
        m_schedulingPolicies = schedulingPolicies;
    }
    
    public void setSystemConfigurationVersion(
            int newSystemConfigurationVersion
            ) {
        m_systemConfigurationVersion = newSystemConfigurationVersion;
    }
    public int getSystemConfigurationVersion() {
        return m_systemConfigurationVersion;
    }
    
    public void setUserIndices(
            List<Integer> newUserIndices
            ) {
        m_userIndices = newUserIndices;
    }
    public List<Integer> getUserIndices() {
        return m_userIndices;
    }
    
    public void setMasterUserIndices(
            List<Integer> newMasterUserIndices
            ) {
        m_masterUserIndices = newMasterUserIndices;
    }
    public List<Integer> getMasterUserIndices() {
        return m_masterUserIndices;
    }
    
    public void setInputtingUserIndices(
            Vector<Integer> newInputtingUserIndices
            ) {
        m_inputtingUserIndices = newInputtingUserIndices;
    }
    public Vector<Integer> getInputtingUserIndices() {
        return m_inputtingUserIndices;
    }
    
    public void setMastersRunningUI(
            Vector<Integer> newMastersRunningUI
            ) {
        m_mastersRunningUI = newMastersRunningUI;
    }
    public Vector<Integer> getMastersRunningUI() {
        return m_mastersRunningUI;
    }
    
    public void setNetworkOverlays(
            HashMap<Integer, Overlay> newNetworkOverlays
            ) {
        m_networkOverlays = newNetworkOverlays;
    }
    public HashMap<Integer, Overlay> getNetworkOverlays() {
        return m_networkOverlays;
    }
    
    public void setSchedulingPolicies(
            HashMap<Integer, SchedulingPolicy> schedulingPolicies
            ) {
        m_schedulingPolicies = schedulingPolicies;
    }
    public HashMap<Integer, SchedulingPolicy> getSchedulingPolicies() {
        return m_schedulingPolicies;
    }
    
    public boolean equals( Object other ) {
        
        if ( other instanceof SysConfig == false ) {
            return false;
        }
        
        SysConfig otherSysConfig = (SysConfig) other;
        
        if ( otherSysConfig.getUserIndices().size() != m_userIndices.size() ) {
            return false;
        }
        for ( int i = 0; i < m_userIndices.size(); i++ ) {
            if ( otherSysConfig.getUserIndices().contains( m_userIndices.get( i ) ) == false ) {
                return false;
            }
        }
        
        if ( otherSysConfig.getMasterUserIndices().size() != m_masterUserIndices.size() ) {
            return false;
        }
        for ( int i = 0; i < m_masterUserIndices.size(); i++ ) {
            if ( otherSysConfig.getMasterUserIndices().contains( m_masterUserIndices.get( i ) ) == false ) {
                return false;
            }
        }
        
        if ( otherSysConfig.getInputtingUserIndices().size() != m_inputtingUserIndices.size() ) {
            return false;
        }
        for ( int i = 0; i < m_inputtingUserIndices.size(); i++ ) {
            if ( otherSysConfig.getInputtingUserIndices().contains( m_inputtingUserIndices.get( i ) ) == false ) {
                return false;
            }
        }
        
        for ( int i = 0; i < m_inputtingUserIndices.size(); i++ ) {
            Overlay one = m_networkOverlays.get( m_inputtingUserIndices.get( i ) );
            Overlay two = otherSysConfig.getNetworkOverlays().get( m_inputtingUserIndices.get( i ) );
            if ( one.equals( two ) == false ) {
                return false;
            }
        }
        
        for ( int i = 0; i < m_userIndices.size(); i++ ) {
            SchedulingPolicy one = m_schedulingPolicies.get( m_userIndices.get( i ) );
            SchedulingPolicy two = otherSysConfig.getSchedulingPolicies().get( m_userIndices.get( i ) );
            if ( one.equals( two ) == false && one != null && two != null ) {
                return false;
            }
        }
        
        return true;
        
    }

    public static SysConfig deepCopy(
            SysConfig sysConfig
            ) {
        SysConfig sysConfigDeepCopy = new ASysConfig(
                sysConfig.getSystemConfigurationVersion(),
                sysConfig.getUserIndices(),
                sysConfig.getMasterUserIndices(),
                sysConfig.getInputtingUserIndices(),
                sysConfig.getMastersRunningUI(),
                sysConfig.getNetworkOverlays(),
                sysConfig.getSchedulingPolicies()
                );
        return sysConfigDeepCopy;
    }
}
