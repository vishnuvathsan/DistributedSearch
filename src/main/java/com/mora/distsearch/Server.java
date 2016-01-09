package com.mora.distsearch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.log4j.Logger;

import com.mora.distsearch.model.Request;
import com.mora.distsearch.util.Constant;

/**
 * Stand alone server to send and receive packets.
 * 
 */
public abstract class Server implements AutoCloseable {
	/**
	 * Logger to log the events.
	 */
	private static final Logger LOGGER = Logger.getLogger(Server.class);

	/**
	 * Socket to receive the requests.
	 */
	private DatagramSocket socket;

	public int start() throws SocketException {
		return start(-1);
	}

	public int start(int port) throws SocketException {
		if (socket != null) {
			// Server is already running
			throw new RuntimeException("Server is already running.");
		}
		if (port <= 0) {
			socket = new DatagramSocket();
		} else {
			socket = new DatagramSocket(port);
		}

		int localPort = socket.getLocalPort();
		LOGGER.info("Server is started at " + localPort);
		startReceiving();

		return localPort;
	}

	/**
	 * Close the server.
	 */
	@Override
	public void close() {
		if (socket != null) {
			if (!socket.isClosed()) {
				socket.close();
				socket = null;
				LOGGER.info("Server is stopped");
			}
		}
	}

	public void send(String messsage, String ip, int port) {
		LOGGER.debug("Sending " + messsage + " to " + ip + ":" + port);
		try {
			DatagramPacket packet = new DatagramPacket(messsage.getBytes(), messsage.getBytes().length,
					InetAddress.getByName(ip), port);
			socket.send(packet);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void startReceiving() {
		new Thread() {
			public void run() {
				while (socket != null && !socket.isClosed()) {
					byte[] buffer = new byte[Constant.BUFFER_SIZE];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					try {
						socket.receive(packet);

						byte[] data = packet.getData();
						String message = new String(data, 0, packet.getLength());

						Request response = new Request(packet.getAddress().getHostAddress(), packet.getPort(), message);
						onRequest(response);
					} catch (IOException e) {
						LOGGER.error("Error in receiving packet.", e);
					}
				}
			}
		}.start();
	}

	public abstract void onRequest(Request request);

}
