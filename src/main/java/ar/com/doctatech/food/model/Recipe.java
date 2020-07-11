package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;

public class Recipe {
    private Ingredient ingredient;
    private int quantity;

    public Recipe(Ingredient ingredient, int quantity)
    {
        this.ingredient = ingredient;
        this.quantity = quantity;
    }


    public Ingredient getIngredient()
    {
        return ingredient;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setIngredient(Ingredient ingredient)
    {
        this.ingredient = ingredient;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
}
