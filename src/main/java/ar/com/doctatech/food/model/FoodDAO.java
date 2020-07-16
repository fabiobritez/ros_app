package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public interface FoodDAO {

    void save(Food food) throws SQLException;

    void update(Food food) throws SQLException;

    /**
     * SI SE ELIMINA CAMBIA EL ESTADO Y SOLO SE VERÁN EN
     * LOS HISTORIALES.
     */
    void remove(int foodID) throws SQLException;

    void addItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException;

    void removeItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException;

    Food get(int foodID) throws NotFoundException, SQLException;

    HashMap<String, Food> getAll() throws SQLException;

}
