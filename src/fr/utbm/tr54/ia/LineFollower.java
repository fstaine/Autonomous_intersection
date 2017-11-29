package fr.utbm.tr54.ia;

import fr.utbm.tr54.ev3.RobotController;
import lejos.robotics.Color;
import lejos.utility.Delay;

public class LineFollower implements AutoCloseable {
	
	RobotController ev3 = RobotController.getInstance();
	
	State currentState = State.STOP;

	final float minDistToObstacle = 15f;
	float distToObstacle = minDistToObstacle;
	
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
	 * Make the robot following the line indefinitely
	 */
	public void run() {
		while(true) {
			updateStateFromColor();
			updateDist();
			//System.out.println(distToObstacle);
			move();
			Delay.msDelay(5);
		}
	}
	
	private void updateDist() {
		distToObstacle = ev3.distance();
	}

	private void move() {
		if (distToObstacle < minDistToObstacle) {
			setState(State.STOP);
			System.out.println(distToObstacle);
		}
		switch (currentState) {
		case FORWARD:
			ev3.left.forward();
			ev3.right.forward();
			break;
		case TURN_LEFT:
			ev3.left.stop(true);
			ev3.right.forward();
			break;
		case TURN_RIGHT:
			ev3.left.forward();
			ev3.right.stop(true);
			break;
		case STOP:
			ev3.left.stop(true);
			ev3.right.stop();
			break;
		}
	}

	@Override
	public void close() {
		ev3.close();
	}

	public void setSpeed(float newSpeed) {
		ev3.left.setSpeed(newSpeed);
		ev3.right.setSpeed(newSpeed);
	}
	
	private void updateStateFromColor() {
		switch (ev3.getColor()) {
		case Color.WHITE:
			setState(State.TURN_RIGHT);
			break;
		case Color.ORANGE:
			// TODO: envoi status...
			setState(State.FORWARD);
			break;
		case Color.BLACK:
			setState(State.TURN_LEFT);
			break;
		case Color.BLUE:
			setState(State.FORWARD);
			break;
		default:
			setState(State.FORWARD);
			//System.out.println(ev3.getColor());
			break;
		}
	}
	
	private void setState(State state) {
		this.currentState = state;
		
	}

	public enum State {
		FORWARD, STOP, TURN_RIGHT, TURN_LEFT;
	}
}
