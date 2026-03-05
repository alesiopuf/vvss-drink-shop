package drinkshop.export;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {
    public static void exportOrders(List<Product> products, List<Order> orders, String path) {
        try (FileWriter w = new FileWriter(path)) {
            w.write("OrderId,Product,Quantity,Price,OrderTotal\n");
            double sum = 0.0;
            for (Order o : orders) {
                for (OrderItem i : o.getItems()) {
                    Product p = products.stream()
                            .filter(p1 -> i.getProduct().getId() == p1.getId())
                            .findFirst()
                            .orElse(null);
                    String prodName = (p != null) ? p.getNume() : "N/A";
                    w.write(o.getId() + "," + prodName + "," + i.getQuantity() + "," + i.getTotal() + "," + o.getTotal()
                            + "\n");
                }
                sum += o.getTotal();
            }
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            w.write(",,,TOTAL " + date + "," + sum + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}