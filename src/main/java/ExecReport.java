/**
 * Created by jacopo on 23/04/2018.
 */
public class ExecReport {
    public final int id;
    public final ReportType type;
    public final int qty;
    public final double price;
    public final Side side;


    ExecReport(int id, ReportType type, double price, int qty, Side side) {
        this.id = id;
        this.type = type;
        this.price = price;
        this.qty = qty;
        this.side = side;
    }


    @Override
    public String toString() {
        return "ExecReport{" +
                "id=" + id +
                ", type=" + type +
                ", qty=" + qty +
                ", price=" + price +
                ", side=" + side +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecReport that = (ExecReport) o;

        if (id != that.id) return false;
        if (qty != that.qty) return false;
        if (Double.compare(that.price, price) != 0) return false;
        if (type != that.type) return false;
        return side == that.side;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + qty;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (side != null ? side.hashCode() : 0);
        return result;
    }
}
