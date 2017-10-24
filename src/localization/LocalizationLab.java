package localization;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * LocalizationLab uses a system to localize the robot using the ultrasonic sensor and the light sensor
 * the robot moves and it calculates its position. It can then be places on the (0,0) origin on the grid
 * @author Marine Huynh
 * @author Sihui Shen
 * @since 10-04-2017
 *
 */
public class LocalizationLab {

	//create the ports
	private static final EV3LargeRegulatedMotor leftMotor = 
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final EV3ColorSensor lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
	private static boolean isFallingEdge;
	private static double WHEEL_BASE = 9.75;
	private static double WHEEL_RADIUS = 2.1;
	
	private static SampleProvider colorSensor;
	private static float[] colorData;

	/**
	 * TODO
	 * @param args
	 * @return nothing 
	 */
	public static void main(String[] args) {
		int buttonChoice=0;

		//create the instances
		final TextLCD t=LocalEV3.get().getTextLCD();
		Odometer odometer=new Odometer(leftMotor,rightMotor, WHEEL_BASE);
		OdometryDisplay odometrydisplay=new OdometryDisplay(odometer,t);
		UltrasonicLocalizer usLocalizer;
		LightLocalization lightLocalizer;
		Navigator navigation = new Navigator(leftMotor, rightMotor, odometer);


		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
		// this instance
		float[] usData = new float[usDistance.sampleSize()];
		
		SensorModes colorMode = lightSensor;
		SampleProvider colorSensor = colorMode.getMode("Red");
		float[] colorData = new float[colorMode.sampleSize()];
		
		// usData is the buffer in which data are
		// returned  
		do {
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left   | Right  ", 0, 0);
			t.drawString("         |        ", 0, 1);
			t.drawString(" Falling | Rising ", 0, 2);
			t.drawString("  Edge   |  Edge  ", 0, 3);
			t.drawString("         |        ", 0, 4);
			buttonChoice= Button.waitForAnyPress();
		} while (buttonChoice!=Button.ID_LEFT && buttonChoice!=Button.ID_RIGHT);
		
		if(buttonChoice==Button.ID_LEFT) {
			setFallingEdge(true);
		}
		else {
			setFallingEdge(false);
		}

		do {
			// clear the display
			t.clear();

			// ask the user which sensor to use
			t.drawString("< Left   | Right  ", 0, 0);
			t.drawString("  Start  | Start  ", 0, 1);
			t.drawString(" Ultra   | Light  ", 0, 2);
			t.drawString("Localiz- | Localiz- ", 0, 3);
			t.drawString(" ation   | ation  ", 0, 4);
			buttonChoice= Button.waitForAnyPress();

		} while(buttonChoice!=Button.ID_LEFT && buttonChoice!=Button.ID_RIGHT);

		if(buttonChoice == Button.ID_LEFT) {
			odometer.start();
			odometrydisplay.start();
			usLocalizer = new UltrasonicLocalizer(leftMotor, rightMotor, odometer, usSensor, usData);
			usLocalizer.doLocalization();
		} else {
			odometer.start();
			odometrydisplay.start();
			t.clear();
			usLocalizer = new UltrasonicLocalizer(leftMotor, rightMotor, odometer, usSensor, usData);
			usLocalizer.doLocalization();
			lightLocalizer = new LightLocalization(odometer, colorSensor, colorData, navigation);
			lightLocalizer.doLocalization();
			navigation.travelTo(0, 2);
			navigation.travelTo(2, 2);
		}

		while(Button.waitForAnyPress()!=Button.ID_ESCAPE);
		System.exit(0);
	}

	/**
	 * method that checks if it is in falling edge mode
	 * @return boolean, true if is in falling edge
	 * 					false if it is not, which mean it is in rising edge
	 */
	public static boolean getFallingEdge() {
		return isFallingEdge;
	}

	/**
	 * setter for the falling edge
	 * @param isFallingEdge
	 * @return nothing
	 */
	public static void setFallingEdge(boolean isFallingEdge) {
		LocalizationLab.isFallingEdge = isFallingEdge;
	}


}
