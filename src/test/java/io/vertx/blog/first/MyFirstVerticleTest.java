package io.vertx.blog.first;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {
	
	private static int counter;
	
	private Vertx vertx;
	private int port;
	private static MongodProcess MONGO;
	private static int MONGO_PORT = 12345;
	
	  @BeforeClass
	  public static void startMongo() throws IOException {
		MongodStarter starter = MongodStarter.getDefaultInstance();

		IMongodConfig mongodConfig = new MongodConfigBuilder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
			.build();

		MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
		MONGO = mongodExecutable.start();
	  }
	  
	  @AfterClass
	  public static void stopMongo() {
		MONGO.stop();
	  }
	
	@Before
	public void setUp(TestContext context) throws Exception {
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		System.out.println(++counter + ") Running on port: " + port);
		
		DeploymentOptions options = new DeploymentOptions()
			.setConfig(new JsonObject()
				.put("http.port", port)
				.put("db_name", "whiskies-test")
				.put("connection_string", "mongodb://localhost:" + MONGO_PORT)
			);
			
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
				context.assertTrue(response.headers().get("content-type")
					.contains("text/html"));
				response.bodyHandler(body -> {
					context.assertTrue(body.toString()
						.contains("<title>My Whisky Collection</title>"));
					async.complete();
				});
			}
		);
	}
	
	@Test
	public void checkThatWeCanAdd(TestContext context) {
		Async async = context.async();
		final String json = Json.encodePrettily(new Whisky("Jameson", "Ireland"));
		postJson("/api/whiskies", json, response -> {
			context.assertEquals(response.statusCode(), 201);
			context.assertTrue(response.headers().get("content-type")
				.contains("application/json"));
			response.bodyHandler(body -> {
				final Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
				context.assertEquals(whisky.getName(), "Jameson");
				context.assertEquals(whisky.getOrigin(), "Ireland");
				context.assertNotNull(whisky.getId());
				async.complete();
			});
        });
	}
	
	private void postJson(String json, Handler<HttpClientResponse> handler) {
		postJson("localhost", "/", json, handler);
	}
	
	private void postJson(String path, String json, Handler<HttpClientResponse> handler) {
		postJson("localhost", path, json, handler);
	}
	
	private void postJson(String host, String path, String json,
			Handler<HttpClientResponse> handler) {
		final String length = Integer.toString(json.length());
		vertx.createHttpClient().post(port, host, path)
			.putHeader("content-type", "application/json")
			.putHeader("content-length", length)
			.handler(handler)
			.write(json)
			.end();
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
