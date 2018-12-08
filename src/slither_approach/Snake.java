package slither_approach;


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
    
    private ArrayList<Player> playerList;
    
    private int amountAIs = 10;
    private int factorBigGrid = 10;
    private int amountFoodStart = 1000;

    private boolean gameOver = false;
    
    Random rand = new Random();

    private GridElement[][] bigGrid = null;
    private boolean[][] isFoodGrid = null;
    
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
        HEAD,BODY1,BODY2,BODYLEFT,BODYRIGHT,BODYTAIL,FREE,BLOCKED,FOOD;
    }

    public Snake(int size, int scale, int speed)
    {
        this.size = size;

        //center the start position
        int startX = (size*factorBigGrid)/2;
        int startY = (size*factorBigGrid)/2;

        //inits the relevant game entities
        this.grid = new GridElement[size][size];
        this.gc= new GameControl(speed);
        this.player = new Player(startX,startY);
        this.board = new Board(scale);
        this.food = new Food(amountFoodStart);
        
        
        playerList = new ArrayList<Player>();
        Random rand = new Random();

        for (int i = 0; i < amountAIs; i++) {
        	int posX = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
    	    int posY = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
			playerList.add(new Player(posX,posY));
		}
        
        this.bigGrid = new GridElement[size*factorBigGrid][size*factorBigGrid];
        this.isFoodGrid = new boolean[size*factorBigGrid][size*factorBigGrid];

        food.createNewFood();

        //inits the grid
        for(int i = 0 ; i< bigGrid.length;i++)
        {
            for(int j = 0 ; j< bigGrid[i].length;j++)
            {
                if ( i == 0)
                	bigGrid[i][j] = GridElement.BLOCKED;

                else if ( j == 0)
                	bigGrid[i][j] = GridElement.BLOCKED;

                else if ( i == size-1)
                	bigGrid[i][j] = GridElement.BLOCKED;

                else if ( j == size-1)
                	bigGrid[i][j] = GridElement.BLOCKED;

                else if ( i == startX && j == startY)
                	bigGrid[i][j] = GridElement.HEAD;
                else
                	bigGrid[i][j] = GridElement.FREE;

            }
        }
        
        int i2 = 0;
        int j2 = 0;
        for (int i = -(grid.length/2); i < (grid.length/2); i++) {
			for (int j = -(grid.length/2); j < (grid.length/2); j++) {
				grid[i2][j2] = bigGrid[startX+i][startY+j];
				j2++;
			}
			i2++;
			j2 = 0;
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
    public void update(){

    	/*
    	 * clear all snake parts on board
    	 */
    	for(Object[] positions : player.snakeParts){
        	bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.FREE;
        }
    	for (Player p : playerList) {
    		for(Object[] positions : p.snakeParts){
            	bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.FREE;
            }
    	}

        if(player.isDead()){
        	for(Object[] part : player.snakeParts){
				bigGrid[(int)part[0]][(int)part[1]] = GridElement.FOOD;
			}
        	
        	player.died();
        	
        	int posX = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
    	    int posY = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
        	player = new Player(posX,posY);
        }else{
        	if(player.move(direction)){
        		
        		for(Object[] part : player.snakeParts){
					bigGrid[(int)part[0]][(int)part[1]] = GridElement.FOOD;
				}
            	
            	player.died();
            	
            	int posX = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
        	    int posY = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
            	player = new Player(posX,posY);
            }
        }
        
        ArrayList<Player> toRemove = new ArrayList<Player>();
        
        for (Player p : playerList) {
			if(p.moveRandomAI(p.old)){
				p.died();
				System.out.println("died at -- "+p.currentX+" : "+p.currentY);
				
				for(Object[] part : p.snakeParts){
					bigGrid[(int)part[0]][(int)part[1]] = GridElement.FOOD;
				}
				
				toRemove.add(p);
            }
		}
        
        for(Player t : toRemove){
        	playerList.remove(t);
        	int posX = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
    	    int posY = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
        	playerList.add(new Player(posX,posY));
        }
        
        /*
         * set all snake parts on board
         */
        
        boolean isHead = true;
        int counter = 1;
        for(Object[] positions : player.snakeParts){
        	if(isHead){
        		bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.HEAD;     
//        		bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
        		isHead = false;
        	}else{
        		if((counter%3)==0){
        			bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.BODY1;
//        			bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
        		}else{
        			bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.BODY2;   
//        			bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
        		}
        	}
        	counter++;
        }
        
        for(Player p : playerList){
	        isHead = true;
	        counter = 1;
	        for(Object[] positions : p.snakeParts){
	        	if(isHead){
	        		bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.HEAD;     
	//        		bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
	        		isHead = false;
	        	}else{
	        		if((counter%3)==0){
	        			bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.BODY1;
	//        			bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
	        		}else{
	        			bigGrid[(int) positions[0]][(int) positions[1]] = GridElement.BODY2;   
	//        			bigGrid[(int) positions[0]][(int) positions[1]] = (GridElement) positions[2];
	        		}
	        	}
	        	counter++;
	        }
        }
        
        for(int[] fArray : food.foodArray){
        	bigGrid[fArray[0]][fArray[1]] = GridElement.FOOD;   
        }
        
        int i2 = 0;
        int j2 = 0;
    	for (int i = -(grid.length/2); i < (grid.length/2); i++) {
			for (int j = -(grid.length/2); j < (grid.length/2); j++) {
				
				int x = 0;
				int y = 0;
				
				if((player.currentX+i) < 0 || (player.currentX+i) >= size*factorBigGrid){
					x = Math.abs(player.currentX+i)%size*factorBigGrid;
				}else{
					x = player.currentX+i;
				}
				
				if((player.currentY+j) < 0 || (player.currentY+j) >= size*factorBigGrid){
					y = Math.abs(player.currentX+i)%size*factorBigGrid;
				}else{
					y = player.currentY+j;
				}
				
				grid[i2][j2] = bigGrid[x][y];
				j2++;
			}
			i2++;
			j2 = 0;
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
        private MoveDirection old;
        
        Object[] partInfo = new Object[3];
        
        private ArrayList<Object[]> snakeParts = new ArrayList<Object[]>();
        
        /**
         * Inits a player on a start position x and y.
         * @param startX
         * @param startY
         */
        public Player(int startX, int startY)
        {
            this.currentX = startX;
            this.currentY = startY;
            
            snakeParts.add(new Object[]{startX, startY, GridElement.HEAD});
            snakeParts.add(new Object[]{startX-1, startY, GridElement.BODY1});
            snakeParts.add(new Object[]{startX-2, startY, GridElement.BODY1});
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
        
        private int counter = 1;
        Random ran = new Random();
        int[] target = null;
        
        public boolean moveRandomAI(MoveDirection direction){
        		
    		if(target == null){
        		int di2 = ran.nextInt(amountFoodStart);
        		target = food.foodArray.get(di2);
    		}
    		
    		if(target[0] > currentX && direction != MoveDirection.LEFT){
    			return move(MoveDirection.RIGHT);
    		}
    		if(target[0] < currentX && direction != MoveDirection.RIGHT){
    			return move(MoveDirection.LEFT);
    		}
    		if(target[1] > currentY && direction != MoveDirection.UP){
    			return move(MoveDirection.DOWN);
    		}
    		if(target[1] > currentY && direction != MoveDirection.DOWN){
    			return move(MoveDirection.UP);
    		}
    		
    		return move(direction);
    		eedizze
        }

        /**
         * Moves the snake in a given direction.
         * @param direction -- the direction either LEFT, RIGHT, TOP, or DOWN
         */
        public boolean move(MoveDirection direction)
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
        	
        	GridElement tmpNew = GridElement.BODY1;
        	
            if ( direction == MoveDirection.LEFT){
            	
            	currentX--;  
            	
//            	if(old != direction){            		
//            		tmpNew = GridElement.BODYLEFT;
//            	}
            	
            }else if ( direction == MoveDirection.RIGHT){
            	
            	currentX++;      
            	
//            	if(old != direction){            		
//            		tmpNew = GridElement.BODYRIGHT;
//            	}
            
            }else if ( direction == MoveDirection.UP){
                currentY--;
            }else if ( direction == MoveDirection.DOWN){
                currentY++;
            }
            
            for(int i = 0; i < snakeParts.size()-1; i++){
            	if(((int)snakeParts.get(i)[0]) == currentX && ((int)snakeParts.get(i)[1]) == currentY){
            		System.out.println("die #################");
            		return true;
            	}
            }

            snakeParts.add(0, new Object[]{currentX, currentY,tmpNew});

            boolean flag = false;
            int ind = 0;
            for(int[] fArray : food.foodArray){
            	if(currentX == fArray[0] && currentY == fArray[1]){
            		bigGrid[fArray[0]][fArray[1]] = GridElement.FREE;
            		isFoodGrid[fArray[0]][fArray[1]] = false;
            		food.foodArray.remove(ind);
            		flag = true;
            		target = null;
            		break;
            	}
            	ind++;
            }
            
            if(!flag){
            	snakeParts.remove(snakeParts.size()-1);            	
            }
            
            old = direction;
            return false;
            
        }
        
        public void died(){
        	for(Object[] p : snakeParts){
        		bigGrid[(int)p[0]][(int)p[1]] = GridElement.FREE;
        	}
        }

        /**
         * Check for collision
         * @return
         */
        public boolean isDead()
        {
        	
        	if(currentX <= 1 || currentX >= (size*factorBigGrid)-2 || currentY <= 1 || currentY >= (size*factorBigGrid)-2){
        		
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
    	
    	private int amountFood;
    	
    	public Food(int amount){
    		this.amountFood = amount;
    	}
    	
    	boolean isFood = false;
    	
    	ArrayList<int[]> foodArray = new ArrayList<int[]>();
    	
    	private int posX = -1;
    	private int posY = -1;
    	
    	public void createNewFood(){
    		Random rand = new Random();
    		
    		while(foodArray.size() <= amountFood){
    			
    			posX = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
        	    posY = rand.nextInt((((size*factorBigGrid)-3) - 2) + 1) + 2;
    			
//        	    for(Object[] parts : player.snakeParts){
//        	    	if(posX != ((int)parts[0]) && posY != ((int)parts[1]) && !isFoodGrid[posX][posY]){
        	    		foodArray.add(new int[]{posX,posY});
        	    		isFoodGrid[posX][posY] = true;
//        	    	}
//        	    }
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
            if(food.foodArray.size() <= amountFoodStart){
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
                    
                    if ( grid[i][j] == GridElement.BODYLEFT)
                    {
                        g.setColor(Color.BLACK);
                        g.fillArc(i*scale_size, j*scale_size, scale_size, scale_size, 0, 90);
                        continue;
                    }
                    
                    if ( grid[i][j] == GridElement.BODYRIGHT)
                    {
                        g.setColor(Color.BLACK);
                        g.fillArc(i*scale_size, j*scale_size, scale_size, scale_size, 0, 90);
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
            
            g.setColor(Color.RED);
            g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));
            String str2 = player.currentX+" : "+player.currentY;
            float width2 = g.getFontMetrics().stringWidth(str2);

            g.drawString(str2, Math.round((this.getWidth()/2)-((g.getFontMetrics().stringWidth(str2)/2))), 20);
            
            if (gameOver)
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
