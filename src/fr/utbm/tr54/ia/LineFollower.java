package fr.utbm.tr54.ia;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import fr.utbm.tr54.client.Client;
import fr.utbm.tr54.ev3.RobotController;
import fr.utbm.tr54.net.FreeRequest;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.PositionningRequest;
import fr.utbm.tr54.net.ServerRequest;
import lejos.hardware.Sound;
import lejos.robotics.Color;

public class LineFollower implements AutoCloseable {
	
	RobotController ev3 = RobotController.getInstance();
	State state = State.Stop;
	ForwardState forwardState;
	ServerState serverState = ServerState.NoInfo;
	
	BlockingQueue<ServerRequest> requests = new LinkedBlockingQueue<>();
	
	/**
	 * Min dist before stopping (cm)
	 */
	private final float minDist = 10f;
	
	private final int tachoCalculator = 2200;
	
	public LineFollower() {
		float speed = 300;//ev3.left.getMaxSpeed() / 3f;
		ev3.right.setSpeed(speed);
		ev3.left.setSpeed(speed);
		
		int acceleration = 1000;
		ev3.right.setAcceleration(acceleration);
		ev3.left.setAcceleration(acceleration);
	}

	public void run() {
		while (true) {
			
			if(serverState == ServerState.Go || serverState == ServerState.NoInfo) {
				if (serverState == ServerState.Go) {
					if (ev3.left.getTachoCount() > tachoCalculator) {
						Sound.beepSequence();
						serverState = ServerState.NoInfo;
						Client.getInsance().send(new FreeRequest());
					}
				}
				getStateFromDistance();
				if (state == State.Forward) {
					
					getStateFromColor();
					
					if (forwardState == ForwardState.TurnLeft) {
						ev3.right.forward();
						ev3.left.stop(true);
					} else if (forwardState == ForwardState.TurnRight) {
						ev3.right.stop(true);
						ev3.left.forward();
					} else {
						ev3.right.forward();
						ev3.left.forward();
					}
				} else {
					ev3.stop();
				}
			} else {
				ev3.stop();
			}
		}
	}

	private void getStateFromDistance() {
		if (ev3.distance() < minDist ) {
			state = State.Stop;
		} else {
			state = State.Forward;
		}
	}

	private void getStateFromColor() {
		int color = ev3.getColor();
		switch (color) {
		case Color.BLACK:
			forwardState = ForwardState.TurnLeft;
			break;
		case Color.WHITE:
			forwardState = ForwardState.TurnRight;
			break;
		case Color.BLUE:
			forwardState = ForwardState.Forward;
			break;
		case Color.ORANGE:
		case Color.RED:
		//case Color.BROWN:
			if (serverState == ServerState.NoInfo) {
				ev3.stop();
				askServerForForwardState();
			}
		default:
			break;
		}
	}

	private void askServerForForwardState() {
		Client.getInsance().send(new PositionningRequest(1));
		ServerRequest request = null;
		try {
			request = requests.poll(500, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (request != null && request instanceof GoRequest) {
			System.out.println("Received:" + request);
			serverState = ServerState.Go;
			state = State.Forward;
			forwardState = ForwardState.TurnLeft;
			Sound.beepSequenceUp();
			ev3.left.resetTachoCount();
		} else {
			System.out.println("Stopppppp");
			serverState = ServerState.Stop;
			state = State.Stop;
		}
	}
	
	public void addMessage(ServerRequest message) {
		try {
			requests.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		ev3.close();
	}
	
	private enum State {
		Stop, Forward
	}
	
	private enum ForwardState {
		TurnLeft, TurnRight, Forward, 
	}
	
	private enum ServerState {
		NoInfo, Go, Stop
	}
}
