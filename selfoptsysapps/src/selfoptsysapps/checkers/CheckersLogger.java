package selfoptsysapps.checkers;

import selfoptsys.*;

public class CheckersLogger extends ALoggable implements BoardDisplay, CheckersEngine
{
  BoardDisplay bd;
  CheckersEngine ce;
  int updatesConsumed = 0;
  
  private CheckersCommand m_curCmd = null;
  
  boolean m_resetting = false;

  	public CheckersLogger(
  			int userIndex,
  			String registryHost,
  			int registryPort,
  			boolean userInputsCommands
  			) {

  		super( userIndex,
  				registryHost,
  				registryPort,
  				false
  				);
  		
        setOverrideConfigFileUsersWhoInputSettings( false );
        setUserInputsCommands( true );
        setRunningUIAsMaster( true );
        
	  }
  
    public CheckersLogger(
            int userIndex,
            String registryHost,
            int registryPort
            ) {
        super( userIndex,
        	   registryHost,
        	   registryPort,
               false
               );

        setOverrideConfigFileUsersWhoInputSettings( false );
        setUserInputsCommands( true );
        setRunningUIAsMaster( true );
        
    }

    public synchronized void replayToView(Object command) {

    	//System.out.println( "replayToView 1 " + System.currentTimeMillis() );
    	
        CheckersCommand cc = (CheckersCommand) command;
        
        if (cc.cmd.equals("updateBlackPicksUpAt")) {
            bd.updateBlackPicksUpAt(cc.i, cc.j);
        } else if (cc.cmd.equals("updateBlackPutsDownAt")) {
            bd.updateBlackPutsDownAt(cc.i, cc.j);
        } else if (cc.cmd.equals("updateResetBoard")) {
            bd.updateResetBoard();
        } else if (cc.cmd.equals("updateBlackMouseMove")) {
            bd.updateBlackMouseMove(cc.i, cc.j, cc.gameId);
        } else if (cc.cmd.equals("updateBlackClicksProceed")) {
            bd.updateBlackClicksProceed(cc.i, cc.j);
        } else /* if(cc.cmd.equals("updateWhiteMoved")) */{
            bd.updateWhiteMoved(cc.score, cc.newpos, cc.result, cc.counter);
        }
        
    	//System.out.println( "replayToView 2 " + System.currentTimeMillis() );
    	
    }
  
    public void replayToModel(Object command)
  {
    CheckersCommand cc = (CheckersCommand) command;
    
    for (int k = 0; k < cc.getPreCommands()
                              .size(); k++) {
            if (cc.getPreCommands()
                  .elementAt( k ).cmd.equals( "eventBlackMouseMove" )) {
                bd.updateBlackMouseMove( cc.getPreCommands()
                                           .elementAt( k ).i,
                                         cc.getPreCommands()
                                           .elementAt( k ).j,
                                         cc.getPreCommands()
                                           .elementAt( k ).gameId );
            }
        }
    
    if(cc.cmd.equals("eventBlackPicksUpAt")){
      ce.eventBlackPicksUpAt(cc.i,cc.j);
    } else if(cc.cmd.equals("eventBlackPutsDownAt")){
      ce.eventBlackPutsDownAt(cc.i,cc.j);
    } else if(cc.cmd.equals("eventResetBoard")){
      ce.eventResetBoard();
    } else if (cc.cmd.equals("eventBlackMouseMove")){
      ce.eventBlackMouseMove(cc.i, cc.j, cc.gameId);
    } else if (cc.cmd.equals("eventBlackClicksProceed")){
      ce.eventBlackClicksProceed(cc.i,cc.j);
    } else /* if(cc.cmd.equals("eventMoveWhite")) */ {
      ce.eventMoveWhite(cc.newpos);
    }
  }

  public void updateBlackClicksProceed (int i, int j)
  {
      sendOutputMsg( new CheckersCommand( "updateBlackClicksProceed", i, j ), false, false );
  }

  public void updateBlackMouseMove(int i, int j, String gameId)
  {
      sendOutputMsg( new CheckersCommand( "updateBlackMouseMove", i, j, gameId ), false, false );
  }

    public void updateBlackPicksUpAt( int i,
                                      int j ) {
        CheckersCommand cmd = new CheckersCommand( "updateBlackPicksUpAt",
                                                   i,
                                                   j );
        sendOutputMsg( cmd, false, false );
    }

    public void updateBlackPutsDownAt( int i,
                                       int j ) {
        
        CheckersCommand cmd = new CheckersCommand( "updateBlackPutsDownAt",
                                                   i,
                                                   j );
        sendOutputMsg( cmd, false, false );
    }

  public void updateWhiteMoved(int score, int[][] newpos, int[] result, int[] counter){
      sendOutputMsg( new CheckersCommand( "updateWhiteMoved", score, newpos, result, counter ), true, true );
  }

  public void updateResetBoard(){
	  if ( !m_resetting ) {
		  sendOutputMsg( new CheckersCommand( "updateResetBoard" ), false, false );
	  }
  }

  public void setEngine(CheckersEngine ce){
    this.ce = ce;
  }

  public synchronized void eventBlackClicksProceed (int i, int j)
  {
    sendInputMsg( new CheckersCommand( "eventBlackClicksProceed", i, j ), false, false );
  }

  public synchronized void eventBlackPicksUpAt( int i, int j ) {
      CheckersCommand cmd = new CheckersCommand( "eventBlackPicksUpAt",
                                                 i,
                                                 j );
      if (m_curCmd != null) {
          for (int k = 0; k < m_curCmd.getPreCommands()
                                      .size(); k++) {
              cmd.addPreCommand( m_curCmd.getPreCommands()
                                         .elementAt( k ) );
          }
      }
      m_curCmd = null;
      
      sendInputMsg( cmd, false, false );
  }

  public synchronized void eventBlackPutsDownAt(int i, int j){
      CheckersCommand cmd = new CheckersCommand( "eventBlackPutsDownAt",
                                                 i,
                                                 j );
      if (m_curCmd != null) {
          for (int k = 0; k < m_curCmd.getPreCommands()
                                      .size(); k++) {
              cmd.addPreCommand( m_curCmd.getPreCommands()
                                         .elementAt( k ) );
          }
      }
      m_curCmd = null;
      
      sendInputMsg( cmd, false, false );
  }

  public synchronized void eventBlackMouseMove(int i, int j, String gameId)
  {
      if ( m_curCmd == null) {
          m_curCmd = new CheckersCommand( "temp" );
      }
      m_curCmd.addPreCommand( new CheckersCommand( "eventBlackMouseMove", i, j, gameId ) );

  }

  public void eventMoveWhite(int[][] newpos){
	  sendInputMsg( new CheckersCommand( "eventMoveWhite", newpos ), true, true );
  }

  public synchronized void eventResetBoard(){
	  if ( !m_resetting ) {
		  sendInputMsg( new CheckersCommand( "eventResetBoard" ), false, false );
	  }
  }

  public void setDisplay(BoardDisplay bd){
    this.bd = bd;
  }
  
  public void quit() {
      super.quit();
  }
  
  public void sessionStarted() {
      return;
  }
  
  public void resetLoggableForArchChange(
		  int masterUserIndex
		  ) {
	  
	  m_resetting = true;
	  if ( m_myLogger != null ) {
		  if ( masterUserIndex != m_myLogger.getUserIndex() ) {
			  //ce.eventResetBoard();
		  }
		  else {
			  ce.eventResetBoard();
			  bd.updateResetBoard();
		  }
	  }
	  m_resetting = false;
	  
	  super.resetLoggableForArchChange( masterUserIndex );
  }
  
  public boolean shouldProcCostForMsgBeReported(
          Object msg
          ) {
      CheckersCommand cmd = (CheckersCommand) msg;
      if ( cmd.cmd.equals( "eventMoveWhite" ) ) {
          return true;
      }
      
      return false;
  }
  public boolean shouldTransCostForMsgBeReported(
          Object msg
          ) {
      CheckersCommand cmd = (CheckersCommand) msg;
      if ( cmd.cmd.equals( "eventMoveWhite" ) ) {
          return true;
      }
      
      return false;
  }

  
}
