package ar.com.doctatech.order;

import ar.com.doctatech.customer.model.CustomerDAO;
import ar.com.doctatech.customer.model.CustomerDAOMySQL;
import ar.com.doctatech.food.model.FoodDAO;
import ar.com.doctatech.food.model.FoodDAOMySQL;
import ar.com.doctatech.order.dao.OrderDAO;
import ar.com.doctatech.order.dao.OrderDAOMySQL;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Font;

import java.util.List;
import java.util.Optional;

public class OrderServices {

    FoodDAO foodDAO;
    OrderDAO orderDAO;
    CustomerDAO customerDAO;
    {
        foodDAO = new FoodDAOMySQL();
        orderDAO = new OrderDAOMySQL();
        customerDAO = new CustomerDAOMySQL();
    }

    //region ==== ADD-REMOVE ITEMS ====
    protected Integer getQuantityFood(String nameFood)
    {
        TextInputDialog textInputDialog = new TextInputDialog("1");
        textInputDialog.setTitle("Agregar");
        textInputDialog.setHeaderText(nameFood);

        textInputDialog.getEditor().setPrefSize(45,35);
        textInputDialog.getEditor().setAlignment(Pos.CENTER_RIGHT);
        textInputDialog.getEditor().setFont(Font.font(30));
        textInputDialog.setGraphic(null);
        textInputDialog.setResizable(true);

        FXTool.setTextFieldInteger( textInputDialog.getEditor() );
        Optional<String> result = textInputDialog.showAndWait();

        if(result.isPresent())
        {
            if(result.get().equals(""))
                return 1;
            return Integer.parseInt(result.get());
        }else
            return null;
    }

    protected double getTotal(List<ItemFood> list)
    {
        double total = 0.0;
        for (ItemFood itemFood : list)
            total += itemFood.getAmount();
        return total;
    }

    //endregion


}
