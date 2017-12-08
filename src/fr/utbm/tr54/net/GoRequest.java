package fr.utbm.tr54.net;

/**
 * Tell the robot he can move into the intersection freely
 * @author TSB Team
 * @see ServerRequest
 */
public class GoRequest extends ServerRequest {
	
	public static final String KEYWORD = "Go";
	
	public GoRequest() {
		
	}
	
	@Override
	public String toString() {
		return KEYWORD;
	}
}
