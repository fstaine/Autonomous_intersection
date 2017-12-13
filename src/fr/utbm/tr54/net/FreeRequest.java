package fr.utbm.tr54.net;

import java.net.InetAddress;

/**
 * Tell the server the intersection isn't use by the robot anymore
 * @author TSB Team
 * @see RobotRequest
 */
public class FreeRequest extends RobotRequest {
	
	public static final String KEYWORD = "Free";
	
	public FreeRequest(InetAddress sender) {
		super(sender);
	}
	
	public FreeRequest() {
		super();
	}
	
	@Override
	public String toString() {
		return KEYWORD;
	}
}
