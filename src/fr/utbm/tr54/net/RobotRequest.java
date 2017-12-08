package fr.utbm.tr54.net;

import java.net.InetAddress;

/**
 * Requests sent by the robots to communicate with the server
 * @author TSB Team
 * @see {@link RobotRequest} {@link Request}
 */
public abstract class RobotRequest implements Request {
	
	/**
	 * Parse a String request into the corresponding RobotRequest. 
	 * @param str the string of the input request
	 * @param sender the sender's address of the request
	 * @return the corresponding RobotRequest
	 */
	public static RobotRequest parseRequest(String str, InetAddress sender) {
		if (str.startsWith(PositionningRequest.KEYWORD)) {
			String[] strs = str.split(":");
			int position = Integer.valueOf(strs[1]);
			return new PositionningRequest(position, sender);
		} else if (str.startsWith(FreeRequest.KEYWORD)) {
			return new FreeRequest(sender);
		} else if (str.startsWith(CloseRequest.KEYWORD)) {
			return new CloseRequest(sender);
		}
		return null;
	}
	
	private InetAddress sender;
	
	public RobotRequest() {
		
	}
	
	public RobotRequest(InetAddress sender) {
		this.sender = sender;
	}
	
	/**
	 * Get the emiter {@link InetAddress}
	 * @return the emiter {@link InetAddress}
	 */
	public InetAddress getSender() {
		return this.sender;
	}
}
