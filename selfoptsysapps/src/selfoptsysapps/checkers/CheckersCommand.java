package selfoptsysapps.checkers;

import java.util.*;

public class CheckersCommand implements java.io.Serializable
{
	private static final long serialVersionUID = 4299878130168205386L;
String cmd;
  String gameId;
  int i, j;
  int score;
  int[][] newpos;
  int[] result;
  int[] counter;

    private Vector<CheckersCommand> m_preCommands = new Vector<CheckersCommand>();
  
  public CheckersCommand(String cmd){
    this.cmd = cmd;
  }

  public CheckersCommand(String cmd, int[][] newpos){
    this.cmd = cmd;
    this.newpos = newpos;
  }

  public CheckersCommand(String cmd, int i, int j){
    this.cmd = cmd;
    this.i = i;
    this.j = j;
  }

  public CheckersCommand(String cmd, int i, int j, int[][] newpos){
      this.cmd = cmd;
      this.i = i;
      this.j = j;
      this.newpos = newpos;
    }
  
  public CheckersCommand(String cmd, int i, int j, String gameId){
    this.cmd = cmd;
    this.i = i;
    this.j = j;
    this.gameId = gameId;
  }

  public CheckersCommand(String cmd, int score, int[][] newpos, int[] result, int[] counter){
    this.cmd = cmd;
    this.score = score;
    this.newpos = newpos;
    this.result = result;
    this.counter = counter;
  }

  public CheckersCommand(String cmd, String gameId, int score, int[][] newpos, int[] result, int[] counter){
    this.cmd = cmd;
    this.score = score;
    this.newpos = newpos;
    this.result = result;
    this.counter = counter;
    this.gameId = gameId;
  }

  
    public void addPreCommand( CheckersCommand cmd ) {
        m_preCommands.add( cmd );
    }
    
    public Vector<CheckersCommand> getPreCommands() {
        return m_preCommands;
    }
}
