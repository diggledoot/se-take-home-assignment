package com.feedme;

import com.feedme.model.Order;
import com.feedme.worker.CookingBot;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class McDonaldCookingBotController {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Order> vipQueue = new PriorityBlockingQueue<>();
        BlockingQueue<Order> normalQueue = new PriorityBlockingQueue<>();
        BlockingQueue<Order> completedQueue = new LinkedBlockingQueue<>();
        Deque<CookingBot> cookingBots = new LinkedList<>();
        boolean exit = false;
        int orderNumber = 1;
        int botNumber = 1;

        Scanner scanner = new Scanner(System.in);

        while (!exit) {
            System.out.println("McDonald Order Controller");
            System.out.println("=========================");
            printPendingOrders(vipQueue, normalQueue);
            printCompletedOrders(completedQueue);
            printCookingBots(cookingBots);
            System.out.println("""
                    press 'a' for new normal order
                    press 's' for new vip order
                    press 'z' to add a bot
                    press 'x' to remove a bot
                    press 'r' to refresh screen
                    press 'q' to exit
                    """);
            System.out.print(": ");
            System.out.flush();
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "a":
                    normalQueue.put(new Order(orderNumber, false));
                    orderNumber += 1;
                    break;
                case "s":
                    vipQueue.put(new Order(orderNumber, true));
                    orderNumber += 1;
                    break;
                case "z":
                    CookingBot newCookingBot = new CookingBot(vipQueue, normalQueue, completedQueue, "Bot" + botNumber);
                    cookingBots.add(newCookingBot);
                    newCookingBot.start();
                    botNumber += 1;
                    break;
                case "x":
                    if (cookingBots.isEmpty()) {
                        System.out.println("No bots to remove!");
                        waitForEnter(scanner);
                        break;
                    }
                    CookingBot removedCookingBot = cookingBots.pop();
                    removedCookingBot.shutdown();
                    removedCookingBot.join();
                    break;
                case "q":
                    exit = true;
                    for (CookingBot bot : cookingBots) {
                        bot.shutdown();
                        bot.join();
                    }
                    vipQueue.clear();
                    normalQueue.clear();
                    completedQueue.clear();
                    System.out.println("Exiting program! Bye!");
                    waitForEnter(scanner);
                    break;
                case "r":
                    break;
                default:
                    System.out.println("Unknown command!");
                    waitForEnter(scanner);
            }
            refreshScreen();
        }
    }

    public static void waitForEnter(Scanner scanner){
        System.out.println("Press enter to continue...");
        scanner.nextLine();
    }

    public static void refreshScreen(){
        System.out.print("\033[H\033[2J"); // clears Linux terminals only
        System.out.flush();
    }

    public static void printPendingOrders(BlockingQueue<Order> vipQueue, BlockingQueue<Order> normalQueue) {
        StringBuilder stringBuilder = new StringBuilder("Pending Orders:\n");

        for (Order order : vipQueue) {
            stringBuilder.append("  - Order ID: ").append(order.getId()).append(" (VIP)\n");
        }
        for (Order order : normalQueue) {
            stringBuilder.append("  - Order ID: ").append(order.getId()).append("\n");
        }

        System.out.println(stringBuilder);
    }

    public static void printCompletedOrders(BlockingQueue<Order> completedQueue) {
        StringBuilder stringBuilder = new StringBuilder("Completed Orders:\n");

        for (Order order : completedQueue) {
            stringBuilder.append("  - Order ID: ").append(order.getId());
            if (order.isVip()) {
                stringBuilder.append(" (VIP)\n");
            } else {
                stringBuilder.append("\n");
            }
        }

        System.out.println(stringBuilder);
    }

    public static void printCookingBots(Deque<CookingBot> cookingBots) {
        StringBuilder stringBuilder = new StringBuilder("Bots:\n");
        for (CookingBot cookingBot : cookingBots) {
            stringBuilder
                    .append("  - Bot ID: ").append(cookingBot.getName())
                    .append(" (")
                    .append(cookingBot.getBotStatus()).append(")\n");
        }
        System.out.println(stringBuilder);
    }
}
