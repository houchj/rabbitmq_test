package com.sap.sme.occ.product;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.log.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class LightHttpServer {

	private static ExecutorService httpThreadPool;
	private static Logger logger;

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		LightHttpServer.logger = logger;
	}

	public static ExecutorService getHttpThreadPool() {
		return httpThreadPool;
	}

	public static void main(String[] args) throws Exception {
		startHttpServer(58080, null);
	}

	public static void startHttpServer(int port, final Logger logger) throws Exception {
		InetSocketAddress addr = new InetSocketAddress(port);
		HttpServer server = HttpServer.create(addr, 0);
		server.createContext("/", new RootHandler());
		server.createContext("/exit", new ExitHandler(server));
		server.createContext("/messages", new MessagesHandler(server));
		server.createContext("/clear", new ClearHandler(server));
		server.setExecutor(Executors.newCachedThreadPool());

		httpThreadPool = Executors.newFixedThreadPool(2);
		server.setExecutor(httpThreadPool);

		server.start();
		LightHttpServer.logger = logger;
		Executor exc = server.getExecutor();
		System.out.println("Server is listening on port " + port);
	}
}

@SuppressWarnings("restriction")
class MessagesHandler implements HttpHandler {

	final HttpServer server;

	public MessagesHandler(HttpServer server) {
		this.server = server;
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, 0);

			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();

			List<String> messages = MQTest.getMessages();
			String ret = "[";
			for (int i = 0; i < messages.size(); i++) {
				if (i > 0) {
					ret = ret + ", " + messages.get(i);
				} else {
					ret = ret + messages.get(i);
				}
			}
			ret = ret + "]";
			responseBody.write(ret.getBytes());

			responseBody.close();
		}
	}
}

@SuppressWarnings("restriction")
class ClearHandler implements HttpHandler {

	final HttpServer server;

	public ClearHandler(HttpServer server) {
		this.server = server;
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, 0);

			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();

			MQTest.getMessages().clear();

			responseBody.write("clear message successfully".getBytes());

			responseBody.close();
		}
	}
}

@SuppressWarnings("restriction")
class ExitHandler implements HttpHandler {

	final HttpServer server;

	public ExitHandler(HttpServer server) {
		this.server = server;
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);

			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();
			Set<String> keySet = requestHeaders.keySet();
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				List values = requestHeaders.get(key);
				String s = key + " = " + values.toString() + "\n";
				responseBody.write(s.getBytes());
			}
			responseBody.close();
			LightHttpServer.getLogger().info("try to exit current thread3!");
			System.out.println("try to exit current thread3!");
			server.stop(0);
			LightHttpServer.getHttpThreadPool().shutdown();
			MQTest.killAMQConnection();
			StandardJMeterEngine.stopEngineNow();
			System.exit(0);
		}
	}
}

@SuppressWarnings("restriction")
class RootHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET")) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/plain");
			exchange.sendResponseHeaders(200, 0);

			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();
			Set<String> keySet = requestHeaders.keySet();
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				List values = requestHeaders.get(key);
				String s = key + " = " + values.toString() + "\n";
				responseBody.write(s.getBytes());
			}
			responseBody.close();
		}
	}
}