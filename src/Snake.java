

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * Starting point for the snake-game project
 * @author pesch
 *
 */
public class Snake
{
    private Board board ;
    private GameControl gc;
    private Player player;
    private Food food;

    private boolean gameOver = false;

    //the actual game grid
    private GridElement[][] grid = null;

    //the size of the grid
    int size;

    //the current moving direction of the snake
    private MoveDirection direction = MoveDirection.RIGHT;

    /**
     * Enum for the direction
     * @author pesch
     *
     */
    enum MoveDirection
    {
        LEFT, RIGHT, UP, DOWN;
    }


    enum GridElement
    {
        HEAD,BODY1,BODY2,FREE,BLOCKED,FOOD;
    }

    public Snake(int size, int scale, int speed)
    {
        this.size = size;

        //center the start position
        int startX = size/2;
        int startY = size/2;


        //inits the relevant game entities
        this.grid = new GridElement[size][size];
        this.gc= new GameControl(speed);
        this.player = new Player(startX,startY);
        this.board = new Board(scale);
        this.food = new Food();


        //inits the grid
        for(int i = 0 ; i< grid.length;i++)
        {
            for(int j = 0 ; j< grid[i].length;j++)
            {
                if ( i == 0)
                    grid[i][j] = GridElement.BLOCKED;

                else if ( j == 0)
                    grid[i][j] = GridElement.BLOCKED;

                else if ( i == size-1)
                    grid[i][j] = GridElement.BLOCKED;

                else if ( j == size-1)
                    grid[i][j] = GridElement.BLOCKED;

                else if ( i == startX && j == startY)
                    grid[i][j] = GridElement.HEAD;
                else
                    grid[i][j] = GridElement.FREE;

            }
        }
        
        grid[0][10] = GridElement.FREE;
        grid[0][30] = GridElement.FREE;
        grid[grid.length-1][10] = GridElement.FREE;
        grid[grid.length-1][30] = GridElement.FREE;
        
        
        for (int i = 0; i < 5; i++) {
        	grid[0][18+i] = GridElement.FREE;
        	grid[grid.length-1][18+i] = GridElement.FREE;
		}
    }


    /**
     * Start the game speeds' timer
     */
    public void start()
    {
        gc.start();
    }

    public void gameOver()
    {
        gc.stop();
    }

    /**
     * Update the board (redraw)
     */
    public void update()
    {

//        grid[player.getCurrentX()][player.getCurrentY()] = GridElement.FREE;
    	/*
    	 * clear all snake parts on board
    	 */
    	for(int[] positions : player.snakeParts){
        	grid[positions[0]][positions[1]] = GridElement.FREE;
        }

        if(player.isDead())
        {
            gameOver = true;
            gc.stop();
        }
        else
        {
            player.move(direction);
        }
        
        /*
         * set all snake parts on board
         */
        boolean isHead = true;
        int counter = 1;
        for(int[] positions : player.snakeParts){
        	if(isHead){
        		grid[positions[0]][positions[1]] = GridElement.HEAD;        		
        		isHead = false;
        	}else{
        		if((counter%3)==0){
        			grid[positions[0]][positions[1]] = GridElement.BODY1;
        		}else{
        			grid[positions[0]][positions[1]] = GridElement.BODY2;        		        			
        		}
        	}
        	counter++;
        }
        
        if(food.getPosX() > 1){
        	grid[food.getPosX()][food.getPosY()] = GridElement.FOOD;        	
        }


        board.update();
    }
    /**
     * @return the actual game board
     */
    public JPanel getBoard()
    {
        return board;
    }


    /**
     * Represents the player (the actual snake)
     * @author pesch
     *
     */
    public class Player
    {
        private int currentX;
        private int currentY;
        
        private ArrayList<int[]> snakeParts = new ArrayList<int[]>();
        
        /**
         * Inits a player on a start position x and y.
         * @param startX
         * @param startY
         */
        public Player(int startX, int startY)
        {
            this.currentX = startX;
            this.currentY = startY;
            
            snakeParts.add(new int[]{startX, startY});
            snakeParts.add(new int[]{startX-1, startY});
            snakeParts.add(new int[]{startX-1, startY});
        }

        /**
         * @return   The current (head) x position
         */
        public int getCurrentX()
        {
            return currentX;
        }
        /**
         * @return   The current (head) y position
         */
        public int getCurrentY()
        {
            return currentY;
        }

        /**
         * Moves the snake in a given direction.
         * @param direction -- the direction either LEFT, RIGHT, TOP, or DOWN
         */
        public void move(MoveDirection direction)
        {
        	
        	Random rand = new Random();
        	
        	int random = rand.nextInt(1 + 1);
        	int yValue = -1;
        	
        	switch (random) {
			case 0:
				yValue = 10;
				break;
			case 1:
				yValue = 30;
				break;
			}
        	
            if ( direction == MoveDirection.LEFT)
            	
            	if(currentX <= 0){
            		currentX = grid.length-1;
            		currentY = yValue;
            	}else{
            		currentX--;            		
            	}
            	
            else if ( direction == MoveDirection.RIGHT)
            	if(currentX >= grid.length-1){
            		currentX = 0;
            		currentY = yValue;
            	}else{
            		currentX++;            		
            	}
            else if ( direction == MoveDirection.UP)
                currentY--;
            else if ( direction == MoveDirection.DOWN)
                currentY++;
            
            
            for(int i = 0; i < player.snakeParts.size()-1; i++){
            	if(player.snakeParts.get(i)[0] == player.currentX && player.snakeParts.get(i)[1] == player.currentY){
            		gameOver = true;
            		gc.stop();
            	}
            }

            snakeParts.add(0, new int[]{currentX, currentY});

            if(currentX == food.getPosX() && currentY == food.getPosY()){
            	food.isFood = false;
            	grid[food.getPosX()][food.getPosY()] = GridElement.FREE;
            }else{
            	snakeParts.remove(snakeParts.size()-1);            	
            }
            
        }

        /**
         * Extends the size of the snake
         */
        public void extend()
        {
        	
        }

        /**
         * Check for collision
         * @return
         */
        public boolean isDead()
        {
        	
        	if(currentX <= 1 || currentX >= size-2 || currentY <= 1 || currentY >= size-2){
        		
        		if(currentY == 10 || currentY == 30){
        			return false;
        		}else if(currentY < 18 || currentY > 23){
        			return true;
        		}
        	}
        	
			return false;
        }
    }
    
    public class Food{
    	
    	boolean isFood = false;
    	
    	private int posX = -1;
    	private int posY = -1;
    	
    	public void createNewFood(){
    		Random rand = new Random();
    		
    		posX = rand.nextInt(((size-3) - 2) + 1) + 2;
     	    posY = rand.nextInt(((size-3) - 2) + 1) + 2;
    		
    		boolean freePosition = false;
    		
    		while(!freePosition){
    			
    			freePosition = true;
    			
    			posX = rand.nextInt(((size-3) - 2) + 1) + 2;
        	    posY = rand.nextInt(((size-3) - 2) + 1) + 2;
    			
        	    for(int[] parts : player.snakeParts){
        	    	if(posX == parts[0] && posY == parts[1]){
        	    		freePosition = false;
        	    	}
        	    }
    		}
    	}

		public int getPosX() {
			return posX;
		}
		public int getPosY() {
			return posY;
		}
    	
    }

    /**
     * Represents control elements such as the 'speed' of the game, and user inputs.
     * @author pesch
     *
     */
    public class GameControl  extends KeyAdapter implements ActionListener
    {
        Board board = null;
        Timer timer = null;

        public GameControl(int speed)
        {
            //see javax.swing.Timer
            this.timer = new Timer(speed,this);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            //action performed after each timer interval
            update();
            if(!food.isFood){
            	food.createNewFood();            	
            	food.isFood = true;
            }
        }

        /**
         * Starts the internal timer
         */
        public void start()
        {
            timer.start();
        }

        public void stop()
        {
            timer.stop();
        }

        /**
         * Updates the direction upon user input
         * @param e -- the key pressed by the user
         */
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            if ((key == KeyEvent.VK_LEFT) && direction != MoveDirection.RIGHT)  {
                direction = MoveDirection.LEFT;
            }

            if ((key == KeyEvent.VK_RIGHT) && direction != MoveDirection.LEFT)  {
                direction = MoveDirection.RIGHT;
            }

            if ((key == KeyEvent.VK_UP) && direction != MoveDirection.DOWN) {
                direction = MoveDirection.UP;
            }

            if ((key == KeyEvent.VK_DOWN) && direction != MoveDirection.UP) {
                direction = MoveDirection.DOWN;
            }

        }

    }

    /**
     * Representation of the game board as JPanel
     * @author pesch
     *
     */
    public class Board extends JPanel
    {
        private static final long serialVersionUID = 1L;

        //scale for the grid
        private int scale_size;

        public Board(int scale)
        {

            this.setFocusable(true);

            //the GameControl is registered as key listener (listing of user inputs)
            addKeyListener(gc);

            this.scale_size = scale;

            setPreferredSize(new Dimension(size*scale, size*scale));

            this.setBackground(Color.DARK_GRAY);

        }

        public void update()
        {
            this.repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            draw(g);
        }


        /**
         * Redraws the board.
         * @param g
         */
        public void draw(Graphics g)
        {

            for(int i = 0 ; i< grid.length;i++)
            {
                for(int j = 0 ; j< grid[i].length;j++)
                {
                	
                    if ( grid[i][j] == GridElement.BLOCKED )
                    {
                        g.setColor(Color.GRAY);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);

                        continue;
                    }

                    if ( grid[i][j] == GridElement.HEAD )
                    {
                        g.setColor(Color.MAGENTA);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);

                        continue;
                    }
                    
                    if ( grid[i][j] == GridElement.BODY1 )
                    {
                        g.setColor(Color.BLACK);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);

                        continue;
                    }
                    if ( grid[i][j] == GridElement.BODY2 )
                    {
                        g.setColor(Color.YELLOW);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);

                        continue;
                    }
                    
                    if ( grid[i][j] == GridElement.FOOD )
                    {
                        g.setColor(Color.RED);
                        
                        g.fillRoundRect(i*scale_size, j*scale_size, scale_size, scale_size, scale_size, scale_size);
                        g.setColor(Color.BLACK);
                        g.drawLine((i*scale_size)+(scale_size/2), (j*scale_size), (i*scale_size)+(scale_size/2), (j*scale_size)+(scale_size/4));
//                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);

                        continue;
                    }

                }
            }
            if ( gameOver)
            {
                g.setColor(Color.RED);
                g.setFont(new Font(Font.MONOSPACED,Font.BOLD,46));
                String str = "GAME OVER";
                float width = g.getFontMetrics().stringWidth(str);

                g.drawString(str, Math.round((this.getWidth()/2)-(width/2)), this.getHeight()/2);
            }

            Toolkit.getDefaultToolkit().sync();

        }

    }

    /**
     * The main method constructs inits. the game and shows the board within a jpanel.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        final Snake snake = new Snake(40,10,120);

        final JFrame frame = new JFrame("Snake");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);
        frame.add(snake.getBoard());
        frame.pack();

        snake.start();
        frame.setVisible(true);

    }

}
