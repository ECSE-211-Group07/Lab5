package ca.mcgill.ecse211.Lab5;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import localization.*;

public class ZiplineLab {

	//create the ports
	private static final EV3LargeRegulatedMotor leftMotor = 
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	//	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort=LocalEV3.get().getPort("S1");
	private static boolean isFallingEdge;
	// initiate integer to store coordinates
	private static int startCorner=0;

	//TODO for testing only, set back to 0 
	private static int xo=2;
	private static int yo=7;
	private static int xc=0;
	private static int yc=0;

	/**
	 * TODO
	 * @param args
	 * @return nothing 
	 */
	public static void main(String[] args) {
		int buttonChoice=0;

		//create the instances
		final TextLCD t=LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor,rightMotor);
		OdometryDisplay odometryDisplay =new OdometryDisplay(odometer,t);
		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer);
		Navigator navigator = new Navigator(leftMotor, rightMotor, odometer);
		//LightLocalization lightLocalizer;


		//created for the distance measured
		//@SuppressWarnings("resource") // Because we don't bother to close this resource
		//SensorModes colorSensor= new EV3ColorSensor(colorPort);
		//		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		//		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
		//SampleProvider colorValue=colorSensor.getMode("Red");
		//		float[] usData = new float[usDistance.sampleSize()];
		//float[] colorSample = new float[colorValue.sampleSize()];
		//		UltrasonicLocalizer usLocalizer=new UltrasonicLocalizer(leftMotor, rightMotor, 
		//				odometer, usDistance, usData);


		//select the xo and yo coordinates where the robot must pass by to access the zipline
		t.clear();
		t.drawString("  Enter         ", 0, 0);
		t.drawString("  X0, Y0           ", 0, 1);	
		t.drawString("  PRESS ANYBUTTON  ", 0, 2);
		t.drawString("  TO START           ", 0, 3);
		Button.waitForAnyPress();

		t.clear();
		t.drawString(" Xo="+xo +"Yo="+yo, 0, 0);
		t.drawString(" LEFT X-1", 0, 1);
		t.drawString(" RIGHT X+1",0, 2);
		t.drawString(" UP Y+1", 0, 3);
		t.drawString(" DOWN Y-1 ", 0,4 );
		t.drawString(" CONFIRM PRESS ENTER ", 0, 5);

		buttonChoice= Button.waitForAnyPress();
		t.clear();
		while (buttonChoice!=Button.ID_ENTER) {
			if(buttonChoice==Button.ID_LEFT) {
				xo--;
				t.drawString(" Xo="+xo +"Yo="+yo, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_RIGHT) {
				xo++;
				t.drawString(" Xo="+xo +"Yo="+yo, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_UP) {
				yo++;
				t.drawString(" Xo="+xo +"Yo="+yo, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_DOWN) {
				yo--;
				t.drawString(" Xo="+xo +"Yo="+yo, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}

			buttonChoice=Button.waitForAnyPress();
		}
		t.clear();
		t.drawString(" X0="+xo +"Yo="+yo   , 0, 0);

		t.drawString("   CONFIRMED   ", 0, 3);

		Button.waitForAnyPress();
		
		//select the xc and yc coordinates where the robot will get on the zipline
		t.clear();
		t.drawString(" Enter         ", 0, 0);
		t.drawString(" XC,YC           ", 0, 1);
		t.drawString(" PRESS ANYBUTTON  ", 0, 3);
		t.drawString(" TO START         ", 0, 4);
		Button.waitForAnyPress();

		t.clear();
		t.drawString(" Xc="+xc +"Yc="+yc, 0, 0);
		t.drawString(" LEFT X-1", 0, 1);
		t.drawString(" RIGHT X+1",0, 2);
		t.drawString(" UP Y+1", 0, 3);
		t.drawString(" DOWN Y-1 ", 0,4 );
		t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
		buttonChoice=Button.waitForAnyPress();


		while (buttonChoice!=Button.ID_ENTER) {
			if(buttonChoice==Button.ID_LEFT) {
				xc--;
				t.drawString(" Xc="+xc +"Yc="+yc, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_RIGHT) {
				xc++;
				t.drawString(" Xc="+xc +"Yc="+yc, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_UP) {
				yc++;
				t.drawString(" Xc="+xc +"Yc="+yc, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_DOWN) {
				yc--;
				t.drawString(" Xc="+xc +"Yc="+yc, 0, 0);
				t.drawString(" LEFT X-1", 0, 1);
				t.drawString(" RIGHT X+1",0, 2);
				t.drawString(" UP Y+1", 0, 3);
				t.drawString(" DOWN Y-1 ", 0,4 );
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}

			buttonChoice=Button.waitForAnyPress();
		}
		t.clear();
		t.drawString(" Xc="+xc+"Yc="+yc, 0, 0);
		t.drawString(" CONFIRMED   ", 0, 3);

		t.drawString("  Enter         ", 0, 0);
		t.drawString("  Start Corner      ", 0, 1);	
		t.drawString("  PRESS ANYBUTTON  ", 0, 2);
		t.drawString("  TO START           ", 0, 3);
		Button.waitForAnyPress();

		//select the corner at which the robot will start
		t.clear();
		t.drawString(" Starting Corner:" +startCorner, 0, 0);
		t.drawString(" LEFT startCorner-1", 0, 1);
		t.drawString(" RIGHT startcorner+1",0, 2);
		t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
		buttonChoice=Button.waitForAnyPress();

		while (buttonChoice!=Button.ID_ENTER) {
			if(buttonChoice==Button.ID_LEFT) {
				startCorner--;
				t.drawString(" Starting Corner:" + startCorner, 0, 0);
				t.drawString(" LEFT startCorner-1", 0, 1);
				t.drawString(" RIGHT startcorner+1",0, 2);
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			else if(buttonChoice==Button.ID_RIGHT) {
				startCorner++;
				t.drawString(" Starting Corner:" + startCorner, 0, 0);
				t.drawString(" LEFT startCorner-1", 0, 1);
				t.drawString(" RIGHT startcorner+1",0, 2);
				t.drawString(" CONFIRM PRESS ENTER  ", 0, 5);
			}
			buttonChoice=Button.waitForAnyPress();
		}
		t.clear();
		t.drawString(" Starting Corner:" + startCorner, 0, 0);
		t.drawString(" CONFIRMED   ", 0, 3);
		Button.waitForAnyPress();

		t.clear();

		odometer.start();
		odometryDisplay.start();
		//odometryCorrection.start();
		navigator.start();
		Button.waitForAnyPress();

		//usLocalizer.doLocalization();

		System.exit(0);

	}

	/**
	 * Getter that gets the integer of the corner where the robot started
	 * starting from 0 to 3, at origin 0, then going anti-clockwise
	 * @return the integer number of the corner startCorner
	 */
	public static int getStartCorner() {
		return startCorner;
	}

	/**
	 * getter that gets the array of the coordinates passed to the brick
	 * gets, coordinate{xo, yo, xc, yc}
	 * @return an array of integers for the coordinates passed
	 */
	public static int[] getCoordinate() {
		int[] coordinate= {xo,yo,xc,yc};
		return coordinate;
	}


}
