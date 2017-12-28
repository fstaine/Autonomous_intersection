package fr.utbm.tr54.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.RobotRequest;
import fr.utbm.tr54.net.ServerRequest;

/**
 * Robot side handler for a network connection between the server and a robot. 
 * Receives / send messages from / to the server. 
 * @author TSB Team
 */
public class Client implements Closeable, Runnable {

	private static Client instance = null;
	
	/**
	 * Get the client instance.
	 * The instance have already been created before
	 * @return
	 */
	public static Client getInsance() {
		return instance;
	}
	
	/**
	 * TCP Socket connection
	 */
	private Socket connexion;
	private BufferedReader reader;
	private PrintWriter writer;
	
	/**
	 * Queue of requests to send
	 */
	private BlockingQueue<RobotRequest> outputPendingRequest = new LinkedBlockingQueue<>();
	
	private volatile boolean isRunning = false;

	/**
	 * The attached robot
	 */
	private RobotManager robot;

	public Client(String host, int port, RobotManager robot) throws UnknownHostException, IOException {
		instance = this;
		this.robot = robot;
		connexion = new Socket(host, port);
		writer = new PrintWriter(connexion.getOutputStream(), true);
		reader = new BufferedReader(new InputStreamReader(connexion.getInputStream()));
	}
	
	public void run() {
		isRunning = true;
		try {
			while (isRunning) {
				//==================================================
				if (hasIncommingRequest()) {
					ServerRequest request = getRequest();
					robot.addMessage(request);
				}
				
				// Send request if any present in the queue
				RobotRequest request = outputPendingRequest.poll();
				if (request != null) {
					System.out.println("send : msg="+request);
					writer.println(request.toString());
					writer.flush();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			if (connexion != null)
				connexion.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a message to the messages to send
	 * @param message the message to send
	 */
	public void send(RobotRequest message){
		outputPendingRequest.add(message);
	}
	
	/**
	 * Check if a request is incoming.
	 * A true value do not assure the {@link getRequest} will not have to wait for the request to be fully read. 
	 * @return false if nothing ha been written to the input stream, true otherwise. 
	 * @throws IOException
	 */
	private boolean hasIncommingRequest() throws IOException {
		return reader.ready();
	}
	
	/**
	 * Read the next request. Block until the request is ready
	 * @return the request sent by a robot
	 * @throws IOException
	 */
	private ServerRequest getRequest() throws IOException {
		String strRequest = reader.readLine();
		return ServerRequest.parseRequest(strRequest);
	}

	@Override
	public void close() throws IOException {
		isRunning = false;
		if (connexion != null)
			connexion.close();
		if (reader != null)
			reader.close();
		if (writer != null)
			writer.close();
	}
}
