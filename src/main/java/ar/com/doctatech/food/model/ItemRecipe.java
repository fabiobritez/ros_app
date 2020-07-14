package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;


/**
 * CLASE EXCLUSIVA PARA
 */


public class ItemRecipe
{
    private Ingredient ingredient;
    private String description;
    private int quantity;

    public ItemRecipe(Ingredient ingredient, int quantity)
    {
        this.ingredient = ingredient;
        this.description = ingredient.getDescription();
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

    public String getDescription()
    {
        return description;
    }

    public void setIngredient(Ingredient ingredient)
    {
        this.description = ingredient.getDescription();
        this.ingredient = ingredient;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
}
