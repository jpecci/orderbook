import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by jacopo on 06/06/2018.
 */
public class OrderBookTest {

    @Test
    public void insertOrder() {
        OrderBook book = new OrderBook();
        Order o = new Order(1, Side.Sell, 123.4, 10);

        Collection<ExecReport> added = book.insert(o);
        assertEquals(1, added.size());
        assertTrue(added.contains(new ExecReport(o.id, ReportType.New, o.price, o.qty, o.side)));
    }


    @Test
    public void cancelOrder() {

        OrderBook book = new OrderBook();
        Order o = new Order(1, Side.Sell, 123.4, 10);

        book.insert(o);
        Collection<ExecReport> removed = book.remove(o.id);
        assertEquals(1, removed.size());

        assertTrue(removed.contains(new ExecReport(o.id, ReportType.Cancelled, o.price, o.qty, o.side)));

    }

    @Test
    public void fillNewOrderWithPriceAlreadyInTheBook() {
        OrderBook book = new OrderBook();

        Collection<ExecReport> reports = new HashSet<>();

        reports.addAll(book.insert(new Order(1, Side.Sell, 100, 1)));
        reports.addAll(book.insert(new Order(2, Side.Buy, 101, 1)));

        assertTrue(reports.contains(new ExecReport(1, ReportType.FullFill, 100, 1, Side.Sell)));
        assertTrue(reports.contains(new ExecReport(2, ReportType.FullFill, 100, 1, Side.Buy)));


        reports.clear();
        reports.addAll(book.insert(new Order(3, Side.Buy, 101, 1)));
        reports.addAll(book.insert(new Order(4, Side.Sell, 100, 1)));

        assertTrue(reports.contains(new ExecReport(3, ReportType.FullFill, 101, 1, Side.Buy)));
        assertTrue(reports.contains(new ExecReport(4, ReportType.FullFill, 101, 1, Side.Sell)));
    }

    @Test
    public void partialFillOrder() {
        OrderBook book = new OrderBook();

        Order o1 = new Order(1, Side.Sell, 100, 10);
        book.insert(o1);

        Order o2 = new Order(2, Side.Buy, 100, 3);
        Collection<ExecReport> reports = book.insert(o2);

        assertEquals(7, o1.getUnfilledQty());
        assertEquals(0, o2.getUnfilledQty());

        assertTrue(reports.contains(new ExecReport(o1.id, ReportType.PartialFill, 100, 3, o1.side)));
        assertTrue(reports.contains(new ExecReport(o2.id, ReportType.FullFill, 100, 3, o2.side)));

    }

    @Test
    public void fullFillOrders() {
        OrderBook book = new OrderBook();

        Order o1 = new Order(1, Side.Sell, 100, 10);
        Order o2 = new Order(2, Side.Buy, 100, 3);
        Order o3 = new Order(3, Side.Buy, 101, 7);
        Collection<ExecReport> reports = new HashSet<>();

        reports.addAll(book.insert(o1));
        reports.addAll(book.insert(o2));
        reports.addAll(book.insert(o3));

        assertTrue(reports.contains(new ExecReport(o1.id, ReportType.PartialFill, 100, 3, o1.side)));
        assertTrue(reports.contains(new ExecReport(o1.id, ReportType.FullFill, 100, 7, o1.side)));
        assertTrue(reports.contains(new ExecReport(o3.id, ReportType.FullFill, 100, 7, o3.side)));

    }

    @Test
    public void queueNewOrders() {
        OrderBook book = new OrderBook();

        Order o1 = new Order(1, Side.Sell, 103, 10);
        Order o2 = new Order(2, Side.Buy, 100, 3);
        Order o3 = new Order(3, Side.Buy, 101, 7);
        Collection<ExecReport> reports = new HashSet<>();

        reports.addAll(book.insert(o1));
        reports.addAll(book.insert(o2));
        reports.addAll(book.insert(o3));

        for (ExecReport report : reports) {
            assertTrue(report.type == ReportType.New);
        }
    }

    @Test
    public void auction() {
        OrderBook book = new OrderBook();
        Map<Integer, Order> orders = new HashMap<>();
        Random rand = new Random();

        for (int i = 0; i < 1000; i++) {
            Side side = rand.nextBoolean() ? Side.Sell : Side.Buy;
            double price = 100 + 10 * (rand.nextDouble() - 0.5);
            int qty = 1 + rand.nextInt(10);

            Order order = new Order(i, side, price, qty);
            orders.put(i, order);
            book.queue(order);
        }

        double px = 100.0;
        Collection<ExecReport> reports = book.matchAuction(px);

        //Map<Integer, Integer> cumQty = new HashMap<>();

        for (ExecReport r : reports) {
            assertTrue(r.price == px);

            Order o = orders.get(r.id);
            assertTrue(r.qty <= o.qty);
            //assertTrue(cQty <= o.qty);

            if (r.side == Side.Sell) {
                assertTrue(r.price >= o.price);
            } else {
                assertTrue(r.price <= o.price);
            }
        }

        assertEquals(
                reports.stream().filter(r -> r.side == Side.Sell).map(r -> r.qty).reduce(0, (q1, q2) -> q1 + q2),
                reports.stream().filter(r -> r.side == Side.Buy).map(r -> r.qty).reduce(0, (q1, q2) -> q1 + q2)
        );


    }
}