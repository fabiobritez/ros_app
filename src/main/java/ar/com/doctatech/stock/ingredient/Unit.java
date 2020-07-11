package ar.com.doctatech.stock.ingredient;

import javax.swing.event.MouseInputListener;

public enum Unit {
    GRAMS("GRAMOS"),
    QUANTITY("UNIDADES"),
    MILLILITERS("MILILITROS");

    private String unit;

    Unit(String unit) {
        this.unit = unit;
    }

    public static Unit getUnit(String unit)
    {
        switch (unit)
        {
            case "GRAMOS":
                return GRAMS;
            case "MILILITROS":
                return MILLILITERS;
            case "UNIDADES":
            default:
                return QUANTITY;
        }
    }

    @Override
    public String toString()
    {
        switch (this)
        {
            case GRAMS:
                return "GRAMOS";
            case MILLILITERS:
                return "MILILITROS";
            case QUANTITY:
            default:
                return "UNIDADES";
        }
    }
}
