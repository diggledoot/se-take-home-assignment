package com.feedme.worker;

import com.feedme.constant.BotStatus;
import com.feedme.constant.OrderStatus;
import com.feedme.model.Order;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;

@Getter
@Setter
public class CookingBot extends Thread {
    private final BlockingQueue<Order> vipQueue;
    private final BlockingQueue<Order> normalQueue;
    private final BlockingQueue<Order> completedQueue;

    private boolean running;
    private BotStatus botStatus;
    private Order currentOrder;

    private static final long ONE_SECOND = 1000;
    private int cookingIteration = 10;

    public CookingBot(
            BlockingQueue<Order> vipQueue,
            BlockingQueue<Order> normalQueue,
            BlockingQueue<Order> completedQueue,
            String name
    ) {
        super(name);
        this.vipQueue = vipQueue;
        this.normalQueue = normalQueue;
        this.completedQueue = completedQueue;
        this.running = true;
        this.botStatus = BotStatus.IDLE;
        this.currentOrder = null;
    }

    @Override
    public void run() {
        while (running) {
            try {
                setBotStatus(BotStatus.IDLE);

                // poll vip queue, if empty, poll normal queue
                Order order = getVipQueue().poll();
                if (order == null) {
                    order = getNormalQueue().poll();
                }

                // if normal queue is empty, idle the bot
                if (order == null) {
                    Thread.sleep(ONE_SECOND);
                    setBotStatus(BotStatus.IDLE);
                    continue;
                }

                setCurrentOrder(order);
                setBotStatus(BotStatus.RUNNING);
                getCurrentOrder().setOrderStatus(OrderStatus.COOKING);

                for (int i = 0; i < cookingIteration && running; i++) {
                    Thread.sleep(ONE_SECOND);
                }

                if (running) {
                    getCurrentOrder().setOrderStatus(OrderStatus.COMPLETE);
                    getCompletedQueue().put(currentOrder);
                } else {
                    requeueCurrentOrder();
                }

                setCurrentOrder(null);
            } catch (InterruptedException e) {
                requeueCurrentOrder();
                Thread.currentThread().interrupt();
                running = false;
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
                    getVipQueue().put(currentOrder);
                } else {
                    getNormalQueue().put(currentOrder);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
