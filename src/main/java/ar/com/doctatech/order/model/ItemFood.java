package ar.com.doctatech.order.model;

import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.food.model.ItemRecipe;

public class ItemFood
{
    private Food food;
    private String description;
    private Integer quantity;
    private double costAtTheMoment;
    private double priceAtTheMoment;
    private double amount;

    /**
     * Constructor cuando voy enlistar ordenes ya realizadas
     * @param food
     * @param quantity
     * @param costAtTheMoment
     * @param priceAtTheMoment
     */
    public ItemFood(Food food, Integer quantity, double costAtTheMoment, double priceAtTheMoment)
    {
        this.food = food;
        this.description = food.getName();
        this.quantity = quantity;
        this.costAtTheMoment = costAtTheMoment;
        this.priceAtTheMoment = priceAtTheMoment;
        this.amount = quantity * priceAtTheMoment;
    }

    /**
     * Constructor cuando voy a crear una nueva orden
     * @param food Food
     * @param quantity Quantity
     */
    public ItemFood(Food food, Integer quantity)
    {
        this.food = food;
        this.description = food.getName();
        this.quantity = quantity;
        this.costAtTheMoment = food.getCost();
        this.priceAtTheMoment = food.getPrice();
        this.amount = quantity * priceAtTheMoment;
    }

    public String getDescription()
    {
        return description;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.amount =  quantity*priceAtTheMoment;
    }

    public double getCostAtTheMoment() {
        return costAtTheMoment;
    }

    public void setCostAtTheMoment(double costAtTheMoment) {
        this.costAtTheMoment = costAtTheMoment;
    }

    public double getPriceAtTheMoment() {
        return priceAtTheMoment;
    }

    public void setPriceAtTheMoment(double priceAtTheMoment) {
        this.priceAtTheMoment = priceAtTheMoment;
        this.amount = quantity * priceAtTheMoment;
    }

    public double getAmount()
    {
        return amount;
    }
}
