package ar.com.doctatech.order;

import ar.com.doctatech.customer.dialogs.CustomerDialog;
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
    }

    //region=================== NEW ORDER =======================

    //region FXML NEW ORDER
    @FXML private ListView <String> listViewFood;
    @FXML private void onMouseClickedFoodList(MouseEvent event){
        if(event.getButton() == MouseButton.PRIMARY &&
                event.getClickCount()==2)  addItemFoodToOrder();
    }
    @FXML private TextField textfieldSearchFood, textfieldSurcharge, textfieldDiscount, textfieldTotalNO;
    @FXML private void onMouseClickedClear(){ textfieldSearchFood.clear();textfieldSearchFood.requestFocus();}
    @FXML private void onKeyReleasedExtras(){ textfieldTotalNO.setText( String.format(Locale.US, "%.2f",calculateTotal()) ); }

    @FXML private TextField textfieldCustomerName, textfieldWhatsapp, textfieldTelefono, textfieldStreet, textfieldApartment;
    @FXML private Button buttonSaveCustomer, buttonNewCustomer;
    @FXML private void onActionSaveCustomer()   { saveCustomer();   }
    @FXML private void onActionNewCustomer()    { newCustomer();    }
    @FXML private void onActionSelectCustomer() { selectCustomer(); }
    @FXML private void onActionClearOrder()     { clearOrder();     }


    @FXML private TableView<ItemFood> tableviewNewOrder;
    @FXML private TableColumn<ItemFood, String> columnDescriptionNO;
    @FXML private TableColumn<ItemFood, Number> columnQuantityNO;
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
    private FilteredList<String> flFood;
    private void loadNewOrder()
    {
        columnDescriptionNO.prefWidthProperty().bind(tableviewNewOrder.widthProperty().divide(2));
        columnQuantityNO.prefWidthProperty().bind(tableviewNewOrder.widthProperty().divide(6));
        columnPriceNO.prefWidthProperty().bind(tableviewNewOrder.widthProperty().divide(6));
        columnAmountNO.prefWidthProperty().bind(tableviewNewOrder.widthProperty().divide(6));

        columnDescriptionNO.setCellValueFactory( new PropertyValueFactory<>("description"));
        columnQuantityNO.setCellValueFactory(value -> value.getValue().quantityProperty());
        columnPriceNO.setCellValueFactory( new PropertyValueFactory<>("priceAtTheMoment"));
        columnAmountNO.setCellValueFactory( new PropertyValueFactory<>("amount"));

        try
        {
            mapFood = foodDAO.getAll();
            obsFood = FXCollections.observableArrayList(mapFood.keySet());
            flFood  = new FilteredList<>(obsFood, s -> true);

            obsItemsNO = FXCollections.observableArrayList();

            tableviewNewOrder.setItems(obsItemsNO);
            listViewFood.setItems(flFood);

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

        textfieldSearchFood.textProperty().addListener((obs, old, value) ->
                flFood.setPredicate(description ->
                        description.toLowerCase().contains(value.trim().toLowerCase()) ));

        //DOWN KEY, ON TEXT BROWSER
        textfieldSearchFood.setOnKeyReleased(event -> {
            if(event.getCode()==KeyCode.DOWN) {
                listViewFood.getSelectionModel().selectFirst();
                listViewFood.requestFocus();
            }} );

        textfieldSearchFood.requestFocus();
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

                if(!obsItemsNO.contains(itemToAdd))
                    obsItemsNO.add(itemToAdd);
                else
                    obsItemsNO.forEach(itemFood -> {
                        if (itemFood.equals(itemToAdd))
                            itemFood.addQuantity(quantity);
                    });
                obsItemsNO.removeIf(itemFood -> itemFood.getQuantity()<=0);

                textfieldTotalNO.setText( String.format(Locale.US, "%.2f", calculateTotal()) );
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
            textfieldTotalNO.setText(String.format(Locale.US, "%.2f",calculateTotal()) );
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

        return getSubtotal(obsItemsNO) - discount + surcharge;
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
                        getSubtotal(obsItemsNO),
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
        textfieldSurcharge.setText("");
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

    @FXML private Button buttonSendOrder, buttonPayOrder,
            buttonDeliverOrder, buttonCancelOrder;
    @FXML MenuItem menuItemDeliver, menuItemSend, menuItemPay;

    @FXML private void onTabOrderSelected(){Platform.runLater(this::updateOrdersTable);}
    @FXML private void onActionButtonUpdateOrder(){updateOrder();}
    @FXML private void onActionButtonSendOrder(){sendOrder();}
    @FXML private void onActionButtonDeliverOrder(){deliverOrder();}
    @FXML private void onActionButtonCancelOrder(){cancelOrder();}
    //endregion

    ObservableList<OrderModel> obsInProcessOrder;

    private void loadOrdersComponents()
    {
        columnDate.prefWidthProperty().bind(tableViewOrders.widthProperty()     .divide(5.55)); //18%
        columnHour.prefWidthProperty().bind(tableViewOrders.widthProperty()     .divide(11.11));// 9%
        columnCustomer.prefWidthProperty().bind(tableViewOrders.widthProperty() .divide(4    ));//25%
        columnStatus.prefWidthProperty().bind(tableViewOrders.widthProperty()   .divide(5.55 ));//18%
        columnIsPaid.prefWidthProperty().bind(tableViewOrders.widthProperty()   .divide(7.5  ));//13.33%
        columnTotal.prefWidthProperty().bind(tableViewOrders.widthProperty()    .divide(6    ));//16.66%

        columnDate.setCellValueFactory(param -> param.getValue().getDate());
        columnHour.setCellValueFactory(param -> param.getValue().getHour());
        columnCustomer.setCellValueFactory(new PropertyValueFactory<>("nameCustomer"));
        columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDelivery"));
        columnIsPaid.setCellValueFactory(new PropertyValueFactory<>("statusPaid"));
        columnTotal.setCellValueFactory(new PropertyValueFactory<>("total"));


        columnNameItem.prefWidthProperty().bind(tableViewItems.widthProperty().divide(1.66)); //60%
        columnQuantityItem.prefWidthProperty().bind(tableViewItems.widthProperty().divide(6));
        columnPriceItem.prefWidthProperty().bind(tableViewItems.widthProperty().divide(4.5)); //22

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
        tableViewOrders.getSelectionModel().selectFirst();
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
            buttonPayOrder.setVisible(!isFinished);
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
            textfieldTotalIP.setText(String.format(Locale.US, "%.2f",orderSelected.getTotal()) );
            textfieldStreetIP.setText(orderSelected.getStreet());
            textfieldAparmentIP.setText(orderSelected.getApartment());
            textfieldDeliveryPersonIP.setText(orderSelected.getDeliveryPerson());

            checkBoxIsPaid.setDisable(orderSelected.isPaid());
            checkBoxIsPaid.setSelected(orderSelected.isPaid());

            buttonPayOrder.setDisable(orderSelected.isPaid());
            menuItemPay.setDisable(orderSelected.isPaid());

            boolean isDisableDeliver = orderSelected.getStatusDelivery().
                    equals(OrderStatus.DELIVERED.toString());
            buttonDeliverOrder.setDisable(isDisableDeliver);
            menuItemDeliver.setDisable(isDisableDeliver);

            boolean isDisableSend = (
                    orderSelected.getNameCustomer().equals("CLIENTE FINAL") ||
                    orderSelected.getStatusDelivery().equals(OrderStatus.DELIVERED.toString()) ||
                    orderSelected.getStatusDelivery().equals(OrderStatus.ON_THE_WAY.toString()) );
            buttonSendOrder.setDisable(isDisableSend);
            menuItemSend.setDisable(isDisableSend);

            buttonCancelOrder.setDisable(false);
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
            buttonDeliverOrder.setDisable(true);
            buttonSendOrder.setDisable(true);
            buttonPayOrder.setDisable(true);
            buttonCancelOrder.setDisable(true);
        }
        if(comboBoxFilter.getSelectionModel().getSelectedItem().equals("FINALIZADO"))
        {
            checkBoxIsPaid.setDisable(true);

        }
    }

    private void updateOrder()
    {
        OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();
        checkBoxIsPaid.setSelected(true);
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
            }
            catch (SQLException e) { FXTool.alertException(e);  }
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
            textfieldDeliveryPersonIP.requestFocus();
        }
    }

    private void deliverOrder()
    {
        OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();

        if(orderModel!=null)
        {
            if(!orderModel.getNameCustomer().equals("CLIENTE FINAL") || orderModel.isPaid())
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
            else
                FXTool.alertInformation("EL CLIENTE FINAL NO PUEDE DEBER",
                        "Ya que no se puede identificar quien \nes el que debe pagar");

        }
    }

    private void cancelOrder()

            //TODO VERIFICAR QUE SEA ADMINISTRADOR Y PEDIR PASSWORD
            //TODO SUMAR STOCK NUEVAMENTE
    {
        if(hasPermissionToCancel())
        {
            DetailStatus detailStatus = new DetailStatus(
                    UserSession.getUser().getUsername(),//change
                    OrderStatus.CANCELED.toString(),textfieldDeliveryPersonIP.getText(),//change
                    checkBoxIsPaid.isSelected(),
                    textfieldCommentsIP.getText()
            );

            try
            {
                OrderModel orderModel = tableViewOrders.getSelectionModel().getSelectedItem();
                orderDAO.updateOrderStatus(
                    orderModel.getOrderID(),
                    detailStatus);
                orderDAO.restoreStock(orderModel.getOrderID());
                Platform.runLater(this::updateOrdersTable);
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }

    }


    //endregion
}
