package fr.utbm.tr54.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class ClientProcessor implements Runnable {
	
	public final Socket sock;
	private BufferedInputStream reader = null; // buffer de lecture
	private PrintWriter writer = null;
	private Server server;

	
	public ClientProcessor(Socket sock, Server server) {
		this.sock = sock;
		this.server = server;
		try {
			reader = new BufferedInputStream(sock.getInputStream());
			writer = new PrintWriter(sock.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		System.out.println("Une connexion d'un client a ete recue: " + sock.getInetAddress());
		while (!sock.isClosed()) {

			// send speed to client
//			int newSpeed = randRange(200, 300);
//			writer.write(newSpeed + ";");
//			writer.flush();
//			System.out.println("Sent : " + newSpeed + " to " + sock.getInetAddress());
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			try {
				// On attend la demande du client
				String request = getRequest();
				System.out.println(request + "-" + sock.getInetAddress());
				Boolean t = server.isDangerZoneOccuped(sock.getInetAddress());
				writer.write(t?"STOP;":"GO;");
				writer.flush();
				System.out.println("Sent : " + (t?"STOP;":"GO;"));
				
				// set speed from the leader
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	};
	
	private void setRequest(float speed) throws IOException {
		
	}
	
	private int randRange(int min, int max) {
		  Random r = new Random(); 
		  int x = min + r.nextInt(max - min);
		  return x;
		}
	
	private String getRequest() throws IOException {
		String response = "";
		byte[] b = new byte[1];
		while (!response.contains(";")) {
			int count = reader.read(b);
			if (count <= 0 ) {
				return "0;";
			}
			response += new String(b, 0, count);
		}
		return response.replace(";", "");
	}
}
