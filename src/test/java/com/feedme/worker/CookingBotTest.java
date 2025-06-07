package com.feedme.worker;


import com.feedme.constant.BotStatus;
import com.feedme.constant.OrderStatus;
import com.feedme.model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

class CookingBotTest {
    private CookingBot mockCookingBot;
    private BlockingQueue<Order> mockVipQueue;
    private BlockingQueue<Order> mockNormalQueue;
    private BlockingQueue<Order> mockCompletedQueue;

    @BeforeEach
    void setup() {
        this.mockVipQueue = new PriorityBlockingQueue<>();
        this.mockNormalQueue = new PriorityBlockingQueue<>();
        this.mockCompletedQueue = new LinkedBlockingQueue<>();
        this.mockCookingBot =
                new CookingBot(
                        this.mockVipQueue,
                        this.mockNormalQueue,
                        this.mockCompletedQueue,
                        "MockBot-1"
                );
        this.mockCookingBot.setCookingIteration(3); // to speed up testing
    }

    @AfterEach
    void cleanup() throws InterruptedException {
        if (this.mockCookingBot != null && this.mockCookingBot.isRunning()) {
            this.mockCookingBot.shutdown();
            this.mockCookingBot.join();
        }
    }

    @Test
    void testVipOrderFirst() throws InterruptedException {
        // arrange
        Order mockVipOrder = new Order(2, true);
        Order mockNormalOrder = new Order(1, false);
        this.mockVipQueue.put(mockVipOrder);
        this.mockNormalQueue.put(mockNormalOrder);

        // act
        this.mockCookingBot.start();

        // assert
        Assertions.assertEquals(mockVipOrder, this.mockCompletedQueue.take());
    }

    @Test
    void testVipOrderOrdering() throws InterruptedException {
        // arrange
        Order mockVipOrder1 = new Order(2,true);
        Order mockVipOrder2 = new Order(1,true);
        this.mockVipQueue.put(mockVipOrder1);
        this.mockVipQueue.put(mockVipOrder2);

        // assert
        Assertions.assertEquals(mockVipOrder2, this.mockVipQueue.take());
        Assertions.assertEquals(mockVipOrder1,this.mockVipQueue.take());
    }

    @Test
    void noOrdersBotIdle() {
        // act
        this.mockCookingBot.start();

        // assert
        Assertions.assertEquals(BotStatus.IDLE, this.mockCookingBot.getBotStatus());
    }

    @Test
    void requeueOrderOnBotShutdown() throws InterruptedException {
        // arrange
        Order mockNormalOrder = new Order(1, false);
        this.mockNormalQueue.put(mockNormalOrder);

        // act
        this.mockCookingBot.start();
        Thread.sleep(1000);
        this.mockCookingBot.shutdown();
        this.mockCookingBot.join();

        // assert
        Assertions.assertEquals(1, this.mockNormalQueue.size());
        Order actualRequeuedNormalOrder = this.mockNormalQueue.take();
        Assertions.assertEquals(mockNormalOrder, actualRequeuedNormalOrder);
        Assertions.assertEquals(OrderStatus.PENDING, actualRequeuedNormalOrder.getOrderStatus());
    }

    @Test
    void completeOrderSuccessfully() throws InterruptedException {
        // arrange
        Order mockVipOrder = new Order(1, true);
        Order mockNormalOrder = new Order(2, false);
        Order mockNormalOrder2 = new Order(3, false);
        this.mockVipQueue.put(mockVipOrder);
        this.mockNormalQueue.put(mockNormalOrder);
        this.mockNormalQueue.put(mockNormalOrder2);

        // act
        this.mockCookingBot.start();
        Thread.sleep(15000);

        // assert
        Assertions.assertEquals(3, this.mockCompletedQueue.size());
        Assertions.assertEquals(mockVipOrder, this.mockCompletedQueue.take());
        Assertions.assertEquals(mockNormalOrder, this.mockCompletedQueue.take());
        Assertions.assertEquals(mockNormalOrder2, this.mockCompletedQueue.take());
    }
}
