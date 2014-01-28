import lejos.nxt.*;
import lejos.util.*;

/*
 * Odometer.java
 * Constantly updates the X and Y position of the robot
 * 
 * Assumptions:
 * 
 * short time interval
 * angular velocity is constant
 */

public class Odometer extends Thread {
	// robot position
	/*constants*/
	NXTRegulatedMotor leftMotor = Motor.A;
	NXTRegulatedMotor rightMotor = Motor.B;
	// odometer update period, in ms
	private static final int ODOMETER_PERIOD = 25;

	//constants 
	private static double WHEEL_BASE = 15.5;
	private static double WHEEL_RADIUS = 2.16;
	
	/*variables*/ 
	//Tacho Count at last thread call
	private static int previousTachoL,previousTachoR;
	//Tacho Count when thread wakes
	private static int currentTachoL,  currentTachoR;
	//current position
	private double x, y, theta;

	

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		previousTachoL = 0;
		previousTachoR = 0;
		currentTachoL = 0;
		currentTachoR = 0;
		
	    LCD.clear();
	    LCD.drawString("Odometer Demo",0,0,false);
	    LCD.drawString("Current X  ",0,4,false);
	    LCD.drawString("Current Y  ",0,5,false);
	    LCD.drawString("Current T  ",0,6,false);

	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			//variables to calculate
			double leftDistance, rightDistance, deltaDistance, deltaTheta, dX, dY;
			//updates tachometer count of Left and Right motors
			currentTachoL = leftMotor.getTachoCount();
			currentTachoR = rightMotor.getTachoCount();
			/* 
			 * Calculates how many degrees the servos have rotated since the last poll
			 * Converts that value to a circumference -- Equal to the change in distance since the last poll
			 */
			leftDistance = 3.14159 * WHEEL_RADIUS * (currentTachoL - previousTachoL) / 180;
			rightDistance = 3.14159 * WHEEL_RADIUS * (currentTachoR - previousTachoR) / 180;
			//updates TachoCount to new value
			previousTachoL = currentTachoL;
			previousTachoR = currentTachoR;
			//Averages the left and right distances to approximate the change relative to the robot's center
			deltaDistance = .5 * (leftDistance + rightDistance);
			/*
			 * Estimates a change in heading by calculating the distance change 
			 * of the left and right motors relative to the center of the robot
			 */			
			deltaTheta = (leftDistance - rightDistance) / WHEEL_BASE;
			//X, Y and Theta must be changed at the same time, so synchronization is used
			synchronized (lock) {
				//Increment theta, around 360 degrees
				theta = (theta + deltaTheta) % 360;
				//calculate change in X and Y coordinates
				dX = deltaDistance * Math.sin(theta);
				dY = deltaDistance * Math.cos(theta);
				//Increment x and y
				x += dX;
				y += dY;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta / (2 * 3.141592) * 360;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}