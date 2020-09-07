package io.vertx.blog.first;

import java.util.Map;
import java.util.LinkedHashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;

import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MyFirstVerticle extends AbstractVerticle {
	
	private Map<Integer, Whisky> products = new LinkedHashMap<>();
	
	private void createSomeData() {
		Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
		products.put(bowmore.getId(), bowmore);
		Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
		products.put(talisker.getId(), talisker);
	}
	
	@Override
	public void start(Future<Void> fut) {
		
		createSomeData();
		
		Router router = Router.router(vertx);
		
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response
				.putHeader("content-type", "text/html")
				.end("<h1>Hello from my first Vert.x 3 application</h1>");
		});
		
		router.route("/hi").handler(this::getHi);
		router.route("/bye").handler(this::getBye);
		router.route("/assets/*").handler(StaticHandler.create("assets"));
		
		// Whisky API
		router.get("/api/whiskies").handler(this::getAll);
		router.route("/api/whiskies*").handler(BodyHandler.create());
		router.post("/api/whiskies").handler(this::addOne);
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
		
		vertx
			.createHttpServer()
			.requestHandler(router::accept)
			.listen(
				config().getInteger("http_port", 8080),
				result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				}
			);
	}
	
	private Route getHi(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
			response
				.putHeader("content-type", "text/html")
				.end("<h1>Hi! I'm a method reference! =)</h1>");
				
		return routingContext.currentRoute();
	}
	
	private Route getBye(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
			response
				.putHeader("content-type", "text/html")
				.end("<h1>Bye! And good night!</h1>");
				
		return routingContext.currentRoute();
	}
	
	private void getAll(RoutingContext routingContext) {
	  routingContext.response()
		  .putHeader("content-type", "application/json; charset=utf-8")
		  .end(Json.encodePrettily(products.values()));
	}
	
	private void addOne(RoutingContext routingContext) {
		System.out.println("routingContext.getBodyAsString() = " +
			routingContext.getBodyAsString());
	  final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
		  Whisky.class);
	  products.put(whisky.getId(), whisky);
	  routingContext.response()
		  .setStatusCode(201)
		  .putHeader("content-type", "application/json; charset=utf-8")
		  .end(Json.encodePrettily(whisky));
	}
	
	private void deleteOne(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			Integer idAsInteger = Integer.valueOf(id);
			products.remove(idAsInteger);
		}
		routingContext.response().setStatusCode(204).end();
	}
}
