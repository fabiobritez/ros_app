package ar.com.doctatech.food.model;

import ar.com.doctatech.stock.ingredient.Ingredient;
import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.exceptions.NotFoundException;

import java.io.IOException;
import java.sql.*;
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
                     connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            preparedStatement.setString(1, food.getName()  );
            preparedStatement.setDouble(2, food.getCost()  );
            preparedStatement.setDouble(3, food.getProfit());
            preparedStatement.setDouble(4, food.getPrice() );
            preparedStatement.setString(5, food.getImage() );
            preparedStatement.executeUpdate();

            try( ResultSet rs = preparedStatement.getGeneratedKeys() )
            {
                while (rs.next()){ food.setFoodID(rs.getInt(1)); }
            }
        }

        /*
         * query = "INSERT INTO recipe (food_foodID, ingredient_description, quantity) " +
                "VALUES (?, ?, ?)";

        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(query))
        {
            for (ItemRecipe itemRecipe : food.getRecipe())
            {
                preparedStatement.setInt   (1, food.getFoodID()           );
                preparedStatement.setString(2, itemRecipe.getDescription());
                preparedStatement.setInt   (3, itemRecipe.getQuantity()   );
                preparedStatement.executeUpdate();
            }

        }
         */
    }

    @Override
    public void update(Food food) throws SQLException {
        String query = "UPDATE food SET name = ? , cost = ?, profit = ?, price = ?, " +
                " image = ? WHERE foodID = ?";

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(query))
        {
            System.out.println("NAME: "+ food.getName() );
            System.out.println("COST: "+ food.getCost() );
            System.out.println("PROFIT: "+ food.getProfit() );
            System.out.println("PRICE: "+ food.getPrice() );
            System.out.println("IMAGE: "+ food.getImage() );
            System.out.println("ID: "+ food.getFoodID() );

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
        String query = "UPDATE food SET exist=false WHERE foodID = ?";

        try (PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            preparedStatement.setInt(1,foodID);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void addItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException {

    }

    @Override
    public void removeItemRecipe(int foodID, ItemRecipe itemRecipe) throws SQLException {

    }


    @Override
    public Food get(int foodID) throws NotFoundException, SQLException {
        String query = "SELECT * FROM food WHERE foodID = ? ";
        Food food = null;
        try(PreparedStatement preparedStatement =
                connection.prepareStatement(query))
        {
            preparedStatement.setInt(1, foodID);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    food = new Food(
                            resultSet.getInt("foodID"),
                            resultSet.getString ("name"),
                            resultSet.getDouble ("cost"),
                            resultSet.getDouble ("profit"),
                            resultSet.getDouble ("price"),
                            resultSet.getString ("image"),
                            resultSet.getBoolean("exist")
                    );
                }
            }
        }
        if ( food == null )
        {
            throw new NotFoundException("Food: not found.");
        }
        return food;
    }

    @Override
    public HashMap<String, Food> getAll() throws SQLException
    {

        String query = "SELECT * FROM food " +
                       "LEFT OUTER JOIN recipe r on food.foodID = r.food_foodID " +
                       "LEFT OUTER JOIN ingredient i on r.ingredient_description = i.description" +
                " WHERE exist=true";

        HashMap<String, Food> foodFound = new HashMap<>();

        try (PreparedStatement preparedStatement
                     = connection.prepareStatement(query))
        {
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                    while (resultSet.next())
                    {
                        String name = resultSet.getString("name");
                        if(!foodFound.containsKey(name))
                        {
                            Food food = new Food(
                                    resultSet.getInt("foodID"),
                                    name,
                                    resultSet.getDouble("cost"),
                                    resultSet.getDouble("profit"),
                                    resultSet.getDouble("price"),
                                    resultSet.getString("image"),
                                    resultSet.getBoolean("exist")
                                    );

                            if(resultSet.getString("description") != null)
                                food.addIngredient(
                                    new ItemRecipe(
                                        new Ingredient(
                                            resultSet.getString("description"),
                                            resultSet.getInt   ("stock"),
                                            resultSet.getInt   ("stockMin"),
                                            resultSet.getString("unit")
                                        ),
                                        resultSet.getInt("quantity")
                                    )
                                );

                            foodFound.put(name, food);
                        }
                        else
                        {
                            if(resultSet.getString("description") != null)
                                foodFound.get(name).addIngredient(
                                    new ItemRecipe(
                                            new Ingredient(
                                                    resultSet.getString("description"),
                                                    resultSet.getInt("stock"),
                                                    resultSet.getInt("stockMin"),
                                                    resultSet.getString("unit")
                                            ),
                                            resultSet.getInt("quantity")
                                    )
                                );
                        }
                    }
            }
        }
        return foodFound;
    }

}
