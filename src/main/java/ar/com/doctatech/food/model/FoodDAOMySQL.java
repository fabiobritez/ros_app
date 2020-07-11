package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FoodDAOMySQL implements FoodDAO
{

    Connection connection;
    {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (IOException | SQLException exception) {
            System.out.println(exception.toString());
        }
    }

    @Override
    public void save(Food food) throws SQLException
    {
        //SAVE FOOD
        String query = "INSERT INTO food (NAME, COST, PROFIT, PRICE, IMAGE) " +
                "VALUES (?, ?, ?, ?, ?)";


        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(query))
        {
            preparedStatement.setString(1, food.getName()  );
            preparedStatement.setDouble(2, food.getCost()  );
            preparedStatement.setDouble(3, food.getProfit());
            preparedStatement.setDouble(4, food.getPrice() );
            preparedStatement.setString(5, food.getImage() );

            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            food.setFoodID(rs.getInt("foodID"));
        }

        query = "INSERT INTO recipe (food_foodID, ingredient_description, quantity) " +
                "VALUES (?, ?, ?)";

        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(query))
        {
            for (Ingredient ingredient : food.getRecipe().keySet()) {
                preparedStatement.setInt   (1, food.getFoodID());
                preparedStatement.setString(2, ingredient.getDescription());
                preparedStatement.setInt   (3, food.getRecipe().get(ingredient));
                preparedStatement.executeUpdate();
            }

        }
    }

    @Override
    public void update(Food food) throws SQLException {
        String query = "UPDATE food SET name = ? , cost = ?, profit = ?, price = ?, " +
                "image = ? WHERE foodID = ?";

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(query))
        {
            preparedStatement.setString(1, food.getName()  );
            preparedStatement.setDouble(2, food.getCost()  );
            preparedStatement.setDouble(3, food.getProfit());
            preparedStatement.setDouble(4, food.getPrice() );
            preparedStatement.setString(5, food.getImage() );
            preparedStatement.setInt   (6, food.getFoodID());

            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void remove(int foodID) throws SQLException {

    }

    @Override
    public void addRecipe(int foodID, Map<Ingredient, Integer> recipe) {

    }

    @Override
    public Food get(int foodID) throws NotFoundException, SQLException {
        return null;
    }

    @Override
    public HashMap<Integer, Food> getAll() throws SQLException {
        return null;
    }
}
