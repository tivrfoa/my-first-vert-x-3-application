package io.vertx.blog.first;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpClientResponse;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {
	
	private Vertx vertx;
	private int port;
	
	@Before
	public void setUp(TestContext context) throws Exception {
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		
		DeploymentOptions options = new DeploymentOptions()
			.setConfig(new JsonObject().put("http_port", port));
			
		vertx = Vertx.vertx();
		vertx.deployVerticle(MyFirstVerticle.class.getName(),
			options,
			context.asyncAssertSuccess());
	}
	
	@After
	public void tearDown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}
	
	@Test
	public void testMyApplication(TestContext context) {
		final Async async = context.async();
		
		vertx.createHttpClient().getNow(port, "localhost", "/",
			response -> {
				response.handler(body -> {
					context.assertTrue(body.toString().contains("Hello"));
					async.complete();
				});
			}
		);
	}
	
	@Test
	public void checkThatTheIndexPageIsServed(TestContext context) {
		Async async = context.async();
		getNow("/assets/index.html",
			response -> {
				context.assertEquals(response.statusCode(), 200);
				context.assertEquals(response.headers().get("content-type"), "text/html");
				response.bodyHandler(body -> {
					context.assertTrue(body.toString()
						.contains("<title>My Whisky Collection</title>"));
					async.complete();
				});
			}
		);
	}
	
	private void getNow(Handler<HttpClientResponse> handler) {
		getNow("localhost", "/", handler);
	}
	
	private void getNow(String path, Handler<HttpClientResponse> handler) {
		getNow("localhost", path, handler);
	}
	
	private void getNow(String host, String path, Handler<HttpClientResponse> handler) {
		vertx.createHttpClient().getNow(port, host, path, handler);
	}
}
