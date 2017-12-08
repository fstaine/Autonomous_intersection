package fr.utbm.tr54.net;

public class GoRequest extends ServerRequest {
	
	public static final String KEYWORD = "Go";
	
	public GoRequest() {
		
	}
	
	@Override
	public String toString() {
		return KEYWORD;
	}
}
