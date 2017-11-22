package fr.utbm.tr54.ev3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class RobotController implements AutoCloseable {
	
	private static RobotController instance = null;
	
	public EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.B);
	public EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.C);
	
	public EV3UltrasonicSensor dist = new EV3UltrasonicSensor(SensorPort.S2);
	public EV3ColorSensor color = new EV3ColorSensor(SensorPort.S3);
	
	private SampleProvider distanceSampleProvider;
	
	private RobotController() {
	}
	
	public static RobotController getInstance() {
		if (instance == null) {
			instance = new RobotController();
		}
		return instance;
	}
	
	/**
	 * Move the robot forward
	 */
	public void forward() {

		left.forward();
		right.forward();
	}
	
	/**
	 * Move the robot backward
	 */
	public void backward() {

		left.backward();
		right.backward();
	}
	
	/**
	 * Set a speed ration between 0 and 100% of the max speed
	 * @param p a float between 0 and 100
	 */
	public void setSpeedPercent(float p) {
		setSpeedRatio(p / 100);
	}

	/**
	 * Set a speed ration between 0 and 100% of the max speed
	 * @param p an int between 0 and 100
	 */
	public void setSpeedPercent(int p) {
		setSpeedRatio(p * 1f / 100);
	}
	
	/**
	 * Set a speed ration between 0 and 1 time the max speed
	 * @param ratio a float between 0 and 1
	 */
	public void setSpeedRatio(float ratio) {
		setSpeed(ratio*left.getMaxSpeed());
	}
	
	public void setSpeed(float speed) {
		left.setSpeed(speed);
		right.setSpeed(speed);
	}
	
	public float getSpeed() {
		return left.getSpeed();
	}

	/**
	 * Move to the left.
	 * If flag value is big enough, set an important max speed for the left engine
	 * @param flag to check if the robot should have a higher speed
	 */
	public void left(int flag) {
		int accINT, accEXT, vitINT, vitEXT;
		
		if (flag>=70){
			accEXT = 7000;
			accINT = 2000;
			vitEXT = 800;
			vitINT = 5;
			left.setSpeed(vitINT);
//			System.out.println("flagLEFT="+flag);
		} else {		
			accEXT = 4000;
			accINT = 2000;
			vitEXT = 500;
			vitINT = 50;
			left.setSpeed(vitINT);
		}
		
		right.setAcceleration(accEXT);
		right.setSpeed(vitEXT);
		right.backward();
		
		left.setAcceleration(accINT);
		left.backward();
	}

	/**
	 * Move to the right.
	 * If flag value is big enough, set an important max speed for the right engine
	 * @param flag to check if the robot should have a higher speed
	 */
	public void right(int flag) {
		int accINT, accEXT, vitINT, vitEXT;
		
		if (flag>=70){
			accEXT = 7000;
			accINT = 2000;
			vitEXT = 800;
			vitINT = 5;
			right.setSpeed(vitINT);
//			System.out.println("flagRIGHT="+flag);
		} else {			
			accEXT = 4000;
			accINT = 2000;
			vitEXT = 500;
			vitINT = 50;
			right.setSpeed(vitINT);
		}
		
		right.setAcceleration(accINT);
		right.backward();
		
		left.setSpeed(vitEXT);
		left.setAcceleration(accEXT);
		left.backward();
	}
	
	/**
	 * Rotate the robot according to a certain angle.
	 * Should not be used since rotation isn't always correct,
	 * depending of the requested angle
	 * @param rad angle to rotate in rad
     * @param immediate_return if true do not wait for the move to complete
	 */
	@Deprecated
	public void rotate(float rad, boolean immediate_return) {
		float rotation_factor = 2; 
		left.rotate(toDeg(rad * rotation_factor), true);
		right.rotate(toDeg(-rad * rotation_factor), immediate_return);
	}
	
	/**
	 * Rotate the robot accoring to a certain angle, returns at the end of the rotation
	 * @param rad angle to rotate in rad
	 */
	@Deprecated
	public void rotate(float rad) {
		rotate(rad, false);
	}
	
	/**
	 * Stop the robot
	 */
	public void stop() {
		left.stop(true);
		right.stop();
	}
	
	/**
	 * Enable distance sensor
	 */
	public void enableDist() {
		dist.enable();
	}
	
	/**
	 * Get the current distance between the sensor and 
	 * the closest object in line of sight
	 * @return the distance in meter to the wall
	 */
	public float distance() {
		float sample[] = new float[1];
		dist.getDistanceMode().fetchSample(sample, 0);
		return sample[0] * 100;
	}
	
	/**
	 * Get the mean over n samples of the distance
	 * @param n
	 * @return
	 */
	public float distance(int n) {
		float sample[] = new float[n];
		if (distanceSampleProvider == null) {
			distanceSampleProvider = dist.getDistanceMode();
		}
		for (int i=0;i<n;i++) {
			// FIXME: get every time the same value ??
			float val[] = new float[1];
			distanceSampleProvider.fetchSample(val, 0);
			sample[i] = val[0];
			// Delay.msDelay(10);
		}
		return mean(sample);
	}
	
	/**
	 * Get the current color under the sensor
	 * @return an int representing the detected color
	 */
	public int getColor() {
		int c = color.getColorID();
		//System.out.println(c);
		return c;
	}
	
	/**
	 * Convert Radians to Degrees
	 * @param rad angle in rad
	 * @return angle in deg
	 */
	private static int toDeg(float rad) {
		return (int) (rad * 360 / (2*Math.PI));
	}
	
	/**
	 * Get the mean of the values given in parameter
	 * @param vals the values to be averaged
	 * @return the mean value between all the parameters
	 */
	private static float mean(float... vals) {
		float res = 0;
		for (float f : vals) {
			res += f;
		}
		return res / vals.length;
	}

	@Override
	public void close() {
		dist.close();
		color.close();
		left.close();
		right.close();
	}
}
