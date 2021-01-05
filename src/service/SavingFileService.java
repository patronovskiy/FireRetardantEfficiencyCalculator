package service;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.io.*;

//вспомогательный класс для сохранения файла отчета
public class SavingFileService {

    //метод для создания отчета
    public Workbook createReport(File file,
                                 TextField testNameValue,
                                 TextField testDateValue,
                                 TextArea notesValue,
                                 TextField sampleChannelsValue,
                                 TextField owenChannelsValue,
                                 TextField resultValue,
                                 XYChart chart) {
        //создаем книгу Excel
        OPCPackage pkg = OPCPackage.create(file);
        XSSFWorkbook workbook = new XSSFWorkbook();
        //создаем первый лист книги
        Sheet sheet = workbook.createSheet("Отчет об испытаниях");

        //создаем строки
        Row headerRow = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        Row row2 = sheet.createRow(2);
        Row row3 = sheet.createRow(3);
        Row row4 = sheet.createRow(4);
        Row row5 = sheet.createRow(5);
        Row row6 = sheet.createRow(6);
        Row row7 = sheet.createRow(7);
        Row row8 = sheet.createRow(8);

        //заполняем ячейки информацией из интерфейса
        Cell headerCell = headerRow.createCell(0, CellType.STRING);
        headerCell.setCellValue("Отчет об испытаниях");
        Cell cell1_0 = row1.createCell(0);
        Cell cell2_0 = row2.createCell(0);
        Cell cell3_0 = row3.createCell(0);
        Cell cell4_0 = row4.createCell(0);
        Cell cell5_0 = row5.createCell(0);
        Cell cell6_0 = row6.createCell(0);
        Cell cell8_0 = row8.createCell(0);

        Cell cell1_1 = row1.createCell(1);
        Cell cell2_1 = row2.createCell(1);
        Cell cell3_1 = row3.createCell(1);
        Cell cell4_1 = row4.createCell(1);
        Cell cell5_1 = row5.createCell(1);
        Cell cell6_1 = row6.createCell(1);
        Cell cell8_1 = row8.createCell(1);

        cell1_0.setCellValue("Название (№) опыта:");
        cell2_0.setCellValue("Дата:");
        cell3_0.setCellValue("Примечания:");
        cell4_0.setCellValue("№ термопар на образце:");
        cell5_0.setCellValue("№ термопар в печи:");
        cell6_0.setCellValue("Результат испытания, мин:");

        cell1_1.setCellValue(testNameValue.getText());
        cell2_1.setCellValue(testDateValue.getText());
        cell3_1.setCellValue(notesValue.getText());
        cell4_1.setCellValue(sampleChannelsValue.getText());
        cell5_1.setCellValue(owenChannelsValue.getText());
        cell6_1.setCellValue(resultValue.getText());

        //сохранение графика как изображения
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        WritableImage image = chart.snapshot(snapshotParameters, null);
        File imageFile = new File("image.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", imageFile);

        } catch (IOException e) {
            System.out.println("Ошибка при формировании снимка");
        }
        try {
            //FileInputStream obtains input bytes from the image file
            InputStream inputStream = new FileInputStream("image.png");
            //Get the contents of an InputStream as a byte[].
            byte[] bytes = IOUtils.toByteArray(inputStream);
            //Adds a picture to the workbook
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            //Returns an object that handles instantiating concrete classes
            CreationHelper helper = workbook.getCreationHelper();
            //Creates the top-level drawing patriarch.
            Drawing drawing = sheet.createDrawingPatriarch();
            //Create an anchor that is attached to the worksheet
            ClientAnchor anchor = helper.createClientAnchor();
            //set top-left corner for the image
            anchor.setCol1(1);
            anchor.setRow1(8);
            //Creates a picture
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            //Reset the image to the original size
            pict.resize();
            inputStream.close();
        } catch (IOException ex) {
            System.out.println("Ошибка при сохранении графика" + ex);
        }

        return workbook;
    }

    //метод для сохранения файла
    public void saveReport(Stage stage,
                           TextField testNameValue,
                           TextField testDateValue,
                           TextArea notesValue,
                           TextField sampleChannelsValue,
                           TextField owenChannelsValue,
                           TextField resultValue,
                           XYChart chart) {

        //Класс работы с диалогом выборки и сохранения
        FileChooser fileChooser = new FileChooser();
        //Заголовок диалога
        fileChooser.setTitle("Сохранить отчет");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");//Расширение
        fileChooser.getExtensionFilters().add(extFilter);
        //Указываем текущую сцену
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            //создем отчет
            try{
                Workbook workbook = createReport(   file,
                                                    testNameValue,
                                                    testDateValue,
                                                    notesValue,
                                                    sampleChannelsValue,
                                                    owenChannelsValue,
                                                    resultValue,
                                                    chart);
                try{
                    //запись файла
                    FileOutputStream outFile = new FileOutputStream(file);
                    workbook.write(outFile);
                    System.out.println("Сохранен файл: " + file.getAbsolutePath());
                    workbook.close();
                    outFile.close();
                } catch (IOException ex) {
                    System.out.println("Ошибка при сохранении отчета");
                }

            } catch (Exception ex) {
                System.out.println("Ошибка при создании отчета");
            }
        }

    }

}
