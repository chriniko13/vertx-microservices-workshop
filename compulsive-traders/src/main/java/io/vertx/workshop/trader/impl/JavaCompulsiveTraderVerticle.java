package io.vertx.workshop.trader.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * Measures CPU time by thread.
 *
 * https://www.baeldung.com/java-variable-handles
 * https://www.youtube.com/watch?v=Xro4KwoMNJ8
 * https://www.youtube.com/watch?v=qG9FFlKV4LQ
 * https://www.youtube.com/watch?v=6HADC_Bu4-A
 * https://www.youtube.com/watch?v=w2zaqhFczjY
 * https://www.youtube.com/watch?v=_bVcHCt-J6Y
 * https://www.baeldung.com/java-unsafe
 * https://skymind.ai/wiki/restricted-boltzmann-machine
 */

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {

  private String company;
  private int numberOfShares;

  @Override
  public void start(Future<Void> future) {
    super.start();

    //TODO [DONE]
    //----

    company = TraderUtils.pickACompany();
    numberOfShares = TraderUtils.pickANumber();

    Future<PortfolioService> portfolioServiceF = Future.future();
    Future<MessageConsumer<JsonObject>> marketDataMessageSourceF = Future.future();

    EventBusService.getServiceProxyWithJsonFilter(
            discovery,
            new JsonObject().put("name", "portfolio"),
            PortfolioService.class,
            portfolioServiceF.completer());

    MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"),
            (Handler<AsyncResult<MessageConsumer<JsonObject>>>) event -> {
              if (event.succeeded()){
                marketDataMessageSourceF.complete(event.result());
              } else {
                marketDataMessageSourceF.fail(event.cause());
              }
            });

    CompositeFuture.all(portfolioServiceF, marketDataMessageSourceF).setHandler(event -> {


      if (event.failed()) {
        future.fail(event.cause());
      }else {

        //TODO...
        CompositeFuture compositeFuture = event.result();

        PortfolioService portfolioService = compositeFuture.resultAt(0);
        MessageConsumer<JsonObject> marketDataMessageConsumer = compositeFuture.resultAt(1);

        marketDataMessageConsumer.handler(marketDataObjectMessage -> {
          JsonObject marketDataObject = marketDataObjectMessage.body();
          TraderUtils.dumbTradingLogic(company, numberOfShares, portfolioService, marketDataObject);
        });

        future.complete();
      }

    });

    System.out.println("Java compulsive trader configured for company " + company + " and shares: " + numberOfShares);
    // ----
  }


}
