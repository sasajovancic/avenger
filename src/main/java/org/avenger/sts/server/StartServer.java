package org.avenger.sts.server;

public class StartServer {

	public static void main(String[] args) {
		SingleThreadedServer server = new SingleThreadedServer(9000);
		new Thread(server).start();

		try {
		    Thread.sleep(4000 * 1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();  
		}
		
		System.out.println("Stopping Server");
		server.stop();
	}

}
