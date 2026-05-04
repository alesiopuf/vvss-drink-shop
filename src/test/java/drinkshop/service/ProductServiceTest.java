package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductServiceTest {

    private ProductService setupServiceWithDummyRepo() {
        Repository<Integer, Product> dummyRepo = new Repository<>() {
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

    @ParameterizedTest
    @ValueSource(doubles = {15.5}) // Clasa validă: (0, 500]
    @DisplayName("ECP Valid - Actualizare produs cu preț valid")
    @Tag("ECP")
    void testUpdateProduct_ECP_ValidPrice(double validPrice) {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        service.updateProduct(productId, "Espresso", validPrice, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(validPrice, updatedProduct.getPret(), "Prețul ar trebui să fie actualizat corect.");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.0, 600.0})
    @DisplayName("ECP Non-Valid - Aruncă excepție pentru prețuri în afara limitelor")
    @Tag("ECP")
    void testUpdateProduct_ECP_InvalidPrice(double invalidPrice) {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Espresso", invalidPrice, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Ar trebui să se arunce IllegalArgumentException pentru preț invalid.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cappuccino"})
    @DisplayName("ECP Valid - Actualizare produs cu nume valid")
    @Tag("ECP")
    void testUpdateProduct_ECP_ValidName(String validName) {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        service.updateProduct(productId, validName, 20.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(validName, updatedProduct.getNume(), "Numele ar trebui să fie actualizat corect.");
    }

    // ==========================================
    // BVA (Boundary Value Analysis) TESTS
    // ==========================================

    @ParameterizedTest
    @CsvSource({
            "0.01",
            "500.0"
    })
    @DisplayName("BVA Valid - Limite valide pentru preț")
    @Tag("BVA")
    void testUpdateProduct_BVA_ValidPrice(double boundaryPrice) {
        // Arrange
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        // Act
        service.updateProduct(productId, "Latte", boundaryPrice, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        // Assert
        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(boundaryPrice, updatedProduct.getPret(), "Prețul la limită ar trebui acceptat.");
    }

    @ParameterizedTest
    @CsvSource({
            "0.0",
            "500.01"
    })
    @DisplayName("BVA Non-Valid - Imediat în afara limitelor pentru preț")
    @Tag("BVA")
    void testUpdateProduct_BVA_InvalidPrice(double boundaryInvalidPrice) {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.updateProduct(productId, "Latte", boundaryInvalidPrice, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);
        }, "Prețurile fix în afara limitelor trebuie respinse.");
    }

    @ParameterizedTest
    @CsvSource({
            "Cea, 3",
            "UnNumeFoarteLungPentruBauturaCareAreExactCinciZeci, 50"
    })
    @DisplayName("BVA Valid - Limite valide pentru lungimea numelui")
    @Tag("BVA")
    void testUpdateProduct_BVA_ValidName(String boundaryName, int expectedLength) {
        ProductService service = setupServiceWithDummyRepo();
        int productId = 1;

        service.updateProduct(productId, boundaryName, 15.0, CategorieBautura.CLASSIC_COFFEE, TipBautura.BASIC);

        Product updatedProduct = service.findById(productId);
        Assertions.assertEquals(expectedLength, updatedProduct.getNume().length());
        Assertions.assertEquals(boundaryName, updatedProduct.getNume());
    }
}