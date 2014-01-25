import lejos.nxt.ColorSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final int LINE_VALUE = 280;
	private Odometer odometer;
    private   ColorSensor cs = new ColorSensor(SensorPort.S1);
    private Object lock;
    public static int counter = 0;
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
            	counter++;
            	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
			// put your correction code here
            synchronized (lock) {
            	
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
	public int getCounter()
	{
		return counter;
	}
}