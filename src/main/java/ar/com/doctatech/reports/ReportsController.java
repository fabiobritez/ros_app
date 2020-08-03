package ar.com.doctatech.reports;

import ar.com.doctatech.order.model.OrderModel;
import ar.com.doctatech.shared.utilities.FXTool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportsController extends ReportServices
        implements Initializable {

    @FXML private DatePicker datePickerStart, datePickerEnd;
    @FXML private ComboBox<String> comboBoxStatus, comboBoxIsPaid;
    @FXML private TableView<OrderModel> tableViewOrders;
    @FXML private TableColumn<OrderModel, String> columnDate,columnCustomer, columnTotal,columnIsPaid, columnIsDelivered, columnUser;

    @FXML private void onActionButtonFilter(){ filterOrders(); }
    @FXML private void onActionButtonExport(){ exportToExcel(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadComponents();
    }

    private ObservableList<OrderModel> obsOrders;

    private void loadComponents()
    {
        setLocalFormat(datePickerStart);
        setLocalFormat(datePickerEnd);
        comboBoxStatus.setItems(FXCollections.observableArrayList(
                "TODOS", "ENTREGADO", "CANCELADO"));
        comboBoxIsPaid.setItems(FXCollections.observableArrayList(
                "TODOS", "PAGADO", "DEBE"));

        datePickerEnd.setValue(LocalDate.now());
        datePickerStart.setValue(LocalDate.now());
        comboBoxStatus.getSelectionModel().select("TODOS");
        comboBoxIsPaid.getSelectionModel().select("TODOS");

        datePickerStart.setEditable(false);
        datePickerEnd.setEditable(false);


        columnDate.setCellValueFactory(value -> value.getValue().getDate());
        columnCustomer.setCellValueFactory(new PropertyValueFactory<>("nameCustomer"));
        columnTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        columnIsPaid.setCellValueFactory(new PropertyValueFactory<>("statusPaid"));
        columnIsDelivered.setCellValueFactory(new PropertyValueFactory<>("statusDelivery"));
        columnUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        obsOrders = FXCollections.observableArrayList();
        tableViewOrders.setItems(obsOrders);
    }

    private void filterOrders()
    {
        Date dateStart = Date.valueOf(datePickerStart.getValue());
        Date dateEnd = Date.valueOf(datePickerEnd.getValue());
        String status = comboBoxStatus.getSelectionModel().getSelectedItem();
        String paid = comboBoxIsPaid.getSelectionModel().getSelectedItem();

        Boolean isPaid = null;
        if(paid.equals("PAGADO")) isPaid = true;
        else if (paid.equals("DEBE")) isPaid = false;

        try {
            obsOrders.clear();
            obsOrders.addAll( orderDAO.getOrders(dateStart,dateEnd,isPaid,status));
        } catch (SQLException e) {
            FXTool.alertException(e);
        }
    }

    private void exportToExcel()
    {
        if(!tableViewOrders.getItems().isEmpty())
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel(*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("*.xlsx");

            File fileSaved = fileChooser.showSaveDialog(comboBoxIsPaid.getScene().getWindow());
            if(fileSaved!=null)
            {
                if(!fileSaved.getName().endsWith(".xlsx"))
                    fileSaved = new File(fileSaved.getAbsolutePath()+ ".xlsx");
                try {
                    exportSales(obsOrders, fileSaved);
                    //TODO LOADING
                } catch (IOException e) {
                    FXTool.alertException(e);
                }
            }
        }
    }

}
