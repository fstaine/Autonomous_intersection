package fr.utbm.tr54.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import fr.utbm.tr54.ev3.RobotController;
import fr.utbm.tr54.net.FreeRequest;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.PositionningRequest;
import fr.utbm.tr54.net.ServerRequest;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.robotics.Color;

public class RobotManager implements AutoCloseable {
	
	/**
	 * LED colors constants
	 */
	public static final int STATIC_RED = 2;
	public static final int NORMAL_BLINK_YELLOW = 6;
	public static final int FAST_BLINK_GREEN = 7;
	public static final int TURN_OFF = 0;
	
	/**
	 * Robot instance, allow to access actuators & sensors
	 */
	RobotController ev3 = RobotController.getInstance();
	
	/**
	 * State variables of the robot
	 */
	private State state = State.Stop;
	private ForwardState forwardState;
	private ServerState serverState = ServerState.NoInfo;
	
	/**
	 * Position of the robot: 1 -> First orange color, 2 -> Second Orange color
	 */
	private int position = 1;
	
	/**
	 * List of pending requests received from the server
	 */
	BlockingQueue<ServerRequest> requests = new LinkedBlockingQueue<>();
	
	/**
	 * Min dist before stopping (cm)
	 */
	private final float minDist = 15f;
	
	/**
	 * Tacho count at the exit of the conflict zone
	 */
	private final int tachoEndCalculator = 2300;
	
	/**
	 * Tacho count at the exit of the storage zone
	 */
	private final int tachosWaitingCalculator = 1000;
	
	public RobotManager() {
		// Default speed
		float speed = 300;
		ev3.right.setSpeed(speed);
		ev3.left.setSpeed(speed);
		
		// Default acceleration
		int acceleration = 1000;
		ev3.right.setAcceleration(acceleration);
		ev3.left.setAcceleration(acceleration);
	}

	public void run() {
		while (true) {
			if (serverState == ServerState.WaitingZone) {
				if (ev3.getMeanTachoCount() <= tachosWaitingCalculator) {
					ServerRequest request = requests.poll();
					if (request != null && request instanceof GoRequest) {
						onGoReceived((GoRequest) request);
					}
				} else {
					// Stop after the waiting zone
					ev3.stop();
					setLedColor(STATIC_RED);
					ServerRequest request = null;
					try {
						request = requests.poll(100, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (request != null && request instanceof GoRequest) {
						onGoReceived((GoRequest) request);
					}
				}
			}
			if (serverState == ServerState.Go) {
				if (ev3.getMeanTachoCount() >= tachoEndCalculator) {
					Sound.beepSequence();
					serverState = ServerState.NoInfo;
					setLedColor(TURN_OFF);
					Client.getInsance().send(new FreeRequest());
				}
			}
			getStateFromDistance();
			if (state == State.Forward) { // If min distance is respected
				
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
			} else { // Too close ot another object
				ev3.stop();
			}
		}
	}
	
	/**
	 * Update the position after 
	 */
	private void updatePosition() {
		if (position == 1) {
			position = 2;
		} else {
			position = 1;
		}
	}

	/**
	 * Set the initial position of the robot
	 * @param pos initial position of the robot
	 */
	public void setPosition(int pos) {
		this.position = pos;
	}

	/**
	 * Update state according to the distance with it's predecessor
	 */
	private void getStateFromDistance() {
		if (ev3.distance() < minDist ) {
			state = State.Stop;
		} else {
			state = State.Forward;
		}
	}

	/**
	 * Update the state of the robot according to the line color
	 */
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
			forwardState = ForwardState.TurnLeft;
			if (serverState == ServerState.NoInfo) {
				enterWaitingZone();
			}
		default:
			break;
		}
	}

	/**
	 * Set robot state in WaitingZone 
	 */
	private void enterWaitingZone() {
		serverState = ServerState.WaitingZone;
		setLedColor(NORMAL_BLINK_YELLOW);
		ev3.resetTachoCount();
		updatePosition();
		
		// Send positioning request
		Client.getInsance().send(new PositionningRequest(position));
	}
	
	/**
	 * Add a message to the incoming message queue
	 * @param message the incoming message
	 */
	public void addMessage(ServerRequest message) {
		try {
			requests.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * When a Go request is received <br/>
	 * Update robot state accordingly
	 * @param request
	 */
	public void onGoReceived(GoRequest request) {
		System.out.println("Received:" + request);
		serverState = ServerState.Go;
		Button.LEDPattern(1);
		state = State.Forward;
		Sound.beepSequenceUp();
		setLedColor(FAST_BLINK_GREEN);
	}
	
	/**
	 * Set blinking mode: </br>
	 * 0: turn off button lights </br>
	 * 1/2/3: static light green/red/yellow </br>
	 * 4/5/6: normal blinking light green/red/yellow </br>
	 * 7/8/9: fast blinking light green/red/yellow </br>
	 * @param i Blinking mode
	 */
	public void setLedColor(int i) {
		Button.LEDPattern(i);
	}

	@Override
	public void close() {
		setLedColor(TURN_OFF);
		ev3.close();
	}
	
	private enum State {
		/**
		 * Robot must stop
		 */
		
		Stop, 
		/**
		 * Robot can move
		 */
		Forward
	}
	
	private enum ForwardState {
		/**
		 * Robot must turn left to regain the track
		 */
		
		TurnLeft, 
		/**
		 * Robot must turn right to regain the track
		 */
		
		TurnRight, 
		/**
		 * Robot is on the track
		 */
		Forward, 
	}
	
	private enum ServerState {
		/**
		 * No info received from the server
		 */
		NoInfo, 
		
		/**
		 * Can go inside the conflict zone
		 */
		Go, 
		
		/**
		 * Robot must wait before the conflict zone
		 */
		Stop, 
		
		/**
		 * Robot has enter the waiting zone
		 */
		WaitingZone
	}
}
