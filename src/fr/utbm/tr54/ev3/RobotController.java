package fr.utbm.tr54.ev3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * Interface to interact with the Robot sensors and motors
 * @author TSB Team
 */
public class RobotController implements AutoCloseable {
	
	private static RobotController instance = null;
	
	public EV3LargeRegulatedMotor left = new EV3LargeRegulatedMotor(MotorPort.B);
	public EV3LargeRegulatedMotor right = new EV3LargeRegulatedMotor(MotorPort.C);
	
	public EV3UltrasonicSensor dist = new EV3UltrasonicSensor(SensorPort.S2);
	public EV3ColorSensor color = new EV3ColorSensor(SensorPort.S3);
	
	private SampleProvider distanceSampleProvider;
	
	private RobotController() {
		dist.enable();
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
	 * Stop the robot
	 */
	public void stop() {
		left.stop(true);
		right.stop();
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
			float val[] = new float[1];
			distanceSampleProvider.fetchSample(val, 0);
			sample[i] = val[0];
		}
		return mean(sample);
	}
	
	/**
	 * Get the current color under the sensor
	 * @return an int representing the detected color
	 */
	public int getColor() {
		int c = color.getColorID();
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
	
	/**
	 * Reset the TachoCounts of the motors
	 */
	public void resetTachoCount() {
		left.resetTachoCount();
		right.resetTachoCount();
	}
	
	/**
	 * Get the current tacho count.
	 * It's value if the mean between the left and the right engine
	 * @return the current tacho count
	 */
	public int getMeanTachoCount() {
		return (left.getTachoCount() + right.getTachoCount()) / 2;
	}

	@Override
	public void close() {
		dist.close();
		color.close();
		left.close();
		right.close();
	}
}
