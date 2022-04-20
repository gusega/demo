package com.example.demo.resources;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static org.neo4j.driver.Values.parameters;

import com.example.demo.resources.Sell.SellRequest.ProductToSell;
import java.util.List;
import java.util.function.Function;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Remove(Sell) product and update the inventory
 */
@Path("sell")
@Component
@AllArgsConstructor
public class Sell {

  private final SellRepository sellRepository;

  @Produces(MediaType.APPLICATION_JSON)
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sell(@NonNull SellRequest sellRequest) {
    return sellRepository.apply(sellRequest.productsToSell()) ? ok().build() : noContent().build();
  }

  @AllArgsConstructor
  @Repository
  static class SellRepository implements Function<List<ProductToSell>, Boolean> {
    private final Driver driver;
    private static final Query SELL_PRODUCTS_QUERY = new Query(
        """
            unwind $productsToSell as productToSell
            with productToSell
            // check that every product has enough articles are available
            match (product:Product{name:productToSell.name})-[contains_article:contains_article]->(article:Article)
            with min(toInteger(article.stock) >= toInteger(contains_article.amount) * productToSell.quantity) as allProductsHaveEnoughArticles
            where allProductsHaveEnoughArticles = true
            // update articles stock
            unwind $productsToSell as productToSell
            with productToSell
            match (product:Product{name:productToSell.name})-[contains_article:contains_article]->(article:Article)
            set article.stock = toInteger(article.stock) - toInteger(contains_article.amount) * productToSell.quantity
            return product;"""
    );

    @Override
    public Boolean apply(List<ProductToSell> productsToSell) {
      Query productsToSellQuery = SELL_PRODUCTS_QUERY.withParameters(
          parameters("productsToSell", productsToSell.stream().map(p ->
                  parameters("name", p.name(), "quantity", p.quantity()))
              .collect(toList())));
      try (var session = driver.session()) {
        return session.writeTransaction(tx -> tx.run(productsToSellQuery).hasNext());
      }
    }
  }

  record SellRequest(@NonNull List<ProductToSell> productsToSell) {
    SellRequest {
      if (productsToSell.isEmpty()) {
        throw new IllegalArgumentException("products to sell list must be non empty");
      }
    }
    record ProductToSell(@NonNull String name, int quantity) {
      ProductToSell {
        if (quantity <= 0) {
          throw new IllegalArgumentException("value must be greater than zero");
        }
        if (name.isEmpty()) {
          throw new IllegalArgumentException("name must be non-empty");
        }
      }
    }
  }
}
