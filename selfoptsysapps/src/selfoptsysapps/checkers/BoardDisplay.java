package selfoptsysapps.checkers;

public interface BoardDisplay
{
	public void updateBlackPicksUpAt(int i, int j);
	public void updateBlackPutsDownAt(int i, int j);
	public void updateWhiteMoved(int score, int[][] newpos, int[] result, int[] counter);
	public void updateResetBoard();
	public void setEngine(CheckersEngine ce);
        public void updateBlackMouseMove(int i, int j, String gameId);

        public void updateBlackClicksProceed (int i, int j);
}
