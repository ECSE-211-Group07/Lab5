package localization;


import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import localization.Odometer;

public class Navigator {
	
	
	// vehicle variables
	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor, rightMotor;
	private static final double RADIUS = 2.1;
	private static final double TRACK = 9.75;
	private final int MOTOR_ACCELERATION = 50;
	
	// navigation variables
	public static final int FORWARD_SPEED = 250, ROTATE_SPEED = 100;

	
	/* TODO
	 * If there is any changes to TRACK or RADIUS make sure to change odometer  
	 */
	public Navigator(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.leftMotor.setAcceleration(MOTOR_ACCELERATION);
		this.rightMotor.setAcceleration(MOTOR_ACCELERATION);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180*distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		int output = convertDistance(radius, Math.PI * width * angle / 360.0);
		System.out.println(output);
		return output;
	}
	

	// Drives robot to specified cartesian coordinate
	public void travelTo(double x, double y) {
		x= x*30.48;
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
		turnTo(minAngle, false);
		
		// calculate the distance to next point
		double distance  = Math.hypot(deltaX, deltaY);
		
		// move to the next point
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(RADIUS,distance), true);
		rightMotor.rotate(convertDistance(RADIUS, distance), false);

		leftMotor.stop(true);
		rightMotor.stop(true);
	}
	

	// Turn to the min angle that you have chosen
	public void turnTo(double theta, boolean block) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		int angle = convertAngle(RADIUS, TRACK, theta);

		LCD.drawString(Integer.toString(angle), 0, 4);
		LCD.drawString(Double.toString(theta), 0, 5);
		
		if(theta < 0) { // if angle is negative, turn to the left
			LCD.drawString("Negative", 0, 6);
			leftMotor.rotate(angle, true);
			rightMotor.rotate(-angle, block);
			
		} else { // angle is positive, turn to the right
			LCD.drawString("Positive", 0, 6);
			leftMotor.rotate(angle, true);
			rightMotor.rotate(-angle, block);
		}

	}
	
	
	public void pointTo(double theta) {
		turnTo(theta - odometer.getThetaDegrees(), false);
	}
	
	public void stop() {
		rightMotor.stop();
		leftMotor.stop();
	}
	
	public void synchronizeStop() {
		synchronizeMotors();
		startSynchronization();
		leftMotor.stop();
		rightMotor.stop();
		endSynchronization();
	}
	
	//Allows robot to drive set # of cm
	public void driveDistance(int distance, boolean forward) {
		if (forward) {
			leftMotor.rotate(convertDistance(RADIUS, distance), true);
			rightMotor.rotate(convertDistance(RADIUS, distance), false);
		} else {
			leftMotor.rotate(-convertDistance(RADIUS, distance), true);
			rightMotor.rotate(-convertDistance(RADIUS, distance), false);
		}

	}
	
	public void synchronizeMotors() {
		RegulatedMotor[] motors = new RegulatedMotor[] {leftMotor};
		rightMotor.synchronizeWith(motors);
	}
	
	public void startSynchronization() {
		rightMotor.startSynchronization();
	}
	
	public void endSynchronization() {
		rightMotor.endSynchronization();
	}

	// Sets robot speed
	public void setSpeed(int leftM, int rightM) {
		leftMotor.setSpeed(leftM);
		rightMotor.setSpeed(rightM);
		
		if (leftM > 0) {
			leftMotor.forward();
		} else {
			leftMotor.backward();
		}
		
		if (rightM > 0) {
			rightMotor.forward();
		} else {
			rightMotor.backward();
		}
	}
	
	// Checks whether robot is navigating
	public boolean isNavigating() {
		return (leftMotor.isMoving() && rightMotor.isMoving());
	}
}
