package selfoptsysapps.checkers;

import selfoptsys.config.*;
import commonutils.basic2.*;

public class PseudoEngine implements CheckersEngine
{
	Checkers game;
	BoardDisplay display;

	private ConfigParamProcessor m_mainCpp;
	
	public PseudoEngine(Checkers checkers){
		m_mainCpp = AMainConfigParamProcessor.getInstance();
          game = checkers;
	}

        public synchronized void eventBlackClicksProceed (int i, int j)
        {
          display.updateBlackClicksProceed(-1, -1);
        }

	public synchronized void eventBlackPicksUpAt(int i, int j){
          display.updateBlackPicksUpAt(i,j);
	}

	public synchronized void eventBlackPutsDownAt(int i, int j){
          display.updateBlackPutsDownAt(i,j);
	}

    public synchronized void eventBlackMouseMove(int i, int j, String gameId)
    {
      display.updateBlackMouseMove(i,j, gameId);
    }

	public synchronized void eventResetBoard(){
		display.updateResetBoard();
	}

	public synchronized void eventMoveWhite(int[][] newpos){
		int score;
		int[] result = new int[4];
		int[] counter = new int[1];
		counter[0] = 0;
		OperationMode operationMode = OperationMode.valueOf( m_mainCpp.getStringParam ( Parameters.OPERATION_MODE ) );
		if ( operationMode != OperationMode.REPLAY ) {
			System.out.println("");
			System.out.println( "user " + game.getUserIndex() + ": calculating moves" );
		}
		score = Engine.MiniMax(newpos,0,Integer.parseInt(game.maxDepth.getSelectedItem()),result,Checkers.WHITE,counter);
		display.updateWhiteMoved(score,newpos,result,counter);
	}

	public void setDisplay(BoardDisplay bd){
		display = bd;
	}
}
