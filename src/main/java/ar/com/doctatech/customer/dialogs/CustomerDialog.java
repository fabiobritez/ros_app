package ar.com.doctatech.customer.dialogs;

import ar.com.doctatech.customer.model.Customer;
import ar.com.doctatech.customer.model.CustomerDAO;
import ar.com.doctatech.customer.model.CustomerDAOMySQL;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class CustomerDialog extends Dialog<Customer>
{
    private HashMap<String, Customer> customerMap;
    TextField textfieldSearch;
    ListView<String> listViewCustomers;

    public CustomerDialog()
    {
        getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dialogs.css").toExternalForm());
        getDialogPane().getStyleClass().add("panel-back");



        setTitle("Lista de clientes...");
        textfieldSearch = new TextField();
        textfieldSearch.setPromptText("Buscar cliente..");
        listViewCustomers = new ListView<>();

        CustomerDAO customerDAO = new CustomerDAOMySQL();
        try
        {
            customerMap = customerDAO.getAll();
            setListenersProperties();
        }
        catch (SQLException e) { FXTool.alertException(e); }

        VBox vBox = new VBox(textfieldSearch,listViewCustomers);
        vBox.setPrefSize(250,250);
        vBox.setPadding(new Insets(8,8,8,8));
        vBox.setSpacing(10);
        getDialogPane().setContent(vBox);

        //BUTTON
         ButtonType buttonTypeSelect = new ButtonType("Seleccionar", ButtonBar.ButtonData.YES);
         getDialogPane().getButtonTypes().add(buttonTypeSelect);
         setResultConverter(buttonType -> {
         if(buttonType== buttonTypeSelect)
            return customerMap.get(
               listViewCustomers.getSelectionModel().getSelectedItem()
            );
            return null;
         });
         setOnCloseRequest(event -> close());
    }

    private void setListenersProperties()
    {
        FilteredList<String> filteredList = new FilteredList<>(
                FXCollections.observableArrayList(customerMap.keySet()), s -> true);

        textfieldSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> filteredList.setPredicate(
                        customerName -> customerName.toUpperCase().trim().contains(
                                newValue.trim().toUpperCase())
                ));

        textfieldSearch.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.DOWN)
            {
                listViewCustomers.getSelectionModel().selectFirst();
                listViewCustomers.requestFocus();
            }
        } );
        listViewCustomers.setItems(filteredList);

        listViewCustomers.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
            {
                setResult(customerMap.get( listViewCustomers.getSelectionModel().getSelectedItem() ));
                close();
            }else if(event.getCode() == KeyCode.UP)
            {
                if(listViewCustomers.getSelectionModel().isSelected(0))
                    textfieldSearch.requestFocus();
            }
        });
        listViewCustomers.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY &&
                    event.getClickCount()==2)
            {
                setResult(customerMap.get( listViewCustomers.getSelectionModel().getSelectedItem() ));
                close();
            }

        });
    }

    public Optional<Customer> showAndStop()
    {
       this.getDialogPane().requestFocus();
       textfieldSearch.requestFocus();
       return showAndWait();
    }

}
