package ar.com.doctatech.ingredient.model;

import java.sql.SQLException;
import java.util.HashMap;

public interface IngredientDAO
{
    void save(Ingredient ingredient) throws SQLException;

    void update(Ingredient ingredient) throws SQLException;

    void remove(String ingredient) throws SQLException;

    HashMap<String, Ingredient> getAll() throws SQLException;
}
