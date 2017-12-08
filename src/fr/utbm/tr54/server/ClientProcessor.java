package fr.utbm.tr54.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.CloseRequest;
import fr.utbm.tr54.net.Request;
import fr.utbm.tr54.net.RobotRequest;
import fr.utbm.tr54.net.ServerRequest;

public class ClientProcessor implements Runnable {
	
	public final Socket sock;
	private BufferedInputStream reader;
	private PrintWriter writer;
	private Server server;
	private Scanner scanner;
	private IntersectionManager manager = IntersectionManager.getInstance();
	private BlockingQueue<ServerRequest> outputPendingRequest = new LinkedBlockingQueue<>();
	
	public ClientProcessor(Socket sock, Server server) {
		this.sock = sock;
		this.server = server;
		try {
			reader = new BufferedInputStream(sock.getInputStream());
			writer = new PrintWriter(sock.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    scanner = new Scanner(reader, StandardCharsets.UTF_8.name());
	    scanner.useDelimiter(Request.DELIMITER);
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
							sock.close();
						} else {
							manager.addReceivedRequests(request);
						}
					}
				}
				if (hasOutcommingPendingRequest()) {
					ServerRequest request = outputPendingRequest.poll();
					if (request != null) {
						writer.write(request.toString());
						writer.flush();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	private int randRange(int min, int max) {
		Random r = new Random(); 
		int x = min + r.nextInt(max - min);
		return x;
	}
	
	private boolean hasIncommingRequest() {
		return scanner.hasNext();
	}
	
	private boolean hasOutcommingPendingRequest() {
		return !outputPendingRequest.isEmpty();
	}
	
	private RobotRequest getRequest() throws IOException {
		if (scanner.hasNext()) {
			String strRequest = scanner.next();
			return RobotRequest.parseRequest(strRequest, sock.getInetAddress());
		}
		return null;
	}
	
	public void sendRequest(ServerRequest request) throws InterruptedException {
		outputPendingRequest.put(request);
	}
}
