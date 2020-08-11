package ar.com.doctatech.customer;

import ar.com.doctatech.customer.model.CustomerDAO;
import ar.com.doctatech.customer.model.CustomerDAOMySQL;
import ar.com.doctatech.order.dao.OrderDAO;
import ar.com.doctatech.order.dao.OrderDAOMySQL;
import ar.com.doctatech.order.model.ItemFood;
import ar.com.doctatech.order.model.OrderModel;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CustomerServices {
    CustomerDAO customerDAO;
    OrderDAO orderDAO;
    {
        orderDAO = new OrderDAOMySQL();
        customerDAO = new CustomerDAOMySQL();
    }

    public void exportSales(ObservableList<OrderModel> obsSales, File file)
            throws IOException {

        //keep 50 rows in memory, exceeding rows will be flushed to disk
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
    }

    public double getTotalDebt(List<OrderModel> orders)
    {
        return orders.stream().mapToDouble(OrderModel::getTotal).sum();
    }
}
