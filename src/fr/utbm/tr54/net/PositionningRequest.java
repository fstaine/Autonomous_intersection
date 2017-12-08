package fr.utbm.tr54.net;

import java.net.InetAddress;

/**
 * Tell the robot's position to the server
 * @author TSB Team
 * @see RobotRequest
 */
public class PositionningRequest extends RobotRequest {
	public static final String KEYWORD = "Here";
	
	private int position;

	public PositionningRequest(int position) {
		this.position = position;
	}

	public PositionningRequest(int position, InetAddress sender) {
		super(sender);
		this.position = position;
	}
	
	public int getPosition() {
		return this.position;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d", KEYWORD, position);
	}
}
