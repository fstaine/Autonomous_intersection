package fr.utbm.tr54.server;

import fr.utbm.tr54.net.Request;

/**
 * The RequestHandler interface should be implemented by any class that should treat multiple {@link Request} in another Thread.  
 * @author TSB Team
 *
 * @param <T> the type of request is handles
 */
public interface RequestHandler<T extends Request> {
	/**
	 * Add a new message to handle.
	 * @param request the incoming request
	 * @throws InterruptedException if interrupted while waiting for the request to be stored
	 */
	public void receive(T request) throws InterruptedException;
}
