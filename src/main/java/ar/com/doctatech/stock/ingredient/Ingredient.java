package ar.com.doctatech.stock.ingredient;

public class Ingredient
{

    private String description;
    private int stock;
    private int stockMin;
    private String unit;

    public Ingredient(String description, int stock,int stockMin, String unit)
    {
        this.description = description;
        this.stock = stock;
        this.unit = unit;
        this.stockMin = stockMin;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getStock()
    {
        return stock;
    }

    public void reduceStock(int value)
    {
        this.stock-=value;
    }

    public void addStock(int value)
    {
        this.stock+= value;
    }

    public void setStock(int stock)
    {
        this.stock = stock;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public int getStockMin() {
        return stockMin;
    }

    public void setStockMin(int stockMin) {
        this.stockMin = stockMin;
    }
}
