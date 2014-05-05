package selfoptsysapps.ppt;

import powerpoint.*;
import java.io.*;
import java.util.concurrent.*;

import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

import winsyslib.*;

import selfoptsys.config.*;

public class APptApp {

    public static final String POWERPOINT_PROCESS_NAME = "POWERPNT.EXE";
    
    public static Application theApp = null;
    
    /*
     * Setting this to true will stop the aPptLogger from firing back the
     * input command just entered in normal and record modes. Otherwise, 
     * we get into an infinite loop. Since we don't want the "model" to 
     * receive the input, check for this flag in replayToModel in NORMAL 
     * and RECORD MODEs.
     */
    public static boolean skipNextInputCmd = false;
    
    /*
     * Setting this to true will stop the aPptLogger from firing back the
     * output command just received in normal and record modes. Otherwise, 
     * we may ask PowerPoint to repeat the event that caused the output.
     * Since we don't want the "view" to receive the output, check for
     * this flag in replayToView in NORMAL and RECORD MODEs.
     */
    public static boolean skipNextOutputCmd = false;

    /*
     * Setting this to true makes sure that if we manipulate PPT as a
     * result of an output, we don't treat the resulting PPT event as
     * a new input.
     */
    public static boolean skipNextPptEvent = false;
    
    /*
     * Use this queue to block until PowerPoint generates an event in
     * response to a command that we just invoked on the presentation.
     */
    public static BlockingQueue<Integer> waitForPPTEvent = new ArrayBlockingQueue<Integer>( 1 );
       
    public static void main( String[] args ) {

    	// You can get the precompiled Java proxy jar files from here:
    	// http://j-integra.intrinsyc.com/support/kb/article.aspx?id=81869&cNode=3D8W4P
    	
        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance(
                args,
                true
                );
        String configFilePath = mainCpp.getStringParam( Parameters.CUSTOM_CONFIGURATION_FILE );
        AMainConfigParamProcessor.overrideValuesByThoseSpecifiedInSource(
        		mainCpp,
		        ASettingsFileConfigParamProcessor.getInstance(
		                configFilePath,
		                true
		                )
		        );
    	
    	int userIndex = mainCpp.getIntParam( Parameters.USER_INDEX );
    	String registryHost = mainCpp.getStringParam( Parameters.RMI_REGISTRY_HOST );
    	int registryPort = mainCpp.getIntParam( Parameters.RMI_REGISTRY_PORT );
    	
    	/*
    	 * PPT File Dir must be specified as an absolute path. For some reason, specifying
    	 * the relative path of the PPT file to open does not work.
    	 */
    	String pptFilesDir = mainCpp.getStringParam( Parameters.PPT_FILES_DIR );
    	String pptPresentationFile = mainCpp.getStringParam( Parameters.PPT_PRESENTATION_FILE );    	
        String pptSingleSlideFilePrefix = mainCpp.getStringParam( Parameters.PPT_SINGLE_SLIDE_FILE_PREFIX );        
        int pptSingleSlideFileStartNum = mainCpp.getIntParam( Parameters.PPT_SINGLE_SLIDE_FILE_START_NUM );        
        int pptSingleSlideFileEndNum = mainCpp.getIntParam( Parameters.PPT_SINGLE_SLIDE_FILE_END_NUM );        
    	
        String inputDirectory = mainCpp.getStringParam( Parameters.INPUT_DIRECTORY );

        try {
        	/*
        	 * When in a Windows environment, this call should be made
        	 * If in a non-Windows environment, comment out this call
        	 * http://j-integra.intrinsyc.com/support/com/doc/dcom_native.html
        	 * 
        	 * ntvinv.dll must be in the java path, so add <jintegra_install_dir>\bin
        	 * to your path environment variable or add it through 
        	 * java.library.path JVM parameter
        	 * 
        	 * System.setProperty("JINTEGRA_NATIVE_MODE", "");
        	 * 
        	 * ------------------------------
        	 * 
        	 * BUT WHEN RUNNING IN NATIVE MODE, THERE ARE PROBLEMS. 
        	 * For example, we cannot serialize/deserialize COM objects:
        	 * http://j-integra.intrinsyc.com/support/kb/article.aspx?id=101327&query=no+valid+constructor+deserialize
        	 * 
        	 * So we need to use default DCOM mode. To make everything work, please
        	 * the instructions posted here:
        	 * http://j-integra.intrinsyc.com/support/com/doc/remoteaccess.html#winxpsp2
        	 * ---- to run the Component Services in Vista, use the following run command:
        	 *      windows\System32\comexp.msc
        	 * 
        	 * Helpful website:
        	 * http://j-integra.intrinsyc.com/support/kb/article.aspx?id=30337&query=0x5+-+Access+is+denied+powerpoint
        	 * 
        	 * NOTE: when using DCOM mode, I could not open a PPT file programmatically 
        	 * from java. I could start PowerPoint but not load a file. A workaround is
        	 * as follows: run PPT either before starting the java application or using
        	 * Runtime.getRuntime().exec() calls from Java. Once the PPT file is open,
        	 * you can go access it programmatically.
        	 * 
        	 * ------------------------------
        	 * FINAL CONCLUSION:
        	 * Use native mode (don't forget to put ntvinv.dll in the java path). Serialization
        	 * does not actually serialize any of the data. It only serializes proxies.
        	 * 
        	 */
        	
        	System.setProperty("JINTEGRA_NATIVE_MODE", "");
        	
        	Runtime.getRuntime().exec( "taskkill /f /im " + POWERPOINT_PROCESS_NAME );
        	try {
        		Thread.sleep(2000);
        	}
        	catch ( Exception e ) {}
        	
            theApp = new Application();
            theApp.setVisible( 1 );

            int[] coresToUseForExternalLoggableApps = mainCpp.getIntArrayParam( Parameters.CORES_TO_USE_FOR_EXTERNAL_LOGGABLE_APPS );
            coresToUseForExternalLoggableApps = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForExternalLoggableApps );
            WinSysLibUtilities.setProcessorsToUseForProcess_0Based(
                    POWERPOINT_PROCESS_NAME,
                    coresToUseForExternalLoggableApps
                    );
            
            String presPath = pptPresentationFile;
            if ( pptFilesDir != null ) {
            	presPath = inputDirectory + "\\" + pptFilesDir + "\\" + presPath;
            }
            File f = new File( presPath );
            boolean doIEnterFirstCommand = mainCpp.getIntArrayParam( Parameters.USER_TURNS )[ 0 ] == userIndex ? true : false;
            if ( doIEnterFirstCommand == false && f.exists() ) {
                f.delete();
            }
            
            if ( f.exists() ) {
            	theApp.getPresentations().open( f.getAbsolutePath(), 0, 0, -1 );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptApp: error in main while trying to open PowerPoint file",
                    e
                    );
            return;
        }
        
        boolean reportStartPresentationCosts = mainCpp.getBooleanParam( Parameters.PPT_REPORT_START_PRESENTATION_COSTS );
        boolean reportSlideCosts = mainCpp.getBooleanParam( Parameters.PPT_REPORT_SLIDE_COSTS );
        boolean reportAnimationCosts = mainCpp.getBooleanParam( Parameters.PPT_REPORT_ANIMATION_COSTS );

        APptLogger logger = new APptLogger( 
        		userIndex,
        		registryHost,
        		registryPort,
        		pptPresentationFile,
        		inputDirectory + "\\" + pptFilesDir,
        		pptSingleSlideFilePrefix,
        		pptSingleSlideFileStartNum,
        		pptSingleSlideFileEndNum,
        		reportStartPresentationCosts,
        		reportSlideCosts,
        		reportAnimationCosts
        		);
        startListeners( logger );

        String outputDirectory = mainCpp.getStringParam( Parameters.OUTPUT_DIRECTORY );
        boolean printCosts = mainCpp.getBooleanParam( Parameters.PRINT_COSTS );
        logger.setPrintCosts( printCosts );
        String printCostsFile = mainCpp.getStringParam( Parameters.COSTS_OUTPUT_FILE );
        logger.setCostsOutputFile( outputDirectory + "\\" + printCostsFile );

        boolean waitForUserToReleaseTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_RELEASE_TASKS );
        logger.setWaitForUserToReleaseTasks( waitForUserToReleaseTasks );
        boolean waitForUserToScheduleTasks = mainCpp.getBooleanParam( Parameters.WAIT_FOR_USER_TO_SCHEDULE_TASKS );
        logger.setWaitForUserToScheduleTasks( waitForUserToScheduleTasks );
        
        logger.startLogger();
    }

    public static void startListeners( APptLogger logger ) {
        try {
            APptAppListener appListener = new APptAppListener( logger );
            theApp.addEApplicationListener( appListener );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptApp: error while starting application listeners",
                    e
                    );
        }
    }
}
