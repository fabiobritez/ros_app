package ar.com.doctatech.order.dao;


import ar.com.doctatech.food.model.ItemRecipe;
import ar.com.doctatech.order.model.*;
import ar.com.doctatech.shared.db.DatabaseConnection;
import ar.com.doctatech.shared.utilities.FXTool;

import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderDAOMySQL implements OrderDAO {
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
            throws SQLException {
        String query = "SELECT method FROM payMethod";
        List<String> payments = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(query)) {
            try (ResultSet resultSet =
                         preparedStatement.executeQuery()) {
                while (resultSet.next()) payments.add(resultSet.getString("method"));
            }
        }
        return payments;
    }

    @Override
    public void takeOrder(Order order) throws SQLException {
        connection.setAutoCommit(false);
        String insertOrder =
                "INSERT INTO `order` (customer_customerID, subtotal, " +
                        "discount, surcharge, payMethod_method, exist) " +
                        "VALUES (?, ?, ?, ?, ?, true)";

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
        try {
            //INSERT ORDER
            try (PreparedStatement psOrder = connection.
                    prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getCustomer().getCustomerID());
                psOrder.setDouble(2, order.getSubtotal());
                psOrder.setDouble(3, order.getDiscount());
                psOrder.setDouble(4, order.getSurcharge());
                psOrder.setString(5, order.getPayMethod());
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    while (rs.next()) order.setOrderID(rs.getInt(1));
                }
            }

            //INSERT ITEMS
            try (PreparedStatement psItems =
                         connection.prepareStatement(insertItem)) {
                psItems.setInt(1, order.getOrderID());
                for (ItemFood itemFood : order.getItems()) {
                    psItems.setInt(2, itemFood.getFood().getFoodID());
                    psItems.setInt(3, itemFood.getQuantity());
                    psItems.setDouble(4, itemFood.getCostAtTheMoment());
                    psItems.setDouble(5, itemFood.getPriceAtTheMoment());

                    psItems.executeUpdate();
                }
            }

            //INSERT STATUS
            try (PreparedStatement psDetail = connection.
                    prepareStatement(insertDetail, Statement.RETURN_GENERATED_KEYS)) {
                DetailStatus status = order.getOrderStatusList().getLast();
                psDetail.setInt(1, order.getOrderID());
                psDetail.setString(2, status.getUserUsername());
                psDetail.setString(3, status.getStatus());
                psDetail.setBoolean(4, status.isPaid());
                psDetail.setString(5, status.getComments());
                psDetail.executeUpdate();

                try (ResultSet rs = psDetail.getGeneratedKeys()) {
                    while (rs.next()) order.getOrderStatusList().
                            getLast().setStatusID(rs.getInt(1));
                }
            }

            //REMOVE STOCK
            try (PreparedStatement psStock =
                         connection.prepareStatement(removeStock)) {
                for (ItemFood itemFood : order.getItems()) {
                    for (ItemRecipe itemRecipe : itemFood.getFood().getRecipe()) {
                        psStock.setInt(1, itemFood.getQuantity() * itemRecipe.getQuantity());
                        psStock.setString(2, itemRecipe.getDescription());
                        psStock.executeUpdate();
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void updateOrderStatus(Integer orderID, DetailStatus detailStatus)
            throws SQLException {
        String updateQuery = "INSERT INTO detailStatus (order_orderID, " +
                "user_username,deliveryPerson,status,isPaid, comments) VALUES (? ,? ,? ,? , ?, ?)";
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(updateQuery)) {
            preparedStatement.setInt(1, orderID);
            preparedStatement.setString(2, detailStatus.getUserUsername());
            preparedStatement.setString(3, detailStatus.getDeliveryPerson());
            preparedStatement.setString(4, detailStatus.getStatus());
            preparedStatement.setBoolean(5, detailStatus.isPaid());
            preparedStatement.setString(6, detailStatus.getComments());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Set<OrderModel> getOrdersByStatus(boolean isFinished)
            throws SQLException
    {
        String query =
         "SELECT dS.dateUpdate, o.orderID, c.name, dS.status,  "+
         "o.subtotal, o.discount, o.surcharge, o.payMethod_method, "+
         "c.numberPhone, c.numberWhatsapp, c.street, c.apartment, "+
         "f.name, i.quantity, i.priceAtTheMoment, dS.deliveryPerson, "+
         "dS.isPaid, dS.comments, u.username "+
       "FROM `order` AS o "+
       "LEFT OUTER JOIN customer c      ON o.customer_customerID = c.customerID "+
       "LEFT OUTER JOIN detailStatus dS ON o.orderID = dS.order_orderID "+
       "LEFT OUTER JOIN user u          ON dS.user_username = u.username "+
       "LEFT OUTER JOIN itemFood i      ON o.orderID = i.order_orderID "+
       "LEFT OUTER JOIN food f          ON i.food_foodID = f.foodID "+
       "WHERE  dS.dateUpdate > DATE_SUB(NOW(), INTERVAL 3 DAY) "+
       "ORDER BY dS.dateUpdate DESC";

        HashMap<Integer, OrderModel> mapOrder = new HashMap<>();

        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(query)) {
            try (ResultSet rs =
                         preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Integer orderID = rs.getInt("orderID");
                    if (!mapOrder.containsKey(orderID)) {
                        OrderModel orderModel = new OrderModel(
                                orderID,
                                rs.getDouble   ("o.subtotal"),
                                rs.getDouble   ("o.discount"),
                                rs.getDouble   ("o.surcharge"),
                                rs.getString   ("o.payMethod_method"),
                                rs.getTimestamp("dS.dateUpdate").toLocalDateTime(),

                                rs.getString("c.name"),
                                rs.getString("c.numberPhone"),
                                rs.getString("c.numberWhatsapp"),
                                rs.getString("c.street"),
                                rs.getString("c.apartment"),

                                rs.getString ("ds.status"),
                                rs.getString ("dS.deliveryPerson"),
                                rs.getBoolean("dS.isPaid"),
                                rs.getString ("dS.comments"),
                                rs.getString ("u.username")
                        );

                        mapOrder.put(orderID, orderModel);
                    }
                    if (mapOrder.containsKey(orderID)) {
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

        String isFinishCondition  = isFinished ? "ENTREGADO" : "EN CAMINO";
        String isFinishCondition2 = isFinished ? "CANCELADO" : "ACEPTADO";

        return mapOrder.values().stream().filter(order ->
            order.getStatusDelivery().equals(isFinishCondition) ||
                 order.getStatusDelivery().equals(isFinishCondition2))
                    .collect(Collectors.toSet());
    }

    @Override
    public Set<OrderModel> getOrders(Date start, Date end,
                       Boolean isPaid, String isDelivered) throws SQLException
    {
        StringBuilder query = new StringBuilder();
        query.append(
                "SELECT orderID, subtotal, discount, surcharge, payMethod_method, dateUpdate, " +
                        "c.name,numberPhone, numberWhatsapp, street, apartment, " +
                        "f.name, quantity, priceAtTheMoment, dS.deliveryPerson, " +
                        "dS.status, isPaid, comments, u.username FROM `order` " +
                        "LEFT OUTER JOIN customer c on `order`.customer_customerID = c.customerID " +
                        "LEFT OUTER JOIN detailStatus dS on `order`.orderID = dS.order_orderID " +
                        "LEFT OUTER JOIN user u on dS.user_username = u.username " +
                        "LEFT OUTER JOIN itemFood i on `order`.orderID = i.order_orderID " +
                        "LEFT OUTER JOIN food f on i.food_foodID = f.foodID ");

        query.append(" WHERE dateUpdate>=TIMESTAMP(?,'00:00:00') AND dateUpdate<=TIMESTAMP(?,'23:59:59')");
        if (isPaid != null)
            query.append(" AND isPaid = ").append(isPaid);
        if (!isDelivered.equals("TODOS"))
            query.append(" AND dS.status = '").append(isDelivered).append("'");
        else
            query.append(" AND (dS.status = 'ENTREGADO' OR dS.status='CANCELADO')");
        query.append(" ORDER BY dateUpdate DESC ");

        HashMap<Integer, OrderModel> mapOrder = new HashMap<>();

        try (PreparedStatement ps =
                     connection.prepareStatement(query.toString())) {
            ps.setDate(1, start);
            ps.setDate(2, end);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer orderID = rs.getInt("orderID");
                    if (!mapOrder.containsKey(orderID)) {
                        OrderModel orderModel = new OrderModel(
                                orderID,
                                rs.getDouble("subtotal"),
                                rs.getDouble("discount"),
                                rs.getDouble("surcharge"),
                                rs.getString("payMethod_method"),
                                rs.getDate("dateUpdate").toLocalDate().atStartOfDay(),
                                rs.getString("c.name"),
                                rs.getString("numberPhone"),
                                rs.getString("numberWhatsapp"),
                                rs.getString("street"),
                                rs.getString("apartment"),

                                rs.getString("ds.status"),
                                rs.getString("dS.deliveryPerson"),
                                rs.getBoolean("isPaid"),
                                rs.getString("comments"),
                                rs.getString("u.username")
                        );

                        mapOrder.put(orderID, orderModel);
                    }
                    if (mapOrder.containsKey(orderID)) {
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
        return new HashSet<>(mapOrder.values());
    }

    @Override
    public void restoreStock(Integer orderID) throws SQLException {
        String restoreStock =
                "UPDATE ingredient " +
                "LEFT JOIN recipe r        ON ingredient.description = r.ingredient_description " +
                "LEFT JOIN itemFood i      ON r.food_foodID = i.food_foodID " +
                "LEFT OUTER JOIN `order` o ON o.orderID = i.order_orderID "+
                "SET stock = stock + (r.quantity*i.quantity) WHERE orderID=?";

        try (PreparedStatement psStock =
                     connection.prepareStatement(restoreStock)) {
                    psStock.setInt(1, orderID);
                    psStock.executeUpdate();
        }
    }

    @Override
    public Set<OrderModel> getOrdersByCustomer(String ID)
            throws SQLException
    {
            String query =
                    "SELECT orderID, subtotal, discount, surcharge, payMethod_method, " +
                    "dateUpdate, c.name,numberPhone, numberWhatsapp, street, apartment, " +
                    "f.name, quantity, priceAtTheMoment, dS.deliveryPerson, " +
                    "dS.status, isPaid, comments, u.username " +
                    "FROM `order` AS o " +
                    "LEFT OUTER JOIN customer c      ON o.customer_customerID = c.customerID " +
                    "LEFT OUTER JOIN detailStatus dS ON o.orderID = dS.order_orderID " +
                    "LEFT OUTER JOIN user u          ON dS.user_username = u.username " +
                    "LEFT OUTER JOIN itemFood i      ON o.orderID = i.order_orderID " +
                    "LEFT OUTER JOIN food f          ON i.food_foodID = f.foodID " +
                    "WHERE dS.status = 'ENTREGADO'  AND o.exist " +
                     "AND c.customerID=? AND dS.isPaid=false " +
                    "ORDER BY dateUpdate DESC ";
            //TODO ERROR GRAVE: CAUNDO PONGO AND  , SOLO ME APARECEN LOS FALSOS Y SI ESTA PAGADO NO APARECE
            HashMap<Integer, OrderModel> mapOrder = new HashMap<>();

            try (PreparedStatement ps =
                         connection.prepareStatement(query)) {
                ps.setString(1, ID);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer orderID = rs.getInt("orderID");
                        if (!mapOrder.containsKey(orderID)) {
                            OrderModel orderModel = new OrderModel(
                                    orderID,
                                    rs.getDouble("subtotal"),
                                    rs.getDouble("discount"),
                                    rs.getDouble("surcharge"),
                                    rs.getString("payMethod_method"),
                                    rs.getDate("dateUpdate").toLocalDate().atStartOfDay(),
                                    rs.getString("c.name"),
                                    rs.getString("numberPhone"),
                                    rs.getString("numberWhatsapp"),
                                    rs.getString("street"),
                                    rs.getString("apartment"),

                                    rs.getString("ds.status"),
                                    rs.getString("dS.deliveryPerson"),
                                    rs.getBoolean("isPaid"),
                                    rs.getString("comments"),
                                    rs.getString("u.username")
                            );

                            mapOrder.put(orderID, orderModel);
                        }
                        if (mapOrder.containsKey(orderID)) {
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
            return new HashSet<>(mapOrder.values());
    }
}