package selfoptsysapps.checkers;

import java.awt.*;
import java.util.*;

/*
 *
 * Board
 *
 */
class Board extends Canvas implements BoardDisplay
{
	private static final long serialVersionUID = 2825159108085203483L;
	boolean incomplete = false;
	boolean highlight = false;
	private int start_i,start_j,end_i,end_j;
    
    boolean bWhiteMoved = false;
    boolean bBlackPickedUp = false;
    boolean bBlackPutDown = false;
    
	Checkers game;

//        private int telep_x = 0;
//        private int telep_y = 0;
        private boolean userTelepointing = false;

        Image offImage;
        Graphics offGraphics;

        CheckersEngine eng;
        boolean localMove = false;

        Vector<TelepointerInfo> telepointers = new Vector<TelepointerInfo>();
        
        public synchronized void updateBlackClicksProceed (int i, int j)
        {
          game.updateProceedLabel ("Proceed");
          game.setEnabledProceedButton(false);
        }

	public synchronized void updateBlackPicksUpAt(int i, int j){
			highlight = true;
			start_i = i;
			start_j = j;
			repaint();
            bBlackPickedUp = true;
	}

  public synchronized void updateBlackMouseMove(int i, int j, String gameId)
  {
//    /*if (gameId.equals(game.uniqueID))
//    {
//      return;
//    }*/
//
//    TelepointerInfo teleInfo = null;
//    boolean teleInfoExists = false;
//
//    telep_x = i;
//    telep_y = j;
//
//    for (Enumeration e = telepointers.elements();
//         e.hasMoreElements();
//         )
//    {
//      teleInfo = (TelepointerInfo) e.nextElement();
//      if (teleInfo.gameId.equals(gameId))
//      {
//        teleInfo.x = i;
//        teleInfo.y = j;
//        teleInfoExists = true;
//        break;
//      }
//    }
//
//    if (!teleInfoExists)
//    {
//      telepointers.add(new TelepointerInfo(gameId, i, j));
//    }
//
//    // TO DO: How do I get rid of screen flicker?
//    offscreen_paint(this.getGraphics());

  }

  public boolean mouseMove(Event evt, int x, int y)
  {
    //System.out.println("Mouse moved to x = " + x + ", y = " + y);
//    if (userTelepointing)
//    {
//      if (Math.abs(telep_x - x) + Math.abs(telep_y - y) > 2)
//      {
//        telep_x = x;
//        telep_y = y;
//        eng.eventBlackMouseMove(x, y, game.getUniqueGameID());
//      }
//    }
    
    return true;
  }


	public synchronized void updateBlackPutsDownAt(int i, int j){
		end_i = i;
		end_j = j;
		int status = Move.ApplyMove(game.position,start_i,start_j,end_i,end_j);
		switch (status)
		{
		case Move.LEGALMOVE:
            bBlackPutDown = true;
			incomplete = false;
			highlight = false;
			game.switch_toMove();
                        game.updateProceedLabel("Wait");
			break;
		case Move.INCOMPLETEMOVE:
			incomplete = true;
			highlight = true;
			// the ending square is now starting square
			// for the next capture
			start_i = i;
			start_j = j;
			break;
		} // switch
		repaint();
	}

	public synchronized void updateResetBoard(){
		game.resetBoard2();
	}

	public synchronized void updateWhiteMoved(int score, int[][] newpos, int[] result, int[] counter){
        bWhiteMoved = true;
//        whiteMoveEnd_i = Integer.parseInt( Integer.toString( result[2] )
//                                                  .substring( 0,
//                                                              1 ) );
//        whiteMoveEnd_j = Integer.parseInt( Integer.toString( result[3] )
//                                                  .substring( 0,
//                                                              1 ) );
//        whiteMoveCounter++;
		game.switch_toMove2(score,newpos,result,counter);
	}

	public void setEngine(CheckersEngine ce){
		eng = ce;
	}
	
	Board (Checkers checkers)
	{
		game = checkers;
	}

	public boolean mouseDown(Event evt, int x, int y)
	{
          if (evt.metaDown())
          {
            if (userTelepointing == true) {
              userTelepointing = false;
            }
            else {
              userTelepointing = true;
            }
            return true;
          }
          if (userTelepointing == true) {
            return true;
          }

		int i = x / 30;
		int j = y / 30;
		
		if ( i > 8 || j > 8 ) {
			return true;
		}

		if (game.toMove == Checkers.WHITE &&
			(game.position[i][j] == Checkers.WHITE ||
			 game.position[i][j] == Checkers.WKING)
			 ||
			 game.toMove == Checkers.BLACK &&
			 (game.position[i][j] == Checkers.BLACK ||
			 game.position[i][j] == Checkers.BKING))
		{

			// we don't want to lose the incomplete move info:
			// only set new start variables if !incomplete
			if (!incomplete)
			{
				eng.eventBlackPicksUpAt(i,j);
			}
		}
		else if ( highlight  && (float)(i+j) / 2 != (i+j) / 2)
		{
			localMove = true;
            
			eng.eventBlackPutsDownAt(i,j);
		} // else if
		return true;
	} // MouseDown()

	// to avoid flicker, the board is drawn to offImage using offscreen_paint
	public void paint(Graphics g)
	{
		if (offImage == null)
		{
			offImage = createImage(240,240);
			offGraphics = offImage.getGraphics();
		}
		offscreen_paint(g);
		// g.drawImage(offImage,0,0,this);
	}


    int m_prevColor = -1;
    
	public void offscreen_paint(Graphics g)
	{

		// draw the checkerboard pattern
		for (int i=0; i<8; i++)
			for (int j=0; j<8; j++)
		{
			Color darkgreen = new Color(0,128,0);
			if ( (float)(i+j) / 2 != (i+j) / 2) {
                g.setColor(darkgreen);
            }
			else {
                g.setColor(Color.darkGray);
            }
			g.fillRect(i*30,j*30,30,30);

			if (highlight && i==start_i && j==start_j)
			{
				if (!Move.canCapture(game.position,i,j) && Move.canCapture(game.position,game.toMove))
					g.setColor(Color.blue);
				else
				g.setColor(Color.orange);
				g.fillRect(start_i*30,start_j*30,30,30);
			}

			switch (game.position[i][j])
			{
				case Checkers.WHITE:
				g.setColor(Color.white);
				g.fillOval(i*30+5,j*30+5,20,20);
				break;
				case Checkers.BLACK:
				g.setColor(Color.black);
				g.fillOval(i*30+5,j*30+5,20,20);
				break;
				case Checkers.WKING:
				g.setColor(Color.white);
				g.fillOval(i*30+5,j*30+5,20,20);
				g.setColor(Color.red);
				g.fillOval(i*30+10,j*30+10,10,10);
				break;
				case Checkers.BKING:
				g.setColor(Color.black);
				g.fillOval(i*30+5,j*30+5,20,20);
				g.setColor(Color.yellow);
				g.fillOval(i*30+10,j*30+10,10,10);

			} // switch
		} // for
        
        if ( m_prevColor == -1) {
            g.setColor( Color.blue );
            g.fillRect( 270,
                        270,
                        30,
                        30 );
        }
        
        if (bWhiteMoved || bBlackPickedUp || bBlackPutDown) {
            bWhiteMoved = false;
            bBlackPickedUp = false;
            bBlackPutDown = false;
            
            if (m_prevColor == 0 || m_prevColor == -1) {
                g.setColor( Color.red );
                m_prevColor = 1;
            }
            else if (m_prevColor == 1) {
                g.setColor( Color.blue );
                m_prevColor = 0;
            }
            g.fillRect( 270,
                        270,
                        30,
                        30 );
        }

    TelepointerInfo teleInfo = null;
    for (Enumeration<TelepointerInfo> e = telepointers.elements();
         e.hasMoreElements();
         ) {
      teleInfo = (TelepointerInfo) e.nextElement();
      g.setColor(Color.RED);
      g.fillRect(teleInfo.x, teleInfo.y, 4, 10);
      g.fillRect(teleInfo.x - 3, teleInfo.y + 3, 10, 4);
    }


	} // paint()


	// overriden so as to avoid flicker
	public void update(Graphics g)
	{
		paint(g);
	} // update()

} // Board

