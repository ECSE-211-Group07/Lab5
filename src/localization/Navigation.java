package localization;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;

public class Navigation extends Thread {

	private TextLCD t = LocalEV3.get().getTextLCD();
	
	private static final int FORWARD_SPEED = 100;
	private static final int ROTATE_SPEED = 120;
	
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;

	private static double wheelBase;
	private static double wheelRadius;

	public double currentTheta, currentX, currentY;
	private boolean navigating;
	private Odometer odometer;

	public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			double wheelBase, double wheelRadius) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.wheelBase = wheelBase;
		this.wheelRadius = wheelRadius;
		this.odometer = odometer;
	}

	public void travelTo(double x, double y) {
		x = 30.48 * x;
		y = 30.48 * y;
		synchronized (odometer.lock) {
			currentTheta = odometer.getTheta() * 180 / Math.PI; //Convert Theta to degrees
			currentX = odometer.getX();
			currentY = odometer.getY();
		}
		
		double differenceX = x - currentX; //Number of grid lines it has to travel in terms of X
		double differenceY = y - currentY; //Number of grid lines it has to travel in terms of Y
		double deltaTheta = Math.atan2(differenceX, differenceY) * 180 / Math.PI; //the angle it has to turn if the robot was at the coordinates (0,0)
		double thetaTurn = deltaTheta - currentTheta; //The angle it has to turn to to go through the shortest way 
		double distance = Math.sqrt(Math.pow((y-currentY), 2) + Math.pow((x-currentX), 2));
		//We find the shortest distance that we need to travel using Pythagorian theorem
		if(thetaTurn < -180) {
			turnTo(thetaTurn + 360);
		}
		else if(thetaTurn > 180) {
			turnTo(thetaTurn - 360);
		}
		else  {
			turnTo(thetaTurn);
		}
		// We find the smallest turn that the robot needs to make to go to its destination
		
		travel(distance);
	}

	public void travel(double distance) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		// for isNavigatingMethod
		navigating = true;

		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), false);

		navigating = false;
	}

	public void turnTo(double theta) {
		// turn degrees clockwise
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		navigating = true;
		// calculates angle to turn to and rotates
		leftMotor.rotate(convertAngle(wheelRadius, wheelBase, theta), true);
		rightMotor.rotate(-convertAngle(wheelRadius, wheelBase, theta), false);

		navigating = false;
	}
	
	public boolean isNavigating() {
		return this.navigating;
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}