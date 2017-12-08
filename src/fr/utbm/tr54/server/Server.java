package fr.utbm.tr54.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Server thread, accept incoming connection on the selected port
 * @author TSB Team
 *
 */
public class Server extends Thread {
	private static Server instance;
	
	public static Server getInstance() {
		return instance;
	}
	
	private ServerSocket server;
	private volatile boolean isRunning = false;
	private Map<InetAddress, ClientProcessor> clients = new HashMap<>();
	
	public Server(int port) {
		instance = this;
		try {
			server = new ServerSocket(port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		isRunning = true;
		System.out.println("Server running... wait for client connection");
		while (isRunning == true) {
			try {
				// On attend une connexion d'un client
				Socket client = server.accept();

				// Une fois reçue, on la traite dans un thread séparé
				ClientProcessor proc = new ClientProcessor(client, this);
				clients.put(client.getInetAddress(), proc);
				Thread serverProcessorThread = new Thread(proc);
				serverProcessorThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
			server = null;
		}
	}
	
	/**
	 * Get client associated to a InetAddress. 
	 * @param clientAddress address of the client to get
	 * @return null if no client is associated to the address, the corresponding client otherwise
	 */
	public ClientProcessor getClient(InetAddress clientAddress) {
		return clients.get(clientAddress);
	}
	
	public static void main(String[] args) {
		Server s = new Server(8888);
		s.run();
		IntersectionManager manager = IntersectionManager.getInstance();
	}
}
