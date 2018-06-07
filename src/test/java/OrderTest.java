import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jacopo on 06/06/2018.
 */
public class OrderTest {


    @Test
    public void newOrderQty() {
        Order o = new Order(1, Side.Sell, 123.4, 10);

        assertEquals(o.getFilledQty(), 0);
        assertEquals(o.getUnfilledQty(), 10);
    }

    @Test
    public void fillQty() {
        Order o = new Order(1, Side.Sell, 123.4, 10);
        o.addFilledQty(9);

        assertEquals(o.getFilledQty(), 9);
        assertEquals(o.getUnfilledQty(), 1);


    }

    @Test(expected = IllegalArgumentException.class)
    public void fillMoreThenAvailableQty() {
        Order o = new Order(1, Side.Sell, 123.4, 10);

        o.addFilledQty(10);
        o.addFilledQty(1);
    }

    @Test
    public void equalOrders() {
        int id = 1;
        Order o1 = new Order(id, Side.Sell, 123.4, 10);
        Order o2 = new Order(id, Side.Buy, 0.1234, 12);
        assertEquals(o1, o2);
    }

}