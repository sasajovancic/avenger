package org.avenger.sts.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SingleThreadedServer implements Runnable {

	protected int serverPort = 8080;
	protected ServerSocketChannel serverSocketChannel = null;
	protected Selector selector = null;
	protected boolean isStopped = false;

	public SingleThreadedServer(int port) {
		this.serverPort = port;
	}

	public void run() {

		openServerSocket();
		SocketChannel socketChannel = null;

		while (!isStopped()) {
			System.out.println("is running");
			try {
				int readyChannels = selector.select();
				if (readyChannels == 0) {
					System.out.println("none");
					continue;
				}

				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {
					System.out.println("while");

					SelectionKey lkey = keyIterator.next();

					if (lkey.isAcceptable()) {
						// a connection was accepted by a ServerSocketChannel.
						System.out.println("accept");
						try {
							socketChannel = serverSocketChannel.accept();
							if (socketChannel != null) {
								socketChannel.configureBlocking(false);
								socketChannel.socket().setTcpNoDelay(true);
								socketChannel.register(selector,
										SelectionKey.OP_READ);
							}

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else if (lkey.isConnectable()) {
						// a connection was established with a remote server.
						System.out.println("connected");
					} else if (lkey.isReadable()) {
						// a channel is ready for reading
						System.out.println("read");
						socketChannel.register(selector, SelectionKey.OP_WRITE);
					} else if (lkey.isWritable()) {
						System.out.println("write");
						try {
							processClientRequest(socketChannel);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					keyIterator.remove();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		System.out.println("Server Stopped!");
	}

	private void processClientRequest(SocketChannel socketChannel)
			throws IOException {
		if (socketChannel == null)
			return;

		long time = System.currentTimeMillis();

		// TODO should direct buffer be used if we need large capacity?,
		// allocate vs allocateDirect
		// when allocateDirect is used socketchannel throws exception
		// TODO create one buffer if possible
		ByteBuffer inbuf = ByteBuffer.allocate(48);
		int bytesRead = socketChannel.read(inbuf);

		//System.out.println(bytesRead);
		//System.out.println("new String(inbuf.array()) 1: "
			//	+ new String(inbuf.array()));

		// char[] cctest = inbuf.array();

		String read = "";
		while (bytesRead > 0) {
			read = read + new String(((ByteBuffer) inbuf.flip()).array());
			bytesRead = socketChannel.read(inbuf);
			// System.out.println(bytesRead);
			// System.out.println("new String(inbuf.array()) 0: " + new
			// String(inbuf.array()));
			// System.out.println("READ: " + read);
			inbuf.compact();
		}
		System.out.println("READ: " + read);

		// use charBuffer or Trove char listS
		/*
		 * CharBuffer read = CharBuffer.allocate(1024); while (bytesRead > 0) {
		 * char[] rr = ((ByteBuffer)inbuf.flip()).asCharBuffer().array();
		 * read.put(rr); bytesRead = socketChannel.read(inbuf); inbuf.compact();
		 * } System.out.println(read.array());
		 */

		// TODO parse char[] not string
		HttpRequestParser parser = new HttpRequestParser();
		try {
			parser.parseRequest(read);
			System.out.println("RequestLine: " + parser.getRequestLine());
			System.out.println("Host: " + parser.getHeaderParam("Host"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String newData = "HTTP/1.1 200 OK\n\n<html><body>"
				+ "Singlethreaded Server: " + time + "</body></html>";

		ByteBuffer outbuf = ByteBuffer.allocate(newData.getBytes().length);
		outbuf.clear();
		outbuf.put(newData.getBytes());

		outbuf.flip();

		while (outbuf.hasRemaining()) {
			socketChannel.write(outbuf);
		}

		socketChannel.close();

		System.out.println("Request processed: " + time);
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop() {
		this.isStopped = true;
		try {
			this.serverSocketChannel.close();
		} catch (IOException e) {
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.socket().bind(
					new InetSocketAddress(this.serverPort));
			this.serverSocketChannel.configureBlocking(false);

			selector = Selector.open();
			SelectionKey key = this.serverSocketChannel.register(selector,
					SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port: " + this.serverPort,
					e);
		}
		System.out.println("Server started ! on port: " + this.serverPort);
	}
}