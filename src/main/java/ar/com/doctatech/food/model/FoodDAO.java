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

    void addRecipe(int foodID, Map<Ingredient,Integer> recipe);

    Food get(int foodID) throws NotFoundException, SQLException;

    HashMap<Integer, Food> getAll() throws SQLException;

}
