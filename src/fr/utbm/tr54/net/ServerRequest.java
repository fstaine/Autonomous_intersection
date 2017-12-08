package fr.utbm.tr54.net;

public abstract class ServerRequest implements Request {
	public static ServerRequest parseRequest(String str) {
		if (str.startsWith(GoRequest.KEYWORD)) {
			return new GoRequest();
		}
		return null;
	}
}
