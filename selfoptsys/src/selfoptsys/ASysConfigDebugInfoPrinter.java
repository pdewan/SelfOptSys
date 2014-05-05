package selfoptsys;

import java.io.*;
import java.util.*;

import selfoptsys.config.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public class ASysConfigDebugInfoPrinter {

	private static boolean m_outputDebugInfo = 
		AMainConfigParamProcessor.getInstance().getBooleanParam( Parameters.OUTPUT_SYS_CONFIG_CHANGES );
	private static String m_outputDirectory = 
		AMainConfigParamProcessor.getInstance().getStringParam( Parameters.OUTPUT_DIRECTORY );
	private static String m_outputDebugInfoFile = 
	    AMainConfigParamProcessor.getInstance().getStringParam( Parameters.SYS_CONFIG_CHANGES_OUTPUT_FILE );
	
	public static void outputDebugInfoForRequestedCommArchs(
			String initialMessage,
			int lastSysConfigVersion,
			Map<Integer, ASessionRegistrySysConfigInfo> sysConfigInfos
			) {
		if ( !m_outputDebugInfo ) {
			return;
		}
		
		try {
			PrintStream outputStream = new PrintStream( new FileOutputStream( new File( m_outputDirectory + "\\" + m_outputDebugInfoFile ), false  ) );

			for ( int i = 0; i <= lastSysConfigVersion; i++ ) {
	            outputStream.println( "---------------------------------------------" );
	            outputStream.println( initialMessage );
	            
	            ASessionRegistrySysConfigInfo sysConfigInfo = sysConfigInfos.get( i );

	            List<ASessionRegistryUserInfo> sortedUserInfos = new LinkedList<ASessionRegistryUserInfo>();
                Iterator<ASessionRegistryUserInfo> itr = sysConfigInfo.UserInfos.values().iterator();
                while ( itr.hasNext() ) {
                    ASessionRegistryUserInfo userInfo = itr.next();
                    int insertionPoint = 0;
                    for ( int j = 0; j < sortedUserInfos.size(); j++ ) {
                        if ( userInfo.getUserIndex() > sortedUserInfos.get( j ).getUserIndex() ) {
                            insertionPoint++;
                        }
                        else {
                            break;
                        }
                    }
                    sortedUserInfos.add(
                            insertionPoint,
                            userInfo
                            );
                }
	            
	            outputStream.println( "Sys Config Version: " + i );
	            
                outputStream.println( "MasterUserIndices: " );
	            for ( int j = 0; j < sysConfigInfo.MasterUserIndices.size(); j++ ) {
	                outputStream.print( sysConfigInfo.MasterUserIndices.get( j ) );
	                if ( j != sysConfigInfo.MasterUserIndices.size() - 1 ) {
	                    outputStream.print( "," );
	                }
	            }
	            outputStream.println();
	            
	            outputStream.println( "Overlays:" );
	            for ( int j = 0; j < sortedUserInfos.size(); j++ ) {
	                if ( sortedUserInfos.get( j ).isInputtingCommands() ) {
    	                outputStream.println( "Source user: " + sortedUserInfos.get( j ).getUserIndex() );
    	                outputStream.println( sysConfigInfo.OverlayManager.getCurrentOverlay( sortedUserInfos.get( j ).getUserIndex() ).toString() );
	                }
	            }
	            
	            outputStream.println( "Scheduling policies: " );
	            for ( int j = 0; j < sortedUserInfos.size(); j++ ) {
	                outputStream.print( sortedUserInfos.get( j ).getUserIndex() + ":" + sortedUserInfos.get( j ).getSchedulingPolicy() );
	                if ( j != sortedUserInfos.size() - 1 ) {
	                    outputStream.print( ";" );
	                }
	            }
	            outputStream.println();
	            
	            outputStream.println( "=============================================" );
			}
			
			outputStream.close();
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ACommArchDebugInfoPrinter: Error while outputting information",
                    e
                    );
		}
		

	}
	
}
