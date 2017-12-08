package fr.utbm.tr54.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import fr.utbm.tr54.net.ServerRequest;

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
	
	public boolean sendRequest(InetAddress client, ServerRequest request) throws InterruptedException {
		ClientProcessor proc = clients.get(client);
		if (proc == null) {
			return false;
		} else {
			proc.sendRequest(request);
			return true;
		}
	}
	
	public static void main(String[] args) {
		Server s = new Server(8888);
		s.run();
		IntersectionManager manager = IntersectionManager.getInstance();
	}
}
