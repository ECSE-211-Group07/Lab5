package localization;

import lejos.hardware.Sound;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;

public class LightLocalization extends Thread {
	private double sensorReading;
	private int direction;
	private int sampleSize = 100;
	private float sample, prevSample = 0;
	private EV3ColorSensor sensor;
	
	//count how many black lines we have encountered
	private int counter = 0;
	//distance from front of robot to sensor
	private double d = 19;
	
	public static double x, y;
	
	private Navigation navigator;
	private Odometer odometer;
	
	//array to hold samples obtained from light sensor
	private float[] samples = new float[sampleSize];
	
	public LightLocalization(Navigation navigator, Odometer odometer) {
		this.navigator = navigator;
		this.odometer = odometer;
		sensor = new EV3ColorSensor(SensorPort.S4);
	}
	

	public void run() {
		boolean isBlack = false;
		while (true) {
			double thetaOneX = 0, thetaTwoX = 0, thetaOneY = 0, thetaTwoY = 0, deltaX, deltaY;
			sensor.getRedMode().fetchSample(samples, 0);
			sample = samples[0]; //get most recent sample
			if (prevSample - sample >= 0.1) {
				isBlack = true;
			} else {
				isBlack = false;
			}
			prevSample = sample;
			//black lines are any value less than 0.2
			if (isBlack) {
				Sound.beep();
				
				//increment counter
				counter++;
				//obtain theta values at each line crossing
				if (counter == 1) {
					thetaOneX = odometer.getTheta();
				} else if (counter == 2) {
					thetaOneY = odometer.getTheta();
				}
				else if (counter == 3) {
					thetaTwoX = odometer.getTheta();
					deltaX = thetaTwoX - thetaOneX;
					//use formula provided
					this.y = -d * Math.cos(deltaX / 2);
					//set y so we can use the travelTo method
					odometer.setY(-this.y + 5);
				} else if (counter == 4) {
					thetaTwoY = odometer.getTheta();
					deltaY = thetaTwoY - thetaOneY;
					// use formula provided
					this.x = -d * Math.cos(deltaY / 2);
					System.out.println("fourth line encountered");
					//set x so we can use travelTo method
					odometer.setX(-this.x + 5);
				}
			}
		}
		
	}
	
}
