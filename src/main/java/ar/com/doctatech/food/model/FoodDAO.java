package ar.com.doctatech.food.model;

import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.sql.SQLException;
import java.util.HashMap;

public interface FoodDAO {

    void save(Food food) throws SQLException;

    void update(Food food) throws SQLException;

    void remove(int foodID) throws SQLException;

    void addItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException;

    void removeItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException;

    Food get(int foodID) throws NotFoundException, SQLException;

    HashMap<String, Food> getAll() throws SQLException;

}
