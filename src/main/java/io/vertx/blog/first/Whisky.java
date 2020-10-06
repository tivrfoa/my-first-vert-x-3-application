package io.vertx.blog.first;


import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;


/**
 * Are you drunk my friend?
 */
public class Whisky {
	
  private static final AtomicInteger COUNTER = new AtomicInteger();
	
  private String id;

  private String name;

  private String origin;

  public Whisky(String name, String origin) {
    this.name = name;
    this.origin = origin;
    this.id = "";
  }

  public Whisky(JsonObject json) {
	System.out.println("Creating Whisky using JsonObject ...");
    this.name = json.getString("name");
    this.origin = json.getString("origin");
    this.id = json.getString("_id");
  }

  public Whisky() {
    this.id = "";
  }
  
  public JsonObject toJson() {
    JsonObject json = new JsonObject()
        .put("name", name)
        .put("origin", origin);
    if (id != null && !id.isEmpty()) {
      json.put("_id", id);
    }
    return json;
  }

  public Whisky(String id, String name, String origin) {
    this.id = id;
    this.name = name;
    this.origin = origin;
  }

  public String getName() {
    return name;
  }

  public String getOrigin() {
    return origin;
  }

  public String getId() {
    return id;
  }
  
  public Whisky setId(String id) {
    this.id = id;
    return this;
  }

  public Whisky setName(String name) {
    this.name = name;
    return this;
  }

  public Whisky setOrigin(String origin) {
    this.origin = origin;
    return this;
  }
  
	@Override
	public String toString() {
	  return "{ id: " + id + ",\n" +
	         "name: " + name + ",\n" +
	         "origin: " + origin;
	}
}
