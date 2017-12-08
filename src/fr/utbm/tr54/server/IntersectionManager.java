package fr.utbm.tr54.server;

import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.utbm.tr54.net.FreeRequest;
import fr.utbm.tr54.net.GoRequest;
import fr.utbm.tr54.net.PositionningRequest;
import fr.utbm.tr54.net.RobotRequest;

/**
 * Singleton Thread that controls the intersection. 
 * @author TSB team
 */
public class IntersectionManager extends Thread implements RequestHandler<RobotRequest> {
	private static IntersectionManager instance;
	
	public static IntersectionManager getInstance() {
		if (instance == null) {
			instance = new IntersectionManager();
		}
		return instance;
	}
	
	private Server server;
	private volatile boolean isRunning = false;
	private BlockingQueue<RobotRequest> incommingRequests = new LinkedBlockingQueue<>();
	private InetAddress prevRobot;
	
	private IntersectionManager() {
		server = Server.getInstance();
		if (server == null) {
			throw new NullPointerException();
		}
		this.start();
	}
	
	@Override
	public void run() {
		isRunning = true;
		try {
			while(isRunning) {
				RobotRequest request = incommingRequests.take();
				System.out.println("Received (" + request.getSender() + "): " + request);
				if (request instanceof PositionningRequest) {
					boolean sendGo = false;
					if (prevRobot == null/* || prevRobot == request.getEmitter()*/) {
						sendGo = true;
					}
					prevRobot = request.getSender();
					if (sendGo) {
						ClientProcessor client = server.getClient(prevRobot);
						client.sendRequest(new GoRequest());
					}
				} else if (request instanceof FreeRequest) {
					prevRobot = null;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receive(RobotRequest request) throws InterruptedException {
		incommingRequests.put(request);
	}
}
