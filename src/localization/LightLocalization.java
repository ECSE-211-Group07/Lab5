package localization;

import lejos.hardware.Sound;
import lejos.ev3.tools.EV3Console;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import localization.Navigator;
import localization.Odometer;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class LightLocalization {
	private Odometer odometer;
	private Navigator navigation;
	private static double SENSOR_DISTANCE = 18;
	double [] lightData;
	
	private SampleProvider colorSensor;
	private float[] colorData;

	public LightLocalization(Odometer odometer, SampleProvider colorSensor,
						  float[] colorData, Navigator navigator) {
		this.odometer = odometer;
		this.navigation = navigator;
		this.lightData = new double [5];
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}

	public void doLocalization() {

		// goToApproxOrigin();

		// Will rotate the robot and collect lines
		rotateLightSensor();
		
		// correct position of our robot using light sensor data
		correctPosition();
		
		// travel to 0,0 then turn to the 0 angle
		navigation.travelTo(0, 0);
		
		// Corrects theta value
		navigation.turnTo(-50, false);
		
		// navigation.setSpeed(0,0);
	}
	
	/*
	 * Corrects angle of theta and stops the robot if it runs over 
	 * another set of lines
	 */
	public void correctAngle() {
		navigation.turnTo(-odometer.getThetaDegrees()%360, true);
		
		colorSensor.fetchSample(colorData, 0);
		while (navigation.isNavigating()) {
//			if(colorData[0] < 0.35 && (odometer.getThetaDegrees() < 20 || odometer.getThetaDegrees() > 340)) {
//				navigation.synchronizeStop();
//				break;
//			}
			colorSensor.fetchSample(colorData, 0);
		}
		
	}
	
	/* 
	* These next two calls will drive our robot to a 
	* region which will allow it rotate and scan the lines
	*/ 
	private void goToApproxOrigin() {
		navigation.turnTo(45, false);

		navigation.driveDistance(15, true);
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
		// navigation.setSpeed(0,0);
	}
	
	
	/**
	 * Uses mathematical calculations to compute the correct robot position
	 */
	private void correctPosition() {
		//compute difference in angles
		double deltaThetaY= Math.abs(lightData[1]-lightData[3]);
		double deltaThetaX= Math.abs(lightData[2]-lightData[4]);
		
		//use trig to determine position of the robot 
		double Xnew = -SENSOR_DISTANCE*Math.cos(Math.toRadians(deltaThetaY) / 2);
		double Ynew = -SENSOR_DISTANCE*Math.cos(Math.toRadians(deltaThetaX) / 2);
		
		odometer.setPosition(new double [] {Xnew, Ynew, 0}, 
					new boolean [] {true, true, false});

	}
}
