package fr.utbm.tr54.net;

import java.net.InetAddress;

public abstract class RobotRequest implements Request {
	public static RobotRequest parseRequest(String str, InetAddress emitter) {
		if (str.startsWith(PositionningRequest.KEYWORD)) {
			String[] strs = str.split(":");
			int position = Integer.valueOf(strs[1]);
			return new PositionningRequest(position, emitter);
		} else if (str.startsWith(FreeRequest.KEYWORD)) {
			return new FreeRequest();
		} else if (str.startsWith(CloseRequest.KEYWORD)) {
			return new CloseRequest();
		}
		return null;
	}
	
	private InetAddress emitter;
	
	public RobotRequest() {
		
	}
	
	public RobotRequest(InetAddress emitter) {
		this.emitter = emitter;
	}
	
	public InetAddress getEmitter() {
		return this.emitter;
	}
}
