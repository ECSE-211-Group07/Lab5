package localization;

import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation {
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3MediumRegulatedMotor usMotor;
	private TextLCD t;
	private Odometer odometer;
	private SampleProvider us;
	private float[] usData;
	
	//start a 2D array of waypoints
	private static int[][] waypoints=new int[][]{{2,1},{1,1},{1,2},{2,0}};
	
	private static final int FORWARD_SPEED = 300;
	private static final int ROTATE_SPEED = 70;
	private static final double WHEEL_RADIUS=2.2;
	private static final double TRACK= 9.88;
	private static final double SQRTLENGTH=30.48;
	
	private int x;
	private int y;
	
	/**
	 * Constructor for the NAvigation class that takes in the two motors, and the odometer to be
	 * able to move
	 * @param leftMotor
	 * @param rightMotor
	 * @param odometer
	 */
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.leftMotor=leftMotor;
		this.rightMotor=rightMotor;
		this.odometer=odometer;
	}
	

	public void run() {
		for(int i=0; i<waypoints.length; i++) {
			double x= waypoints[i][0]*SQRTLENGTH;
			double y=waypoints[i][1]*SQRTLENGTH;
			travelTo(x,y);
		}
	}
		
		/**
		 * This method causes the robot to travel to the absolute field location(x,y),
		 * specified in tile points. This will make sure that the heading
		 * is updated until you reach your exact goal. 
		 * This method will poll the odometer for current position information.
		 * @param x
		 * @param y
		 * @return nothing
		 */
	public void travelTo(double x, double y) {
		x= -x*30.48;
		y= y*30.48;
		
		double deltaX = x - odometer.getX();
		double deltaY = y - odometer.getY();
		
		
		// calculate the minimum angle
		double minAngle = Math.toDegrees(Math.atan2(deltaX, deltaY)) - odometer.getThetaDegrees();
		
		// Adjust the angle to make sure it takes the min angle
		if (minAngle < -180) {
			minAngle = 360 + minAngle;
		} else if (minAngle > 180) {
			minAngle = minAngle - 360;
		}
		
		// turn to the minimum angle
		turnTo(minAngle);
		
		// calculate the distance to next point
		double distance  = Math.hypot(deltaX, deltaY);
		
		// move to the next point
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(distanceConvert(WHEEL_RADIUS,distance), true);
		rightMotor.rotate(distanceConvert(WHEEL_RADIUS, distance), false);

		leftMotor.stop(true);
		rightMotor.stop(true);
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
	}
		
		/**
		 *  Method that causes the robot to turn on itself to the absolute heading theta. 
		 * This method should turn a MINIMAL angle to its target.
		 * @param theta
		 * @return Nothing
		 */
		void turnTo(double theta) {
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			
			leftMotor.rotate(angleConvert(WHEEL_RADIUS,TRACK,theta),true);
			rightMotor.rotate(-angleConvert(WHEEL_RADIUS,TRACK,theta),false);
		
		}

	/**
	 * This method goes forward for a specific distance d
	 * This method calls rotate and distanceConvert(radius, distance) to have the value in tachocount
	 * @param d
	 * @return nothing
	 */
		public void goForward(double d) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();

		
			leftMotor.rotate(distanceConvert(WHEEL_RADIUS,d),true);
			rightMotor.rotate(distanceConvert(WHEEL_RADIUS,d),false);
		
		}
		
		/**
		 * Method returns the number of tachocount it need to travel a certain distance
		 * Calculates the tachocount from the radius of the wheel
		 * @param radius
		 * @param distance
		 * @return
		 */
		public static int distanceConvert(double radius, double distance) {
			return(int) (distance*180/Math.PI/radius);
		}

		/**
		 * This method converts the angle into the number of tachocount it need to turn for this degree
		 * @param radius
		 * @param width
		 * @param angle
		 * @return an integer that is the distance
		 */
		private static int angleConvert(double radius, double width, double angle) {

			return distanceConvert(radius, Math.PI * width * angle / 360.0);
		}
	/**
	 * This method is to know whether or not the robot is travelling
	 * 
	 * @return Boolean isTravelling
	 */
		boolean isNavigating() {
			boolean isTravelling = true;
			return isTravelling;
		}
		
		
}
