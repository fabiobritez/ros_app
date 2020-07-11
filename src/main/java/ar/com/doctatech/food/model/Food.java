package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;

import java.util.HashMap;

public class Food
{
    private int foodID;
    private String name;
    private double cost;
    private double profit;
    private double price;
    private String image;
    private HashMap<Ingredient, Integer> recipe;

    public Food(int foodID,String name,double cost)
    {
        this.foodID = foodID;
        this.name = name;
        this.cost = cost;
        this.profit = 0;
        this.price = cost;
        this.image = "";
        this.recipe = new HashMap<>();
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
        this.price = cost + ((cost * profit)/100);
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.profit = ( ( (price-cost)*100)/cost);
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

    public void addIngredient(Ingredient ingredient, int quantity)
    {
        recipe.put(ingredient, quantity);
    }

    public void removeIngredient(String description)
    {
        for ( Ingredient ingredient : recipe.keySet() )
            if(ingredient.getDescription().equals(description))
            {
                recipe.remove(ingredient);
                return;
            }
    }

    public HashMap<Ingredient, Integer> getRecipe()
    {
        return recipe;
    }

    public void setRecipe(HashMap<Ingredient, Integer> recipe)
    {
        this.recipe = recipe;
    }
}
