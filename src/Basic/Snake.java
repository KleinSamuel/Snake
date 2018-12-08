package Basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * Starting point for the snake-game project
 * @author Samuel Klein
 *
 */
public class Snake {
	
	/* instances of the different game parts (model, view, controller) */
	private Player player;
	private Board board ;
    private GameControl gc;

    private boolean gameOver = false;
    
    /* the actual game grid */
    private GridElement[][] grid = null;
    
    /* the size of the grid */
    int size;
    
    /* the current moving direction of the snake */
    private MoveDirection direction = MoveDirection.RIGHT;
    
    /* Enum for the direction */
    enum MoveDirection {
        LEFT, RIGHT, UP, DOWN;
    }
    
    /* enum for the different states of the game grid blocks */
    enum GridElement {
        HEAD,BODY,FREE,BLOCKED;
    }
    
    
    /* constructor fot the snake class */
    public Snake(int size, int scale, int speed){
    	
        this.size = size;
        
        /* center the snakes start position */
        int startX = size/2;
        int startY = size/2;
        
        
        /* inits the relevant game entities */
        this.grid = new GridElement[size][size];
        this.gc= new GameControl(speed);
        this.player = new Player(startX,startY);
        this.board = new Board(scale);
        
        
        /* initializes the grid by iterating over each row and column*/
        for(int i = 0 ; i< grid.length;i++){
        	
            for(int j = 0 ; j< grid[i].length;j++){
            	
            	/* the first column is a wall */
                if(i == 0){
                	
                    grid[i][j] = GridElement.BLOCKED;
    
                }
                /* the first row is a wall */
                else if(j == 0){
                	
                    grid[i][j] = GridElement.BLOCKED;
                
                }
                /* the last column is a wall */
                else if(i == size-1){
                	
                    grid[i][j] = GridElement.BLOCKED;
                
                }
                /* the last row is a wall */
                else if(j == size-1){
                	
                    grid[i][j] = GridElement.BLOCKED;
                    
                }
                /* the starting point of the snake is the snake head */
                else if(i == startX && j == startY){
                	
                    grid[i][j] = GridElement.HEAD;
                
            	}
                /* everything else on the grid is free */
                else{ 
                    grid[i][j] = GridElement.FREE;
        		}
            }
        }
    }
    
    /* Start the game speeds' timer */
    public void start(){
        gc.start();
    }
    
    /* stops the timer when game is over */
    public void gameOver(){
        gc.stop();
    }
    
    /* Update the board (redraw) THIS METHOD IS CALLED BY THE TIMER */
    public void update(){
        
    	/* set the snakes old position to free */
        grid[player.getCurrentX()][player.getCurrentY()] = GridElement.FREE;
        
        /* check if the game is lost */
        if (player.isDead()){
            gameOver = true;
            gc.stop();
        }
        /* move the snake in the corresponding direction */
        else{
            player.move(direction);
        }
        
        /* get the new position and set it to head */
        grid[player.getCurrentX()][player.getCurrentY()] = GridElement.HEAD;
        
        /* redraw the board */
        board.update();
    }
    
    /* returns the actual game board */
    public JPanel getBoard(){
        return board;
    }
    
    /* represents the player (the actual snake) */
    public class Player {
    	
    	/* current position of the snakes head */
        private int currentX;
        private int currentY;

        /* return the current (head) x position */
        public int getCurrentX(){
            return currentX;
        }
        
        /* returns the current (head) y position */
        public int getCurrentY(){
            return currentY;
        }
        
        /* inits a player on a start position x and y */
        public Player(int startX, int startY){
            this.currentX = startX;
            this.currentY = startY;
        }
        
        /* Moves the snake in a given direction */
        public void move(MoveDirection direction){
        	
        	/* checks the given direction and changes the snakes current coordinates */
            if (direction == MoveDirection.LEFT)
                currentX--;
            else if (direction == MoveDirection.RIGHT)
                currentX++;
            else if (direction == MoveDirection.UP)
                currentY--;
            else if (direction == MoveDirection.DOWN)
                currentY++;
        }
        
        /* Extends the size of the snake */
        public void extend(){
            //TODO: implement the extension.
        }
        
        /* Check for collision */
        public boolean isDead(){
        	/* returns true if snake is out of bounds */
            return (currentX <= 1 || currentX >= size-2 || currentY <= 1 || currentY >= size-2);
        }
    }
    
    /* Represents control elements such as the 'speed' of the game, and user inputs. */
    public class GameControl  extends KeyAdapter implements ActionListener {
        Board board = null;
        Timer timer = null;
        
        public GameControl(int speed){
            /* see javax.swing.Timer */
            this.timer = new Timer(speed,this);
        }
        
        /* this method is called by the timer in given intervals (speed) */
        @Override
        public void actionPerformed(ActionEvent e){
            update();
        }
        
        /* Starts the internal timer */
        public void start(){
            timer.start();
        }
        
        /* Stops the internal timer */
        public void stop(){
            timer.stop();
        }
        
        /* Updates the direction upon user input */
        @Override
        public void keyPressed(KeyEvent e) {

        	/* get the integer code of the pressed key */
            int key = e.getKeyCode();
            
            /* check which key was pressed and change the current direction */
            
            if ((key == KeyEvent.VK_LEFT)){
                direction = MoveDirection.LEFT;
            }

            if ((key == KeyEvent.VK_RIGHT)){
                direction = MoveDirection.RIGHT;
            }

            if ((key == KeyEvent.VK_UP)){
                direction = MoveDirection.UP;
            }

            if ((key == KeyEvent.VK_DOWN)){
                direction = MoveDirection.DOWN;
            }
            
        }

    }
    
    /* Representation of the game board as JPanel */
    public class Board extends JPanel {
    	
    	/* do not note this */
        private static final long serialVersionUID = 1L;

        /* scale for the grid */
        private int scale_size;
        
        public Board(int scale){
    
        	/* sets the focus to the window so key actions are catched */
            this.setFocusable(true);
            
            /* the GameControl is registered as key listener (listing of user inputs) */
            addKeyListener(gc);
            
            this.scale_size = scale;
            
            /* set the size of the window */
            setPreferredSize(new Dimension(size*scale, size*scale));
            
            /* set the background color */
            this.setBackground(Color.WHITE);
              
        }
        
        /* this method calls the paint method below */
        public void update(){
            this.repaint();
        }
        
        /* this method calls the draw method below */
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            draw(g);
        }
       
        
        /* Redraws the board */
        public void draw(Graphics g){
            
        	/* iterate over each row and column of the board and paint it in the corresponding color */
            for(int i = 0 ; i< grid.length;i++){
            	
                for(int j = 0 ; j< grid[i].length;j++){
                	
                	/* check if current grid element is blocked */
                    if(grid[i][j] == GridElement.BLOCKED){
                        g.setColor(Color.GRAY);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                    
                        continue;
                    }
                    
                    /* check if current grid element is the snakes head */
                    if(grid[i][j] == GridElement.HEAD){
                        g.setColor(Color.RED);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                    
                        continue;
                    }
                    
                    /*
                     * TODO: implement checks for BODY and FOOD
                     */
                    
                }
            }
            
            /* check if game is lost and draw GAME OVER message in the window */
            if(gameOver){
                g.setColor(Color.GREEN);
                g.setFont(new Font(Font.MONOSPACED,Font.BOLD,46));
                String str = "Game Over";
                float width = g.getFontMetrics().stringWidth(str);
                
                g.drawString(str, Math.round((this.getWidth()/2)-(width/2)), this.getHeight()/2);
            }
               
            /* do not note this */
            Toolkit.getDefaultToolkit().sync();
            
        }
     
    }
    
    /* The main method constructs inits. the game and shows the board within a jpanel */
    public static void main(String[] args) throws Exception
    {
    	
    	/* create a new instance of the Snake class with parameters */
        final Snake snake = new Snake(40,10,140);
        
        /* create a new window and set title to "Snake" */
        final JFrame frame = new JFrame("Snake");
        
        /* exits the java program when window is closed */
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /* user is not able to resize the window */
        frame.setResizable(false);
        /* add the game board to the window */
        frame.add(snake.getBoard());
        /* sets the size of the window so the board fits in */
        frame.pack();
        /* starts the game */
        snake.start();
        /* set the window to visible */
        frame.setVisible(true);     
        
    }

}