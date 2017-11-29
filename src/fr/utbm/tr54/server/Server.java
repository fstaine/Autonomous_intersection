package fr.utbm.tr54.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server extends Thread {
	private ServerSocket server;
	private volatile boolean isRunning = false;
	private InetAddress passing = null;
	
	public Server(int port) {
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
	
	public static void main(String[] args) {
		Server s = new Server(8888);
		s.run();
	}

	public boolean isDangerZoneOccuped(InetAddress inetAddress) {
		if(passing == null){
			passing = inetAddress;
			return false;
		}
		
			return true;
		
	}

	public void setZoneFree() {
		System.out.println("LA ZONE EST LIBRE");
		passing = null;
		
	}
}
