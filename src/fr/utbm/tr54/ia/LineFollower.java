package fr.utbm.tr54.ia;

import java.util.Timer;
import java.util.TimerTask;

import fr.utbm.tr54.client.Client;
import fr.utbm.tr54.ev3.RobotController;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.ServerRequest;
import lejos.robotics.Color;
import lejos.utility.Delay;

public class LineFollower implements AutoCloseable {
	
	RobotController ev3 = RobotController.getInstance();
	
	private Timer tasksTimer = new Timer();
	
	State currentState = State.STOP;
	ServerOrder currentServerOrder = ServerOrder.NOINFO;
	private boolean isOnThePlace = false;

	final float minDistToObstacle = 15f;
	float distToObstacle = minDistToObstacle;
	
	public LineFollower() {
		float speed = 300;//ev3.left.getMaxSpeed() / 3f;
		setSpeed(speed);
		
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
			ev3.left.stop(true);
			ev3.right.stop();
			System.out.println(distToObstacle);
		}

		if(currentServerOrder == ServerOrder.PASSING){
			System.out.println("passing");
			ev3.left.forward();
			ev3.right.forward();
		}else if(currentServerOrder == ServerOrder.NOTPASSING){
			ev3.left.stop(true);
			ev3.right.stop();
		}
		else if (currentServerOrder == ServerOrder.NOINFO) {
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
		System.out.println(ev3.getColor());
		
		switch (ev3.getColor()) {
		case Color.WHITE:
			setState(State.TURN_RIGHT);
			break;
		case Color.ORANGE:
			System.out.println("Orange !!!!!!!");
		case Color.RED:
		case Color.BROWN:
			if(currentServerOrder == ServerOrder.NOINFO && !isOnThePlace)
				setWaitForResponseState();
			// TODO: envoi status...
			//setState(State.FORWARD);
//			setState(State.STOP);
//			Delay.msDelay(50);
			break;
		case Color.BLACK:
			setState(State.TURN_LEFT);
			break;
		case Color.BLUE:
			setState(State.FORWARD);
			break;
		default:
			setState(State.FORWARD);
			break;
		}
	}
	
	private void setWaitForResponseState() {
		Client.getInsance().send("Je suis là =);");
		isOnThePlace = true;
	}
	
	private void setState(State state) {
		this.currentState = state;
		
	}

	public enum State {
		FORWARD, STOP, TURN_RIGHT, TURN_LEFT, PASSING;
	}
	
	public enum ServerOrder {
		PASSING, NOTPASSING, NOINFO;
	}

	public void getResponse(ServerRequest request) {
		System.out.println("COUCOUY " + request);
		if(request instanceof GoRequest) {
			currentServerOrder = ServerOrder.PASSING;
			TimerTask freeRobot = new TimerTask() {
				
				@Override
				public void run() {
					currentServerOrder = ServerOrder.NOINFO;
					
				}
			};
			tasksTimer.schedule(freeRobot, 500);
			
			TimerTask freeZone = new TimerTask() {
				
				@Override
				public void run() {
					Client.getInsance().sendFreeZone();
					isOnThePlace = false;
				}
			};
			
			tasksTimer.schedule(freeZone, 10000);
			
			System.out.println("J'ai droit");
		}else{
			currentServerOrder = ServerOrder.NOTPASSING;
		}
		
	}
}
