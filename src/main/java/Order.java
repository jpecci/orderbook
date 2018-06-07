/**
 * Created by jacopo on 23/04/2018.
 */
public class Order {

    public final int id;
    public final Side side;
    public final double price;
    public final int qty;

    private int filledQty;

    private OrderStatus status = OrderStatus.Pending;

    public Order(int id, Side side, double price, int qty) {
        this.id = id;
        this.side = side;
        this.price = price;
        this.qty = qty;
    }

    public int addFilledQty(int qty) {
        assert (qty >= 0);
        if (qty > getUnfilledQty()) {
            throw new IllegalArgumentException("Qty > UnfilledQty");
        }

        filledQty += qty;
        return filledQty;
    }

    public int getFilledQty() {
        return filledQty;
    }

    public int getUnfilledQty() {
        return qty - filledQty;
    }

    public boolean isFullfilled() {
        return getUnfilledQty() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return id == order.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", filledQty=" + filledQty +
                ", status=" + status +
                '}';
    }
}
