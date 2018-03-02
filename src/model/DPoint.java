package model;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by carltidelius on 2018-02-26.
 */
public class DPoint {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    private Point p;
    private int direction;
    public DPoint(Point p, int direction){
        this.p = p;
        this.direction = direction;
    }
    /*
    * Returns this points direction (North, south, east, west)
     */
    public int getDirection(){
        return direction;
    }
    /*
    * Returns this points x coordinate
     */
    public int getX(){
        return (int)p.getX();
    }
    /*
    * Returns this points y coordinate
     */
    public int getY(){
        return (int)p.getY();
    }

    public ArrayList<DPoint> getNeighbors(int rows, int cols){
        ArrayList<DPoint> neighbors = new ArrayList<>();
        DPoint temp4 = new DPoint(new Point((int)p.getX(), (int)p.getY() - 1), SOUTH);
        DPoint temp3 = new DPoint(new Point((int)p.getX() + 1, (int)p.getY()), EAST);
        DPoint temp2 = new DPoint(new Point((int)p.getX(), (int)p.getY() + 1), NORTH);
        DPoint temp1 = new DPoint(new Point((int)p.getX() - 1, (int)p.getY()), WEST);
        if (temp1.isInside(rows, cols)) neighbors.add(temp1);
        if (temp2.isInside(rows, cols)) neighbors.add(temp2);
        if (temp3.isInside(rows, cols)) neighbors.add(temp3);
        if (temp4.isInside(rows, cols)) neighbors.add(temp4);
        return neighbors;
    }
    /*
    * Checks if this points direction is into a wall
     */
    public boolean facesWall(int rows, int cols){
        return direction == NORTH && p.getY() == 0 || direction == SOUTH && p.getY() == rows-1 ||
                direction == WEST && p.getX() == 0 || direction == EAST && p.getX() == cols-1;
    }
    /*
    * Checks if this point is inside of the board Cols x Rows
     */
    public boolean isInside(int rows, int cols){
        return p.getX() >= 0 && p.getX() < cols && p.getY() >= 0 && p.getY() < rows;
    }
    /*
    * Check is this point can be reached by another point dp
     */
    public boolean willReach(DPoint dp){
        return ((p.getX() == dp.p.getX() && p.getY() == dp.p.getY() + 1 && dp.direction == NORTH)
                || (p.getX() == dp.p.getX() && p.getY() == dp.p.getY() - 1
                && dp.direction == SOUTH)
                || (p.getX() == dp.p.getX() - 1 && p.getY() == dp.p.getY()
                && dp.direction == EAST)
                || (p.getX() == dp.p.getX() + 1 && p.getY() == dp.p.getY()
                && dp.direction == WEST));

    }
    /*
    * Gets the surrounding squares of the point p
     */
    public ArrayList<DPoint> getLayerOne( int rows, int cols){
        ArrayList<DPoint> layerOne = new ArrayList<>();
        for (int i = getX()-1; i <= getX() + 1; i++){
            for (int j = getY() - 1; j <= getY() + 1; j++){
                DPoint temp = new DPoint(new Point(i, j), getDirection());
                if (temp.isInside(rows, cols) && !compare(temp)){
                    layerOne.add(temp);
                }
            }
        }
        return layerOne;
    }
    /*
    * Gets the surrounding sqares of the squares surrounding point p
     */
    public ArrayList<DPoint> getLayerTwo( int rows, int cols){
        ArrayList<DPoint> layerTwo = new ArrayList<>();
        ArrayList<DPoint> layerOne = getLayerOne(rows, cols);
        for (int i = getX()-2; i <= getX() + 2; i++){
            for (int j = getY() - 2; j <= getY() + 2; j++){
                DPoint temp = new DPoint(new Point(i, j), getDirection());
                if (temp.isInside(rows, cols) && !compare(temp) && !checkLayerOne(layerOne, temp)){
                    layerTwo.add(temp);
                }
            }
        }
        return layerTwo;
    }
    /*
    * Helper method for getLayerTwo to check if a point temp is in layer one
     */
    private boolean checkLayerOne(ArrayList<DPoint> layerOne, DPoint temp){
        for (DPoint dp : layerOne){
            if (dp.compare(temp)){
                return true;
            }
        }
        return false;
    }
    /*
    * Compares two points
     */
    private boolean compare(DPoint p){
        if (getX() == p.getX() && getY() == p.getY() && getDirection() == p.getDirection()){
            return true;
        }
        return false;
    }

}
