package localization;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.ColorDetector;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalization {
	private final int FORWARD_SPEED = 80;
	private Odometer odometer;
	private EV3ColorSensor colorSensor;
	private int sampleSize = 100;
	private float[] samples = new float[sampleSize];
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Navigation navigation;
	private TextLCD LCD = LocalEV3.get().getTextLCD();

	/**
	 * Constructor of the class LightLocalization uses the following parameters to
	 * locate the position of the robot, and move the robot to the origin of the
	 * grid
	 * 
	 * @param odometer
	 * @param colorSample
	 * @param colorSensor
	 * @param leftMotor
	 * @param rightMotor
	 */
	public LightLocalization(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.odometer = odometer;
		colorSensor = new EV3ColorSensor(SensorPort.S4);
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftMotor.setAcceleration(300);
		this.rightMotor.setAcceleration(300);
		navigation = new Navigation(leftMotor, rightMotor, odometer);

	}

	/**
	 * run method that move the robot to the origin it uses the lightSensor to
	 * localize its position on the grid, and travels to the origin
	 * 
	 * @return nothing
	 */
	public void doLocalization() {
		int counter = 0;
		boolean isNavigating = true;
		double thetaOneX = 0, thetaTwoX = 0, thetaOneY = 0, thetaTwoY = 0, deltaX, deltaY;
		do {

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.backward();
			while (isNavigating) {
				if (isBlack()) {
					counter++;
					// obtain theta values at each line crossing
					if (counter == 1) {
						Sound.beep();
						thetaOneX = odometer.getTheta();
					} else if (counter == 2) {
						Sound.beep();
						thetaOneY = odometer.getTheta();
					} else if (counter == 3) {
						Sound.beep();
						thetaTwoX = odometer.getTheta();
						deltaX = thetaTwoX - thetaOneX;
						// use formula provided
						// this.y = -d * Math.cos(deltaX / 2);
						// set y so we can use the travelTo method
					} else if (counter == 4) {
						Sound.beep();
						isNavigating = false;
						thetaTwoY = odometer.getTheta();
						deltaY = thetaTwoY - thetaOneY;
						// use formula provided
						// this.x = -d * Math.cos(deltaY / 2);
						// set x so we can use travelTo method
					}
					if (!isNavigating) {
						double angler = odometer.getTheta();
						navigation.turnTo(360-angler);
						leftMotor.stop();
						rightMotor.stop();
					}
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (Button.waitForAnyPress() != Button.ID_ESCAPE);

	}

	/**
	 * method that collects the data from the lightSensor and checks if there is a
	 * change in color from a beige square to a black line
	 * 
	 * @return boolean, if there is a change in the color, return true if there is
	 *         not change, meaning still on the beige square, return false
	 */
	private boolean isBlack() {
		colorSensor.getRedMode().fetchSample(samples, 0);
		System.out.println(samples[0]);
		return samples[0] <= 0.2;
	}
}
