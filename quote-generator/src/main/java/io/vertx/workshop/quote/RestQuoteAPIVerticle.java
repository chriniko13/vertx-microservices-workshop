package io.vertx.workshop.quote;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This verticle exposes a HTTP endpoint to retrieve the current / last values of the maker data (quotes).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RestQuoteAPIVerticle extends AbstractVerticle {

    private Map<String /*company name*/, JsonObject> quotes = new HashMap<>();

    private long logCounter = 0;

    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonObject>consumer(GeneratorConfigVerticle.ADDRESS)
                .handler(message -> {
                    // TODO Populate the `quotes` map with the received quote [DONE]
                    // Quotes are json objects you can retrieve from the message body
                    // The map is structured as follows: name -> quote
                    // ----

                    JsonObject quote = message.body();
                    String quoteName = quote.getString("name");

                    if (++logCounter % 5 == 0) {
                        System.out.println("Just received the following quote name: " + quoteName + " --- payload: " + quote.toString() );
                        logCounter = 0;
                    }

                    quotes.put(quoteName, quote);

                    // ----
                });


        vertx.createHttpServer()
                .requestHandler(request -> {

                    HttpServerResponse response = request.response()
                            .putHeader("content-type", "application/json");

                    // TODO [DONE]
                    // The request handler returns a specific quote if the `name` parameter is set, or the whole map if none.
                    // To write the response use: `request.response().end(content)`
                    // Responses are returned as JSON, so don't forget the "content-type": "application/json" header.
                    // If the symbol is set but not found, you should return 404.
                    // Once the request handler is set,
                    // ----

                    @Nullable String quoteName = request.getParam("name");

                    if (quoteName != null) {

                        JsonObject entry = quotes.get(quoteName);
                        if (entry == null) {
                            response.setStatusCode(404).end();
                        } else {
                            response.end(entry.encodePrettily());
                        }

                    } else {
                        response.end(Json.encodePrettily(quotes));
                    }

                    // ----
                })
                .listen(config().getInteger("http.port"), ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Server started");
                    } else {
                        System.out.println("Cannot start the server: " + ar.cause());
                    }
                });
    }
}
