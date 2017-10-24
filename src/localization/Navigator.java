package localization;


import ca.mcgill.ecse211.Lab5.ZiplineLab;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import localization.Odometer;

public class Navigator extends Thread {
	
	
	// vehicle variables
	private static EV3LargeRegulatedMotor leftMotor, rightMotor;
	private static Odometer odometer;
	private static final double RADIUS = 2.093;
	private static final double TRACK = 10.88;
	private static final double SQUARELENGTH = 30.48;
	private final int MOTOR_ACCELERATION = 200;
	
	// navigation variables
	public static final int FORWARD_SPEED = 200, ROTATE_SPEED = 100;
	//offset to made adjustments on the motor speed
	public static final int L_OFFSET_SPD=8;
	public static final int S_OFFSET_SPD=2;
	/* TODO
	 * If there is any changes to TRACK or RADIUS make sure to change odometer 
	 */
	public Navigator(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
	}
	
	public void run() {
		double currentX=0, currentY=0, currentTheta=0;
		int startCorner=ZiplineLab.getStartCorner();
		int [] coordinate = ZiplineLab.getCoordinate();
		
		/*
		 * depending on the corner selected, the robot will either move along the a-xis first
		 * or the y-axis, assuming the ramp is placed parallel to the x-axis
		 * set like this for lab5 only
		 */
		
		switch(startCorner) {
		case 0: currentX=1;
				currentY=1;
				currentTheta=0;
				odometer.setX(currentX*SQUARELENGTH);
				odometer.setY(currentY*SQUARELENGTH);
				odometer.setTheta(currentTheta);
				goForward(Math.abs(coordinate[1]-currentY)*SQUARELENGTH);
				turnTo(90, false);
				goForward(Math.abs(coordinate[0]-currentX)*SQUARELENGTH);
				break;
		case 1: currentX=7;
				currentY=1;
				currentTheta=270;
				odometer.setX(currentX*SQUARELENGTH);
				odometer.setY(currentY*SQUARELENGTH);
				odometer.setTheta(currentTheta);
				goForward(Math.abs(coordinate[0]-currentX)*SQUARELENGTH);
				turnTo(90,false);
				goForward(Math.abs(coordinate[1]-currentY)*SQUARELENGTH);
				break;
		case 2: currentX=7;
				currentY=7;
				currentTheta=180;
				odometer.setX(currentX*SQUARELENGTH);
				odometer.setY(currentY*SQUARELENGTH);
				odometer.setTheta(currentTheta);
				turnTo(90, false);
				goForward(Math.abs(coordinate[0]-currentX)*SQUARELENGTH);
				//travelTo(coordinate[0], coordinate[1]);
				turnTo(-90, false);
				goForward(Math.abs(coordinate[1]-currentY)*SQUARELENGTH);
				break;
		case 3: currentX=1;
				currentY=7;
				currentTheta=90;
				odometer.setX(currentX*SQUARELENGTH);
				odometer.setY(currentY*SQUARELENGTH);
				odometer.setTheta(currentTheta);
				turnTo(90,false);
				goForward(Math.abs(coordinate[1]-currentY)*SQUARELENGTH);
				turnTo(-90, false);
				goForward(Math.abs(coordinate[0]-currentX)*SQUARELENGTH);
				break;
		}
	}
	
	
	// Convert how far they need to travel
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}


	// Determine the angle the motors need to turn
	private static int convertAngle(double radius, double TRACK, double angle) {
		return convertDistance(radius, Math.PI * TRACK * angle / 360.0);
	}
	

	// Drives robot to specified cartesian coordinate
	public void travelTo(double x, double y) {
		x= x*SQUARELENGTH;
		y= y*SQUARELENGTH;
		
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
//		leftMotor.setAcceleration(100);
//		rightMotor.setAcceleration(100);
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED + L_OFFSET_SPD);
		leftMotor.rotate(convertDistance(RADIUS,distance), true);
		rightMotor.rotate(convertDistance(RADIUS, distance), false);

		leftMotor.stop(true);
		rightMotor.stop(true);
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
	}
	
	/**
	 * This method goes forward for a specific distance d
	 * This method calls rotate and distanceConvert(radius, distance) to have the value in tachocount
	 * @param d
	 * @return nothing
	 */
		public void goForward(double d) {
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED + S_OFFSET_SPD);
			leftMotor.forward();
			rightMotor.forward();

			leftMotor.rotate(convertDistance(RADIUS,d),true);
			rightMotor.rotate(convertDistance(RADIUS,d),false);
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
	
	//because carried by the thread
//	public void stop() {
//		rightMotor.stop();
//		leftMotor.stop();
//	}
	
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
