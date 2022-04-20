package com.example.demo.resources;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.status;

import com.example.demo.resources.Products.AvailableProducts.Product;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.ws.rs.GET;
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
 * Get all products and quantity of each that is available with the current inventory
 */
@Path("products")
@Component
@AllArgsConstructor
public class Products {
  private final ProductsRepository productsRepository;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response products() {
    List<Product> availableProducts = productsRepository.get();
    if (availableProducts.isEmpty()) {
      return noContent().build();
    } else {
      return status(OK).entity(new AvailableProducts(availableProducts)).build();
    }
  }

  @AllArgsConstructor
  @Repository
  static class ProductsRepository implements Supplier<List<Product>> {
    private final Driver driver;
    private static final Query PRODUCTS_QUERY = new Query(
        """
            match (product:Product)-[contains_article:contains_article]->(article:Article)
            with product, min(toInteger(article.stock)/toInteger(contains_article.amount)) as quantity
            where quantity > 0
            return product.name as productName, quantity;"""
    );

    @Override
    public List<Product> get() {
      try (var session = driver.session()) {
        return session.readTransaction(tx -> tx.run(PRODUCTS_QUERY).
            list(r -> new Product(r.get("productName").asString(), r.get("quantity").asInt())));
      }
    }
  }

  record AvailableProducts(@NonNull List<Product> availableProducts) {
    AvailableProducts {
      if (availableProducts.isEmpty()) {
        throw new IllegalArgumentException("list of available products must be non-empty");
      }
    }
    record Product(String name, int quantity) { }
  }
}
