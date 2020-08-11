package ar.com.doctatech.order.model;

import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.food.model.ItemRecipe;
import com.sun.deploy.util.SyncAccess;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ItemFood
{
    private Food food;
    private String description;
    private Integer quantity;
    private double costAtTheMoment;
    private double priceAtTheMoment;
    private double amount;

    private SimpleIntegerProperty quantityProperty;
    private SimpleDoubleProperty amountProperty;


    public SimpleIntegerProperty quantityProperty() {
        return quantityProperty;
    }

    public SimpleDoubleProperty amountProperty() {
        return amountProperty;
    }

    //PARA MOSTRAR EN LAS LISTAS
    public ItemFood(String description, Integer quantity,
                   double priceAtTheMoment)
    {
        this.description = description;
        this.quantity = quantity;
        this.priceAtTheMoment = priceAtTheMoment;
        this.amount = quantity*priceAtTheMoment;

        this.quantityProperty = new SimpleIntegerProperty(this.quantity);
        this.amountProperty = new SimpleDoubleProperty(this.amount);
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

        this.quantityProperty =new SimpleIntegerProperty(this.quantity);
        this.amountProperty = new SimpleDoubleProperty(this.amount);
    }

    public void addQuantity(Integer quantityToAdd)
    {
        this.quantity+=quantityToAdd;

        this.amount =  this.quantity*priceAtTheMoment;

        this.quantityProperty.set(this.quantity);
        this.amountProperty.set(this.amount);
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

        this.quantityProperty.set(this.quantity);
        this.amountProperty.set(this.amount);
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

        this.amountProperty.set(amount);
    }

    public double getAmount()
    {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemFood itemFood = (ItemFood) o;

        return getDescription().equals(itemFood.getDescription());
    }

    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }
}
