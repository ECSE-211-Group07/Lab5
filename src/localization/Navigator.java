package localization;


import ca.mcgill.ecse211.Lab5.ZiplineLab;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import localization.Odometer;

public class Navigator {
	
	
	// vehicle variables
	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor, rightMotor, zipMotor;
	private static final double RADIUS = 2.2;
	private static final double TRACK = 9.88;
	private final int MOTOR_ACCELERATION = 200;
	
	// navigation variables
	public static final int FORWARD_SPEED = 250, ROTATE_SPEED = 100;

	
	/* TODO
	 * If there is any changes to TRACK or RADIUS make sure to change odometer  
	 */
	public Navigator(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
					EV3LargeRegulatedMotor zipMotor, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.zipMotor = zipMotor;
		this.odometer = odometer;
	}
	
	
	//TODO IF NAVIGATION IS BROKEN CHECK HERE
	// Convert how far they need to travel
	private static int convertDistance(double radius, double distance) {
		return (int)((distance) / (2*Math.PI * radius));
	}


	// Determine the angle the motors need to turn
	private static int convertAngle(double radius, double TRACK, double angle) {
		return convertDistance(radius, Math.PI * TRACK * angle / 360.0);
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
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
	}
	

	// Turn to the min angle that you have chosen
	public void turnTo(double theta, boolean block) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		if(theta < 0) { // if angle is negative, turn to the left
			leftMotor.rotate(-convertAngle(RADIUS, TRACK, -theta), true);
			rightMotor.rotate(convertAngle(RADIUS, TRACK, -theta), block);
			
		} 
		else { // angle is positive, turn to the right
			leftMotor.rotate(convertAngle(RADIUS, TRACK, theta), true);
			rightMotor.rotate(-convertAngle(RADIUS, TRACK, theta), block);
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
	public void driveDistance(double distance, boolean forward) {
		if (forward) {
			System.out.println(convertDistance(RADIUS, distance));
			leftMotor.rotate((int) (360*convertDistance(RADIUS, distance)), true);
			rightMotor.rotate((int) (360*convertDistance(RADIUS, distance)), false);
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
	
	
	public void driveZiplineDistance(double distance) {
		zipMotor.rotate(convertDistance(RADIUS, distance), false);
	}
	
	
	public void driveZipline() {
		leftMotor.setAcceleration(60);
		rightMotor.setAcceleration(60);
		zipMotor.setSpeed(350);
		zipMotor.backward();

		driveDistance(30.48 * 9, true);
		Sound.beep();
		
		Sound.beep();
		
	}
}
