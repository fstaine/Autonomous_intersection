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
							manager.addReceivedRequests(request);
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
	
	private int randRange(int min, int max) {
		Random r = new Random(); 
		int x = min + r.nextInt(max - min);
		return x;
	}
	
	private boolean hasIncommingRequest() throws IOException {
		return reader.ready();
	}
	
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
