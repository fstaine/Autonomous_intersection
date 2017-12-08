package fr.utbm.tr54.net;

public class CloseRequest extends RobotRequest {
	
	public static final String KEYWORD = "Close";
	
	public CloseRequest() {
		
	}
	
	@Override
	public String toString() {
		return KEYWORD;
	}
}
