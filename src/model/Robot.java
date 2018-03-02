package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by carltidelius on 2018-02-27.
 */
public class Robot {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    private int rows;
    private int cols;
    private int head;
    private Random rand;
    private DPoint currPos;
    private DPoint currRead;
    public Robot(int rows, int cols, int head){
        this.rows = rows;
        this.cols = cols;
        this.head = head;
        rand = new Random();
        currPos = new DPoint(new Point(rand.nextInt(rows), rand.nextInt(cols)), rand.nextInt(head));
        currRead = new DPoint(new Point(rand.nextInt(rows), rand.nextInt(cols)), rand.nextInt(head));
    }
    /*
    * Moves the robot according to the probabilities below.
    * h_t = current heading (north, south, east and west)
    * P( h_t+1 = h_t | not encountering a wall) = 0.7
    * P( h_t+1 != h_t | not encountering a wall) = 0.3
    * P( h_t+1 = h_t | encountering a wall) = 0.0
    * P( h_t+1 != h_t | encountering a wall) = 1.0
    */
    public void moveBot() {
        double chance = rand.nextDouble();
        ArrayList<DPoint> directions = currPos.getNeighbors(rows, cols);
        if (!directions.contains(currPos.getDirection()) || chance < 0.3)
            currPos = directions.get(rand.nextInt(directions.size()));
        else
            moveStraight();
        updateReading();
    }
    /*
    * Move the robots current position to a new position with the same direction
     */
    public void moveStraight(){
        int x = currPos.getX();
        int y = currPos.getY();
        int direction = currPos.getDirection();
        switch (currPos.getDirection()){
            case WEST:
                currPos = new DPoint(new Point(x - 1, y), direction);
            case SOUTH:
                currPos = new DPoint(new Point(x, y + 1), direction);
            case EAST:
                currPos = new DPoint(new Point(x + 1, y), direction);
            case NORTH:
                currPos = new DPoint(new Point(x, y - 1), direction);
        }
    }
    /*
    * Returns the robots current position
     */
    public DPoint getCurrPos(){
        return currPos;
    }
    /*
    * Returns the robots current sensor reading
     */
    public DPoint getCurrRead(){
        return currRead;
    }
    /*
    * Generates a rows by cols sensor matrix which provides the sensor readings 0.1 at the points position,
    * 0.05 at the first layers positions, 0.025 at the second layers positions.
    */
    public double[][] senseMatrix(int x, int y){
        DPoint pos = new DPoint(new Point(x, y), head);
        ArrayList<DPoint> first = pos.getLayerOne(rows, cols);
        ArrayList<DPoint> second = pos.getLayerTwo(rows, cols);
        double[][] sense = new double[rows][cols];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                if (pos.getX()==i && pos.getY() == j)
                    sense[i][j] = 0.1;
                else
                    sense[i][j] = 0;
                for (DPoint p: first) {
                    if(p.getX()==i && p.getY()==j)
                        sense[i][j] = 0.05;
                }
                for (DPoint p: second) {
                    if (p.getX() == i && p.getY() == j)
                        sense[i][j] = 0.025;
                }

            }
        }return sense;
    }
    /*
    * Update readings based on current Position and a chance
     */
    public void updateReading(){
        ArrayList<DPoint> first = currPos.getLayerOne(rows, cols);
        ArrayList<DPoint> second = currPos.getLayerTwo(rows, cols);
        int sizeFirst = first.size();
        int sizeSecond = second.size();
        double chance = rand.nextDouble();
        if(chance <= 0.1) currRead = currPos;
        else if (chance <= 0.1 + 0.05*sizeFirst) currRead = first.get(rand.nextInt(sizeFirst));
        else if (chance <= 0.1 + 0.05*sizeFirst + 0.025*sizeSecond) currRead = second.get(rand.nextInt(sizeSecond));
        else currRead = currRead;

    }
}
