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

import fr.utbm.tr54.ia.LineFollower;
import fr.utbm.tr54.net.RobotRequest;
import fr.utbm.tr54.net.ServerRequest;

public class Client implements Closeable, Runnable {

	private static Client instance = null;
	
	public static Client getInsance() {
		return instance;// FIXME
	}
	
	public void send(RobotRequest message){ //String message){
		// Add to the list of messages to send
		outputPendingRequest.add(message);
	}
	
	private Socket connexion;
	private BufferedReader reader;
	private PrintWriter writer;
	private BlockingQueue<RobotRequest> outputPendingRequest = new LinkedBlockingQueue<>();
	
	private volatile boolean isRunning = false;

	private LineFollower robot;

	public Client(String host, int port, LineFollower robot) throws UnknownHostException, IOException {
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
				
				//float newSpeed = Float.parseFloat(request);
				
				//robot.setSpeed(newSpeed);

				//System.out.println("Received: " +  request + ", " + newSpeed);
				
				//TODO : Différencier messages
			}
//			try {
//				//Thread.sleep(20);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
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

	public void sendFreeZone() {
		writer.write("FREE;");
		writer.flush();
	}	
}
