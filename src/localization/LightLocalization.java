package localization;

import lejos.hardware.Sound;
import lejos.ev3.tools.EV3Console;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import localization.Navigation;
import localization.Odometer;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class LightLocalization {
	private Odometer odometer;
	private Navigation navigation;
	private static double SENSOR_DISTANCE = 24;//18
	double [] lightData;
	
	private SampleProvider colorSensor;
	private float[] colorData;

	/** Constructor for LightLocalization object that allows EV3 Robot to localize about a point (x, y) 
	 *  using a light sensor and basic trigonometry 
	 * @param odometer
	 * @param colorSensor
	 * @param colorData
	 * @param navigator
	 */
	public LightLocalization(Odometer odometer, SampleProvider colorSensor,
						  float[] colorData, Navigation navigator) {
		this.odometer = odometer;
		this.navigation = navigator;
		this.lightData = new double [5];
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}

	/** Localizes about a point (x, y) by calling subsequent helper functions
	 * @param x
	 * @param y
	 */
	public void doLocalization(double x, double y) {

		// goToApproxOrigin();

		// Will rotate the robot and collect lines
		rotateLightSensor();
		
		// correct position of our robot using light sensor data
		correctPosition(x, y);
		
		
		// travel to 0,0 then turn to the 0 angle
		navigation.travelTo(x, y);
		
		// Corrects theta value
		navigation.turnTo(-50, false);
		
		// navigation.setSpeed(0,0);
	}
	
	
	/* Rotates sensor around the origin and saves the theta 
	 * which the point was encoutered at
	 */
	private void rotateLightSensor() {
		navigation.turnTo(-360, true);
		int lineIndex=1;
		while(navigation.isNavigating()) {
			colorSensor.fetchSample(colorData, 0);
			if(colorData[0] < 0.25 && lineIndex < 5) {
				lightData[lineIndex]=odometer.getThetaDegrees();
				lineIndex++;
				Sound.beep();
			}
		}
	}
	
	
	
	/** Uses mathematical calculations to compute the correct robot position
	 * @param x
	 * @param y
	 */
	private void correctPosition(double x, double y) {
		//compute difference in angles
		double deltaThetaY= Math.abs(lightData[1]-lightData[3]);
		double deltaThetaX= Math.abs(lightData[2]-lightData[4]);
		
		//use trig to determine position of the robot 
		double Xnew = (x * 30.48)-SENSOR_DISTANCE*Math.cos(Math.toRadians(deltaThetaY) / 2);
		double Ynew = (y * 30.48)-SENSOR_DISTANCE*Math.cos(Math.toRadians(deltaThetaX) / 2);
		System.out.println("Xnew :" + Xnew); //7.5, 0.5
		System.out.println("Ynew :" + Ynew);
		
		odometer.setPosition(new double [] {Xnew, Ynew, 0}, 
					new boolean [] {true, true, true});

	}
}
