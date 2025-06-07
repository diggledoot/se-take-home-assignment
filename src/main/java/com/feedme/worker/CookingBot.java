package com.feedme.worker;

import com.feedme.constant.BotStatus;
import com.feedme.constant.OrderStatus;
import com.feedme.model.Order;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingDeque;

@Setter(AccessLevel.PRIVATE)
@Getter
public class CookingBot extends Thread {
    private final BlockingDeque<Order> orderQueue;
    private final BlockingDeque<Order> completedQueue;

    private boolean running;
    private BotStatus status;
    private Order currentOrder;

    private static final long ONE_SECOND = 1000;

    public CookingBot(BlockingDeque<Order> orderQueue, BlockingDeque<Order> completedQueue, String name) {
        super(name);
        this.orderQueue = orderQueue;
        this.completedQueue = completedQueue;
        this.running = true;
        this.status = BotStatus.IDLE;
        this.currentOrder = null;
    }

    @Override
    public void run() {
        while (running) {
            try {
                setStatus(BotStatus.IDLE);
                setCurrentOrder(orderQueue.takeFirst()); // blocks until the queue has an order
                setStatus(BotStatus.RUNNING);
                currentOrder.setOrderStatus(OrderStatus.COOKING);

                for (int i = 0; i < 10 && running; i++) {
                    Thread.sleep(ONE_SECOND);
                }

                if (running) {
                    getCurrentOrder().setOrderStatus(OrderStatus.COMPLETE);
                    getCompletedQueue().put(getCurrentOrder());
                } else {
                    requeueCurrentOrder();
                }
                setCurrentOrder(null);
            } catch (InterruptedException interruptedException) {
                requeueCurrentOrder();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        setRunning(false);
        this.interrupt();
    }

    private void requeueCurrentOrder() {
        if (getCurrentOrder() != null && getCurrentOrder().getOrderStatus() == OrderStatus.COOKING) {
            getCurrentOrder().setOrderStatus(OrderStatus.PENDING);
            try {
                if (getCurrentOrder().isVip()) {
                    orderQueue.putFirst(getCurrentOrder());
                } else {
                    orderQueue.putLast(getCurrentOrder());
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
