package io.vertx.blog.first;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import org.apache.log4j.Logger;

public class MyFirstVerticle extends AbstractVerticle {
	
	 static Logger logger = Logger.getLogger(MyFirstVerticle.class);
	
	private int port;
	
	public static final String COLLECTION = "whiskies";
	private MongoClient mongo;
	
	@Override
	public void start(Future<Void> fut) {
		
		port = config().getInteger("http.port", 8080);
		
		mongo = MongoClient.createShared(vertx, config());

		createSomeData(
				(nothing) -> startWebApp(
					(http) -> completeStartup(http, fut)
				), fut);
	}
	
	private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
		if (http.succeeded()) {
		  fut.complete();
		} else {
		  logger.error(http.cause());
		  fut.fail(http.cause());
		}
	}	
		
	private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
		
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
		router.get("/api/whiskies/:id").handler(this::getOne);
		router.put("/api/whiskies/:id").handler(this::updateOne);
		router.delete("/api/whiskies/:id").handler(this::deleteOne);
		
		vertx
			.createHttpServer()
			.requestHandler(router::accept)
			.listen(
				port,
				next
			);
		
		String startInfo = "*************************************** " +
				"Vertx Application Started on port: " + port +
				" *************************";
		System.out.println(startInfo);
		logger.info(startInfo);
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
	
  private void addOne(RoutingContext routingContext)
  {
	final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
          Whisky.class);
    
    mongo.insert(COLLECTION, whisky.toJson(), r ->
		routingContext.response()
			.setStatusCode(201)
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(Json.encodePrettily(whisky.setId(r.result()))));
  }

  private void getOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
		mongo.findOne(COLLECTION, new JsonObject().put("_id", id), null, ar -> {
			if (ar.succeeded()) {
				var json = ar.result();
				if (json == null) {
					routingContext.response().setStatusCode(404).end();
					return;
				}
				Whisky whisky = new Whisky(json);
				routingContext.response()
					.setStatusCode(200)
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(whisky));
			} else {
				routingContext.response().setStatusCode(404).end();
			}
		});
	}
  }

  private void updateOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    JsonObject json = routingContext.getBodyAsJson();
    if (id == null || json == null) {
      routingContext.response().setStatusCode(400).end();
      return;
    }
    mongo.update(COLLECTION,
		new JsonObject().put("_id", id),
		new JsonObject().put("$set", json),
		v -> {
			if (v.failed()) {
				routingContext.response().setStatusCode(404).end();
				return;
			}
            routingContext.response()
                  .putHeader("content-type", "application/json; charset=utf-8")
                  .end(Json.encodePrettily(
						new Whisky(id, json.getString("name"),
							json.getString("origin"))));
        }
	);
  }

  private void deleteOne(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      mongo.removeOne(COLLECTION, new JsonObject().put("_id", id),
          ar -> routingContext.response().setStatusCode(204).end());
    }
  }

  private void getAll(RoutingContext routingContext) {
    mongo.find(COLLECTION, new JsonObject(), results -> {
      List<JsonObject> objects = results.result();
      List<Whisky> whiskies = objects.stream().map(Whisky::new).collect(Collectors.toList());
      routingContext.response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(whiskies));
    });
  }
	
	@Override
	  public void stop() {
		mongo.close();
	  }
	  
  private void createSomeData(Handler<AsyncResult<Void>> next, Future<Void> fut) {
    Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
    Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
    System.out.println(bowmore.toJson());

    // Do we have data in the collection ?
    mongo.count(COLLECTION, new JsonObject(), count -> {
      if (count.succeeded()) {
        if (count.result() == 0) {
          // no whiskies, insert data
          mongo.insert(COLLECTION, bowmore.toJson(), ar -> {
            if (ar.failed()) {
              fut.fail(ar.cause());
            } else {
              mongo.insert(COLLECTION, talisker.toJson(), ar2 -> {
                if (ar2.failed()) {
                  fut.fail(ar2.cause());
                } else {
                  next.handle(Future.succeededFuture());
                }
              });
            }
          });
        } else {
          next.handle(Future.succeededFuture());
        }
      } else {
        // report the error
        fut.fail(count.cause());
      }
    });
  }
}
