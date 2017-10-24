package localization;



import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class UltrasonicLocalizer extends Thread implements UltrasonicController {
	//use two ArrayLists to keep track of distance, theta pairs
	ArrayList<Integer> distances = new ArrayList<Integer>();
	ArrayList<Double> thetas = new ArrayList<Double>();
	private int distance;

	final TextLCD t = LocalEV3.get().getTextLCD();

	private Odometer odometer;
	private Navigator navigator;
	
	//filter variables
	private static final int FILTER_OUT = 20;
	private int filterControl;
	
	private String mode;

	private double firstFallingEdge, secondFallingEdge;

	EV3LargeRegulatedMotor leftMotor, rightMotor;

	public UltrasonicLocalizer(Odometer odometer, Navigator navigator, EV3LargeRegulatedMotor leftMotor,
			EV3LargeRegulatedMotor rightMotor, String mode) {
		this.odometer = odometer;
		this.navigator = navigator;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.mode = mode;
	}

	public void run() {
		
		//switch statement to toggle between falling and rising edge
		switch (this.mode) {
			case "falling":
				//odometer.setTheta(0);		//ensure theta is initially 0 for odometer
				navigator.turnTo(360, false);		//do full 360 rotation, gathering data from US sensor
				double firstFallingEdge = calculateFallingEdge();	
				navigator.turnTo(-360, false);		//do full 360 rotation, gathering data from US sensor
				double secondFallingEdge = calculateFallingEdge();
				double fallingCorner = (firstFallingEdge + secondFallingEdge) / 2;	//angle of corner between two walls
				navigator.turnTo(fallingCorner + 135, false);	// rotate to the corner, and then an additional 135 degrees to get to 0 degrees
				odometer.setTheta(0);
				t.drawString("first: " + firstFallingEdge, 0, 4);
				t.drawString("second: " + secondFallingEdge, 0, 5);
				t.drawString("corner: " + fallingCorner, 0, 6);
				break;
			case "rising":
				odometer.setTheta(0);
				navigator.turnTo(360, false);
				double firstRisingEdge = calculateRisingEdge();
				navigator.turnTo(-360, false);
				double secondRisingEdge = calculateRisingEdge();
				double risingCorner = (firstRisingEdge + secondRisingEdge) / 2;
				navigator.turnTo(risingCorner - 45, false);
				odometer.setTheta(0);
				t.drawString("first: " + firstRisingEdge, 0, 4);
				t.drawString("second: " + secondRisingEdge, 0, 5);
				t.drawString("corner: " + risingCorner, 0, 6);
				break;
		}
		

	}

	public double calculateFallingEdge() {
		//initialize return variable to 0
		double wallTheta = 0;
		// set d variable to 45, we are interested in values less than this distance
		int d = 45;
		//iterate through distances, and find first distance where distance < d
		for (int i = 0; i < this.distances.size(); i++) {
			if (this.distances.get(i) < d && wallTheta == 0) {
				//get corresponding theta from arraylist
				wallTheta = this.thetas.get(i);
				break;
			}
		}
		
		//clear arraylists so it can be used to find second falling edge
		this.distances.clear();
		this.thetas.clear();
		return wallTheta;
	}
	

	public double calculateRisingEdge() {
		//logic is exact same as falling edge, except we want the first distance > d
		double wallTheta = 0;
		//different d for rising edge, obtained experimentally 
		int d = 70;
		for (int i = 0; i < this.distances.size(); i++) {
			if (this.distances.get(i) > d && wallTheta == 0) {
				wallTheta = this.thetas.get(i);
				break;
			}
		}
		this.distances.clear();
		this.thetas.clear();
		return wallTheta;
	}
	

	@Override
	public void processUSData(int distance) {
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
			this.distances.add(distance);
			this.thetas.add(this.odometer.getThetaDegrees());
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			this.distance = distance;
			this.distances.add(distance);
			this.thetas.add(this.odometer.getThetaDegrees());
		}
		
		
		
		
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}