/* Michael Harwood.
 * Nov 2023
 * Program to play the Dots and Boxes game, mostly as a proof of concept to show that if TicTacToe is done correctly
 * it can be used for any grid based game.
 * When you complete a box, your turn does not change.
 * 
 * FIXME If you click perfectly on a line, sometimes it does not register the click! 
 */
//From https://github.com/salamander2/x-and-o-swing/blob/master/Tictactoe3.java

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class DotsGame {

	//inner class to handle all player info
	private class Player {
		int wins=0;
		Color colour = Color.WHITE;
		String name = "";		
		Player(String n, Color c) {name = n; colour = c;}
	}

	/* ===== CONSTANTS ===== */
	final static int SIZE = 4;		//size of board & grid

	final static int BLUE = 0; //IMPORTANT: Do not change these numbers. 
	final static int RED  = 1;
	final static int EMPTY = 99; //This can be any number that is not BLUE or RED

	final static Color COLOURGRID = new Color(140, 140,140);	
	final static Color COLOURBACK = new Color(240, 240, 240);

	//Constants for sides of square
	/* The board[][] integers show what sides are coloured in. Sides are 1,2,4,8 corresponding to Left,Top,Right,Bottom.
	 * Since these are binary type numbers (powers of 2) we can just add the sides and store the number. */
	final static int NOLINE = 0;
	final static int LEFT=1;
	final static int TOP=2;
	final static int RIGHT=4;
	final static int BOTTOM=8;
	final static int COMPLETE=15; //ie L+R+T+B

	/* ===== GLOBAL VARIABLES ===== */
	int player = RED;			//whose turn it is	

	//Info for each of the two players
	Player[] players = {
		new Player("Red", Color.decode("#FFAAAA")), 
		new Player("Blue", Color.decode("#AAAAFF"))
	};

	int[][] board = new int[SIZE+1][SIZE+1];  //+1 to handle sides of the grid
	int[][] squares = new int[SIZE][SIZE];  //Squares contain who owns that box

	JLabel lblStart = new JLabel();		//must be created & initialized here to avoid nullPointer error in initGame().

	public static void main(String[] args) { 
		//It turns out that this is needed to make the repainting happen reliably.
		SwingUtilities.invokeLater(new Runnable() {	public void run() { 
			new DotsGame(); 
		}});
	}

	public DotsGame() {	//constructor
		initGame();
		createAndShowGUI();
	}

	void initGame() {		
		//clear board
		for (int i=0;i<=SIZE;i++) {
			for (int j=0;j<=SIZE;j++) {
				board[i][j]=NOLINE;
				if (i<SIZE && j<SIZE) squares[i][j]=EMPTY;
			}
		}
		lblStart.setText("Score: R=" + players[RED].wins + "   B=" + players[BLUE].wins + "      " + players[player].name + "'s Turn");
		lblStart.setBackground(players[player].colour);
	}

	void createAndShowGUI() {
		JFrame frame = new JFrame("Dots and Boxes");

		//setup top label; text is set in initGame()
		frame.setLayout(new BorderLayout(2,2));				
		lblStart.setFont(new Font("Dialog", Font.BOLD, 15));				
		lblStart.setHorizontalAlignment(SwingConstants.CENTER);
		lblStart.setBackground(new Color(255,255,200));
		lblStart.setOpaque(true);		
		frame.add(BorderLayout.NORTH, lblStart);

		//make main panel
		DrawingPanel gridPanel = new DrawingPanel();
		frame.add(gridPanel, BorderLayout.CENTER);

		initGame();

		//finish setting up the frame
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );		
		frame.pack();	
		frame.setLocationRelativeTo(null);  
		frame.setVisible(true);		
	}

	private class DrawingPanel extends JPanel implements MouseListener{

		//instance variables
		int jpanW, jpanH;	//size of JPanel
		int sqX, sqY;	//size of each square
		int dotR = 4;	//dot radius
		int squareSize = 100; //only used for setting initial size of JPanel.
		int spacing = 50; //border around edges of screen

		DrawingPanel() {
			this.addMouseListener(this);
			setBackground(COLOURBACK);
			this.setPreferredSize(new Dimension(SIZE*squareSize, SIZE*squareSize)); 	
		}

		//** Called by createAndShowGUI()
		void recalcGraphics() {
			jpanW = this.getSize().width - 2*spacing;		
			jpanH = this.getSize().height - 2*spacing;	
			sqX = (int)((jpanW/SIZE)+0.5);
			sqY = (int)((jpanH/SIZE)+0.5);
		}

		@Override
		public void paintComponent(Graphics g){
			super.paintComponent(g); //needed for background colour to paint
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			recalcGraphics(); //needed if the window is resized.

			//Draw faint grid. This shows where the squares would be if it were a standard grid game.
			//However, we need to move everything to the corners of the squares. (huh?)
			g.setColor(Color.LIGHT_GRAY);			
			for (int i=0;i<=SIZE;i++) {
				g.drawLine(sqX*i+spacing,0+spacing,sqX*i+spacing,jpanH+spacing);
				g.drawLine(0+spacing,sqY*i+spacing,jpanW+spacing,sqY*i+spacing);
			}

			g.setColor(Color.BLUE);
			g2.setStroke(new BasicStroke(2));
			g.drawLine(0,0,jpanW+2*spacing,0); //draw top line under the JPanel

			//Colour in squares
			for (int i=0;i<SIZE;i++) {
				for (int j=0;j<SIZE;j++) {	
					if (squares[j][i] != EMPTY) { 
						g.setColor(players[squares[j][i]].colour);
						g.fillRect(i*sqX + spacing, j*sqY + spacing,sqX, sqY);
					}
				}
			}

			g.setColor(Color.DARK_GRAY);
			//draw all circles
			for (int i=0;i<=SIZE;i++) {
				for (int j=0;j<=SIZE;j++) {					
					g.drawOval(i*sqX - dotR + spacing, j*sqY -dotR + spacing, dotR*2, dotR*2);
				}
			}

			//Draw in the lines for each box (if they've been clicked)
			for (int i=0;i<SIZE+1;i++) {
				for (int j=0;j<SIZE+1;j++) {					
					if ((board[j][i] & TOP) == TOP) { 
						g.drawLine(i*sqX + spacing, j*sqY + spacing, (i+1)*sqX + spacing, j*sqY + spacing);
					}
					if ((board[j][i] & BOTTOM) == BOTTOM) { 						
						g.drawLine(i*sqX + spacing, (j+1)*sqY + spacing, (i+1)*sqX + spacing, (j+1)*sqY + spacing);
					}
					if ((board[j][i] & RIGHT) == RIGHT) { 
						g.drawLine(i*sqX + spacing, j*sqY + spacing, i*sqX + spacing, (j+1)*sqY + spacing);
					}
					if ((board[j][i] & LEFT) == LEFT) { 
						g.drawLine((i+1)*sqX + spacing, j*sqY + spacing, (i+1)*sqX + spacing, (j+1)*sqY + spacing);
					}
				}
			}
		}


		//******************* MouseListener methods *****************//

		/* This method for determining where the player clicks could probably be improved.
		 * We want to detect line clicks, but exclude clicking near intersections and near the centre of boxes
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			int far = 20; //20px is so far from line that the mouse click will be ignored. 

			int x = e.getX()-spacing;
			int y = e.getY()-spacing;
			//System.out.println("You clicked "+ x + " " + y);

			//find nearest border
			int bx = (x+sqX/2)/sqX;
			int by = (y+sqY/2)/sqY;	

			boolean onBX = false, onBY=false;

			//either x or y must be on a border but not both!
			if (Math.abs(x - (bx*sqX)) < far) {
				//System.out.println("10 pixels from border " + x/sqX);
				onBX=true;
			}
			if (Math.abs(y - (by*sqY)) < far) { 
				//System.out.println("15 pixels from border " + by + ". x is " + (x-sqX/2)/sqX);
				onBY=true;
			}

			/*** The calculations above here could be improved. The code below here works fine. ***/

			//clicking on a corner or centre
			if (onBX == onBY) return;		//OR  if (onBX ^ onBY) 			

			if (onBY) { //find which box along X axis you clicked in
				if (x-sqX/2 < 0) return; //takes care of the left boundary
				//bx now needs to be the box that you click in and not the border
				bx = x/sqX;

				//You have already clicked on this line
				if ((board[by][bx] & TOP) == TOP) return; 
				if (by>0 && (board[by-1][bx] & BOTTOM) == BOTTOM) return;

				board[by][bx] = board[by][bx] | TOP;				
				if (by>0) board[by-1][bx] = board[by-1][bx] | BOTTOM;

				finishTurn(bx,by,false);  //false = horizontal
			} else {
				if (y-sqY/2 < 0) return; //takes care of the top boundary
				by = y/sqY;

				//You have already clicked on this line
				if ((board[by][bx] & RIGHT) == RIGHT) return; 
				if (bx>0 && (board[by][bx-1] & LEFT) == LEFT) return;

				board[by][bx] = board[by][bx] | RIGHT;
				if (bx>0) board[by][bx-1] = board[by][bx-1] | LEFT;				

				finishTurn(bx,by,true);
			}			

			//lblStart.setText("Score: R=" + wins[RED] + "   B=" + wins[BLUE] + "      " + (player==RED ? "Red" : "Blue") + "'s Turn");
			lblStart.setText(String.format("Score: R=%s   B=%s%10s's Turn", players[RED].wins, players[BLUE].wins, players[player].name));
			lblStart.setBackground(players[player].colour);
			//printBoard();			
			this.repaint();
			checkWin();
		}	

		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}

	} //end of DrawingPanel class

	//This method checks to see if you've made a square
	//if so, update the square array with appropriate player 
	//pass in bx,by and boolean vertical line
	void finishTurn(int x, int y, boolean vertical) {
		boolean changeTurn = true;

		//Did you fill in this (previously empty) square?
		if (board[y][x] == COMPLETE && squares[y][x] == EMPTY) {
			squares[y][x] = player;
			players[player].wins++;
			changeTurn=false;
		}
		//Did you fill in the one on the other side of the line vertically?
		if (vertical) {								
			if (x>0 && board[y][x-1] == COMPLETE && squares[y][x-1] == EMPTY) {
				squares[y][x-1] = player;
				players[player].wins++;
				changeTurn=false; //You do not change tun if you compete a box
			}
		}
		//Did you fill in the one on the other side of the line horizontally? 
		else {
			if (y>0 && board[y-1][x] == COMPLETE && squares[y-1][x] == EMPTY) {
				squares[y-1][x] = player;
				players[player].wins++;
				changeTurn=false;
			}
		}

		if (changeTurn) {
			if (player == RED) player = BLUE; else player = RED;		
		}		
	}

	void checkWin() {

		for (int i=0;i<SIZE;i++) {
			for (int j=0;j<SIZE;j++) {	
				if (squares[j][i] == EMPTY) return;	//an empty square means that you didn't win
			}
		}

		//ok, the board is full so ...
		String s = "";
		if 		(players[RED].wins == players[BLUE].wins) s = "Tie Game!";
		else 	s = (players[BLUE].wins>players[RED].wins) ? "Blue Wins!" : "Red Wins!"; 
		JOptionPane.showMessageDialog(null, s, "Game OVer", JOptionPane.INFORMATION_MESSAGE);
		//clear board, or remove mouseListener or exit game

	}
	void printBoard() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				System.out.printf("%3d", board[row][col]);
			}
			System.out.println();
		}

		for (int i = 0; i < SIZE * 3 + 2; i++)
			System.out.print("=");
		System.out.println();
	}
}
