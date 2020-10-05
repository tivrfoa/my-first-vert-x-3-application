
# Vertx blog tutorial series

 1. [My first Vert.x 3 Application](https://vertx.io/blog/my-first-vert-x-3-application/)
 2. [Vert.x Application Configuration](https://vertx.io/blog/vert-x-application-configuration/)
 3. [Some Rest with Vert.x](https://vertx.io/blog/some-rest-with-vert-x/)
 4. [Unit and Integration Tests](https://vertx.io/blog/unit-and-integration-tests/)
 5. [Using the asynchronous SQL client](https://vertx.io/blog/using-the-asynchronous-sql-client/)
 6. [Combine vert.x and mongo to build a giant](https://vertx.io/blog/combine-vert-x-and-mongo-to-build-a-giant/)



### Running the application with a configuration file

```
java -jar target/my-first-app-1.2.0-fat.jar -conf target/classes/vertx-conf.json
```

### Running Unit Tests

```shell
mvn test
```

### Running Integration Tests

```shell
mvn verify
```
