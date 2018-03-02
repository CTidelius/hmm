package model;

import control.EstimatorInterface;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by carltidelius on 2018-02-26.
 */
public class Localizer implements EstimatorInterface {
    private double[] dPointProbabilities;
    private double[][] transitionMatrix;
    private double[][] sensorMatrix;
    private DPoint[] dPoints;
    private int rows, cols, head;
    private Robot rob;
    private int updatedXtimes;
    private int avgdist;

    public Localizer(int rows, int cols, int head){
        rob = new Robot(rows, cols, head);
        this.cols = cols;
        this.rows = rows;
        this.head = head;
        initDPoints();
        initTransitions();
        updatedXtimes = 0;
        avgdist = 0;
    }
    @Override
    public int getNumRows() {
        return rows;
    }

    @Override
    public int getNumCols() {
        return cols;
    }

    @Override
    public int getNumHead() {
        return head;
    }

    @Override
    public void update() {
        rob.moveBot();
        sensorMatrix = rob.senseMatrix(rob.getCurrRead().getX(), rob.getCurrRead().getY());
        updateProp(sensorMatrix);
        int[] mostLikely = predictedPos();
        int diffRow = Math.abs(rob.getCurrPos().getY() - mostLikely[1]);
        int diffCol = Math.abs(rob.getCurrPos().getX() - mostLikely[0]);
        double dist = diffCol + diffRow;
        updatedXtimes++;
        avgdist+=dist;
        System.out.println("Distance: " + dist + "Avg dist: " + (double)avgdist/updatedXtimes);


    }
    /*
    * Updates the probability vector according to:
    * sensor readings * transitions probabilities * current probabilities
    * also reforms the sensormatrix into a vector for easier handling
     */
    private void updateProp(double[][] sense){
        double[] sensetemp = new double[rows*cols*head];
        int count = 0;
        for (int i = 0; i < cols; i ++){
            for (int j = 0; j < rows; j++){
                for (int h = 0; h < head; h++){
                    sensetemp[count] = sense[i][j];
                    count ++;
                }
            }
        }
        double[] temp = new double[rows*cols*head];
        double prob = 0;
        for (int i = 0; i < sensetemp.length; i ++){
            temp[i] = 0;
            for (int j = 0; j < sensetemp.length; j ++){
                temp[i] += sensetemp[i] * transitionMatrix[j][i] * dPointProbabilities[j];
            }
            prob += temp[i];
        }
        for(int i = 0; i < temp.length; i++) {
            dPointProbabilities[i] = temp[i] / prob;
        }

    }

    @Override
    public int[] getCurrentTruePosition() {
        int[] currpos = new int[2];
        currpos[0] = rob.getCurrPos().getX();
        currpos[1] = rob.getCurrPos().getY();
        return currpos;
    }

    @Override
    public int[] getCurrentReading() {
        int[] curread = new int[2];
        curread[0] = rob.getCurrRead().getX();
        curread[1] = rob.getCurrRead().getY();
        return curread;
    }

    @Override
    public double getCurrentProb(int x, int y) {
        int start = x * cols * head + y * head;
        return dPointProbabilities[start] + dPointProbabilities[start + 1] +
                dPointProbabilities[start + 2] +dPointProbabilities[start + 3];
    }

    @Override
    public double getOrXY(int rX, int rY, int x, int y, int h) {

        DPoint dp = new DPoint(new Point(x,y), h);
        sensorMatrix = getSensorMatrix(x, y);
        ArrayList<DPoint> first = dp.getLayerOne(rows, cols);
        ArrayList<DPoint> second = dp.getLayerTwo(rows, cols);
        if (rX == -1 || rY == -1)
            return 1 - 0.1 - first.size()*0.05 - second.size() * 0.025;
        else{
            return sensorMatrix[rX][rY];
        }
    }

    @Override
    public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
        return transitionMatrix[x * cols + y *cols *  head + h][nX * cols  + nY *cols * head + nH];

    }

    /*
    * Returns an integer vector with x,y coordinates of the most probable location of the robot
     */
    private int[] predictedPos() {
        double highest = 0.0, current;
        int[] pos = new int[2];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                current = getCurrentProb(i, j);
                if (current > highest) {
                    highest = current;
                    pos[0] = i;
                    pos[1] = j;
                }
            }
        }
        return pos;
    }
    /*
    * Returns a rows by cols matrix with the sensor readings
     */
    public double[][] getSensorMatrix(int x, int y){
        return rob.senseMatrix(x, y);
    }
    /*
    * Initialize the points with directions as well as their starting probabilities
     */
    public void initDPoints(){
        int nbrDPoints = rows * cols * head;
        dPoints = new DPoint[nbrDPoints];
        dPointProbabilities = new double[nbrDPoints];
        int count= 0;
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                for (int h = 0; h < head; h++) {
                    DPoint dp = new DPoint(new Point(i,j), h);
                    dPoints[count] = dp;
                    dPointProbabilities[count] = 1.0 / nbrDPoints;
                    count ++;
                }
            }
        }


    }

    /*
    * Initialize the transition matrix
     */
    public void initTransitions(){
        int nbrDPoints = rows * cols * head;
        transitionMatrix = new double[nbrDPoints][nbrDPoints];

        for (int i = 0; i < nbrDPoints; i++) {
            DPoint currState = dPoints[i];
            for (int j = 0; j < nbrDPoints; j++) {
                DPoint other = dPoints[j];
                if (currState.willReach(other)) {
                    int size = currState.getNeighbors(rows,cols).size();
                    if (currState.facesWall(rows,cols)) {
                        transitionMatrix[i][j] = 1.0 / size;
                    }else if (currState.getDirection() == other.getDirection()) {
                        transitionMatrix[i][j] = 0.7;
                    }else{
                        transitionMatrix[i][j] = 0.3 / (size-1);
                    }

                } else {
                    transitionMatrix[i][j] = 0.0;
                }
            }
        }
    }
}
