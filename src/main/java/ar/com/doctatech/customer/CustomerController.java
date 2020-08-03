package ar.com.doctatech.customer;

import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.shared.PROCESS;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ar.com.doctatech.shared.utilities.ControlUtil.checkFields;

public class CustomerController
        extends CustomerServices implements Initializable
{

    @FXML private ListView<String> listViewCustomers;
    @FXML private TextField textfieldBrowserCustomers;

    @FXML private TextField textfieldID, textfieldName, textfieldNumberPhone,
            textfieldWhatsapp, textfieldAddress, textfieldApartment;
    @FXML private Button buttonEditCustomer,
            buttonUpdateCustomer, buttonDeleteCustomer, buttonSaveCustomer;
    @FXML private Label labelTotalSales,labelTotalDebt,labelLastSale;

    @FXML private void onKeyReleasedTextfieldBrowser(KeyEvent event)
    {
        if(event.getCode() == KeyCode.DOWN)
        {
            listViewCustomers.getSelectionModel().selectFirst();
            listViewCustomers.requestFocus();
        }
    }

    @FXML private void onClickButtonNew(){newCustomer();}
    @FXML private void onClickButtonEdit(){editCustomer();}
    @FXML private void onClickButtonUpdate(){updateCustomer();}
    @FXML private void onClickButtonSave(){saveCustomer();}
    @FXML private void onClickButtonDelete(){deleteCustomer();}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCustomers();
        addListenersEvents();
    }


    HashMap<String, Customer> mapCustomers;
    ObservableList<String> obsCustomers;
    private void loadCustomers()
    {
        try
        {
            mapCustomers = customerDAO.getAll();
            obsCustomers = FXCollections.observableArrayList(mapCustomers.keySet());
        }
        catch (SQLException e) { FXTool.alertException(e); }

    }

    private void addListenersEvents()
    {
        //BUSCADOR
        FilteredList<String> filteredList = new FilteredList<>(obsCustomers, s->true);
        textfieldBrowserCustomers.textProperty().addListener(
                (observable, oldValue, newValue) -> filteredList.setPredicate(cus ->
                        cus.toUpperCase().contains(newValue.toUpperCase().trim())));
        listViewCustomers.setItems(filteredList);
        listViewCustomers.getSelectionModel().selectFirst();

        //SELECTED ITEM PROPERTY
        listViewCustomers.getSelectionModel().selectedItemProperty()
                .addListener(observable -> selectCustomer());
        listViewCustomers.setOnKeyPressed(event ->
        {
            if(event.getCode() == KeyCode.UP &&
                    listViewCustomers.getSelectionModel().isSelected(0))
                textfieldBrowserCustomers.requestFocus();

        });
    }

    private void selectCustomer()
    {
        String customerName = listViewCustomers.getSelectionModel().getSelectedItem();
        Customer customer = mapCustomers.get(customerName);
        if(customer!=null)
        {
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
                labelTotalSales.setText("ERR SQL");
                FXTool.alertException(e);
            }
            setProcess(PROCESS.VIEWING);
        }
    }

    private void newCustomer()
    {
        setProcess(PROCESS.ADDING);
    }
    private void editCustomer()
    {
        setProcess(PROCESS.EDITING);
    }

    private void saveCustomer()
    {
        String nameC = textfieldName.getText();
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
               customerDAO.save(customer);
               textfieldID.setText(customer.getCustomerID()+"");
               FXTool.alertInformation("Operación exitosa.", "'"+nameC+"' guardado!");
               setProcess(PROCESS.VIEWING);
           }
           catch (SQLException e)
           { FXTool.alertException(e); }
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
                Customer customer = new Customer(
                        0,
                        nameC,
                        numberPhoneC,
                        numberWhatsapp,
                        streetC,
                        apartmentC
                );
                customerDAO.update(customer);
                FXTool.alertInformation("Operación realizada!", "'"+nameC+"' actualizado con exito.");
                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }

    }

    private void deleteCustomer()
    {
        String ID = textfieldID.getText().trim();
        if(!ID.isEmpty())
        {
            try
            {
                customerDAO.delete(Integer.parseInt(ID));
                listViewCustomers.getSelectionModel().selectFirst();
                setProcess(PROCESS.VIEWING);
            }
            catch (SQLException e)
            {
                FXTool.alertException(e);
            }
        }


    }


    private void setProcess(PROCESS process)
    {
        if(process.equals(PROCESS.ADDING) )
        {
            textfieldName.setEditable(true);
            textfieldNumberPhone.setEditable(true);
            textfieldWhatsapp.setEditable(true);
            textfieldAddress.setEditable(true);
            textfieldApartment.setEditable(true);

            textfieldID.setText("");
            textfieldName.setText("");
            textfieldNumberPhone.setText("");
            textfieldWhatsapp.setText("");
            textfieldAddress.setText("");
            textfieldApartment.setText("");

            buttonEditCustomer.setVisible(false);
            buttonUpdateCustomer.setVisible(false);
            buttonDeleteCustomer.setVisible(false);
            buttonSaveCustomer.setVisible(true);

            labelTotalSales.setText("0000.00");
            labelTotalDebt.setText("0000.00");
            labelLastSale.setText("00/00/00");

            textfieldName.requestFocus();
        }
        else if(process.equals(PROCESS.EDITING))
        {
            textfieldName.setEditable(false);
            textfieldNumberPhone.setEditable(true);
            textfieldWhatsapp.setEditable(true);
            textfieldAddress.setEditable(true);
            textfieldApartment.setEditable(true);

            buttonEditCustomer.setVisible(false);
            buttonUpdateCustomer.setVisible(true);
            buttonDeleteCustomer.setVisible(true);
            buttonSaveCustomer.setVisible(false);

            textfieldNumberPhone.requestFocus();
        }
        else if(process.equals(PROCESS.VIEWING))
        {
            textfieldName.setEditable(false);
            textfieldNumberPhone.setEditable(false);
            textfieldWhatsapp.setEditable(false);
            textfieldAddress.setEditable(false);
            textfieldApartment.setEditable(false);

            buttonEditCustomer.setVisible(true);
            buttonUpdateCustomer.setVisible(false);
            buttonDeleteCustomer.setVisible(true);
            buttonSaveCustomer.setVisible(false);
        }
    }
}
