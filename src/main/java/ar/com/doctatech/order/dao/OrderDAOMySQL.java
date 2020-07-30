package ar.com.doctatech.order.dao;


import ar.com.doctatech.food.model.ItemRecipe;
import ar.com.doctatech.order.model.*;
import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class OrderDAOMySQL implements OrderDAO
{
    Connection connection;
    {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (IOException | SQLException e) {
            FXTool.alertException(e);
        }
    }

    @Override
    public List<String> getAllPaymentTypes()
            throws SQLException
    {
        String query = "SELECT * FROM payMethod";
        List<String> allPaymentTypes = new ArrayList<>();
        try(PreparedStatement preparedStatement =
                 connection.prepareStatement(query) )
        {
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next())
                {
                    allPaymentTypes.add(resultSet.getString("method"));
                }
            }
        }
        return allPaymentTypes;
    }

    @Override
    public void takeOrder(Order order) throws SQLException
    {
        connection.setAutoCommit(false);
        String insertOrder =
                "INSERT INTO `order` (customer_customerID, subtotal, " +
                "discount, surcharge, payMethod_method, exist) " +
                "VALUES (?, ?, ?, ?, ?, true)" ;

        String insertItem =
                "INSERT INTO itemFood (order_orderID, food_foodID, " +
                     "quantity, costAtTheMoment, priceAtTheMoment) " +
                "VALUES (?, ?, ?, ?, ?)";


        String insertDetail =
                "INSERT INTO detailStatus (order_orderID, user_username, " +
                      "status, isPaid, comments) " +
                "VALUES (?, ?, ?, ?, ?)";
        String removeStock =
                "UPDATE ingredient SET stock = stock - ? WHERE description = ?";
        try
        {
            //INSERT ORDER
            try(PreparedStatement psOrder = connection.
                    prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS))
            {
                psOrder.setInt(1, order.getCustomer().getCustomerID());
                psOrder.setDouble(2, order.getSubtotal());
                psOrder.setDouble(3, order.getDiscount());
                psOrder.setDouble(4, order.getSurcharge());
                psOrder.setString(5, order.getPayMethod());
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys())
                {
                    while (rs.next()) order.setOrderID(rs.getInt(1));
                }
            }

            //INSERT ITEMS
            try(PreparedStatement psItems =
                        connection.prepareStatement(insertItem))
            {
                psItems.setInt(1, order.getOrderID());
                for (ItemFood itemFood : order.getItems())
                {
                    psItems.setInt(2, itemFood.getFood().getFoodID());
                    psItems.setInt(3, itemFood.getQuantity());
                    psItems.setDouble(4, itemFood.getCostAtTheMoment());
                    psItems.setDouble(5, itemFood.getPriceAtTheMoment());

                    psItems.executeUpdate();
                }
            }

            //INSERT STATUS
            try (PreparedStatement psDetail = connection.
                    prepareStatement(insertDetail, Statement.RETURN_GENERATED_KEYS))
            {
                DetailStatus status = order.getOrderStatusList().getLast();
                psDetail.setInt(1, order.getOrderID());
                psDetail.setString(2, status.getUserUsername() );
                psDetail.setString(3, status.getStatus() );
                psDetail.setBoolean(4, status.isPaid() );
                psDetail.setString(5, status.getComments() );
                psDetail.executeUpdate();

                try (ResultSet rs = psDetail.getGeneratedKeys())
                {
                    while (rs.next()) order.getOrderStatusList().
                            getLast().setStatusID(rs.getInt(1));
                }
            }

            //REMOVE STOCK
            try (PreparedStatement psStock =
                    connection.prepareStatement(removeStock))
            {
                for (ItemFood itemFood: order.getItems())
                {
                    for (ItemRecipe itemRecipe: itemFood.getFood().getRecipe())
                    {
                        psStock.setInt(1, itemFood.getQuantity()*itemRecipe.getQuantity());
                        psStock.setString(2, itemRecipe.getDescription());
                        psStock.executeUpdate();
                    }
                }
            }

            connection.commit();
        }
        catch (SQLException e)
        {
            connection.rollback();
            throw e;
        }
        finally { connection.setAutoCommit(true); }
    }

    @Override
    public void updateOrderStatus(Integer orderID, DetailStatus detailStatus)
            throws SQLException
    {
        String updateQuery = "INSERT INTO detailStatus (order_orderID, " +
                "user_username, status, comments) VALUES (? ,?, ?, ?)";
        try (PreparedStatement preparedStatement =
                connection.prepareStatement(updateQuery))
        {
            preparedStatement.setInt(1, orderID);
            preparedStatement.setString(2, detailStatus.getUserUsername());
            preparedStatement.setString(3, detailStatus.getStatus());
            preparedStatement.setString(4, detailStatus.getComments());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Set<OrderModel> getOrdersByStatus(boolean isFinished)
            throws SQLException
    {

        String query =
                "SELECT orderID, subtotal, discount, surcharge, dateUpdate, " +
                "c.name,numberPhone, numberWhatsapp, street, apartment, " +
                "f.name, quantity, priceAtTheMoment, dS.deliveryPerson, " +
                "dS.status, isPaid, comments FROM `order` " +
                "LEFT OUTER JOIN customer c on `order`.customer_customerID = c.customerID " +
                "LEFT OUTER JOIN detailStatus dS on `order`.orderID = dS.order_orderID " +
                "LEFT OUTER JOIN itemFood i on `order`.orderID = i.order_orderID " +
                "LEFT OUTER JOIN food f on i.food_foodID = f.foodID " +
                " WHERE date(dateUpdate)=CURDATE() ORDER BY dateUpdate DESC  ";

        HashMap<Integer, OrderModel> mapOrder = new HashMap<>();

        try(PreparedStatement preparedStatement =
                    connection.prepareStatement(query))
        {
            try(ResultSet rs =
                    preparedStatement.executeQuery())
            {
                while (rs.next())
                {
                    Integer orderID = rs.getInt("orderID");
                        if(!mapOrder.containsKey(orderID))
                        {
                            OrderModel orderModel = new OrderModel(
                                    orderID,
                                    rs.getDouble("subtotal"),
                                    rs.getDouble("discount"),
                                    rs.getDouble("surcharge"),
                                    rs.getTimestamp("dateUpdate").toLocalDateTime(),

                                    rs.getString("c.name"),
                                    rs.getString("numberPhone"),
                                    rs.getString("numberWhatsapp"),
                                    rs.getString("street"),
                                    rs.getString("apartment"),

                                    rs.getString("ds.status"),
                                    rs.getString("dS.deliveryPerson"),
                                    rs.getBoolean("isPaid"),
                                    rs.getString("comments")
                            );

                            mapOrder.put(orderID, orderModel);
                        }
                        if(mapOrder.containsKey(orderID)){
                            ItemFood itemFood = new ItemFood(
                                            rs.getString("f.name"),
                                            rs.getInt("quantity"),
                                            rs.getDouble("priceAtTheMoment")
                                    );

                            mapOrder.get(orderID).addItemFood(itemFood);
                        }
                }
            }
        }

        String criterio1 = isFinished ? "ENTREGADO" : "EN CAMINO";
        String criterio2 = isFinished ? "CANCELADO" : "ACEPTADO";

        return mapOrder.values().stream().filter(ovm ->
                ovm.getStatusDelivery().equals(criterio1) ||
                ovm.getStatusDelivery().equals(criterio2)
        ).collect(Collectors.toSet());
    }
}
