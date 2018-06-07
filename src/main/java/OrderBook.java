import java.util.*;

/**
 * Created by jacopo on 23/04/2018.
 */
public class OrderBook {
    private PriorityQueue<Order> asks;
    private PriorityQueue<Order> bids;

    private Set<Integer> askIds;
    private Set<Integer> bidIds;

    public OrderBook() {

        Comparator<Order> askComparator = (o1, o2) -> {
            double px1 = o1.price;
            double px2 = o2.price;

            if (px1 == px2) {
                return 0;
            } else if (px1 < px2) {
                return -1;
            } else {
                return 1;
            }
        };

        Comparator<Order> bidComparator = (o1, o2) -> -1 * askComparator.compare(o1, o2);

        this.asks = new PriorityQueue<>(10, askComparator);
        this.bids = new PriorityQueue<>(10, bidComparator);

        this.askIds = new HashSet<>(10);
        this.bidIds = new HashSet<>(10);

    }


    private boolean canFillAt(double price) {
        if (bids.isEmpty() || asks.isEmpty()) {
            return false;
        }
        return bids.peek().price >= price && price >= asks.peek().price;

    }

    public Collection<ExecReport> insert(Order order) {
        Collection<ExecReport> result = new ArrayList<>();

        result.add(queue(order));

        result.addAll(matchOrder(order));

        return result;
    }


    private Collection<ExecReport> matchOrder(Order order) {
        Collection<ExecReport> result = new ArrayList<>();

        PriorityQueue<Order> bookOfOrder = order.side == Side.Sell ? asks : bids;
        PriorityQueue<Order> bookToMatch = order.side == Side.Sell ? bids : asks;

        if (!bookOfOrder.isEmpty()) { //there should be at least 'order' in bookOfOrder

            boolean isTopOfTheBook = order.equals(bookOfOrder.peek());
            if (isTopOfTheBook) {

                while (order.getUnfilledQty() > 0 && !bookToMatch.isEmpty()) {
                    Order level = bookToMatch.peek();

                    if (canFillAt(level.price)) {

                        result.addAll(matchTopOfBookOnly(level.price));
                    } else {
                        break;
                    }
                }
            }

        }
        return result;
    }


    public Collection<ExecReport> matchAuction(double price) {
        Collection<ExecReport> reports = new ArrayList<>();

        while (canFillAt(price)) {
            reports.addAll(matchTopOfBookOnly(price));
        }

        return reports;
    }

    private Collection<ExecReport> matchTopOfBookOnly(double price) {
        Collection<ExecReport> reports = new ArrayList<>(2);

        boolean hasLiquidity = !bids.isEmpty() && !asks.isEmpty();
        if (hasLiquidity) {

            Order bid = bids.peek();
            Order ask = asks.peek();


            if (canFillAt(price)) {

                int tradedQty = Math.min(bid.getUnfilledQty(), ask.getUnfilledQty());
                reports.add(fillOrder(bid, tradedQty, price));
                reports.add(fillOrder(ask, tradedQty, price));

                if (bid.isFullfilled()) {
                    removeOrder(bid, bids, bidIds);
                }

                if (ask.isFullfilled()) {
                    removeOrder(ask, asks, askIds);
                }
            }
        }

        return reports;

    }


    public ExecReport queue(Order order) {

        if (askIds.contains(order.id) || bidIds.contains(order.id)) {
            throw new IllegalArgumentException("Order id=" + order.id + " already contained in the book");
        }

        if (Side.Sell == order.side) {

            asks.add(order);
            askIds.add(order.id);
        } else {

            bids.add(order);
            bidIds.add(order.id);
        }

        return new ExecReport(order.id, ReportType.New, order.price, order.qty, order.side);

    }

    private void removeOrder(Order order, PriorityQueue<Order> book, Set<Integer> orderIds) {

        book.remove(order);
        orderIds.remove(order.id);
    }

    private ExecReport cancelOrder(Order order, PriorityQueue<Order> book, Set<Integer> orderIds) {

        removeOrder(order, book, orderIds);

        return new ExecReport(order.id, ReportType.Cancelled, order.price, order.qty, order.side);
    }

    private ExecReport fillOrder(Order order, int tradedQty, double price) {

        order.addFilledQty(tradedQty);
        ReportType fillType = order.getUnfilledQty() == 0 ? ReportType.FullFill : ReportType.PartialFill;

        return new ExecReport(order.id, fillType, price, tradedQty, order.side);

    }

    public Collection<ExecReport> remove(int orderId) {
        Collection<ExecReport> result = new ArrayList<>(1);

        if (askIds.contains(orderId)) {
            Optional<Order> found = asks.stream().filter(o -> o.id == orderId).findAny();

            result.add(cancelOrder(found.get(), asks, askIds));
        } else if (bidIds.contains(orderId)) {

            Optional<Order> found = bids.stream().filter(o -> o.id == orderId).findAny();
            result.add(cancelOrder(found.get(), bids, bidIds));
        }

        return result;
    }

}
