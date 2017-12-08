package fr.utbm.tr54.net;

/**
 * Requests sent by the server to communicate with the robots
 * @author TSB Team
 * @see {@link RobotRequest}, {@link Request}
 */
public abstract class ServerRequest implements Request {
	public static ServerRequest parseRequest(String str) {
		if (str.startsWith(GoRequest.KEYWORD)) {
			return new GoRequest();
		}
		return null;
	}
}
