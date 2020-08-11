package ar.com.doctatech.reports;


import ar.com.doctatech.order.dao.OrderDAO;
import ar.com.doctatech.order.dao.OrderDAOMySQL;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.order.model.OrderModel;
import ar.com.doctatech.shared.utilities.FXTool;
import ar.com.doctatech.shared.utilities.GoogleMail;
import ar.com.doctatech.user.login.RecoveryServices;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import sun.security.acl.WorldGroupImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportServices {

    OrderDAO orderDAO;
    {
        orderDAO = new OrderDAOMySQL();
    }

    public void setLocalFormat(DatePicker datePicker)
    {
        datePicker.setConverter( new StringConverter<LocalDate>()
        {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
            @Override public String toString(LocalDate date)
            {
                return (date!=null) ? dateFormatter.format(date) : "";
            }
            @Override public LocalDate fromString(String string)
            {
                return (string != null && !string.isEmpty()) ?
                        LocalDate.parse(string, dateFormatter): null;
            }
        });
    }

    public Task <Void> exportSalesTask(ObservableList<OrderModel> obsSales, File file)
    {

        Task<Void> exportTask = new Task<Void>(){
            @Override
            protected Void call() throws Exception {

                try(SXSSFWorkbook workbook =
                            new SXSSFWorkbook(50))
                {
                    SXSSFSheet sheet = workbook.createSheet();

                    //CREATE HEADER
                    String[] columns = {"ID","FECHA","CLIENTE","ITEMS","SUBTOTAL","DESCUENTO","RECARGO",
                            "TOTAL", "PAGO", "ESTADO","METODO PAGO","VENDEDOR"};
                    Row header = sheet.createRow(0);
                    int cantColumns = 0;
                    for (String column: columns)
                    {
                        Cell cellOrderID = header.createCell(cantColumns);
                        cellOrderID.setCellValue(column);
                        cantColumns++;
                    }

                    int sizeObs = obsSales.size();
                    sheet.trackAllColumnsForAutoSizing();
                    int cantRows = 1;
                    for (OrderModel order : obsSales)
                    {
                        Row row = sheet.createRow(cantRows);

                        Cell cellOrderID = row.createCell(0);
                        cellOrderID.setCellValue(order.getOrderID());

                        Cell cellDate = row.createCell(1);
                        cellDate.setCellValue(order.getDate().get());

                        Cell cellCustomer = row.createCell(2);
                        cellCustomer.setCellValue(order.getNameCustomer());

                        Cell cellItems = row.createCell(3);
                        //region ITEMS
                        StringBuilder itemsBuilder = new StringBuilder();
                        for (ItemFood itemFood : order.getItems())
                        {
                            itemsBuilder.append(itemFood.getDescription()).append(" : ")
                                    .append(itemFood.getPriceAtTheMoment()).append(" x ")
                                    .append(itemFood.getQuantity()).append("\n");
                        }
                        itemsBuilder.deleteCharAt(itemsBuilder.length()-1);
                        cellItems.setCellValue(itemsBuilder.toString());
                        //endregion

                        Cell cellSubtotal = row.createCell(4);
                        cellSubtotal.setCellValue(order.getSubtotal());

                        Cell cellDiscount = row.createCell(5);
                        cellDiscount.setCellValue(order.getDiscount());

                        Cell cellSurcharge = row.createCell(6);
                        cellSurcharge.setCellValue(order.getSurcharge());

                        Cell cellTotal = row.createCell(7);
                        cellTotal.setCellValue(order.getTotal());
                        Cell cellIsPaid = row.createCell(8);
                        cellIsPaid.setCellValue(order.getStatusPaid());

                        Cell cellStatus = row.createCell(9);
                        cellStatus.setCellValue(order.getStatusDelivery());

                        Cell cellPayMethod = row.createCell(10);
                        cellPayMethod.setCellValue(order.getPayMethod());

                        Cell cellUser = row.createCell(11);
                        cellUser.setCellValue(order.getUsername());

                        //TODO REVISAR Y HACER UN TOTAL
                        row.setHeight((short) (order.getItems().size()*270));
                        cantRows++;
                        updateProgress(cantRows,sizeObs);
                    }
                    if(sheet.isColumnTrackedForAutoSizing(3))
                        sheet.autoSizeColumn(3);


                    try (FileOutputStream out =
                                 new FileOutputStream(file))
                    {
                        workbook.write(out);
                    }
                    workbook.dispose();
                }
                return null;
            }
        };
        exportTask.setOnFailed(e->
                FXTool.alertException((Exception) exportTask.getException()));
        exportTask.setOnSucceeded(e ->  FXTool.alertInformation
                ("ARCHIVO EXPORTADO", "La creación del archivo ha finalizado"));
        return exportTask;
    }
}
