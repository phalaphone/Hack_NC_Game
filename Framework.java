package Hack_NC_Game;

import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Framework that controls the game (Game.java) that created it, update it and draw it on the screen.
 *
 * @author www.gametutorial.net
 */

public class Framework extends Canvas {

    /**
     * Width of the frame.
     */
    public static int frameWidth;
    /**
     * Height of the frame.
     */
    public static int frameHeight;

    /**
     * Time of one second in nanoseconds.
     * 1 second = 1 000 000 000 nanoseconds
     */
    public static final long secInNanosec = 1000000000L;

    /**
     * Time of one millisecond in nanoseconds.
     * 1 millisecond = 1 000 000 nanoseconds
     */
    public static final long milisecInNanosec = 1000000L;

    /**
     * FPS - Frames per second
     * How many times per second the game should update?
     */
    private final int GAME_FPS = 60;
    /**
     * Pause between updates. It is in nanoseconds.
     */
    private final long GAME_UPDATE_PERIOD = secInNanosec / GAME_FPS;

    /**
     * Possible states of the game
     */
    public static enum GameState{STARTING, VISUALIZING, GAME_CONTENT_LOADING, MAIN_MENU, OPTIONS, PLAYING, GAMEOVER, PAUSE}
    /**
     * Current state of the game
     */
    public static GameState gameState;

    /**
     * Elapsed game time in nanoseconds.
     */
    private long gameTime;
    // It is used for calculating elapsed time.
    private long lastTime;

    // The actual game
    private Game game;

    private BufferedImage spaceMenuImg;
    private BufferedImage pauseImg;


    public Framework ()
    {
        super();

        gameState = GameState.VISUALIZING;

        //We start game in new thread.
        Thread gameThread = new Thread() {
            @Override
            public void run(){
                GameLoop();
            }
        };
        gameThread.start();
    }


   /**
     * Set variables and objects.
     * This method is intended to set the variables and objects for this class, variables and objects for the actual game can be set in Game.java.
     */
    private void Initialize()
    {
    
    }

    /**
     * Load files - images, sounds, ...
     * This method is intended to load files for this class, files for the actual game can be loaded in Game.java.
     */
    private void LoadContent()
    {
      try
        {
            URL spaceMenuImgUrl = this.getClass().getResource("/Hack_NC_Game/resources/images/background.jpg");
            spaceMenuImg = ImageIO.read(spaceMenuImgUrl);
            URL pauseMenuImgUrl = this.getClass().getResource("/Hack_NC_Game/resources/images/pause.jpg");
            pauseImg = ImageIO.read(pauseMenuImgUrl);
        }
      catch (IOException ex) {
            Logger.getLogger(Framework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * In specific intervals of time (GAME_UPDATE_PERIOD) the game/logic is updated and then the game is drawn on the screen.
     */
    private void GameLoop()
    {
        // This two variables are used in VISUALIZING state of the game. We used them to wait some time so that we get correct frame/window resolution.
        long visualizingTime = 0, lastVisualizingTime = System.nanoTime();

        // This variables are used for calculating the time that defines for how long we should put threat to sleep to meet the GAME_FPS.
        long beginTime, timeTaken, timeLeft;

        while(true)
        {
            beginTime = System.nanoTime();

            switch (gameState)
            {
                case PLAYING:
                    gameTime += System.nanoTime() - lastTime;

                    long elapsedTime= System.nanoTime() - lastTime;

                    game.UpdateGame(gameTime, elapsedTime, mousePosition());

                    lastTime = System.nanoTime();
                break;
                case GAMEOVER:
                    //...
                break;
                case MAIN_MENU:
                    //...
                break;
                case GAME_CONTENT_LOADING:
                    //...
                break;
                case STARTING:
                    // Sets variables and objects.
                    Initialize();
                    // Load files - images, sounds, ...
                    LoadContent();

                    // When all things that are called above finished, we change game status to main menu.
                    gameState = GameState.MAIN_MENU;
                break;
                case VISUALIZING:
                    // On Ubuntu OS (when I tested on my old computer) this.getWidth() method doesn't return the correct value immediately (eg. for frame that should be 800px width, returns 0 than 790 and at last 798px).
                    // So we wait one second for the window/frame to be set to its correct size. Just in case we
                    // also insert 'this.getWidth() > 1' condition in case when the window/frame size wasn't set in time,
                    // so that we although get approximately size.
                    if(this.getWidth() > 1 && visualizingTime > secInNanosec)
                    {
                        frameWidth = this.getWidth();
                        frameHeight = this.getHeight();

                        // When we get size of frame we change status.
                        gameState = GameState.STARTING;
                    }
                    else
                    {
                        visualizingTime += System.nanoTime() - lastVisualizingTime;
                        lastVisualizingTime = System.nanoTime();
                    }
                break;
            }

            // Repaint the screen.
            repaint();

            // Here we calculate the time that defines for how long we should put threat to sleep to meet the GAME_FPS.
            timeTaken = System.nanoTime() - beginTime;
            timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / milisecInNanosec; // In milliseconds
            // If the time is less than 10 milliseconds, then we will put thread to sleep for 10 millisecond so that some other thread can do some work.
            if (timeLeft < 10)
                timeLeft = 10; //set a minimum
            try {
                 //Provides the necessary delay and also yields control so that other thread can do work.
                 Thread.sleep(timeLeft);
            } catch (InterruptedException ex) { }
        }
    }

    /**
     * Draw the game to the screen. It is called through repaint() method in GameLoop() method.
     */
    @Override
    public void Draw(Graphics2D g2d)
    {
        switch (gameState)
        {
            case PLAYING:
                game.Draw(g2d, mousePosition());
            break;
            case GAMEOVER:
                g2d.drawString("You have died. :(", frameWidth / 2 - 83, (int)(frameHeight * 0.65));
                g2d.setColor(Color.red);
            break;
            case MAIN_MENU:
                g2d.drawImage(spaceMenuImg, 0, 0, frameWidth, frameHeight, null);
                g2d.setColor(Color.white);
                g2d.drawString("Use ASDW to move.", frameWidth / 2 - 85, (int)(frameHeight * 0.1));
                g2d.drawString("Click to shoot.", frameWidth / 2 - 85, (int)(frameHeight * 0.3));
                g2d.drawString("Press space to pause.", frameWidth / 2 - 85, (int)(frameHeight * 0.5));
                g2d.drawString("Press ESC to exit.", frameWidth / 2 - 85, (int)(frameHeight * 0.7));
                g2d.drawString("Press space to begin!", frameWidth / 2 - 85, (int)(frameHeight * 0.9));
        
            break;
            case GAME_CONTENT_LOADING:
                //...
            break;
            case PAUSE:
            	g2d.drawImage(pauseImg, 0, 0, frameWidth, frameHeight, null);
            	g2d.setColor(Color.white);
                g2d.drawString("Press SPACE to resume.", frameWidth / 2 - 85, (int)(frameHeight * 0.3));
                g2d.drawString("Press ENTER to restart.", frameWidth / 2 - 85, (int)(frameHeight * 0.5));
                g2d.drawString("Press ESC to exit.", frameWidth / 2 - 85, (int)(frameHeight * 0.7));
        }
    }

    private Point mousePosition()
    {
        try
        {
            Point mp = this.getMousePosition();

            if(mp != null)
                return this.getMousePosition();
            else
                return new Point(0, 0);
        }
        catch (Exception e)
        {
            return new Point(0, 0);
        }
    }

    /**
     * Starts new game.
     */
    private void newGame()
    {
        // We set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();

        game = new Game();
    }

    /**
     *  Restart game - reset game time and call RestartGame() method of game object so that reset some variables.
     */
    private void restartGame()
    {
        // We set gameTime to zero and lastTime to current time for later calculations.
        gameTime = 0;
        lastTime = System.nanoTime();

        game.RestartGame();

        // We change game status so that the game can start.
        gameState = GameState.PLAYING;
    }


    /**
     * Returns the position of the mouse pointer in game frame/window.
     * If mouse position is null than this method return 0,0 coordinate.
     *
     * @return Point of mouse coordinates.
     */


    /**
     * This method is called when keyboard key is released.
     *
     * @param e KeyEvent
     */
    @Override
    public void keyReleasedFramework(KeyEvent e)
    {
        switch (gameState)
        {
            case MAIN_MENU:
                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    newGame();
                }
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    System.exit(0);
                }
            break;
            case GAMEOVER:
                if(e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
                    restartGame();
            break;
            case PAUSE:
                if (e.getKeyCode() == KeyEvent.VK_SPACE){
                    gameState = GameState.PLAYING;

                    break;
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    restartGame();
                    break;
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                break;
            case PLAYING:
            	if (e.getKeyCode() == KeyEvent.VK_SPACE){
                    gameState = GameState.PAUSE;
                    break;
            	}
            	
        }
    }

    /**
     * This method is called when mouse button is clicked.
     *
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
    	System.out.println("you clicked");
    	Point b = mousePosition();
    	Game.bars.add(new Bar((int)Game.playerChar.getX(),(int)Game.playerChar.getY(),
    			(int)b.getX(),(int)b.getY()));
    	System.out.println(b.getX() + ":" + b.getY());
    }
}
