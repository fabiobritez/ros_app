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
    private String unit;

    public ItemRecipe(Ingredient ingredient, int quantity)
    {
        this.ingredient = ingredient;
        this.description = ingredient.getDescription();
        this.quantity = quantity;
        this.unit = ingredient.getUnit();
    }

    public ItemRecipe(Ingredient ingredient)
    {
        this.ingredient = ingredient;
        this.description = ingredient.getDescription();
        this.unit = ingredient.getUnit();
        this.quantity = 0;
    }


    public Ingredient getIngredient()
    {
        return ingredient;
    }

    public String getUnit() {
        return unit;
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

    public void addQuantity(int quantity)
    {
        this.quantity+=quantity;
    }

    @Override
    public boolean equals(Object obj) {
        ItemRecipe itemRecipe = (ItemRecipe)obj;
        return itemRecipe.getDescription().equals(description);
    }
}
