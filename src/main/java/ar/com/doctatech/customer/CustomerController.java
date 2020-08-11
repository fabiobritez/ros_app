package ar.com.doctatech.customer;

import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.order.model.OrderModel;
import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.user.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

public class CustomerController
        extends CustomerServices implements Initializable
{
    @FXML private ListView<String> listViewCustomers;
    @FXML private TextField textfieldSearchEngine;

    @FXML private TextField textfieldID, textfieldName, textfieldNumberPhone,
            textfieldWhatsapp, textfieldAddress, textfieldApartment;
    @FXML private Button buttonEditCustomer,
            buttonUpdateCustomer, buttonDeleteCustomer, buttonSaveCustomer;
    @FXML private Label labelTotalSales,labelTotalDebt,labelLastSale;
    @FXML private Tab tabReport;

    @FXML private void onKeyReleasedTextfieldBrowser(KeyEvent event)
    {
        if(event.getCode() == KeyCode.DOWN)
        {
            listViewCustomers.requestFocus();
            listViewCustomers.getSelectionModel().selectFirst();
        }
    }
    @FXML private void onActionClear()
    {
        textfieldSearchEngine.setText("");
        textfieldSearchEngine.requestFocus();
    }

    @FXML private void onClickButtonNew(){setProcess(PROCESS.ADDING);}
    @FXML private void onClickButtonEdit(){setProcess(PROCESS.EDITING);}
    @FXML private void onClickButtonUpdate(){updateCustomer();}
    @FXML private void onClickButtonSave(){saveCustomer();}
    @FXML private void onClickButtonDelete(){deleteCustomer();}


    @FXML private TableView<OrderModel> tableViewOrders;
    @FXML private TableColumn<OrderModel, String> columnID, columnDateUpdate, columnIsPaid, columnTotal;
    @FXML private TableView<ItemFood> tableViewItems;
    @FXML private TableColumn<ItemFood, String> columnItem, columnQuantity, columnPrice;
    @FXML private TextField textfieldUser, textfieldDate,textfieldDebt;
    @FXML private Label textfieldDiscount, textfieldSurcharge, textfieldTotal;

    @FXML private void onActionPayAll(){payAllBill();};
    @FXML private void onActionExportTo(){exportToExcel();}

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadBillCustomer();
        addListenersEventsBill();
        loadCustomers();
        addListenersEvents();
    }

    HashMap<String, Customer> mapCustomers;
    ObservableList<String> obsCustomers;
    FilteredList<String> flCustomers;

    private void loadCustomers()
    {
        try
        {
            mapCustomers = customerDAO.getAll();
            obsCustomers = FXCollections.observableArrayList(mapCustomers.keySet());
            flCustomers = new FilteredList<>(obsCustomers, s->true);

            listViewCustomers.setItems(flCustomers);
        }
        catch (SQLException e) { FXTool.alertException(e); }

    }

    private void addListenersEvents()
    {
        //BUSCADOR
       textfieldSearchEngine.textProperty().addListener(
                (obs, old, value) -> flCustomers.setPredicate(cus ->
                        cus.toUpperCase().contains(value.toUpperCase().trim())));

        //SELECTED ITEM PROPERTY
        listViewCustomers.getSelectionModel().selectedItemProperty()
                .addListener(event -> selectCustomer());

        listViewCustomers.setOnKeyPressed(event ->
        {
            if(event.getCode() == KeyCode.UP &&
                    listViewCustomers.getSelectionModel().isSelected(0))
                textfieldSearchEngine.requestFocus();
        });

        listViewCustomers.getSelectionModel().selectFirst();
        textfieldSearchEngine.requestFocus();
    }

    private void selectCustomer()
    {
        String customerName = listViewCustomers.getSelectionModel().getSelectedItem();

        if(customerName!=null)
        {
            Customer customer = mapCustomers.get(customerName);

            textfieldID.setText(customer.getCustomerID()+"");
            textfieldName.setText(customerName);
            textfieldNumberPhone.setText(customer.getNumberPhone());
            textfieldWhatsapp.setText(customer.getNumberPhone());
            textfieldAddress.setText(customer.getStreet());
            textfieldApartment.setText(customer.getApartment());

            try
            {
                labelTotalSales.setText(customerDAO.getTotalSales(customerName)+"");
                labelTotalDebt.setText(customerDAO.getTotalDebt(customerName)+"");
                labelLastSale.setText(customerDAO.getLastSale(customerName)+"");
            }
            catch (SQLException e) {
                labelTotalSales.setText("error");
                FXTool.alertException(e);
            }
            selectBillCustomer(customer.getCustomerID()+"");
            setProcess(PROCESS.VIEWING);
        }
    }

    private void saveCustomer()
    {
        String nameC = textfieldName.getText().trim().toUpperCase();
        String numberPhoneC = textfieldNumberPhone.getText().trim();
        String numberWhatsapp = textfieldWhatsapp.getText().trim();
        String streetC = textfieldAddress.getText().trim();
        String apartmentC = textfieldApartment.getText().trim().toUpperCase();

        if(checkFields(nameC))
        {
           try
           {
               Customer customer = new Customer(
                       0,
                       nameC,
                       numberPhoneC,
                       numberWhatsapp,
                       streetC,
                       apartmentC
               );
               //TODO VERIFICAR DUPLICIDAD name
               customerDAO.save(customer);
               textfieldID.setText(customer.getCustomerID()+"");

               mapCustomers.put(nameC, customer);
               obsCustomers.add(nameC);

               listViewCustomers.requestFocus();
               listViewCustomers.getSelectionModel().select(nameC);
           }
           catch (SQLIntegrityConstraintViolationException e){
               FXTool.alertInformation("El cliente '"+nameC+"' ya fue creado.",
                       "Si no se encuentra en la lista, \n debe eliminarlo desde la base de datos ");
           }
           catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    private void updateCustomer()
    {
        String nameC = textfieldName.getText();
        String numberPhoneC = textfieldNumberPhone.getText().trim();
        String numberWhatsapp = textfieldWhatsapp.getText().trim();
        String streetC = textfieldAddress.getText().trim();
        String apartmentC = textfieldApartment.getText().trim().toUpperCase();
        if(checkFields(nameC)) {
            try {
                Customer customer = mapCustomers.get(nameC);
                customer.setNumberPhone(numberPhoneC);
                customer.setNumberWhatsapp(numberWhatsapp);
                customer.setStreet(streetC);
                customer.setApartment(apartmentC);

                customerDAO.update(customer);

                listViewCustomers.requestFocus();
                listViewCustomers.getSelectionModel().select(nameC);

                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }

    }

    private void deleteCustomer()
    {
        String ID = textfieldID.getText().trim();
        String nameC = textfieldName.getText().trim();

        if(checkFields(ID, nameC) && !nameC.equals("CLIENTE FINAL"))
        {
            try
            {
                Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Se perderán todos los datos de sus pedidos, cuentas, etc",
                        ButtonType.YES,
                        ButtonType.CANCEL
                );
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/alerts.css").toExternalForm());
                alert.setHeaderText("¿Estas seguro que desea eliminar \na '"+nameC+"'?");
                alert.showAndWait();

                if(alert.getResult()==ButtonType.YES)
                {
                    customerDAO.delete(Integer.parseInt(ID));

                    obsCustomers.remove(nameC);
                    mapCustomers.remove(nameC);

                    listViewCustomers.requestFocus();
                    listViewCustomers.getSelectionModel().selectNext();
                }
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }


    private ObservableList<OrderModel> obsOrders;
    private ObservableList<ItemFood> obsItems;

    //TODO CARGAR LA CUENTA Y TENER LA OPCIÓN DE EXPORTAR A EXCEL
    private void loadBillCustomer()
    {
        columnID.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        columnDateUpdate.setCellValueFactory(value->value.getValue().getDate());
        columnIsPaid.setCellValueFactory(new PropertyValueFactory<>("statusPaid"));
        columnTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        columnItem.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        columnPrice.setCellValueFactory(new PropertyValueFactory<>("priceAtTheMoment"));

        obsOrders = FXCollections.observableArrayList();
        obsItems  = FXCollections.observableArrayList();

        tableViewOrders.setItems(obsOrders);
        tableViewItems.setItems(obsItems);
    }

    private void addListenersEventsBill()
    {
        tableViewOrders.getSelectionModel().selectedItemProperty()
                .addListener(obs -> selectOrder());

    }

    private void selectBillCustomer(String customerID)
    {
        textfieldDebt.setText("");
        if(checkFields(customerID))
        {
            try {
                obsOrders.clear();
                obsOrders.addAll(orderDAO.getOrdersByCustomer(customerID));
                tableViewOrders.getSelectionModel().selectFirst();
              textfieldDebt.setText(getTotalDebt(obsOrders)+"");
            }
            catch (SQLException e) { FXTool.alertException(e); }
        }
    }

    private void selectOrder()
    {
        OrderModel orderSelected = tableViewOrders.getSelectionModel().getSelectedItem();
        obsItems.clear();
        if(orderSelected!=null)
        {
            obsItems.addAll(orderSelected.getItems());
            textfieldUser.setText(orderSelected.getUsername());
            textfieldDate.setText(orderSelected.getDate().get());
            textfieldSurcharge.setText(orderSelected.getSurcharge()+"");
            textfieldDiscount.setText(orderSelected.getDiscount()+"");
            textfieldTotal.setText(orderSelected.getTotal()+"");
            //FILL OTHERS
        }else {
            textfieldUser.setText("");
            textfieldDate.setText("");
            textfieldSurcharge.setText("");
            textfieldDiscount.setText("");
            textfieldTotal.setText("");
        }
    }

    private void payAllBill()
    {
        String ID = textfieldID.getText();
        String customerName = textfieldName.getText();
        if(checkFields(ID, customerName))
        {
            if(!tableViewOrders.getItems().isEmpty())
            {
                Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "TOTAL: "+textfieldDebt.getText()+"\nNO HAY VUELTA ATRAS",
                        ButtonType.YES,
                        ButtonType.CANCEL
                );
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/alerts.css").toExternalForm());

                alert.setHeaderText("¿Estas seguro que desea saldar \nla cuenta de a '"+customerName+"'?");
                alert.showAndWait();

                if(alert.getResult()==ButtonType.YES)
                {
                    ArrayList<Integer> ordersID = new ArrayList<>();
                    tableViewOrders.getItems().forEach(order -> ordersID.add(order.getOrderID()));
                    try
                    {
                        customerDAO.payAllBill(UserSession.getUser().getUsername(), ordersID);
                        listViewCustomers.getSelectionModel().selectNext();
                        listViewCustomers.getSelectionModel().selectPrevious();
                    }
                    catch (SQLException e) { FXTool.alertException(e); }
                }

            }else
                FXTool.alertInformation("ERROR", "NO HAY PEDIDOS QUE PAGAR");
        }

    }

    private void exportToExcel()
    {
        if(!tableViewOrders.getItems().isEmpty())
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel(*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("CUENTA_" +
                    textfieldName.getText() + "_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))+".xlsx");

            File fileSaved = fileChooser.showSaveDialog(textfieldDebt.getScene().getWindow());
            if(fileSaved!=null)
            {
                if(!fileSaved.getName().endsWith(".xlsx"))
                    fileSaved = new File(fileSaved.getAbsolutePath()+ ".xlsx");

                File finalFileSaved = fileSaved;
                Platform.runLater(()-> {
                    try { exportSales(obsOrders, finalFileSaved); }
                    catch (IOException e) { FXTool.alertException(e);}
                });

                //TODO LOADING
            }
        }
    }


    private void setProcess(PROCESS process)
    {
        if(process.equals(PROCESS.ADDING) )
        {
            textfieldName.setDisable(false);
            textfieldNumberPhone.setDisable(false);
            textfieldWhatsapp.setDisable(false);
            textfieldAddress.setDisable(false);
            textfieldApartment.setDisable(false);

            textfieldID.setText("");
            textfieldName.setText("");
            textfieldNumberPhone.setText("");
            textfieldWhatsapp.setText("");
            textfieldAddress.setText("");
            textfieldApartment.setText("");

            buttonEditCustomer.setDisable(true);
            buttonUpdateCustomer.setDisable(true);
            buttonDeleteCustomer.setDisable(true);
            buttonSaveCustomer.setDisable(false);

            labelTotalSales.setText("0000.00");
            labelTotalDebt.setText("0000.00");
            labelLastSale.setText("00/00/00");
            tabReport.setDisable(true);
            textfieldName.requestFocus();
        }
        else if(process.equals(PROCESS.EDITING))
        {
            textfieldName.setDisable(true);
            textfieldNumberPhone.setDisable(false);
            textfieldWhatsapp.setDisable(false);
            textfieldAddress.setDisable(false);
            textfieldApartment.setDisable(false);

            buttonEditCustomer.setDisable(true);
            buttonUpdateCustomer.setDisable(false);
            buttonDeleteCustomer.setDisable(false);
            buttonSaveCustomer.setDisable(true);
            tabReport.setDisable(false);
            textfieldNumberPhone.requestFocus();
        }
        else if(process.equals(PROCESS.VIEWING))
        {
            textfieldName.setDisable(true);
            textfieldNumberPhone.setDisable(true);
            textfieldWhatsapp.setDisable(true);
            textfieldAddress.setDisable(true);
            textfieldApartment.setDisable(true);

            tabReport.setDisable(false);
            buttonEditCustomer.setDisable(false);
            buttonUpdateCustomer.setDisable(true);
            buttonDeleteCustomer.setDisable(false);
            buttonSaveCustomer.setDisable(true);
        }
    }
}
