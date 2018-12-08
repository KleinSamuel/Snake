package PathFinding.Snake_PathFindingAI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * *******************************************************************
 *    ▄████████ ███▄▄▄▄      ▄████████    ▄█   ▄█▄    ▄████████ 	**
 *	 ███    ███ ███▀▀▀██▄   ███    ███   ███ ▄███▀   ███    ███ 	**
 *	 ███    █▀  ███   ███   ███    ███   ███▐██▀     ███    █▀  	**
 *	 ███        ███   ███   ███    ███  ▄█████▀     ▄███▄▄▄     	**
 * ▀███████████ ███   ███ ▀███████████ ▀▀█████▄    ▀▀███▀▀▀     	**
 *          ███ ███   ███   ███    ███   ███▐██▄     ███    █▄  	**
 *	  ▄█    ███ ███   ███   ███    ███   ███ ▀███▄   ███    ███ 	**
 *	▄████████▀   ▀█   █▀    ███    █▀    ███   ▀█▀   ██████████ 	**
 *                                     	 ▀                      	**
 *********************************************************************                                    	 
 * Two player Snake with several AI approaches						**
 * @author Samuel Klein												**
 * TODO:															**
 * - ki needs to check if current best path leads to death			**
 * *******************************************************************
 * ***********************************
 * ██╗██████╗ ███████╗ █████╗   	**
 * ██║██╔══██╗██╔════╝██╔══██╗  	**
 * ██║██║  ██║█████╗  ███████║  	**
 * ██║██║  ██║██╔══╝  ██╔══██║  	**
 * ██║██████╔╝███████╗██║  ██║  	**
 * ╚═╝╚═════╝ ╚══════╝╚═╝  ╚═╝  	**
 * ***********************************
 * The AI uses the A* Pathfinding	**
 * algorithm to get to the food.	**
 * 									**
 * When the AI gets trapped into a 	**
 * field with no food it uses an 	**
 * implementation of the hilbert	**
 * curve to fill out all the space	**
 * to win enough time to get out 	**
 * and reach the food.				**
 * ***********************************
 */
public class Snake
{
    private Board board;
    private GameControl gc;
    private Player[] playerArray;
    private Player player, player2;
    private Food[] foodArray;
    private Random random;
    
//    private AudioPlayer playerMainTheme;
//    private AudioPlayer playerFood;
//    private AudioPlayer playerDeath;
    
    private boolean[][] boolArray;
    private boolean[][] blockedArray;

    private boolean gameOver = false;
    private boolean gameIsWon = false;
    private int whoLost = 0;
    private int whoWon = 0;
    private int boundToWin = 250;
    
    private boolean playedDeathSound = false;

    //the actual game grid
    private GridElement[][] grid = null;

    //the size of the grid
    int sizeX;
    int sizeY;
    
    enum MoveDirection{
        LEFT, RIGHT, UP, DOWN;
    }

    enum GridElement{
        HEAD,BODY1,BODY2,FREE,BLOCKED,FOOD,HEAD2, BODY12, BODY22;
    }
    
    enum FruitType{
    	BANANA, APPLE, PINE;
    }

    public Snake(int sizeX, int sizeY, int scale, int speed, int amountFood){
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        //center the start position
        int startX = sizeX/2;
        int startY = sizeY/2;
        
        int startX2 = sizeX/4;
        int startY2 = sizeY/4;

        //inits the relevant game entities
        this.grid = new GridElement[sizeX][sizeY];
        this.gc= new GameControl(speed);
        this.player = new Player(startX,startY, 1, true);
        this.player2 = new Player(startX2, startY2, 2, true);
        this.random = new Random();
        
        playerArray = new Player[]{player, player2};
        
        boolArray = new boolean[sizeX][sizeY];
        blockedArray = new boolean[sizeX][sizeY];
        
        foodArray = new Food[amountFood];
        
//        URL urlDeath = Snake.class.getClassLoader().getResource("soundEffects/pacman_death.wav");
//        URL urlFood = Snake.class.getClassLoader().getResource("soundEffects/pacman_eatfruit.wav");
        
//        playerDeath = new AudioPlayer(urlDeath);
//        playerFood = new AudioPlayer(urlFood);
        
        this.board = new Board(scale);
        
        for (int i = 0; i < foodArray.length; i++) {
			foodArray[i] = new Food();
		}

        // initializes the grid
        for(int i = 0 ; i< grid.length;i++){
            for(int j = 0 ; j< grid[i].length;j++){
                if(i == 0){
                    grid[i][j] = GridElement.BLOCKED;
                    blockedArray[i][j] = true;
                }else if(j == 0){
                    grid[i][j] = GridElement.BLOCKED;
                    blockedArray[i][j] = true;
                }else if(i == sizeX-1){
                    grid[i][j] = GridElement.BLOCKED;
                    blockedArray[i][j] = true;
                }else if(j == sizeY-1){
                    grid[i][j] = GridElement.BLOCKED;
                    blockedArray[i][j] = true;
                }else if(i == startX && j == startY){
                    grid[i][j] = GridElement.HEAD;
                }else if(i == startX2 && j == startY2){
                	grid[i][j] = GridElement.HEAD;
                }else{
                    grid[i][j] = GridElement.FREE;
                }
            }
        }
        
        for(Player p : playerArray){
        	for(int[] parts : p.snakeParts){
        		blockedArray[parts[0]][parts[1]] = true;
        	}
        }
        
        for(Food f : foodArray){
        	f.createNewFood();
        }
        
    }

    /* Start the game speeds' timer */
    public void start(){
        gc.start();
    }

    public void gameOver(){
        gc.stop();
    }

    /* Update the board (redraw) */
    public void update(){
    	
    	boolArray = new boolean[sizeX][sizeY];
    	
    	/* clear all snake parts on board */
    	for(Player p : playerArray){
    		
    		/* free all old positions */
    		for(int[] positions : p.snakeParts){
            	grid[positions[0]][positions[1]] = GridElement.FREE;
            }
    		
    		if(!p.isAi){
    			p.move(p.direction);
    		}else{
//    			MoveDirection tmp = p.changeDirection();
//    			MoveDirection tmp = p.changeDirectionBetter(8);
    			MoveDirection tmp = p.findBestPath(p.direction);
    			p.move(tmp);
    			p.direction = tmp;
    		}

    		/* add snake parts to boolean array except for the head */
    		int cnt = 1;
    		for(int[] positions : p.snakeParts){
    			if(boolArray[positions[0]][positions[1]] != true && cnt != 1){
    				boolArray[positions[0]][positions[1]] = true;
    			}
    			cnt++;
    		}
        }
    	
    	blockedArray = new boolean[sizeX][sizeY];
    	for (int i = 0; i < blockedArray.length; i++) {
			blockedArray[i][0] = true;
			blockedArray[i][blockedArray[i].length-1] = true;
		}
    	for (int i = 0; i < blockedArray[0].length; i++) {
			blockedArray[0][i] = true;
			blockedArray[blockedArray.length-1][i] = true;
		}
    	
    	/* check collisions */
    	int counter3 = 1;
    	boolean flag = false;
    	for(Player p : playerArray){
    		if(boolArray[p.currentX][p.currentY] == true){
    			flag = true;
    		}else if(p.currentX == 0 || p.currentX == sizeX-1 || p.currentY == 0 || p.currentY == sizeY-1){
    			flag = true;
        	}
    		
    		if(flag){
    			gameOver = true;
				whoLost = counter3;
				gc.stop();
    		}
    		
    		for(int[] parts : p.snakeParts){
    			blockedArray[parts[0]][parts[1]] = true;    			
    		}
    		
    		
    		counter3++;
    	}
    	
    	/* set all snake parts on board */
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
        
        isHead = true;
        counter = 1;
        
        for(int[] positions : player2.snakeParts){
        	if(isHead){
        		grid[positions[0]][positions[1]] = GridElement.HEAD2;        		
        		isHead = false;
        	}else{
        		if((counter%3)==0){
        			grid[positions[0]][positions[1]] = GridElement.BODY12;
        		}else{
        			grid[positions[0]][positions[1]] = GridElement.BODY22;        		        			
        		}
        	}
        	counter++;
        }
        
        for(Food f : foodArray){
        	grid[f.getPosX()][f.getPosY()] = GridElement.FOOD;
        }

        board.update();
    }
    /* the actual game board */
    public JPanel getBoard(){
        return board;
    }

    /**
     * Represents the player (the actual snake)
     * @author Samuel Klein
     */
    public class Player{
    	
        private int currentX;
        private int currentY;
        
        private int number;
        
        boolean isAi = false;
        
        private AStarPathFinder pathFinder = new AStarPathFinder();
        
        boolean hasToExtend = false;
        
        MoveDirection direction = MoveDirection.RIGHT;
        
        private ArrayList<int[]> snakeParts = new ArrayList<int[]>();
        
        /* Inits a player on a start position x and y */
        public Player(int startX, int startY, int number, boolean isAi){
            this.currentX = startX;
            this.currentY = startY;
            
            this.number = number;
            
            this.isAi = isAi;
            
            snakeParts.add(new int[]{startX, startY});
            snakeParts.add(new int[]{startX-1, startY});
            snakeParts.add(new int[]{startX-2, startY});
        }

        /* The current (head) x position */
        public int getCurrentX(){
            return currentX;
        }
        /* The current (head) y position */
        public int getCurrentY(){
            return currentY;
        }

        /**
         * Moves the snake in a given direction.
         * @param direction -- the direction either LEFT, RIGHT, TOP, or DOWN
         */
        public void move(MoveDirection direction){
        	
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
        	
            if(direction == MoveDirection.LEFT){
            	if(currentX <= 0){
            		currentX = grid.length-1;
            		currentY = yValue;
            	}else{
            		currentX--;            		
            	}
            	
            }else if(direction == MoveDirection.RIGHT){
            	if(currentX >= grid.length-1){
            		currentX = 0;
            		currentY = yValue;
            	}else{
            		currentX++;            		
            	}
            }else if(direction == MoveDirection.UP){
                currentY--;
            }else if(direction == MoveDirection.DOWN){
                currentY++;
            }

            snakeParts.add(0, new int[]{currentX, currentY});
            
            for(Food f : foodArray){            	
            	if(currentX == f.getPosX() && currentY == f.getPosY()){
            		f.isFood = false;
            		hasToExtend = true;
            		
            		/* TODO: sound doesnt play all the time */
//            		playerFood.playSoundOnce();
            		
            		f.createNewFood();
            		grid[f.getPosX()][f.getPosY()] = GridElement.FREE;
            	}
            }

            if(!hasToExtend){
            	snakeParts.remove(snakeParts.size()-1);   
            }
            
            if(snakeParts.size() >= boundToWin){
            	gameIsWon = true;
            	whoWon = number;
            	gc.stop();
            }
            
            hasToExtend = false;
            
        }
        
        /* returns direction calculated by a naive AI */
        public MoveDirection changeDirection(){
    		
        	/* check if about to crash into a wall */
    		if(currentX == 1){
    			if(currentY == 1){
    				if(direction == MoveDirection.UP){
    					return MoveDirection.RIGHT;
    				}else if(direction == MoveDirection.LEFT){
    					return MoveDirection.DOWN;
    				}
    			}else if(currentY == sizeY-2){
    				if(direction == MoveDirection.LEFT){
    					return MoveDirection.UP;
    				}else if(direction == MoveDirection.DOWN){
    					return MoveDirection.RIGHT;
    				}
    			}else{
    				if(direction == MoveDirection.LEFT){
	    				if(random.nextInt((1)+1) == 1){
	    					return MoveDirection.UP;
	    				}else{
	    					return MoveDirection.DOWN;
	    				}
    				}
    			}
    		}else if(currentX == sizeX-2){
    			if(currentY == 1){
    				if(direction == MoveDirection.UP){
    					return MoveDirection.LEFT;
    				}else if(direction == MoveDirection.RIGHT){
    					return MoveDirection.DOWN;
    				}
    			}else if(currentY == sizeY-2){
    				if(direction == MoveDirection.RIGHT){
    					return MoveDirection.UP;
    				}else if(direction == MoveDirection.DOWN){
    					return MoveDirection.LEFT;
    				}
    			}else{
	    			if(direction == MoveDirection.RIGHT){
	    				if(random.nextInt((1)+1) == 1){
	    					return MoveDirection.UP;
	    				}else{
	    					return MoveDirection.DOWN;
	    				}
					}
    			}
    		}else if(currentY == 1){
    			if(direction == MoveDirection.UP){
    				if(random.nextInt((1)+1) == 1){
    					return MoveDirection.LEFT;
    				}else{
    					return MoveDirection.RIGHT;
    				}
				}
    		}else if(currentY == sizeY-2){
    			if(direction == MoveDirection.DOWN){
    				if(random.nextInt((1)+1) == 1){
    					return MoveDirection.LEFT;
    				}else{
    					return MoveDirection.RIGHT;
    				}
				}
    		}
    		
    		double nearest = Integer.MAX_VALUE;
    		Food nearestFood = null;
    		
//    		for(Food f : foodArray){
//    			int tmpDistance = Math.abs(currentX-f.posX)+Math.abs(currentY-f.posY);
//    			if(tmpDistance < nearest){
//    				nearest = tmpDistance;
//    				nearestFood = f;
//    			}
//    		}
    		
    		for(Food f : foodArray){
    			int tmp = 0;
    			if(currentX < f.posX){
    				for (int i = currentX; i <= f.posX; i++) {
						if(boolArray[i][currentY] == true){
							tmp+=10;
						}else{
							tmp+= 1;
						}
					}
    			}
    			if(currentX > f.posX){
    				for (int i = f.posX; i >= currentX; i--) {
						if(boolArray[i][currentY] == true){
							tmp+=10;
						}else{
							tmp+= 1;
						}
					}
    			}
    			if(currentY < f.posY){
    				for (int i = currentY; i <= f.posY; i++) {
						if(boolArray[currentX][i] == true){
							tmp+=10;
						}else{
							tmp+= 1;
						}
					}
    			}
    			if(currentY > f.posY){
    				for (int i = f.posY; i >= currentY; i--) {
						if(boolArray[i][currentY] == true){
							tmp+=10;
						}else{
							tmp+= 1;
						}
					}
    			}
    			
    			if(tmp <= nearest){
    				nearest = tmp;
    				nearestFood = f;
    			}
    		}
    		
    		/* move into direction of food */
    		if(currentX < nearestFood.posX && direction != MoveDirection.LEFT && !boolArray[currentX+1][currentY]){
    			return MoveDirection.RIGHT;
    		}
    		if(currentX > nearestFood.posX && direction != MoveDirection.RIGHT && !boolArray[currentX-1][currentY]){
    			return MoveDirection.LEFT;
    		}
    		if(currentY < nearestFood.posY && direction != MoveDirection.UP && !boolArray[currentX][currentY+1]){
    			return MoveDirection.DOWN;
    		}
    		if(currentY > nearestFood.posY && direction != MoveDirection.DOWN && !boolArray[currentX][currentY-1]){
    			return MoveDirection.UP;
    		}
    		
    		return direction;
    	}
        
        public MoveDirection findBestPath(MoveDirection curDir){
        	
        	ArrayList<int[]> shortestPath = pathFinder.findPath(blockedArray, currentX, currentY, foodArray[0].posX, foodArray[0].posY);	
        	
        	while(shortestPath == null){
        		
//        		boolean[][] tmp = findFreeAdjacentCells(blockedArray, currentX, currentY);
//        		
//        		int currentDistance = Integer.MAX_VALUE;
//        		
//        		int xTo = -1;
//        		int yTo = -1;
//        		
//        		for (int i = 0; i < tmp[0].length; i++) {
//					for (int j = 0; j < tmp.length; j++) {
//						if(!tmp[j][i]){
//							System.out.print(". ");
//						}else{
//							System.out.print("X ");
//							int distTMP = Math.abs(i-currentX)+Math.abs(j-currentY);
//							if(distTMP < currentDistance){
//								currentDistance = distTMP;
//								xTo = i;
//								yTo = j;
//							}
//						}
//					}
//					System.out.println();
//				}
//        		
//        		shortestPath = pathFinder.findPath(blockedArray, currentX, currentY, xTo, yTo);
        		
        		if(!blockedArray[currentX+1][currentY] && curDir != MoveDirection.LEFT){
        			return MoveDirection.RIGHT;
        		}else if(!blockedArray[currentX-1][currentY] && curDir != MoveDirection.RIGHT){
        			return MoveDirection.LEFT;
        		}else if(!blockedArray[currentX][currentY+1] && curDir != MoveDirection.UP){
        			return MoveDirection.DOWN;
        		}else if(!blockedArray[currentX][currentY-1] && curDir != MoveDirection.DOWN){
        			return MoveDirection.UP;
        		}
        	}
        	
        	int xTmp = shortestPath.get(0)[0];
        	int yTmp = shortestPath.get(0)[1];
        	
        	if(xTmp < currentX){
        		return MoveDirection.LEFT;
        	}else if(xTmp > currentX){
        		return MoveDirection.RIGHT;
        	}else if(yTmp < currentY){
        		return MoveDirection.UP;
        	}else if(yTmp > currentY){
        		return MoveDirection.DOWN;
        	}
        	
			return curDir;
        }
        
        public MoveDirection changeDirectionBetter(int depth){
        	
        	GameTreeNode root = new GameTreeNode(this.direction, this.currentX, this.currentY, boolArray, foodArray, 0, this.snakeParts);
        	createTree(root, depth);
        	return findBestDirection(root, depth);
        	
        }
        
        public void createTree(GameTreeNode current, int depth){
        	if(depth > 0){
        		current.generateChildNodes();
        		for(GameTreeNode n : current.children){
        			createTree(n, depth-1);
        		}
        	}else{
        		current.generateChildNodes();
        	}
        }
        
        public MoveDirection findBestDirection(GameTreeNode root, int depth){
        	
        	int bestScore = Integer.MIN_VALUE;
        	MoveDirection output = root.direction;
        	MoveDirection opposite = null;
        	
        	if(output == MoveDirection.LEFT){
        		opposite = MoveDirection.RIGHT;
        	}else if(output == MoveDirection.RIGHT){
        		opposite = MoveDirection.LEFT;
        	}else if(output == MoveDirection.UP){
        		opposite = MoveDirection.DOWN;
        	}else{
        		opposite = MoveDirection.UP;
        	}
        	
        	for(GameTreeNode n : root.children){
        		int tmpScore = calcBestScore(n, depth);
        		System.out.println("Score: "+tmpScore);
        		if(tmpScore >= bestScore && n.direction != opposite){
        			bestScore = tmpScore;
        			output = n.direction;
        		}
        	}
        	return output;
        }
        
        public int calcBestScore(GameTreeNode node, int depth){
        	if(depth <= 0){
        		return node.score;
        	}
        	for(GameTreeNode n : node.children){
        		calcBestScore(n, depth-1);
        	}
        	return 0;	
        }
    }
    
    /*
     * outputs a boolean array containing true for all free reachable cells from current position
     */
    public boolean[][] findFreeAdjacentCells(boolean curr[][], int sx, int sy){
    	
    	boolean[][] output = new boolean[curr.length][curr[0].length];
    	
    	ArrayList<int[]> toCheck = new ArrayList<int[]>();
    	boolean[][] isAlreadyChecked = new boolean[curr.length][curr[0].length];
    	
    	int[] start = new int[]{sx,sy};
    	
    	toCheck.add(start);
    	isAlreadyChecked[sx][sy] = true;
    	
    	while(toCheck.size() != 0){
    		
    		int[] current = toCheck.get(0);
    		
    		for(int x1 = -1; x1 < 2; x1++){
				for(int y1 = -1; y1 < 2; y1++){
					
					/* skip current node */
					if((x1 == 0) && (y1 == 0)){
						continue;
					}
					/* do not allow diagonal movement */
					if((x1 != 0) && (y1 != 0)){
						continue;
					}
					
					int xP = current[0]+x1;
					int yP = current[1]+y1;
					
					if(isAlreadyChecked[xP][yP]){
						continue;
					}
					
					/* check if out of bounds */
					if(xP < 1 || xP >= sizeX -1 || yP < 1 || yP >= sizeY -1){
						continue;
					}
					
					/* if cell is free store this info and add cell to checklist */
					if(blockedArray[xP][yP] == false){
						output[xP][yP] = true;
						int[] t = new int[]{xP,yP};
						if(!toCheck.contains(t)){
							toCheck.add(t);
						}
					}
					
					isAlreadyChecked[xP][yP] = true;
					
				}
    		}
    		
    		toCheck.remove(current);
    		isAlreadyChecked[current[0]][current[1]] = true;
    	}
    	
    	return output;
    	
    }
    
    /*
     * interface for the A* path finding algorithm
     */
    public interface PathFinder {
    	public ArrayList<int[]> findPath(boolean[][] curr, int sx, int sy, int tx, int ty);
    }
    
    /* 
     * node for representation of a position in the game 
     */
    public class Node implements Comparable<Object>{
    	Node parent;
    	int cost;
    	int posX;
    	int posY;
    	
		@Override
		public int compareTo(Object o){
			if(cost < ((Node)o).cost){
				return -1;
			}else if(cost > ((Node)o).cost){
				return 1;
			}else{				
				return 0;
			}
		}
    }
    
    /*
     * actual path finding class
     */
    public class AStarPathFinder implements PathFinder{
    	
    	/* list of nodes which have already been discovered */
    	private ArrayList<Node> closed = new ArrayList<Node>();
    	/* list of nodes which are next to discover */
    	@SuppressWarnings("rawtypes")
		private ArrayList open = new ArrayList();
    	/* game field as an array of nodes */
    	Node[][] nodes;

		@SuppressWarnings("unchecked")
		@Override
		public ArrayList<int[]> findPath(boolean[][] blockedArrayInternal, int sx, int sy, int tx, int ty) {
			
			
			/* check if destination is unreachable */
			if(blockedArrayInternal[tx][ty] == true){
				return null;
			}
			
			/* clear both lists and node array */
			closed.clear();
			open.clear();
			nodes = new Node[sizeX][sizeY];
			
			/* initialize the node array 
			 * TODO: to it for more than only one food
			 */
			for (int i = 0; i < nodes.length; i++) {
				for (int j = 0; j < nodes[i].length; j++) {
					nodes[i][j] = new Node();
					nodes[i][j].cost = Math.abs(i-foodArray[0].posX)+Math.abs(j-foodArray[0].posY);
					nodes[i][j].posX = i;
					nodes[i][j].posY = j;
				}
			}
			
			/* add starting point to open list */
			open.add(nodes[sx][sy]);
			/* set parent node of target node to null */
			nodes[tx][ty].parent = null;
			
			/* while the open list still contains nodes to search */
			while(open.size() != 0){
				
				/* get first node in the open list */
				Node currentNode = (Node)open.get(0);
				/* check if target node is reached */
				if(currentNode.posX == nodes[tx][ty].posX && currentNode.posY == nodes[tx][ty].posY){
					break;
				}
				/* remove the discovered node from the open list */
				open.remove(0);
				closed.add(currentNode);
				
				/* search through all neighbors of current node */
				for(int x1 = -1; x1 < 2; x1++){
					
					for(int y1 = -1; y1 < 2; y1++){
						
						/* skip current node */
						if((x1 == 0) && (y1 == 0)){
							continue;
						}
						/* do not allow diagonal movement */
						if((x1 != 0) && (y1 != 0)){
							continue;
						}
						
						/* determine location of neighbor and evaluate */
						int xP = x1 + currentNode.posX;
						int yP = y1 + currentNode.posY;
						
						/* check if out of bounds */
						if(xP < 1 || xP > sizeX -1 || yP < 1 || yP > sizeY -1){
							continue;
						}
						
						/* check if neighbor location is a free spot */
						if(blockedArrayInternal[xP][yP] == false){
							/* get cost for next step */
							int nextStepCost = currentNode.cost + 1;
							/* store current neighbor node */
							Node neighbor = nodes[xP][yP];
							
							/* check if new cost is lower than old cost */
							if(nextStepCost < neighbor.cost){
								/* check if neighbor node is in open list if yes then remove */
								if(open.contains(neighbor)){
									open.remove(neighbor);
								}
								/* check if neighbor node is in closed list if yes then remove */
								if(closed.contains(neighbor)){
									closed.remove(neighbor);
								}
							}
							
							/* check if neighbor node is in one of the lists if not then add it with new score */
							if(!closed.contains(neighbor) && !open.contains(neighbor)){
								neighbor.cost = nextStepCost;
								neighbor.parent = currentNode;
								open.add(neighbor);
								Collections.sort(open);
							}
						}
					}
				}
			}
			
			/* check if there is a valid solution if not then return null */
			if(nodes[tx][ty].parent == null){
				return null;
			}
			/* create a new arraylist containing the closest path */
			ArrayList<int[]> path = new ArrayList<int[]>();
			/* temporarily store the target node */
			Node target = nodes[tx][ty];
			
			/* backtrack from the target to the starting position to get the shortest path */
//			while(target.posX != nodes[sx][sy].posX && target.posY != nodes[sx][sy].posY){
			while(target.parent != null){
				path.add(0, new int[]{target.posX, target.posY});
				target = target.parent;
			}
			
			return path;
		}
    	
    }
    
    public class GameTreeNode{
    	
    	boolean[][] currentGameState;
    	int currentScore;
    	private MoveDirection direction;
    	int x;
    	int y;
    	Food[] foodArr;
    	ArrayList<int[]> currentSnakeParts;
    	int score;
    	
    	GameTreeNode[] children;
    	
    	public GameTreeNode(MoveDirection direction, int currentX, int currentY, boolean[][] bool, Food[] food, int oldScore, ArrayList<int[]> snakeParts){
    		
    		this.direction = direction;
    		x = currentX;
    		y = currentY;
    		this.foodArr = food;
    		currentSnakeParts = snakeParts;
    		score = oldScore;
    		currentGameState = bool;
    		
    		children = new GameTreeNode[3];
    	}
    	
    	public Food getNearestFood(int x, int y, Food[] food){
    		int closest = Integer.MAX_VALUE;
    		Food output = null;
    		for(Food f : foodArray){
    			int currDist = Math.abs(x-f.posX)+Math.abs(y-f.posY);
    			if(currDist <= closest){
    				closest = currDist;
    				output = f;
    			}
    		}
    		return output;
    	}
    	
    	public int calculateScore(MoveDirection direction){
    		
    		int scoreOut = 0;
    		boolean isFood = false;
    		if(direction == MoveDirection.LEFT){
    			x = x -1;
    		}else if(direction == MoveDirection.RIGHT){
    			x = x +1;
    		}else if(direction == MoveDirection.UP){
    			y = y -1;
    		}else if(direction == MoveDirection.DOWN){
    			y = y +1;
    		}
 
    		if(boolArray[x][y] == true){
    			scoreOut -= 250;
			}else{
				for(Food f : foodArr){
					if(x == f.posX && y == f.posY){
						scoreOut += 100;
						isFood = true;
						break;
					}
				}
				scoreOut += 5;
			}
    		
    		currentSnakeParts.add(0, new int[]{x, y});
    		currentGameState[x][y] = true;
    		if(!isFood){
    			int[] tmp = currentSnakeParts.get(currentSnakeParts.size()-1);
    			currentSnakeParts.remove(currentSnakeParts.size()-1);
    			currentGameState[tmp[0]][tmp[1]] = false;
    		}
    		
    		return scoreOut;
    	}
    	
    	public MoveDirection getOldDirection(){
    		return this.direction;
    	}
    	
    	public void generateChildNodes(){
    		ArrayList<MoveDirection> dirList = new ArrayList<MoveDirection>();
    		dirList.add(MoveDirection.LEFT);
    		dirList.add(MoveDirection.RIGHT);
    		dirList.add(MoveDirection.UP);
    		dirList.add(MoveDirection.DOWN);
    		dirList.remove(this.direction);
    		
    		int x2 = 0;
    		for(MoveDirection d : dirList){
    			int scoreTmp = score+calculateScore(d);
    			children[x2] = new GameTreeNode(d, x, y, currentGameState, this.foodArr, scoreTmp, currentSnakeParts);
    			x2++;
    		}
    	}
    }
    
    /**
     *  Handles the food
     *  @author Samuel Klein
     */
    public class Food {
    	
    	boolean isFood = false;
    	Random rand = new Random();
    	FruitType type;
    	
    	public Food(){
    		createType();
    	}
    	
    	private int posX = -1;
    	private int posY = -1;
    	
    	public void createType(){
    		switch (rand.nextInt(3)) {
			case 0:
				type = FruitType.APPLE;
				break;
			case 1:
				type = FruitType.BANANA;
				break;
			case 2:
				type = FruitType.PINE;
				break;
			}
    	}
    	
    	public void createNewFood(){
    		
    		createType();
    		
    		Random rand = new Random();
    		posX = rand.nextInt(((sizeX-3) - 2) + 1) + 2;
     	    posY = rand.nextInt(((sizeY-3) - 2) + 1) + 2;
    		boolean freePosition = false;
    		
    		while(!freePosition){
    			freePosition = true;
    			posX = rand.nextInt(((sizeX-3) - 2) + 1) + 2;
        	    posY = rand.nextInt(((sizeY-3) - 2) + 1) + 2;
        	    for(int[] parts : player.snakeParts){
        	    	if(posX == parts[0] && posY == parts[1]){
        	    		freePosition = false;
        	    	}
        	    }
        	    for(Food f : foodArray){
        	    	if(!f.equals(this)){
        	    		if(posX == f.posX && posY == f.posY){
        	    			freePosition = false;
        	    		}
        	    	}
        	    }
        	    for(Player p : playerArray){
        	    	for(int[] a : p.snakeParts){
        	    		if(a[0] == posX && a[1] == posY){
        	    			freePosition = false;
        	    		}
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
     * @author Samuel Klein
     */
    public class GameControl  extends KeyAdapter implements ActionListener{
        Board board = null;
        Timer timer = null;

        public GameControl(int speed){
            //see javax.swing.Timer
            this.timer = new Timer(speed,this);
        }

        @Override
        public void actionPerformed(ActionEvent e){
            //action performed after each timer interval
            update();
        }

        /* Starts the internal timer */
        public void start(){
            timer.start();
        }

        public void stop(){
            timer.stop();
        }

        /* Updates the direction upon user input */
        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();
            
            /* key codes for player 1 */
            if ((key == KeyEvent.VK_LEFT) && playerArray[0].direction != MoveDirection.RIGHT)  {
            	playerArray[0].direction = MoveDirection.LEFT;
            }
            if ((key == KeyEvent.VK_RIGHT) && playerArray[0].direction != MoveDirection.LEFT)  {
            	playerArray[0].direction = MoveDirection.RIGHT;
            }
            if ((key == KeyEvent.VK_UP) && playerArray[0].direction != MoveDirection.DOWN) {
            	playerArray[0].direction = MoveDirection.UP;
            }
            if ((key == KeyEvent.VK_DOWN) && playerArray[0].direction != MoveDirection.UP) {
            	playerArray[0].direction = MoveDirection.DOWN;
            }
            
            /* key codes for player 2 */
            if ((key == KeyEvent.VK_W) && playerArray[1].direction != MoveDirection.DOWN)  {
            	playerArray[1].direction = MoveDirection.UP;
            }
            if ((key == KeyEvent.VK_A) && playerArray[1].direction != MoveDirection.RIGHT)  {
            	playerArray[1].direction = MoveDirection.LEFT;
            }
            if ((key == KeyEvent.VK_S) && playerArray[1].direction != MoveDirection.UP) {
            	playerArray[1].direction = MoveDirection.DOWN;
            }
            if ((key == KeyEvent.VK_D) && playerArray[1].direction != MoveDirection.LEFT) {
            	playerArray[1].direction = MoveDirection.RIGHT;
            }
            
            /* cheat for player 1 */
            if((key == KeyEvent.VK_C)){
            	player.hasToExtend = true;
            }
        }
    }

    /**
     * Representation of the game board as JPanel
     * @author Samuel Klein
     *
     */
    public class Board extends JPanel {
    	
        private static final long serialVersionUID = 1L;

        //scale for the grid
        private int scale_size;

        public Board(int scale){

            this.setFocusable(true);

            //the GameControl is registered as key listener (listing of user inputs)
            addKeyListener(gc);

            this.scale_size = scale;

            setPreferredSize(new Dimension(sizeX*scale, sizeY*scale));

            this.setBackground(Color.DARK_GRAY);
        }

        public void update(){
            this.repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            draw(g);
        }

        /* Redraws the board */
        public void draw(Graphics g){

            for(int i = 0 ; i< grid.length;i++){
                for(int j = 0 ; j< grid[i].length;j++){
                	
                    if(grid[i][j] == GridElement.BLOCKED ){
                        g.setColor(Color.GRAY);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        continue;
                    }
                    if(grid[i][j] == GridElement.HEAD){
                        g.setColor(Color.MAGENTA);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        continue;
                    }
                    if(grid[i][j] == GridElement.BODY1){
                        g.setColor(Color.BLACK);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        g.setColor(Color.YELLOW);
                        g.drawString("1", i*scale_size+(scale_size/2)-2, j*scale_size+scale_size-2);
                        
                        continue;
                    }
                    if(grid[i][j] == GridElement.BODY2){
                        g.setColor(Color.YELLOW);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        g.setColor(Color.BLACK);
                        g.drawString("1", i*scale_size+(scale_size/2)-2, j*scale_size+scale_size-2);
                        continue;
                    }
                    if(grid[i][j] == GridElement.HEAD2){
                        g.setColor(Color.ORANGE);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        continue;
                    }
                    if(grid[i][j] == GridElement.BODY12){
                        g.setColor(Color.BLUE);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        g.setColor(Color.WHITE);
                        g.drawString("2", i*scale_size+(scale_size/2)-4, j*scale_size+scale_size-4);
                        continue;
                    }
                    if(grid[i][j] == GridElement.BODY22){
                        g.setColor(Color.WHITE);
                        g.fillRect(i*scale_size, j*scale_size, scale_size, scale_size);
                        g.setColor(Color.BLUE);
                        g.drawString("2", i*scale_size+(scale_size/2)-4, j*scale_size+scale_size-4);
                        continue;
                    }
                    
                    if(grid[i][j] == GridElement.FOOD){
//                        g.setColor(Color.RED);
//                        g.fillRoundRect(i*scale_size, j*scale_size, scale_size, scale_size, scale_size, scale_size);
//                        g.setColor(Color.BLACK);
//                        g.drawLine((i*scale_size)+(scale_size/2), (j*scale_size), (i*scale_size)+(scale_size/2), (j*scale_size)+(scale_size/4));
                       
                    	URL url = null;
                    	
                    	for(Food f : foodArray){
                    		if(f.posX == i && f.posY == j){
                    			if(f.type == FruitType.APPLE){
                    				url = Snake.class.getClassLoader().getResource("images/apfel.png");
                    			}else if(f.type == FruitType.BANANA){
                    				url = Snake.class.getClassLoader().getResource("images/banane.png");
                    			}else if(f.type == FruitType.PINE){
                    				url = Snake.class.getClassLoader().getResource("images/pine.png");
                    			}
                    		}
                    	}
                    	
                    	RenderedImage img = null;
						try {
							img = ImageIO.read(url);
						} catch (IOException e) {
							e.printStackTrace();
						} 
						g.drawImage((Image) img, i*scale_size,j*scale_size, (scale_size), (scale_size), Color.DARK_GRAY, this);
                        g.finalize();
                        
                        continue;
                    }
                }
            }
            
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,15));
            String strX = "PLAYER 1: "+playerArray[0].snakeParts.size();
            g.drawString(strX, 17, 13); 
            
            g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,15));
            String strY = "PLAYER 2: "+playerArray[1].snakeParts.size();
            float w = g.getFontMetrics().stringWidth(strY);
            g.drawString(strY, this.getWidth()-(17+(int)w), 13); 
          
            g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));
            String strZ = "SCORE TO REACH: "+boundToWin;
            float w2 = g.getFontMetrics().stringWidth(strZ);
            g.drawString(strZ, this.getWidth()/2-((int)w2/2), 13); 
            
            if(gameOver){
                g.setColor(Color.RED);
                g.setFont(new Font(Font.MONOSPACED,Font.BOLD,70));
                String str = "GAME OVER";
                String str2 = "PLAYER "+whoLost+" LOST";
                float width = g.getFontMetrics().stringWidth(str);
                float width2 = g.getFontMetrics().stringWidth(str2);
                
                if(!playedDeathSound){
                	
                	/* TODO: Sound plays 2 times */
//                	playerDeath.playSoundOnce();                	
                	
                	playedDeathSound = true;
                }

                g.drawString(str, Math.round((this.getWidth()/2)-(width/2)), this.getHeight()/2);
                g.drawString(str2, Math.round((this.getWidth()/2)-(width2/2)), this.getHeight()/2+80);
                
            }
            
            if(gameIsWon){
            	 g.setColor(Color.GREEN);
                 g.setFont(new Font(Font.MONOSPACED,Font.BOLD,70));
                 String str = "GAME OVER";
                 String str2 = "PLAYER "+whoWon+" WON";
                 float width = g.getFontMetrics().stringWidth(str);
                 float width2 = g.getFontMetrics().stringWidth(str2);

                 g.drawString(str, Math.round((this.getWidth()/2)-(width/2)), this.getHeight()/2);
                 g.drawString(str2, Math.round((this.getWidth()/2)-(width2/2)), this.getHeight()/2+80);
                 
            }
            
            Toolkit.getDefaultToolkit().sync();
        }
    }
    /*
	 * AudioPlayer for better game experience
	 */
	public class AudioPlayer{
		private Clip clip;
		public AudioPlayer(URL url){
			AudioInputStream in;
			try {
				in = AudioSystem.getAudioInputStream(url);
				clip = AudioSystem.getClip();
				clip.open(in);
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		public void playSound(){clip.loop(100);}
		public void stopSound(){clip.stop();clip.setFramePosition(0);}
		public void playSoundOnce(){clip.loop(0);clip.setFramePosition(0);}
		public void pauseSound(){clip.stop();}
		public void resumeSound(){clip.start();}
	}

    /**
     * The main method constructs inits. the game and shows the board within a jpanel.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        final Snake snake = new Snake(50, 40, 15, 20, 1);

        final JFrame frame = new JFrame("Snake");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);
        frame.add(snake.getBoard());
        frame.pack();

        snake.start();
        frame.setVisible(true);

    }

}
