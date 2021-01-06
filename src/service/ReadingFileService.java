package service;

import domain.EntityCounter;
import domain.TableEntity;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.Date;

//вспомогательный класс для работы с файлами Excel
public class ReadingFileService {

    //метод для чтения данных из файла и построения таблицы
    //предполагем, что структура генерируемого файла всегда одна и та же:
    //всегда используется первый лист книги Excel,
    // заголовок - первая строка, в заголовке указаны date, time, номер канала в формате T1, T2...
    public void getInformationFromFile(File file, TableView tableView,
                                       TextField testDateValue, TextField testNameValue,
                                       EntityCounter entityCounter, int channelsCounter)
            throws IOException, InvalidFormatException {

        //открываем книгу Excel
        String fileAddress = file.getAbsolutePath();
        OPCPackage pkg = OPCPackage.open(new File(fileAddress));
        XSSFWorkbook workbook = new XSSFWorkbook(pkg);
        //получаем первый лист книги
        Sheet sheet = workbook.getSheetAt(0);

        //определяем номера столбцов с нужной информацией
        //изначально задаем отрицательное значение для отсутвующих столбцов
        int dateTimeIndex = -1;
        //максимальное число каналов - 8 (3 термопары на образце, 5  - в печи)
        int t1Index = -1;
        int t2Index = -1;
        int t3Index = -1;
        int t4Index = -1;
        int t5Index = -1;
        int t6Index = -1;
        int t7Index = -1;
        int t8Index = -1;

        //ищем в первой строке названия столбцов и устанавливаем номера столбцов
        Row headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().toLowerCase().contains("datetime")) {
                dateTimeIndex = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("value1")) {
                t1Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value2")) {
                t2Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value3")) {
                t3Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value4")) {
                t4Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value5")) {
                t5Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value6")) {
                t6Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value7")) {
                t7Index = cell.getColumnIndex();
                channelsCounter++;
            }
            if (cell.getStringCellValue().toLowerCase().contains("value8")) {
                t8Index = cell.getColumnIndex();
                channelsCounter++;
            }
        }

        //извлекаем дату из файла и устанавливаем в окно с датой
        Date date = new Date();
        if (sheet.getRow(1).getCell(dateTimeIndex).getCellType() == CellType.NUMERIC) {
            try {
                date = sheet.getRow(1).getCell(dateTimeIndex).getDateCellValue();
            } catch (Exception ex) {
                System.out.println("Ошибка при извлечении даты");
            }
        }
        testDateValue.setText(date.toString());

        //извлекаем название опыта по умолчанию - по имени файла
        int border = fileAddress.lastIndexOf("\\");
        testNameValue.setText(fileAddress.substring(border+1, (fileAddress.length()-5)));

        //добавляем в таблицу строки из файла для каждого канала
        for (Row row : sheet) {
            if (row.getRowNum() >= 1 & row.getCell(dateTimeIndex) != null) {
                TableEntity entityT1 = getTableEntityFromFile(row,"1", dateTimeIndex, t1Index, entityCounter, date);
                TableEntity entityT2 = getTableEntityFromFile(row, "2", dateTimeIndex, t2Index, entityCounter, date);
                TableEntity entityT3 = getTableEntityFromFile(row, "3", dateTimeIndex, t3Index, entityCounter, date);
                TableEntity entityT4 = getTableEntityFromFile(row, "4", dateTimeIndex, t4Index, entityCounter, date);
                TableEntity entityT5 = getTableEntityFromFile(row, "5", dateTimeIndex, t5Index, entityCounter, date);
                TableEntity entityT6 = getTableEntityFromFile(row, "6", dateTimeIndex, t6Index, entityCounter, date);
                TableEntity entityT7 = getTableEntityFromFile(row, "7", dateTimeIndex, t7Index, entityCounter, date);
                TableEntity entityT8 = getTableEntityFromFile(row, "8", dateTimeIndex, t8Index, entityCounter, date);

                addTableEntity(tableView, entityT1, entityT2, entityT3, entityT4, entityT5, entityT6, entityT7, entityT8);
            }
        }
        workbook.close();
    }

    //метод для получения сущности строки из файла Excel
    //принимает номер строки файла, имя канала, индексы колонок времени и канала
    public TableEntity getTableEntityFromFile (Row row, String channelName, int timeIndex, int channelIndex, EntityCounter entityCounter, Date initialMoment) {
        //обязательно &&, иначе "падает" вторая проверка
        int EntityCounter = 1;
        if (channelIndex > 0 && row.getCell(channelIndex) != null) {
            TableEntity tableEntity = new TableEntity(entityCounter.getAndIncrementCounter(),
                    channelName,
                    (row.getCell(timeIndex).getDateCellValue().getTime() - initialMoment.getTime())/60000,
                    (Math.round(row.getCell(channelIndex).getNumericCellValue()*10))/10.0);
            return tableEntity;
        } else {
            return null;
        }
    }

    //метод для добавления полученных из файла excel данных в таблицу интерфейса
    //добавляется каждая сущность строки TableEntity из списка строк
    public void addTableEntity(TableView tableView, TableEntity ... tableEntities) {
        for (TableEntity tableEntity : tableEntities) {
            if (tableEntity != null) {
                tableView.getItems().add(tableEntity);
            }
        }
    }

    //метод для очистки информации из таблицы после загрузки файла
    public void clearInfoTable(TextField testDateValue,
                               TextField testNameValue,
                               TextField initialTempValue,
                               TextField resultValue) {
        testDateValue.clear();
        testNameValue.clear();
        initialTempValue.clear();
        resultValue.clear();
    }
}
