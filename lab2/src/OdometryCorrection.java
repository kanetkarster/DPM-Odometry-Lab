import lejos.nxt.ColorSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	/*constants*/
	//Min ms per call of OdometryCorrection
	private static final long CORRECTION_PERIOD = 10;
	//black value used for lines
	private static final int LINE_VALUE = 280;
	//Radius of Grid
	private static final long SQUARE_SIZE = (long) 30.48;
	//Threshold (in cm) for activating Odometry correction
	private static final long THRESHOLD = 1;
	
	/*variables*/
	private Odometer odometer;
    private   ColorSensor cs = new ColorSensor(SensorPort.S1);
    private Object lock;
/*    //counts black lines crossed over
    public static int counter = 0;*/
    //stores values returned
    private double odoX, odoY;
    //Differences of X and Y values with approximate line values
    private double errorX;
    private double errorY;
	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		while (true) {
			correctionStart = System.currentTimeMillis();
            if(cs.getNormalizedLightValue() < LINE_VALUE)
            {	
/*            	//increases number of lines passed over
            	counter++;*/
            	synchronized (lock){
            		
            		//updates current values of odometer
            		odoX = odometer.getX();
            		odoY = odometer.getY();

            		//sees how many centimeter's the robot THINKS it is away from a line
            		errorX = (odoX % SQUARE_SIZE/2);
            		errorY = (odoY % SQUARE_SIZE/2);
            		/*
            		 * if that is a reasonable distance (ie. it is most likely not on a line or just turned over a line)
            		 * it rounds it's current position to a factor of the size of the square
            		 */
            		if(errorX < THRESHOLD && ((int) errorX)%2 != 1){
            			odometer.setX(SQUARE_SIZE * Math.round(odoX / SQUARE_SIZE));
            		}
            		if(errorY < THRESHOLD && ((int) errorY)%2 != 1){
            			odometer.setY(SQUARE_SIZE * Math.round(odoY / SQUARE_SIZE));
            		}
            	}
            	
            	try {
            		//waits for one second to avoid scanning one line multiple times
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//does nothing
				}
            }

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}