package drinkshop.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import drinkshop.domain.IngredientReteta;
import drinkshop.domain.Reteta;
import drinkshop.domain.Stoc;
import drinkshop.repository.Repository;

public class StocServiceTest {

    private StocService buildService(List<Stoc> initialStock) {
        Map<Integer, Stoc> data = new HashMap<>();
        for (Stoc s : initialStock) {
            data.put(s.getId(), s);
        }

        Repository<Integer, Stoc> repo = new Repository<>() {
            @Override
            public Stoc findOne(Integer id) {
                return data.get(id);
            }

            @Override
            public List<Stoc> findAll() {
                return new ArrayList<>(data.values());
            }

            @Override
            public Stoc save(Stoc entity) {
                data.put(entity.getId(), entity);
                return entity;
            }

            @Override
            public Stoc delete(Integer id) {
                return data.remove(id);
            }

            @Override
            public Stoc update(Stoc entity) {
                data.put(entity.getId(), entity);
                return entity;
            }
        };

        return new StocService(repo);
    }

    @Test
    void testConsuma_F02_TC01_StocInsuficient_ArunkaExceptie() {
        // Arrange
        List<Stoc> stock = List.of(new Stoc(1, "Lapte", 5.0, 0.0));
        StocService service = buildService(stock);

        Reteta reteta = new Reteta(1, List.of(new IngredientReteta("Lapte", 10.0)));

        // Act & Assert
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.consuma(reteta),
                "Ar trebui să se arunce IllegalStateException când stocul este insuficient.");
    }

    @Test
    void testConsuma_F01_TC02_RetetaGoala_TreceCuSucces() {
        // Arrange
        StocService service = buildService(new ArrayList<>());
        Reteta reteta = new Reteta(1, new ArrayList<>());

        // Act & Assert – nicio excepție nu trebuie aruncată
        Assertions.assertDoesNotThrow(
                () -> service.consuma(reteta),
                "O rețetă goală ar trebui să nu arunce nicio excepție.");
    }

    @Test
    void testConsuma_F01_TC03_UnIngredient_StocGol_MetodaTrece() {
        // Arrange – stoc gol, reteta cere 0 unități dintr-un ingredient
        StocService service = buildService(new ArrayList<>());
        Reteta reteta = new Reteta(1, List.of(new IngredientReteta("Zahar", 0.0)));

        // Act & Assert – nicio excepție (0 disponibil >= 0 necesar)
        Assertions.assertDoesNotThrow(
                () -> service.consuma(reteta),
                "Dacă stocul returnează listă goală și cantitatea necesară este 0, metoda trebuie să treacă.");
    }

    @Test
    void testConsuma_F01_TC04_ExactSuficient_CantitateStoculuiDevine0() {
        // Arrange
        Stoc zahar = new Stoc(1, "Zahar", 5.0, 0.0);
        StocService service = buildService(new ArrayList<>(List.of(zahar)));
        Reteta reteta = new Reteta(1, List.of(new IngredientReteta("Zahar", 5.0)));

        // Act
        service.consuma(reteta);

        // Assert
        Assertions.assertEquals(
                0.0,
                zahar.getCantitate(),
                "Cantitatea din stoc ar trebui să fie 0 după consumul exact.");
    }

    @Test
    void testConsuma_F01_TC05_Surplus_PrimulStocDevine0AlDoileaIgnorat() {
        // Arrange
        Stoc zahar1 = new Stoc(1, "Zahar", 5.0, 0.0);
        Stoc zahar2 = new Stoc(2, "Zahar", 5.0, 0.0);
        StocService service = buildService(new ArrayList<>(List.of(zahar1, zahar2)));
        Reteta reteta = new Reteta(1, List.of(new IngredientReteta("Zahar", 5.0)));

        // Act
        service.consuma(reteta);

        // Assert
        double totalConsumed = (5.0 - zahar1.getCantitate()) + (5.0 - zahar2.getCantitate());
        Assertions.assertEquals(5.0, totalConsumed,
                "Totalul consumat trebuie să fie exact 5.");

        // Cel puțin unul dintre stocuri a fost epuizat complet
        boolean unulEsteZero = zahar1.getCantitate() == 0.0 || zahar2.getCantitate() == 0.0;
        Assertions.assertTrue(unulEsteZero,
                "Primul stoc acoperit complet ar trebui să fie 0.");

        // Cel puțin unul rămâne la 5 (nemodificat)
        boolean unulRamaneLa5 = zahar1.getCantitate() == 5.0 || zahar2.getCantitate() == 5.0;
        Assertions.assertTrue(unulRamaneLa5,
                "Al doilea stoc ar trebui să rămână ignorat (cantitate 5).");
    }
}
