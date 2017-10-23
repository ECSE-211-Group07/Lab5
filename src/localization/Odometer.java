package localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
public class Odometer extends Thread {

	// robot position
	private double x;
	private double y;
	private double theta;
	private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	public static final double WHEEL_RADIUS = 2.2;
	public static final double TRACK = 9.88;


	private static final long ODOMETER_PERIOD = 25; /*odometer update period, in ms*/

	public Object lock; /*lock object for mutual exclusion*/

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;

		lock = new Object();
	}
	public double adjustTheta(double angle) {
		double twoPi = 2 * Math.PI;
		if (angle <= twoPi && angle >= 0) return angle;
		else if (angle > twoPi) {
			return angle - twoPi;
		} else {
			return angle + twoPi;
		}
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		//initialization
		int lastLeftTachoCount=0;
		int lastRightTachoCount=0;

		double distL, distR, deltaD, deltaT, dX, dY;

		while (true) {
			updateStart = System.currentTimeMillis();

			// TODO put (some of) your odometer code here

			//get present tachocount
			int currentLeftTachoCount=leftMotor.getTachoCount();
			int currentRightTachoCount=rightMotor.getTachoCount();


			//calculate arclength traveled by two wheels
			distL=Math.PI*(WHEEL_RADIUS)*(currentLeftTachoCount-lastLeftTachoCount)/180;
			distR=Math.PI*(WHEEL_RADIUS)*(currentRightTachoCount-lastRightTachoCount)/180;

			//set last tachocount to be current cachocount
			lastLeftTachoCount=currentLeftTachoCount;
			lastRightTachoCount=currentRightTachoCount;

			//calculate displacement and change in theta
			deltaD=(distL+distR)/2;
			deltaT=((distL-distR)/(TRACK))*180/(Math.PI);



			//normalization , make sure theta is between 0 and 360 degrees
			if (theta >= 360) {
				theta = theta - 360;
			}
			else if( theta < 0) {
				theta = theta +360;
			}

			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here! Only update the values of x, y,
				 * and theta in this block. Do not perform complex math
				 * 
				 */

				//calculate displacement in x and y direction
				dX=deltaD*Math.sin(theta*Math.PI/180);
				dY=deltaD*Math.cos(theta*Math.PI/180);

				//update the x, y and theta reading
				x=x+dX;
				y=y+dY;

				theta=theta+deltaT; // TODO replace example value
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

	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
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

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;
		}
	}
}
