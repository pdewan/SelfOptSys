package selfoptsysapps.checkers;

public interface CheckersEngine
{
	public void eventBlackPicksUpAt(int i, int j);
	public void eventBlackPutsDownAt(int i, int j);
	public void eventMoveWhite(int[][] newpos);
	public void eventResetBoard();
	public void setDisplay(BoardDisplay bd);
    public void eventBlackMouseMove(int x, int y, String gameId);
    public void eventBlackClicksProceed (int i, int j);
}
