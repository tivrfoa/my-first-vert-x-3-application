package io.vertx.blog.first;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MyFirstVerticle extends AbstractVerticle {
	
	@Override
	public void start(Future<Void> fut) {
		
		Router router = Router.router(vertx);
		
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response
				.putHeader("content-type", "text/html")
				.end("<h1>Hello from my first Vert.x 3 application</h1>");
		});
		
		router.route("/hi").handler(this::getHi);
		router.route("/bye").handler(this::getBye);
		
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
}
