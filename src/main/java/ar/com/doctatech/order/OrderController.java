package ar.com.doctatech.order;

import ar.com.doctatech.customer.dialog.CustomerDialog;
import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.order.model.DetailStatus;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.order.model.Order;
import ar.com.doctatech.order.model.OrderStatus;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

public class OrderController
    extends OrderServices
        implements Initializable
{
    @FXML private ListView <String> listViewFood;
    @FXML private void onMouseClickedFoodList(MouseEvent event){
        if(event.getButton() == MouseButton.PRIMARY
                && event.getClickCount()==2)
            addItemFoodToOrder();
    }
    @FXML private TextField textfieldSearchFood, textfieldSurcharge, textfieldDiscount, textfieldTotalNO;
    @FXML private void onMouseClickedClear(){ textfieldSearchFood.clear();textfieldSearchFood.requestFocus();}
    @FXML private void onKeyReleasedExtras(){ textfieldTotalNO.setText( calculateTotal() + "" ); }

    @FXML private TextField textfieldCustomerName, textfieldWhatsapp, textfieldTelefono, textfieldStreet, textfieldApartment;
    @FXML private Button buttonSaveCustomer, buttonNewCustomer;
    @FXML private void onActionSaveCustomer() { saveCustomer(); }
    @FXML private void onActionNewCustomer() { newCustomer(); }
    @FXML private void onActionSelectCustomer() { selectCustomer(); }


    @FXML private TableView<ItemFood> tableviewNewOrder;
    @FXML private TableColumn<ItemFood, String> columnDescriptionNO;
    @FXML private TableColumn<ItemFood, Integer> columnQuantityNO;
    @FXML private TableColumn<ItemFood, Double> columnAmountNO, columnPriceNO;
    @FXML private void onActionMenuItemRemove(){ removeItemFromOrder();}
    @FXML private ComboBox<String> comboboxPayment;

    @FXML private CheckBox checkBoxDelivery, checkBoxPayNow;
    @FXML private TextArea textAreaComments;
    @FXML private void onActionTakeOrder() { takeOrder(); };


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        loadNewOrder();
        loadInProcessOrders();
        loadDeliveredOrders();
        setListenersProperties();

        textfieldSearchFood.requestFocus();
    }


    //=================== NEW ORDER =======================//
    private ObservableList<String> foodList;
    private HashMap<String, Food> foodHashMap;
    private ObservableList<ItemFood> orderItems;


    private void loadNewOrder()
    {
        try
        {
            foodHashMap = foodDAO.getAll();
            foodList = FXCollections.observableArrayList(foodHashMap.keySet());

            orderItems = FXCollections.observableArrayList();
            columnDescriptionNO.setCellValueFactory( new PropertyValueFactory<>("description"));
            columnQuantityNO.setCellValueFactory( new PropertyValueFactory<>("quantity"));
            columnPriceNO.setCellValueFactory( new PropertyValueFactory<>("priceAtTheMoment"));
            columnAmountNO.setCellValueFactory( new PropertyValueFactory<>("amount"));
            tableviewNewOrder.setItems(orderItems);

            comboboxPayment.setItems( FXCollections.observableArrayList( orderDAO.getAllPaymentTypes() ));
            comboboxPayment.getSelectionModel().select("EFECTIVO");
        }
        catch (SQLException e)
        {
            FXTool.alertException(e);
        }
    }

    private void setListenersProperties()
    {
        FXTool.setTextFieldDouble(textfieldDiscount);
        FXTool.setTextFieldDouble(textfieldSurcharge);

        //ENTER EN UN ITEM DE LA LISTA DE COMIDAS
        listViewFood.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
                addItemFoodToOrder();
            else if(event.getCode() == KeyCode.UP &&
                    listViewFood.getSelectionModel().isSelected(0))
                textfieldSearchFood.requestFocus();
        });

        //BUSCADOR
        FilteredList<String> foodFilteredList =
                new FilteredList<>(foodList, s -> true);
        textfieldSearchFood.textProperty().addListener(
                (observable, oldValue, newValue) -> foodFilteredList.setPredicate(
                        foodDescription -> foodDescription.contains(
                                newValue.trim().toUpperCase())
                ));
        textfieldSearchFood.setOnKeyReleased(event -> {
            if(event.getCode()==KeyCode.DOWN)
            {
                listViewFood.getSelectionModel().selectFirst();
                listViewFood.requestFocus();
            }

        } );
        listViewFood.setItems(foodFilteredList);

    }

    //region ==== ADD-REMOVE ITEMS ====
    private void addItemFoodToOrder()
    {
        Food foodSelected = foodHashMap.get(
                listViewFood.getSelectionModel().getSelectedItem() );
        if(foodSelected!=null)
        {
           Integer quantity = getQuantityFood(foodSelected.getName());
            if(quantity!=null)
            {
                orderItems.add( new ItemFood(foodSelected, quantity) );
                textfieldTotalNO.setText( calculateTotal() + "");
                //TODO si ya esta en la lista sumar quantity, sino agregar
            }
            textfieldSearchFood.requestFocus();
        }

    }

    private void removeItemFromOrder()
    {
        ItemFood itemFood = tableviewNewOrder.getSelectionModel().getSelectedItem();

        if(itemFood!=null)
        {
            orderItems.remove(itemFood);
            textfieldTotalNO.setText(calculateTotal() + "");
            textfieldSearchFood.requestFocus();
        }
    }

    private double calculateTotal()
    {
        double discount, surcharge;

        if(textfieldDiscount.getText().trim().isEmpty()) discount = 0.0;
        else discount = Double.parseDouble(textfieldDiscount.getText());

        if(textfieldSurcharge.getText().trim().isEmpty()) surcharge = 0.0;
        else surcharge = Double.parseDouble(textfieldSurcharge.getText());

        return getTotal(orderItems) - discount + surcharge;
    }

    //endregion

    //region  ==== CUSTOMER ====
    private void selectCustomer()
    {
        CustomerDialog customerDialog = new CustomerDialog();
        Optional<Customer> optional = customerDialog.showAndStop();
        if(optional.isPresent())
        {
            Customer customer = optional.get();
            textfieldCustomerName.setText( customer.getName() );
            textfieldWhatsapp.setText( customer.getNumberWhatsapp() );
            textfieldTelefono.setText( customer.getNumberPhone() );
            textfieldStreet.setText( customer.getStreet() );
            textfieldApartment.setText( customer.getApartment() );
            disableCustomer(true);
        }
    }

    private void newCustomer()
    {
        textfieldCustomerName.setText("");
        textfieldWhatsapp.setText("");
        textfieldTelefono.setText("-");
        textfieldApartment.setText("-");
        textfieldStreet.setText("-");
        disableCustomer(false);
        textfieldCustomerName.requestFocus();
    }

    private void saveCustomer()
    {
        String customerName = textfieldCustomerName.getText();
        String whatsappNumber = textfieldWhatsapp.getText();
        String phoneNumber = textfieldTelefono.getText();
        String street = textfieldStreet.getText();
        String apartment = textfieldApartment.getText();
        boolean passRequirements = false;

        if(checkFields(customerName, whatsappNumber))
        {
            if(checkBoxDelivery.isSelected())
            {
                if(!street.trim().isEmpty())
                   passRequirements = true;
            }
            else
                passRequirements = true;
        }

        if(passRequirements) {
            try {
                customerDAO.saveOnKeyDuplicated( new Customer(
                        0,
                        customerName,
                        phoneNumber,
                        whatsappNumber,
                        street,
                        apartment
                ));
                disableCustomer(true);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
    }

    private void disableCustomer(boolean value)
    {
        textfieldCustomerName.setDisable(value);
        textfieldWhatsapp.setDisable(value);
        textfieldTelefono.setDisable(value);
        textfieldApartment.setDisable(value);
        textfieldStreet.setDisable(value);
        buttonNewCustomer.setVisible(value);
        buttonSaveCustomer.setVisible(!value);
    }

    //endregion

    //region ==== TAKE ORDER ====

    private void takeOrder()
    {
        String customerName = textfieldCustomerName.getText().trim().toUpperCase();
        String sDiscount = textfieldDiscount.getText().trim();
        String sSurcharge = textfieldSurcharge.getText().trim();
        String sTotal = textfieldTotalNO.getText().trim();

        if(buttonSaveCustomer.isVisible())
            FXTool.alertInformation("ATENCIÓN", "Primero debe guardar el cliente");
        else if(customerName.isEmpty())
            FXTool.alertInformation("ATENCIÓN","Primero debe agregar un cliente");
        else if(tableviewNewOrder.getItems().size() == 0)
            FXTool.alertInformation("ERROR", "Debes agregar almenos un item");
        else if(sTotal.isEmpty() || sTotal.equals("0.0"))
            FXTool.alertInformation("ERROR", "El total no puede ser 0.0");

        try
        {
            //CUSTOMER
            Customer customer = customerDAO.get(customerName);

            //DETAIL STATUS -> USER
            DetailStatus detailStatus = new DetailStatus(
                UserSession.getUser().getUsername(),
                OrderStatus.ACCEPTED.toString(),
                checkBoxPayNow.isSelected(),
                textAreaComments.getText()
            );

            //ORDER -> ITEMS -> FOOD
            //      -> PAYMETHOD

            if(sDiscount.isEmpty())
                sDiscount="0.0";
            if(sSurcharge.isEmpty())
                sSurcharge="0.0";

            Order order = new Order(
                customer,
                orderItems,
                getTotal(orderItems),
                Double.parseDouble(sDiscount),
                Double.parseDouble(sSurcharge),
                Double.parseDouble(sTotal),
                comboboxPayment.getSelectionModel().getSelectedItem()
            );
            order.addDetailStatus(detailStatus);

            orderDAO.takeOrder(order);


        }
        catch (SQLException e) { FXTool.alertException(e); }

    }


    //endregion

    //================== IN PROCESS ORDER ================//
    private void loadInProcessOrders()
    {

    }


    //================== DELIVERED ORDERS =================//
    private void loadDeliveredOrders()
    {

    }
}
