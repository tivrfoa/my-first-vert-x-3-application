package io.vertx.blog.first;

import com.jayway.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MyRestIT {
	
	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = Integer.getInteger("http.port", 8082);
		
		System.out.printf("RestAssured will try to connect to: %s:%s\n",
				RestAssured.baseURI, RestAssured.port);
	}
	
	@AfterClass
	public static void unconfigureRestAssured() {
		RestAssured.reset();
	}
	
	@Test
	public void checkThatWeCanRetrieveIndividualProduct() {
		// Get the list of bottles, ensure it's a success and extract the first id.
		/*final int id = get("/api/whiskies").then()
		  .assertThat()
		  .statusCode(200)
		  .extract()
		  .jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");*/
		
		final Whisky[] whiskies = get("/api/whiskies").then()
		  .assertThat()
		  .statusCode(200)
		  .extract()
		  .as(Whisky[].class);
		  
		final Whisky whisky = whiskies[0];
		
		System.out.println(whisky);
		  
		final String id = whisky.getId();
		
		System.out.println("Got whisky id: " + id);
		
		// Now get the individual resource and check the content
		get("/api/whiskies/" + id).then()
		  .assertThat()
		  .statusCode(200)
		  .body("name", equalTo("Bowmore 15 Years Laimrig"))
		  .body("origin", equalTo("Scotland, Islay"))
		  .body("id", equalTo(id));
	}
}
