package selfoptsysapps.ppt;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import commonutils.basic.*;
import commonutils.basic2.*;

import powerpoint.*;
import selfoptsys.*;
import selfoptsys.config.*;

public class APptLogger extends ALoggable {
   
    private OperationMode m_operationMode;
    
    private PptPrefetchMode m_prefetchMode;
    
    final int PPT_CMD_BEGIN_SHOW = 0;
    final int PPT_CMD_SHOW_SLIDE = 1;
    final int PPT_CMD_NEXT_BUILD = 2;
    final int PPT_CMD_END_SHOW = 3;
    
    String m_presPath = "";
    String m_pptPresentationFile = "";
    String m_pptFilesDir = "";
    String m_pptSingleSlideFilePrefix = "";
    int m_pptSingleSlideFileStartNum = 0;
    int m_pptSingleSlideFileEndNum = 0;
    
    private int m_curSlideNum = 0;
    
    private ConfigParamProcessor m_mainCpp;
    
    boolean m_hadPresFileAtStart = true;
    
    protected boolean m_reportStartPresentationCosts;
    protected boolean m_reportSlideCosts;
    protected boolean m_reportAnimationCosts;
    
    protected List<byte[]> m_slideImages;
    protected List<Presentation> m_slideFilePresentations;
    
	public APptLogger(
            int userIndex,
            String registryHost,
            int registryPort,
            String pptFile,
            String pptFileDir,
            String pptSingleSlideFilePrefix,
            int pptSingleSlideFileStartNum,
            int pptSingleSlideFileEndNum,
            boolean reportStartPresentationCosts,
            boolean reportSlideCosts,
            boolean reportAnimationCosts
            ) {
        super( 
        	userIndex,
        	registryHost,
        	registryPort,
        	false
        	);
        
        setOverrideConfigFileUsersWhoInputSettings( false );
        setUserInputsCommands( true );
        setRunningUIAsMaster( true );
        
        m_mainCpp = AMainConfigParamProcessor.getInstance();
        
        m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
        
		m_prefetchMode = PptPrefetchMode.valueOf( m_mainCpp.getStringParam( Parameters.PPT_PREFETCH_MODE ) );
        
        m_pptPresentationFile = pptFile;
        File f = new File( pptFileDir );
        m_pptFilesDir = f.getAbsolutePath();
        m_presPath = m_pptFilesDir + "\\" + pptFile;
        m_pptSingleSlideFilePrefix = pptSingleSlideFilePrefix;
        m_pptSingleSlideFileStartNum = pptSingleSlideFileStartNum;
        m_pptSingleSlideFileEndNum = pptSingleSlideFileEndNum;
        
        m_reportStartPresentationCosts = reportStartPresentationCosts;
        m_reportSlideCosts = reportSlideCosts;
        m_reportAnimationCosts = reportAnimationCosts;
        
	}
	
	public void startLogger() {
	    try {
	        if ( APptApp.theApp.getPresentations().getCount() > 0 ) {
    	        m_slideImages = new LinkedList<byte[]>();
    	        
        	    if ( m_prefetchMode == PptPrefetchMode.ON_DEMAND || m_prefetchMode == PptPrefetchMode.HYBRID ) {
                    for ( int i = m_pptSingleSlideFileStartNum; i <= m_pptSingleSlideFileEndNum; i++ ) {
                        byte[] slideImage = getPresSlideImageFromExistingFile( m_pptFilesDir + "\\" + m_pptSingleSlideFilePrefix + i + ".ppt" );
                        m_slideImages.add( slideImage );
                    }
                }
	        }
	        else {
	            m_slideFilePresentations = new LinkedList<Presentation>();
	            if ( m_prefetchMode == PptPrefetchMode.ON_DEMAND || m_prefetchMode == PptPrefetchMode.HYBRID ) {
	                for ( int i = m_pptSingleSlideFileStartNum; i <= m_pptSingleSlideFileEndNum; i++ ) {
	                    Presentation p = APptApp.theApp.getPresentations().open(
	                            m_pptFilesDir + "\\" + m_pptSingleSlideFilePrefix + i + ".ppt", -1, -1, 0 
	                            );
	                    m_slideFilePresentations.add( p );
                    }
	            }
	        }
    	    
            super.startLogger();
	    }
	    catch ( Exception e ) {
	        ErrorHandlingUtils.logSevereExceptionAndContinue(
	                "APptLogger: Error while loading slide images at start",
	                e
	                );
	    }
	}
	
	private byte[] getFileImage(
			String filePath
			) {
		byte[] fileImage = null;
		
        try {
            File presFile = new File( filePath );
            FileInputStream in = new FileInputStream( presFile );
            fileImage = new byte[(int) presFile.length()];
            in.read( fileImage, 0, fileImage.length );
            in.close();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error while getting file image from file '" + filePath + "'",
                    e
                    );
        }
		
		return fileImage;
	}

    private File saveFileImage( 
    		String filePath,
            byte[] fileImage
            ) {
    	File file = null;
    	
        try{
            file = new File( filePath );
            FileOutputStream out = new FileOutputStream( file );
            out.write( fileImage, 0, fileImage.length );
            out.close();
        } catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error while saving file image to file '" + filePath + "'",
                    e
                    );
        }
        
        return file;
    }
	
    private byte[] getPresFileImage(
    		String presPath
    		) {
        byte[] presFileImage = getFileImage( presPath );
        return presFileImage;
    }
    
    private File savePresFileImage(
    		String presPath,
    		byte[] presFileData
    		) {
        File file = saveFileImage(
        		presPath,
        		presFileData
        		);
        return file;
    }
    
//    private byte[] getPresSlideImage( 
//    		int slideNum, 
//    		Presentation pres 
//    		) {
//        byte[] slideFileData = null;
//        
//        try {
//            Presentation multiSlidePres = APptApp.theApp.getPresentations().add( 0 );
//            multiSlidePres.getSlides().add(1,PpSlideLayout.ppLayoutTitle);
//            
//            Slides slides = pres.getSlides();
//            slides.item( new Integer( slideNum ) ).copy();
//            multiSlidePres.getSlides().paste( 1 );
//            
//            String slideFilePath = m_pptFilesDir + "/slideNum" + slideNum + ".ppt";
//            multiSlidePres.getSlides().item( new Integer( 2 ) ).delete();
//            multiSlidePres.saveAs( slideFilePath, PpSaveAsFileType.ppSaveAsDefault, -1);
//            multiSlidePres.close();
//
//            slideFileData = getFileImage( slideFilePath );
//            File file = new File( slideFilePath );
//            file.delete();
//        }
//        catch ( Exception e ) {
//            e.printStackTrace();
//        }
//        
//        return slideFileData;
//    	
//    }
    
    private byte[] getPresSlideImageFromExistingFile( 
            String slideFilePath
            ) {
        byte[] slideFileData = null;
        
        try {
            slideFileData = getFileImage( slideFilePath );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error while getting slide image from file '" + slideFilePath + "'",
                    e
                    );
        }
        
        return slideFileData;
        
    }

//    private File savePresSlideImage(
//            byte[] slideImage
//            ) {
//       	String tempFilePath = m_pptFilesDir + "/singleSlide";
//       	File file = saveFileImage(
//       			tempFilePath,
//       			slideImage
//        		);
//        return file;
//    }
    
//    private void insertSlideIntoPres(
//    		Presentation pres,
//    		int slidePos,
//    		byte[] slideImage
//    		) {
//    	try {
//	    	File tempFile = savePresSlideImage( slideImage );
//	    	
//	        Presentation singleSlidePres = APptApp.theApp.getPresentations().open( tempFile.getAbsolutePath(), -1, -1, 0 );
//	        singleSlidePres.getSlides().item( new Integer ( 1 ) ).copy();
//	        
//	        pres.getSlides().paste( slidePos );
//	        
//	        singleSlidePres.close();
//	        tempFile.delete();
//	    }
//	    catch ( Exception e ) {
//	        e.printStackTrace();
//	    }
//    }
    
	public void inBeginSlideShowWith( EApplicationSlideShowNextSlideEvent theEvent ) {
		try {
			/*
			 * Only master users receive actual slides as part of the input
			 * command to begin the slide show
			 */
			byte[] slidesImage = null;
            int slideNum = theEvent.getWn().getView().getSlide().getSlideIndex();
            
            if ( m_prefetchMode == PptPrefetchMode.ALL_AT_ONCE || m_prefetchMode == PptPrefetchMode.HYBRID ) {
            	slidesImage = getPresFileImage( m_presPath );
            }
            
            inInsert( 
                    new APptCommand( PPT_CMD_BEGIN_SHOW, slideNum, slidesImage ),
                    m_reportStartPresentationCosts
                    );
		}
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in inBeginSlideShowWith",
                    e
                    );
        }
	}

	public void inShowSlide( EApplicationSlideShowNextSlideEvent theEvent ) {
		try {
			/*
			 * Master users have all the slides by the time the first show
			 * next slide input command reaches them, so just send cmd
			 */
            int slideNum = theEvent.getWn().getView().getSlide().getSlideIndex();
            
            inInsert(
                    new APptCommand( PPT_CMD_SHOW_SLIDE, slideNum, null ),
                    m_reportSlideCosts
                    );
		} 
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in inShowSlide",
                    e
                    );
		}
	}

    public void inNextBuild( EApplicationSlideShowNextBuildEvent theEvent ) {
        try {
            inInsert(
                    new APptCommand( PPT_CMD_NEXT_BUILD ),
                    m_reportAnimationCosts
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in inNextBuild",
                    e
                    );
        }
    }

	public void inEndSlideShow( EApplicationSlideShowEndEvent theEvent ) {
        try {
            inInsert(
                    new APptCommand( PPT_CMD_END_SHOW ),
                    false
                    );
        } 
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in inEndSlideShow",
                    e
                    );
        }
    }
    
	public synchronized void replayToView( Object command ) {
    	
		if ( m_operationMode != OperationMode.REPLAY && 
                APptApp.skipNextOutputCmd == true 
                ) {
            APptApp.skipNextOutputCmd = false;
            return;
        }
		
    	APptApp.skipNextPptEvent = true;
        
        APptCommand cmd = (APptCommand) command;
        int cmdType = cmd.getCmdType();
        
        try {
            if ( cmdType == PPT_CMD_BEGIN_SHOW ) {
            	/*
            	 * If this is a master, then just start the show, because either the file
            	 * already existed locally, or while processing input command, the slides
            	 * were all added.
            	 * 
            	 * If this is a slave, then first add the first slide to an empty presentation
            	 * and then start the presentation.
            	 */
            	Presentation pres = null;
            	if ( isMaster() ) {
            		pres = APptApp.theApp.getPresentations().item( new Integer( 1 ) );
            	}
            	else {
            		savePresFileImage( m_presPath, cmd.getData() );
                	pres = APptApp.theApp.getPresentations().open( m_presPath, 0, 0, -1 );
            	}
                pres.getSlideShowSettings().run();
            } 
            else if ( cmdType == PPT_CMD_SHOW_SLIDE ) {
            	/*
            	 * If this is a master, then just show the slide.
            	 * 
            	 * Otherwise, first add the slide to the presentation and then show it.
            	 */
            	if ( !isMaster() ) {
            		Presentation pres = APptApp.theApp.getPresentations().item( new Integer( APptApp.theApp.getPresentations().getCount() ) );
//                	insertSlideIntoPres( pres, cmd.getSlidesInsertPos(), cmd.getData() );
            		int slidePos = cmd.getSlidesInsertPos();
                    Presentation singleSlidePres = m_slideFilePresentations.get( slidePos - 1 );
                    singleSlidePres.getSlides().item( new Integer ( 1 ) ).copy();
                    pres.getSlides().paste( slidePos );
            	}
            	
        		APptApp.theApp.getActivePresentation().getSlideShowWindow().getView().gotoSlide( cmd.getSlidesInsertPos(), -1 );
            }
            else if ( cmdType == PPT_CMD_NEXT_BUILD ) {
                APptApp.theApp.getActivePresentation().getSlideShowWindow().getView().next();
            }
            else if ( cmdType == PPT_CMD_END_SHOW ) {
                Presentation pres = APptApp.theApp.getActivePresentation();
                pres.getSlideShowWindow().getView().exit();
            }
            else {
                System.out.println( "ERROR: Unrecognized command type: " + cmdType );
            }
            
            APptApp.waitForPPTEvent.take();            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in replayToView",
                    e
                    );
        }
    }
    
	public synchronized void replayToModel( Object command ) {
    	
        APptCommand cmd = (APptCommand) command;
        int cmdType = cmd.getCmdType();
        
        if ( m_operationMode != OperationMode.REPLAY && 
                APptApp.skipNextInputCmd == true 
                ) {
            APptApp.skipNextInputCmd = false;
            APptApp.skipNextOutputCmd = true;
            boolean reportCosts = false;
            if ( cmdType == PPT_CMD_BEGIN_SHOW ) {
                reportCosts = m_reportStartPresentationCosts;
            }
            else if ( cmdType == PPT_CMD_SHOW_SLIDE ) {
                reportCosts = m_reportSlideCosts;
            }
            else if ( cmdType == PPT_CMD_NEXT_BUILD ) {
                reportCosts = m_reportAnimationCosts;
            }
            outInsert(
                    command,
                    reportCosts
                    );
            return;
        }
		
        try {
            if ( cmdType == PPT_CMD_BEGIN_SHOW ) {
        		/*
            	 * If this is a master the master that is the master of the user entering
            	 * the first input command, do nothing.
            	 * 
            	 * Otherwise, add all the slides to an empty presentation
            	 */
            	
            	File presFile = new File( m_pptFilesDir + "\\" + m_pptPresentationFile );
            	if ( !presFile.exists() ) {
            		m_hadPresFileAtStart = false;
                	savePresFileImage( m_presPath, cmd.getData() );
                	APptApp.theApp.getPresentations().open( m_presPath, 0, 0, -1 );
            	}
            	
                outBeginSlideShowWith( 1 );
                m_curSlideNum = 1;
            } 
            else if ( cmdType == PPT_CMD_SHOW_SLIDE ) {
                if ( getMasterUserIndex() == getUserIndex() ) {
                    m_curSlideNum++;
                    outShowSlide( m_curSlideNum );
                }
            }
            else if ( cmdType == PPT_CMD_NEXT_BUILD ) {
                if ( getMasterUserIndex() == getUserIndex() ) {
                    outNextBuild();
                }
            }
            else if ( cmdType == PPT_CMD_END_SHOW ) {
                if ( getMasterUserIndex() == getUserIndex() ) {
                    outEndSlideShow();
                }                
            }
            else {
                System.out.println( "ERROR: Unrecognized command type: " + cmdType );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in replayToModel",
                    e
                    );
        }
	}

    public void outBeginSlideShowWith( int slideNum ) {
        try {
        	/*
        	 * Slaves never get all slides at once, so just send the 
        	 * slide with which to begin the presentation to the slave
        	 */
        	byte[] slideImage = null;
            
            if ( m_prefetchMode == PptPrefetchMode.ON_DEMAND || m_prefetchMode == PptPrefetchMode.HYBRID ){
//                Presentation pres = APptApp.theApp.getActivePresentation();
//                slideImage = getPresSlideImage( slideNum, pres );
                slideImage = m_slideImages.get( slideNum - 1 );
            }
            
            outInsert(
                    new APptCommand( PPT_CMD_BEGIN_SHOW, slideNum, slideImage ),
                    m_reportStartPresentationCosts
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in outBeginSlideShowWith",
                    e
                    );
        }
	}
    
    public void outShowSlide( int nextSlideAt ) {
        try {
            /*
             * Slaves get a slide as part of output
             */
        	byte[] slideImage = null;

            if ( m_prefetchMode == PptPrefetchMode.ON_DEMAND || m_prefetchMode == PptPrefetchMode.HYBRID) {
//                Presentation pres = APptApp.theApp.getActivePresentation();
//                slideImage = getPresSlideImage( nextSlideAt, pres );
                slideImage = m_slideImages.get( nextSlideAt - 1 );
            }
            
            outInsert(
                    new APptCommand( PPT_CMD_SHOW_SLIDE, nextSlideAt, slideImage ),
                    m_reportSlideCosts
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in outShowSlide",
                    e
                    );
        }
	}
    
    public void outNextBuild() {
        try {
            outInsert(
                    new APptCommand( PPT_CMD_NEXT_BUILD ),
                    m_reportAnimationCosts
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in outNextBuild",
                    e
                    );
        }
    }
    
    public void outEndSlideShow() {
        try {
            outInsert(
                    new APptCommand( PPT_CMD_END_SHOW ),
                    false
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error in outEndSlideShow",
                    e
                    );
        }
    }
    
    public void quit() {
        try {
            APptApp.theApp.quit();
	        System.out.println( "releasing all JIntegra resources" );
	        com.linar.jintegra.Cleaner.releaseAll();
	
	        if ( !m_hadPresFileAtStart ) {
	        	File presFile = new File( m_pptFilesDir + "\\" + m_pptPresentationFile );
	        	presFile.delete();
	        }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "APptLogger: error while trying to quit",
                    e
                    );
        }
        
        super.quit();
    }
    
    public void outInsert(
            Object command,
            boolean reportCosts
            ) {
        sendOutputMsg(
                command,
                reportCosts,
                reportCosts
                );
        return;
    }

    public void inInsert(
            Object command,
            boolean reportCosts
            ) {
        sendInputMsg(
                command,
                reportCosts,
                reportCosts
                );
        return;
    }
    
    public void sessionStarted() {
        return;
    }
    
    public boolean shouldProcCostForMsgBeReported(
            Object msg
            ) {
        APptCommand cmd = (APptCommand) msg;
        if ( cmd.getCmdType() == PPT_CMD_BEGIN_SHOW ) {
            return m_reportStartPresentationCosts;
        }
        else if ( cmd.getCmdType() == PPT_CMD_SHOW_SLIDE ) {
            return m_reportSlideCosts;
        }
        else if ( cmd.getCmdType() == PPT_CMD_NEXT_BUILD ) {
            return m_reportAnimationCosts;
        }
        
        return false;
    }
    public boolean shouldTransCostForMsgBeReported(
            Object msg
            ) {
        APptCommand cmd = (APptCommand) msg;
        if ( cmd.getCmdType() == PPT_CMD_BEGIN_SHOW ) {
            return m_reportStartPresentationCosts;
        }
        else if ( cmd.getCmdType() == PPT_CMD_SHOW_SLIDE ) {
            return m_reportSlideCosts;
        }
        else if ( cmd.getCmdType() == PPT_CMD_NEXT_BUILD ) {
            return m_reportAnimationCosts;
        }
        
        return false;
    }

}
