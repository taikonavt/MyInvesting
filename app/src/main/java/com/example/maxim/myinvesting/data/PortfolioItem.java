package com.example.maxim.myinvesting.data;

/**
 * Created by maxim on 09.08.17.
 */

public class PortfolioItem {

    int id = 0;
    private String ticker = null; // Ввожу руками
    private String name = null; // Получить из интернета по тикеру
    private int volume = 0; // Высчитываю из базы данных
    private int price = 0; // Получить из интернета и умножить на константу
    private int cost = 0; // Посчитать amount*price
    private int profit = 0; // Доходность акции в процентах годовых

    public PortfolioItem(int lID, String lTicker, int lVolume) {

        id = lID;
        ticker = lTicker;
        volume = lVolume;
    }

    public String getTicker() {
        return ticker;
    }

    public int getId() {
        return id;
    }

    public int getVolume() {
        return volume;
    }

    public int getPrice() {
        return price;
    }

    public int getCost() {
        return cost;
    }

    public int getProfit() {
        return profit;
    }
}
