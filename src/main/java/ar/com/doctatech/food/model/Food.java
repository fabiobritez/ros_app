package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.stock.ingredient.Unit;

import javax.mail.FetchProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Food
{
    private int foodID;
    private String name;
    private double cost;
    private double profit;
    private double price;
    private String image;
    private ArrayList<ItemRecipe> recipe;

    public Food(int foodID, String name, double cost)
    {
        this.foodID = foodID;
        this.name = name;
        this.cost = cost;
        this.profit = 0;
        this.price = cost;
        this.image = "";
        this.recipe = new ArrayList<>();

    }

    public Food(int foodID, String name, double cost, double profit,
                double price, String image, boolean exist)
    {
        this.foodID = foodID;
        this.name = name;
        this.cost = cost;
        this.profit = profit;
        this.price = price;
        this.image = image;
        this.recipe = new ArrayList<>();

    }

    public void setFoodID(int foodID)
    {
        this.foodID = foodID;
    }

    public int getFoodID()
    {
        return foodID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getCost()
    {
        return cost;
    }

    public void setCost(double cost)
    {
        this.cost = cost;
    }

    public double getProfit()
    {
        return profit;
    }

    public void setProfit(double profit)
    {
        this.profit = profit;
       // this.price = cost + ((cost * profit)/100);
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
       // this.profit = ( ( (price-cost)*100)/cost);
        this.price = price;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public void addItemRecipe(ItemRecipe itemRecipe)
    {
        if(!recipe.contains(itemRecipe))
            recipe.add(itemRecipe);
    }

    public void removeItemRecipe(String description)
    {
        recipe.removeIf(itemRecipe -> itemRecipe.getDescription().equals(description));
    }

    public ArrayList<ItemRecipe> getRecipe()
    {
        return recipe;
    }

    public void addRecipe(List<ItemRecipe> recipe)
    {
        this.recipe.addAll(recipe);
    }
}
