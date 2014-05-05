package selfoptsys;

import java.util.*;
import selfoptsys.network.*;
import selfoptsys.overlay.*;


public class ASessionRegistrySysConfigInfo {

    public int SysConfigVersion;
    
    public Map<Integer, ASessionRegistryUserInfo> UserInfos;
    public Vector<Integer> MasterUserIndices;
    
    public GlobalOverlayManager OverlayManager;
    
    public ASessionRegistrySysConfigInfo() {
        OverlayManager = new AGlobalOverlayManager();
        UserInfos = new Hashtable<Integer, ASessionRegistryUserInfo>();
        MasterUserIndices = new Vector<Integer>();
    }
    
    public static ASessionRegistrySysConfigInfo copy(
            ASessionRegistrySysConfigInfo other
            ) {
        
        ASessionRegistrySysConfigInfo newSysConfig = new ASessionRegistrySysConfigInfo();
        
        newSysConfig.SysConfigVersion = other.SysConfigVersion;
        
        Iterator<Map.Entry<Integer, ASessionRegistryUserInfo>> userInfoItr = other.UserInfos.entrySet().iterator();
        while ( userInfoItr.hasNext() ) {
            ASessionRegistryUserInfo userInfo = ASessionRegistryUserInfo.copy( userInfoItr.next().getValue() );
            newSysConfig.UserInfos.put(
                    userInfo.getUserIndex(),
                    userInfo
                    );
        }
        
        for ( int i = 0; i < other.MasterUserIndices.size(); i++ ) {
            newSysConfig.MasterUserIndices.add( other.MasterUserIndices.get( i ) );
        }
        
        Map<Integer, Overlay> newOverlays = new Hashtable<Integer, Overlay>();
        userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
        while ( userInfoItr.hasNext() ) {
            ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
            newSysConfig.OverlayManager.userJoining(
                    userInfo.getUserIndex(),
                    userInfo.getMasterUserIndex(),
                    userInfo.isInputtingCommands()
                    );
        }
        userInfoItr = newSysConfig.UserInfos.entrySet().iterator();
        while ( userInfoItr.hasNext() ) {
            ASessionRegistryUserInfo userInfo = userInfoItr.next().getValue();
            if ( userInfo.isInputtingCommands() ) {
                newOverlays.put(
                        userInfo.getUserIndex(),
                        other.OverlayManager.getCurrentOverlay( userInfo.getUserIndex() )
                        );
            }
        }
        newSysConfig.OverlayManager.setupOverlayForRootUserIndices( newOverlays );

        return newSysConfig;
    }
}
