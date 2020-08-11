package ar.com.doctatech.ingredient.model;

import ar.com.doctatech.shared.db.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class IngredientDAOMySQL implements IngredientDAO
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
    public void save(Ingredient ingredient) throws SQLException
    {
        String query =
                "INSERT INTO ingredient (description, stock, stockMin, unit) " +
                "VALUES (?, ?, ?, ?)";

        try ( PreparedStatement preparedStatement
                      = connection.prepareStatement(query) )
        {
            preparedStatement.setString(1, ingredient.getDescription());
            preparedStatement.setInt   (2, ingredient.getStock()      );
            preparedStatement.setInt   (3, ingredient.getStockMin()   );
            preparedStatement.setString(4, ingredient.getUnit()       );
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void update(Ingredient ingredient) throws SQLException
    {
        String query = "UPDATE ingredient SET stock = ? , stockMin = ?, unit = ? " +
                "WHERE description = ?";

        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(query))
        {
            preparedStatement.setInt(1, ingredient.getStock() );
            preparedStatement.setInt(2, ingredient.getStockMin() );
            preparedStatement.setString(3, ingredient.getUnit() );
            preparedStatement.setString(4, ingredient.getDescription() );

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void remove(String description) throws SQLException
    {
        String query = "DELETE FROM ingredient WHERE description = ?";

        try(PreparedStatement preparedStatement
                    = connection.prepareStatement(query))
        {
            preparedStatement.setString(1, description);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public HashMap<String, Ingredient> getAll() throws SQLException
    {
        String query = "SELECT * FROM ingredient";

        HashMap<String, Ingredient> ingredients = new HashMap<>();

         try(PreparedStatement preparedStatement
                 = connection.prepareStatement(query))
         {
             try(ResultSet resultSet
                         = preparedStatement.executeQuery())
             {
                 while (resultSet.next())
                 {

                     ingredients.put(
                             resultSet.getString("description"),
                             new Ingredient(
                                     resultSet.getString("description"),
                                     resultSet.getInt   ("stock"      ),
                                     resultSet.getInt   ("stockMin"   ),
                                     resultSet.getString("unit"       )
                             )
                     );
                 }
             }
         }

         return ingredients;
    }


}
