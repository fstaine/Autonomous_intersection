package fr.utbm.tr54.net;

import java.net.InetAddress;

public class PositionningRequest extends RobotRequest {
	public static final String KEYWORD = "Here";
	
	private int position;

	public PositionningRequest(int position) {
		this.position = position;
	}

	public PositionningRequest(int position, InetAddress emitter) {
		super(emitter);
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
