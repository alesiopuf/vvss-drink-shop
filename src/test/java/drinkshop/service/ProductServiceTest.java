package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductServiceTest {

    private ProductService setupServiceWithDummyRepo() {
        Repository<Integer, Product> dummyRepo = new Repository<Integer, Product>() {
            private Map<Integer, Product> data = new HashMap<>();
            @Override public Product findOne(Integer id) { return data.get(id); }
            @Override public List<Product> findAll() { return data.values().stream().collect(Collectors.toList()); }
            @Override public Product save(Product entity) { data.put(entity.getId(), entity); return entity; }
            @Override public Product delete(Integer id) { return data.remove(id); }
            @Override public Product update(Product entity) { data.put(entity.getId(), entity); return entity; }
        };

        Product initialProduct = new Product(1, "Cafea Veche", 10.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        dummyRepo.save(initialProduct);

        return new ProductService(dummyRepo);
    }

    // ==========================================
    // ECP (Equivalence Class Partitioning) TESTS
    // ==========================================

    @Test
    void testUpdateProduct_ECP_ValidPrice() {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;
        double validPrice = 15.5;

        service.updateProduct(productId, "Espresso", validPrice, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(validPrice, updatedProduct.getPret(), "Prețul ar trebui să fie actualizat corect.");
    }

    @Test
    void testUpdateProduct_ECP_InvalidPrice() {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Espresso", -5.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Ar trebui să se arunce IllegalArgumentException pentru preț invalid.");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Espresso", 600.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Ar trebui să se arunce IllegalArgumentException pentru preț invalid.");
    }

    // ==========================================
    // BVA (Boundary Value Analysis) TESTS
    // ==========================================

    @Test
    void testUpdateProduct_BVA_ValidPrice() {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        service.updateProduct(productId, "Latte", 0.01, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(0.01, updatedProduct.getPret(), "Prețul la limită ar trebui acceptat.");

        service.updateProduct(productId, "Latte", 500.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        updatedProduct = service.findById(productId);
        Assertions.assertEquals(500.0, updatedProduct.getPret(), "Prețul la limită ar trebui acceptat.");
    }

    @Test
    void testUpdateProduct_BVA_InvalidPrice() {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Latte", 0.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Prețurile fix în afara limitelor trebuie respinse.");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Latte", 500.01, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Prețurile fix în afara limitelor trebuie respinse.");
    }
}