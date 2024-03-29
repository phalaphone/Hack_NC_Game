package Hack_NC_Game;

import java.awt.Graphics;

public abstract class Projectile{

    private int x;
    private int y;
    private double speed, direction;
    
    public Projectile(int avatar_x, int avatar_y, int mouse_x, int mouse_y){
        this.x = avatar_x;
        this.y = avatar_y;
        int x_change = mouse_x - avatar_x;
        int y_change = mouse_y - avatar_y;
        direction = Math.atan(y_change/(float)x_change);
    }
    
    public double getDirection(){
        return this.direction;
    }
     public int getX(){
    	 return x;
     }
    public int getY(){
    	return y;
    }
    
    public void setDirection(double angle){
        direction = angle;
    }
    abstract public void Draw(Graphics g);
    
    public void Update(){
        // Moves the projectile.
        double delta_x = speed*Math.cos(direction);
        double delta_y = speed*Math.sin(direction);
        
        x += (int)delta_x;
        y += (int)delta_y;
    }
}
