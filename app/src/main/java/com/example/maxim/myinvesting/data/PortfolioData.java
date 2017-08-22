package com.example.maxim.myinvesting.data;

import java.util.ArrayList;

/**
 * Created by maxim on 21.08.17.
 */

public class PortfolioData {

    private String name = null;

    private ArrayList<PortfolioItem> portfolioItems = null;

    private long costOfPortfolio = 0;

    private double profitability = 0;

    private long untilDateInMillis = 0;

    private int periodInDays = 0;

    public PortfolioData(String string, long l) {
        name = string;
        untilDateInMillis = l;
    }

    public void setPortfolioItems(ArrayList<PortfolioItem> arrayList) {
        portfolioItems = arrayList;
    }

    public void setCostOfPortfolio(long cost) {
        costOfPortfolio = cost;
    }

    public void setProfitability(double profit) {
        profitability = profit;
    }

    public void setPeriodInDays(int days) {
        periodInDays = days;
    }

    public String getName() {
        return name;
    }

    public ArrayList<PortfolioItem> getPortfolioItems() {
        return portfolioItems;
    }

    public long getCostOfPortfolio() {
        return costOfPortfolio;
    }

    public double getProfitability() {
        return profitability;
    }

    public long getUntilDateInMillis() {
        return untilDateInMillis;
    }
}
