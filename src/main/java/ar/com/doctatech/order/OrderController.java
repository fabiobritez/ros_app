package ar.com.doctatech.order;

import ar.com.doctatech.customer.dialog.CustomerDialog;
import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.food.model.Food;
import ar.com.doctatech.order.model.*;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

public class OrderController
    extends OrderServices
        implements Initializable
{

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Platform.runLater(()->{
            loadNewOrder();
            addListenersEventsNO();
        });

        Platform.runLater(()-> {
                   this.loadOrdersComponents();
                   addListenersEvents();
                });

        textfieldSearchFood.requestFocus();
    }

    //region=================== NEW ORDER =======================

    //region FXML NEW ORDER
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
    @FXML private void onActionTakeOrder() { takeOrder(); }
    //endregion

    private ObservableList<String> obsFood;
    private HashMap<String, Food> mapFood;
    private ObservableList<ItemFood> obsItemsNO;

    private void loadNewOrder()
    {
        columnDescriptionNO.setCellValueFactory( new PropertyValueFactory<>("description"));
        columnQuantityNO.setCellValueFactory( new PropertyValueFactory<>("quantity"));
        columnPriceNO.setCellValueFactory( new PropertyValueFactory<>("priceAtTheMoment"));
        columnAmountNO.setCellValueFactory( new PropertyValueFactory<>("amount"));

        try
        {
            mapFood = foodDAO.getAll();
            obsFood = FXCollections.observableArrayList(mapFood.keySet());
            obsItemsNO = FXCollections.observableArrayList();

            tableviewNewOrder.setItems(obsItemsNO);
            comboboxPayment.setItems( FXCollections.observableArrayList( orderDAO.getAllPaymentTypes() ));
            comboboxPayment.getSelectionModel().select("EFECTIVO");
        }
        catch (SQLException e) { FXTool.alertException(e); }
    }

    private void addListenersEventsNO()
    {
        FXTool.setTextFieldDouble(textfieldDiscount);
        FXTool.setTextFieldDouble(textfieldSurcharge);

        //ENTER AND UP KEY, ON FOOD ITEM
        listViewFood.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
                addItemFoodToOrder();
            else if(event.getCode() == KeyCode.UP &&
                    listViewFood.getSelectionModel().isSelected(0))
                textfieldSearchFood.requestFocus();
        });

        //FOOD BROWSER
        FilteredList<String> foodFilteredList =
                new FilteredList<>(obsFood, s -> true);
        textfieldSearchFood.textProperty().addListener((observable, oldValue, newValue) ->
                foodFilteredList.setPredicate(foodDescription ->
                        foodDescription.toLowerCase().contains(newValue.trim().toLowerCase())
                ));
        listViewFood.setItems(foodFilteredList);

        //DOWN KEY, ON TEXT BROWSER
        textfieldSearchFood.setOnKeyReleased(event -> {
            if(event.getCode()==KeyCode.DOWN)
            {
                listViewFood.getSelectionModel().selectFirst();
                listViewFood.requestFocus();
            }

        } );
    }

    //region ==== ADD-REMOVE ITEMS ====
    private void addItemFoodToOrder()
    {
        Food foodSelected = mapFood.get(
                listViewFood.getSelectionModel().getSelectedItem() );
        if(foodSelected!=null)
        {
           Integer quantity = getQuantityFood(foodSelected.getName());
            if(quantity!=null)
            {
                ItemFood itemToAdd =  new ItemFood(foodSelected, quantity);
                //TODO SOLUCIONAR DE UNA FORMA OPTIMA
                if(!obsItemsNO.contains(itemToAdd))
                    obsItemsNO.add(itemToAdd);
                textfieldTotalNO.setText( calculateTotal() + "");
            }
            textfieldSearchFood.requestFocus();
        }

    }

    private void removeItemFromOrder()
    {
        ItemFood itemFood = tableviewNewOrder.getSelectionModel().getSelectedItem();

        if(itemFood!=null)
        {
            obsItemsNO.remove(itemFood);
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

        return getTotal(obsItemsNO) - discount + surcharge;
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
        else
        {
            try
            {
                //CUSTOMER
                Customer customer = customerDAO.get(customerName);

                //DETAIL STATUS -> USER
                DetailStatus detailStatus = new DetailStatus(
                        UserSession.getUser().getUsername(),
                        OrderStatus.ACCEPTED.toString(),"",
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
                        obsItemsNO,
                        getTotal(obsItemsNO),
                        Double.parseDouble(sDiscount),
                        Double.parseDouble(sSurcharge),
                        Double.parseDouble(sTotal),
                        comboboxPayment.getSelectionModel().getSelectedItem()
                );
                order.addDetailStatus(detailStatus);
                orderDAO.takeOrder(order);

                FXTool.alertInformation("Operación realizada",
                        "Se aceptado la orden existosamente.");

                clearOrder();
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }


    }

    private void clearOrder()
    {
        textfieldCustomerName.setText("");
        textfieldWhatsapp.setText("");
        textfieldTelefono.setText("-");
        textfieldApartment.setText("");
        textfieldStreet.setText("");
        textfieldDiscount.setText("");
        textfieldSearchFood.setText("");
        textfieldTotalNO.setText("0.00");
        textAreaComments.setText("");

        disableCustomer(true);
        obsItemsNO.clear();
        comboboxPayment.getSelectionModel().select("EFECTIVO");
        checkBoxDelivery.setSelected(false);
        checkBoxPayNow.setSelected(false);
    }

    //endregion
    //endregion

    //region================== ALL ORDERS ================

    //region FXML ALL ORDERS
    @FXML private TableView<OrderModel> tableViewOrders;
    @FXML private TableColumn<OrderModel, String> columnDate, columnHour,
            columnCustomer, columnStatus, columnIsPaid, columnTotal;

    @FXML private TableView<ItemFood> tableViewItems;
    @FXML private TableColumn<ItemFood, String> columnQuantityItem, columnNameItem,
            columnPriceItem;

    @FXML private TextField textfieldBrowser, textfieldDeliveryPersonIP, textfieldStreetIP,
            textfieldAparmentIP;
    @FXML private TextArea textfieldCommentsIP;
    @FXML private Label textfieldDiscountIP, textfieldSurchargeIP, textfieldTotalIP;

    @FXML private ComboBox<String> comboBoxFilter;
    @FXML private CheckBox checkBoxIsPaid;

    @FXML private Button buttonSendOrder, buttonUpdateOrder,
            buttonDeliverOrder, buttonCancelOrder;
    @FXML MenuItem menuItemDeliver, menuItemSend;

    @FXML private void onTabOrderSelected(){Platform.runLater(this::updateOrdersTable);}
    @FXML private void onActionButtonUpdateOrder(){updateOrder();}
    @FXML private void onActionButtonSendOrder(){sendOrder();}
    @FXML private void onActionButtonDeliverOrder(){deliverOrder();}
    @FXML private void onActionButtonCancelOrder(){cancelOrder();}
    //TODO IMPORTANTE! = VERIFICAR COMO SOLUCIONAR DUPLICIDAD DE ITEMS ADD ITEM
    //endregion

    ObservableList<OrderModel> obsInProcessOrder;

    private void loadOrdersComponents()
    {
        columnDate.setCellValueFactory(param -> param.getValue().getDate());
        columnHour.setCellValueFactory(param -> param.getValue().getHour());
        columnCustomer.setCellValueFactory(new PropertyValueFactory<>("nameCustomer"));
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDelivery"));
        columnIsPaid.setCellValueFactory(new PropertyValueFactory<>("statusPaid"));
        columnTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        columnNameItem.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnQuantityItem.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        columnPriceItem.setCellValueFactory(new PropertyValueFactory<>("priceAtTheMoment"));

        comboBoxFilter.setItems(FXCollections.observableArrayList("EN PROCESO", "FINALIZADO"));
        comboBoxFilter.getSelectionModel().select("EN PROCESO");


        obsInProcessOrder = FXCollections.observableArrayList();

        //Order Browser
        FilteredList<OrderModel> flOrderInProcess = new FilteredList<>(
                obsInProcessOrder, s->true);

        textfieldBrowser.textProperty().addListener((observable, oldValue, newValue) ->
                flOrderInProcess.setPredicate(otm -> {
                    String textWrited = newValue.trim().toLowerCase();

                    boolean byCustomer = otm.getNameCustomer().toLowerCase().contains(textWrited);
                    boolean byIsPaid = otm.getStatusPaid().toLowerCase().contains(textWrited);
                    boolean byStatus = otm.getStatusDelivery().toLowerCase().contains(textWrited);

                    return (byCustomer || byIsPaid) || byStatus;
                })
        );
        SortedList<OrderModel> sortedList = new SortedList<>(flOrderInProcess);
        tableViewOrders.setItems(sortedList);
        sortedList.comparatorProperty().bind(tableViewOrders.comparatorProperty());

    }

    private void updateOrdersTable()
    {
        try {
            boolean isFinished = comboBoxFilter.getSelectionModel().getSelectedItem().equals("FINALIZADO");

            obsInProcessOrder.clear();
            obsInProcessOrder.addAll(orderDAO.getOrdersByStatus(isFinished));
            tableViewOrders.getSelectionModel().selectFirst();

            //HABILITACION ACTUALIZACIÓN
            buttonDeliverOrder.setVisible(!isFinished);
            buttonUpdateOrder.setVisible(!isFinished);
            buttonSendOrder.setVisible(!isFinished);
            buttonCancelOrder.setVisible(!isFinished);
            menuItemDeliver.setVisible(!isFinished);
            menuItemSend.setVisible(!isFinished);
        }
        catch (SQLException e) { FXTool.alertException(e);}
    }

    private void addListenersEvents()
    {
        tableViewOrders.getSelectionModel().selectedItemProperty().addListener(event-> selectOrder());
    }

    private void selectOrder()
    {
        OrderModel orderSelected = tableViewOrders.getSelectionModel().getSelectedItem();
        tableViewItems.getItems().clear();
        if(orderSelected!=null)
        {

            tableViewItems.getItems().addAll(orderSelected.getItems());
            textfieldDiscountIP.setText(orderSelected.getDiscount()+"");
            textfieldSurchargeIP.setText(orderSelected.getSurcharge()+"");
            textfieldTotalIP.setText(orderSelected.getTotal()+"");
            textfieldStreetIP.setText(orderSelected.getStreet());
            textfieldAparmentIP.setText(orderSelected.getApartment());
            textfieldDeliveryPersonIP.setText(orderSelected.getDeliveryPerson());
            checkBoxIsPaid.setDisable(orderSelected.isPaid());
            checkBoxIsPaid.setSelected(orderSelected.isPaid());
            textfieldCommentsIP.setText(orderSelected.getComments());
        }
        else
        {
            textfieldDiscountIP.setText("");
            textfieldSurchargeIP.setText("");
            textfieldTotalIP.setText("");
            textfieldStreetIP.setText("");
            textfieldAparmentIP.setText("");
            textfieldDeliveryPersonIP.setText("");
            checkBoxIsPaid.setSelected(false);
            textfieldCommentsIP.setText("");
        }
    }

    private void updateOrder()
    {
        OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();

        if(orderModel!=null)
        {
            DetailStatus detailStatus = new DetailStatus(
                    UserSession.getUser().getUsername(),
                    orderModel.getStatusDelivery(),
                    textfieldDeliveryPersonIP.getText(),
                    checkBoxIsPaid.isSelected(),
                    textfieldCommentsIP.getText()
            );
            try
            {
                orderDAO.updateOrderStatus(orderModel.getOrderID(), detailStatus);
                Platform.runLater(()->{
                    this.updateOrdersTable();
                    tableViewOrders.getSelectionModel().select(orderModel);
                });
                FXTool.alertInformation("Orden actualizada!", "");

            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
    }

    private void sendOrder()
    {
        //verificar el combobox finalizado
        String deliveryMan = textfieldDeliveryPersonIP.getText().trim().toUpperCase();
        OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();

        if(!deliveryMan.isEmpty() && orderModel!=null)
        {
           DetailStatus detailStatus = new DetailStatus(
                   UserSession.getUser().getUsername(),
                   OrderStatus.ON_THE_WAY.toString(),
                   deliveryMan,
                   checkBoxIsPaid.isSelected(),
                   textfieldCommentsIP.getText()
           );
            try
            {
                orderDAO.updateOrderStatus(orderModel.getOrderID(), detailStatus);
                Platform.runLater(()->{
                    this.updateOrdersTable();
                    tableViewOrders.getSelectionModel().select(orderModel);
                });
                FXTool.alertInformation("Orden en camino!", "Repartidor: " + deliveryMan +
                           "\nDirección: " + orderModel.getStreet());

            }
            catch (SQLException e) { FXTool.alertException(e); }
        }else
        {
            FXTool.alertInformation("ERROR", "Debe indicar quien es el repartidor");
        }
    }

    private void deliverOrder()
    {
        OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();

        if(orderModel!=null)
        {
            DetailStatus detailStatus = new DetailStatus(
                    UserSession.getUser().getUsername(),
                    OrderStatus.DELIVERED.toString(),
                    orderModel.getDeliveryPerson(),
                    checkBoxIsPaid.isSelected(),
                    textfieldCommentsIP.getText()
            );
            try
            {
                orderDAO.updateOrderStatus(orderModel.getOrderID(), detailStatus);
                FXTool.alertInformation("Orden entregada!", "");
                this.updateOrdersTable();

            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }
    }

    private void cancelOrder()
    {
             DetailStatus detailStatus = new DetailStatus(
                UserSession.getUser().getUsername(),//change
                OrderStatus.CANCELED.toString(),textfieldDeliveryPersonIP.getText(),//change
                checkBoxIsPaid.isSelected(),
                textfieldCommentsIP.getText()
        );

        try
        {
            orderDAO.updateOrderStatus(
                    tableViewOrders.getSelectionModel().getSelectedItem().getOrderID(),
                    detailStatus);
            Platform.runLater(this::updateOrdersTable);
            FXTool.alertInformation("Operación realizada",
                    "Orden cancelada exitosamente!");
        }
        catch (SQLException e) { FXTool.alertException(e); }

    }

    //endregion
}
