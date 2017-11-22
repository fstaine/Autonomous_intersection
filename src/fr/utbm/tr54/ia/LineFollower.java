package fr.utbm.tr54.ia;

import fr.utbm.tr54.ev3.RobotController;
import lejos.robotics.Color;
import lejos.utility.Delay;

public class LineFollower implements AutoCloseable {
	
	RobotController ev3 = RobotController.getInstance();
	
	public LineFollower() {
		float speed = ev3.left.getMaxSpeed() / 3f;
		ev3.left.setSpeed(speed);
		ev3.right.setSpeed(speed);
		
		int acceleration = 1000;
		ev3.left.setAcceleration(acceleration);
		ev3.right.setAcceleration(acceleration);
	}
	
	/**
	 * Start the robot:
	 * Make the robot following the line indeffinately
	 */
	public void run() {
		while(true) {
			switch (ev3.getColor()) {
			case Color.WHITE:
				ev3.left.forward();
				ev3.right.stop(true);
				break;
			case Color.ORANGE:
				// TODO: envoi status...
				ev3.left.forward();
				ev3.right.forward();
				break;
			case Color.BLACK:
				ev3.left.stop(true);
				ev3.right.forward();
				break;
			case Color.BLUE:
				ev3.left.forward();
				ev3.right.forward();
				break;
			default:
				ev3.left.forward();
				ev3.right.forward();
				System.out.println(ev3.getColor());
				break;
			}
			Delay.msDelay(5);
		}
	}
	
	@Override
	public void close() {
		ev3.close();
	}
}
