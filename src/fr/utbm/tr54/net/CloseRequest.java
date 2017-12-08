package fr.utbm.tr54.net;

import java.net.InetAddress;

/**
 * Tell the server the robot want to disconnects himself
 * @author TSB Team
 * @see RobotRequest
 */
public class CloseRequest extends RobotRequest {
	
	public static final String KEYWORD = "Close";
	
	public CloseRequest(InetAddress sender) {
		super(sender);
	}
	
	@Override
	public String toString() {
		return KEYWORD;
	}
}
