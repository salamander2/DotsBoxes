/* M. Harwood.
 * Nov 2023
 * Program to play the Dots and Boxes game, mostly as a proof of concept to show that if TicTacToe is done correctly
 * it can be used for any grid based game.
 * 
 * Things to note: 0,1,2 are special numbers in arrays: 0 = empty, 1 = Blue, 2 = Red.
 * 
 * FIXME: It turns out that you can click on a line that's already clicked in order to change your turn without making a move.
 */
//From https://github.com/salamander2/x-and-o-swing/blob/master/Tictactoe3.java

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DotsGame {

	//constants
	final static int SIZE = 5;		//size of board & grid

	final static int BLUE = 1; //IMPORTANT: Do not change these numbers. TODO make an ENUM
	final static int RED = 2;	
	final static Color[] colours = {Color.WHITE, Color.decode("#AAAAFF"), Color.decode("#FFAAAA")};
	final static Color COLOURGRID = new Color(140, 140,140);	
	final static Color COLOURBACK = new Color(240, 240, 240);

	//global variables
	int player = RED;			//whose turn it is
	int[] wins = {0,0,0};		//array element 0 is not used


	/* The board integers show what sides are coloured in. Sides are 1,2,4,8 corresponding to Left,Top,Right,Bottom.
	 * Since these are binary type numbers (powers of 2) we can just add the sides and store the number. */
	int[][] board = new int[SIZE+1][SIZE+1];  //+1 to handle sides of the grid
	int[][] squares = new int[SIZE][SIZE];  //Squares contain who owns that box

	JLabel lblStart = new JLabel();		//must be created & initialized here to avoid nullPointer error in initGame().

	public static void main(String[] args) { 
		//SwingUtilities.invokeLater(new Runnable() {	public void run() { 
			new DotsGame(); 
		//}});
	}

	public DotsGame() {	//constructor
		initGame();
		createAndShowGUI();
	}

	void initGame() {		
		//clear board
		for (int i=0;i<=SIZE;i++) {
			for (int j=0;j<=SIZE;j++) {
				board[i][j]=0;
				if (i<SIZE && j<SIZE) squares[i][j]=0;
			}
		}
		lblStart.setText("Score: R=" + wins[RED] + "   B=" + wins[BLUE] + "      " + (player==RED ? "Red" : "Blue") + "'s Turn");
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
		int squareSize = 100;
		int offset = 50;

		DrawingPanel() {
			this.addMouseListener(this);
			setBackground(COLOURBACK);
			this.setPreferredSize(new Dimension(SIZE*squareSize, SIZE*squareSize)); 	
		}

		//** Called by createAndShowGUI()
		void recalcGraphics() {
			jpanW = this.getSize().width - 2*offset;		
			jpanH = this.getSize().height - 2*offset;	
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
			//However, we need to move everything to the corners of the squares.
			g.setColor(Color.LIGHT_GRAY);			
			for (int i=0;i<=SIZE;i++) {
				g.drawLine(sqX*i+offset,0+offset,sqX*i+offset,jpanH+offset);
				g.drawLine(0+offset,sqY*i+offset,jpanW+offset,sqY*i+offset);
			}

			g.setColor(Color.BLUE);
			g2.setStroke(new BasicStroke(2));
			g.drawLine(0,0,jpanW+2*offset,0); //draw top line under the JPanel

			//Colour in squares
			for (int i=0;i<SIZE;i++) {
				for (int j=0;j<SIZE;j++) {	
					if (squares[j][i] != 0) { 
						g.setColor(colours[squares[j][i]]);
						g.fillRect(i*sqX + offset, j*sqY + offset,sqX, sqY);
					}
				}
			}

			g.setColor(Color.DARK_GRAY);
			//draw all circles
			for (int i=0;i<=SIZE;i++) {
				for (int j=0;j<=SIZE;j++) {					
					g.drawOval(i*sqX - dotR + offset, j*sqY -dotR + offset, dotR*2, dotR*2);
				}
			}

			//Draw in the lines for each box (if they've been clicked)
			for (int i=0;i<SIZE+1;i++) {
				for (int j=0;j<SIZE+1;j++) {					
					if ((board[j][i] & 2) == 2) { //top
						g.drawLine(i*sqX + offset, j*sqY + offset, (i+1)*sqX + offset, j*sqY + offset);
					}
					if ((board[j][i] & 8) == 8) { //bottom						
						g.drawLine(i*sqX + offset, (j+1)*sqY + offset, (i+1)*sqX + offset, (j+1)*sqY + offset);
					}
					if ((board[j][i] & 4) == 4) { //left
						g.drawLine(i*sqX + offset, j*sqY + offset, i*sqX + offset, (j+1)*sqY + offset);
					}
					if ((board[j][i] & 1) == 1) { //right
						g.drawLine((i+1)*sqX + offset, j*sqY + offset, (i+1)*sqX + offset, (j+1)*sqY + offset);
					}
				}
			}
		}


		//******************* MouseListener methods *****************//
		@Override
		public void mouseClicked(MouseEvent e) {
			int far = 20; //20px is too far from line 

			int x = e.getX()-offset;
			int y = e.getY()-offset;
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

			if (onBX == onBY) return;		//OR  if (onBX ^ onBY) {

			if (onBY) { //find which box along X axis you clicked in
				if (x-sqX/2 < 0) return; //takes care of the left boundary
				//bx now needs to be the box that you click in and not the border
				bx = x/sqX;
				board[by][bx] = board[by][bx] | 2;
				if (by>0) board[by-1][bx] = board[by-1][bx] | 8;
				//System.out.println("**");
				finishTurn(bx,by,false);
			} else {
				//System.out.println("++");
				if (y-sqY/2 < 0) return; //takes care of the top boundary
				by = y/sqY;
				board[by][bx] = board[by][bx] | 4;
				if (bx>0) board[by][bx-1] = board[by][bx-1] | 1;				
				//System.out.println("**");
				finishTurn(bx,by,true);
			}			

			//printBoard();
			//this.repaint();
			this.paintImmediately(getBounds());
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

	//pass in bx,by and boolean vertical line
	void finishTurn(int x, int y, boolean vertical) {
		boolean changeTurn = true;
		//check to see if you've made a square
		//if so, update the square array with appropriate player
		if (board[y][x] == 15 && squares[y][x] == 0) {
			squares[y][x] = player;
			wins[player]++;
			changeTurn=false;
		}
		if (vertical) {								
			if (x>0 && board[y][x-1] == 15 && squares[y][x-1] == 0) {
				squares[y][x-1] = player;
				wins[player]++;
				changeTurn=false;
			}				
		} else {
			if (y>0 && board[y-1][x] == 15 && squares[y-1][x] == 0) {
				squares[y-1][x] = player;
				wins[player]++;
				changeTurn=false;
			}
		}

		if (changeTurn) {
			if (player == RED) player = BLUE; else player = RED;		
		}
		//lblStart.setText("Score: R=" + wins[RED] + "   B=" + wins[BLUE] + "      Turn=" + (player==RED ? "R" : "B"));
		//lblStart.setText("Score: R=" + wins[RED] + "   B=" + wins[BLUE] + "      " + (player==RED ? "Red" : "Blue") + "'s Turn");
		lblStart.setText(String.format("Score: R=%s   B=%s%10s's Turn", wins[RED], wins[BLUE], (player==RED ? "Red" : "Blue")));
		lblStart.setBackground(colours[player]);
	}
	void checkWin() {
		boolean win=true;
		for (int i=0;i<SIZE;i++) {
			for (int j=0;j<SIZE;j++) {	
				if (squares[j][i] == 0) { 
					win=false;
					break;
				}
			}
		}
		if (win) {
			String s = "";
			if 		(wins[BLUE] == wins[RED]) s = "Tie Game!";
			else 	s = (wins[BLUE]>wins[RED]) ? "Blue Wins!" : "Red Wins!"; 
			JOptionPane.showMessageDialog(null, s, "Game OVer", JOptionPane.INFORMATION_MESSAGE);
			//clear board, or remove mouseListener or exit game
		}
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
