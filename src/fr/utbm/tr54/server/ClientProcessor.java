package fr.utbm.tr54.server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.CloseRequest;
import fr.utbm.tr54.net.RobotRequest;
import fr.utbm.tr54.net.ServerRequest;

/**
 * Server side handler for a network connection between the server and a robot. 
 * Receives / send messages from / to the robots. 
 * @author TSB Team
 */
public class ClientProcessor implements Runnable, Closeable {
	
	public final Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	private Server server;
	private IntersectionManager manager = IntersectionManager.getInstance();
	private BlockingQueue<ServerRequest> outputPendingRequest = new LinkedBlockingQueue<>();
	
	public ClientProcessor(Socket sock, Server server) {
		this.sock = sock;
		this.server = server;
		try {
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		System.out.println("Une connexion d'un client a ete recue: " + sock.getInetAddress());
		try {
			while (!sock.isClosed()) {
				//TODO test if present
				if (hasIncommingRequest()) {
					RobotRequest request = getRequest();
					if (request != null) {
						if (request instanceof CloseRequest) {
							this.close();
						} else {
							manager.receive(request);
						}
					}
				}
				if (hasOutcommingPendingRequest()) {
					ServerRequest request = outputPendingRequest.poll();
					if (request != null) {
						writer.println(request.toString());
					}
				}
				Thread.sleep(50);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	};
	
	/**
	 * Generate a random number between two bounds
	 * @param min the min bound (include)
	 * @param max the max bound (exclusive)
	 * @return
	 */
	private int randRange(int min, int max) {
		Random r = new Random(); 
		int x = min + r.nextInt(max - min);
		return x;
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
	 * Check if a request has to be send. 
	 * @return true if a request is ready to be send, false otherwise
	 */
	private boolean hasOutcommingPendingRequest() {
		return !outputPendingRequest.isEmpty();
	}
	
	/**
	 * Read the next request. Block until the request is ready
	 * @return the request sent by a robot
	 * @throws IOException
	 */
	private RobotRequest getRequest() throws IOException {
		String strRequest = reader.readLine();
		return RobotRequest.parseRequest(strRequest, sock.getInetAddress());
	}
	
	/**
	 * Add a request to the sending queue. 
	 * @param request the request to send
	 * @throws InterruptedException if the sending queue is full and the thread is interrupted while waiting 
	 */
	public void sendRequest(ServerRequest request) throws InterruptedException {
		outputPendingRequest.put(request);
	}

	@Override
	public void close() throws IOException {
		if (sock != null)
			sock.close();
		if (reader != null)
			reader.close();
		if (writer != null)
			writer.close();
		System.out.println("Close socket");
	}
}
