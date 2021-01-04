
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import domain.TableEntity;
import domain.TestDescription;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//главный класс приложения с настройкой интерфейса и управлением логикой, событиями
public class Main extends Application {
    //ВСПОМОГАТЕЛЬНЫЕ ПЕРЕМЕННЫЕ
    private String fileAddress = "";                                //адрес файла
    private TestDescription testDescription;                        //сущность-описание опыта
    private int entityCounter = 1;                                  //счетчик строк в таблице
    private  ArrayList<String> owenChannels = new ArrayList<>();    //список печных термопар
    private ArrayList<String> sampleChannels = new ArrayList<>();   //список термопар на образце

    //ГЕТТЕРЫ И СЕТТЕРЫ
    public String getFileAddress() {
        return fileAddress;
    }

    public void setFileAddress(String fileAddress) {
        this.fileAddress = fileAddress;
    }

    //ГЛАВНЫЙ МЕТОД
    @Override
    public void start(Stage primaryStage) throws Exception {
        //настройка компоновщика
        //корневой компоновщик
        //todo рефакторинг - вынести настройку компонентов в отдельные метдоды
        GridPane root = new GridPane();

        //МЕНЮ
        //разметка меню
        MenuBar mainMenuBar = new MenuBar();
        Menu fileMenu = new Menu("Файл");
        MenuItem openMenuItem = new MenuItem("Открыть файл");
        MenuItem saveMenuItem = new MenuItem("Сохранить отчет");
        Menu helpMenu = new Menu("Справка");
        fileMenu.getItems().addAll(openMenuItem, saveMenuItem);
        mainMenuBar.getMenus().addAll(fileMenu, helpMenu);
        root.add(mainMenuBar, 0, 0, 2,1);

        //настройка колонок корневого компоновщика
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(40);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(60);
        root.getColumnConstraints().addAll(column1, column2);

        //компоновщик таблицы с информацией об опыте
        GridPane infoTable = new GridPane();
        infoTable.getStyleClass().add("info-table");
        ColumnConstraints infoTableColumn1 = new ColumnConstraints();
        ColumnConstraints infoTableColumn2 = new ColumnConstraints();
        infoTable.getColumnConstraints().addAll(infoTableColumn1, infoTableColumn2);
        Label testName = new Label("Название (№) опыта:");
        TextField testNameValue = new TextField();

        Label testDate = new Label("Дата:");
        TextField testDateValue = new TextField("");

        Label initialTemp = new Label("Начальная температура:");
        TextField initialTempValue = new TextField("");

        Label criticalTemp = new Label("Критическая температура, ᵒС:");
        TextField criticalTempValue = new TextField("500");

        Label notes = new Label("Примечания:");
        notes.setMaxHeight(Double.MAX_VALUE);
        TextArea notesValue = new TextArea(" ");

        Label sampleChannels = new Label("Термопары на образце:");
        TextField sampleChannelsValue = new TextField();

        Label result = new Label("Результат испытания, мин:");
        TextField resultValue = new TextField();

        Button calculateResultsButton = new Button("Рассчитать результат");
        calculateResultsButton.getStyleClass().add("calculate-results-button");

        infoTable.setGridLinesVisible(true);
        infoTable.addColumn(0, testName, testDate, initialTemp, criticalTemp, notes, sampleChannels, result);
        infoTable.addColumn(1,
            testNameValue, testDateValue, initialTempValue, criticalTempValue, notesValue, sampleChannelsValue, resultValue);
        infoTable.add(calculateResultsButton, 0, 8, 2, 1);

        //компоновщик с таблицей температур
        GridPane tempTablePane = new GridPane();
        tempTablePane.getStyleClass().add("temp-table-pane");
        TableView<TableEntity> tempTable = new TableView<>();
        tempTable.getStyleClass().add("temp-table");
        TableColumn<TableEntity, Integer> number = new TableColumn<>("№");
        TableColumn<TableEntity, String> channel = new TableColumn<>("Канал");
        TableColumn<TableEntity, Double> minutes = new TableColumn<>("Время, мин");
        TableColumn<TableEntity, Double> temperature = new TableColumn<>("Температура, ᵒС");

        //связывание полей таблицы со свойствами объекта TableEntity
        number.setCellValueFactory(new PropertyValueFactory<>("number"));
        channel.setCellValueFactory(new PropertyValueFactory<>("channel"));
        minutes.setCellValueFactory(new PropertyValueFactory<>("minutes"));
        temperature.setCellValueFactory(new PropertyValueFactory<>("temperature"));

        tempTable.getColumns().addAll(number, channel, minutes, temperature);
        tempTablePane.add(tempTable, 0, 0);

        //компоновщик-обертка для таблиц
        GridPane tablePane = new GridPane();
        tablePane.addColumn(0, infoTable, tempTablePane);

        //компоновщик с графиками
        GridPane chartPane = new GridPane();
        chartPane.getStyleClass().add("chart-pane");
        Label chartLabel = new Label("Графики зависимости температуры от времени");
        chartLabel.getStyleClass().addAll("chart-label", "main-chart-label");
        //график температур с печных термопар
        GridPane owenChartPane = new GridPane();
        owenChartPane.getStyleClass().add("chart-wrapper");
        Label owenLabel = new Label("Печные термопары");
        owenLabel.getStyleClass().add("chart-label");
        Label owenChannels = new Label("Каналы: ");
        Label owenChannelsValue = new Label();
        NumberAxis xOwenAxis = new NumberAxis();
        NumberAxis yOwenAxis = new NumberAxis();
        XYChart owenChart = new LineChart<Number, Number>(xOwenAxis, yOwenAxis);
        owenChart.getStyleClass().add("owen-chart");
        owenChartPane.addRow(0, owenLabel, owenChannels, owenChannelsValue);
        owenChartPane.add(owenChart,0,1,2,1);
        createBorderLines(new TestDescription("1", new Date(), 20, 500, 150, "test"), owenChart);
        //график температур термопар с образца
        GridPane sampleChartPane = new GridPane();
        sampleChartPane.getStyleClass().add("chart-wrapper");
        Label sampleLabel = new Label("Термопары на образце");
        sampleLabel.getStyleClass().add("chart-label");

        NumberAxis xSampleAxis = new NumberAxis();
        NumberAxis ySamplexis = new NumberAxis();
        XYChart sampleChart = new LineChart<Number, Number>(xSampleAxis, ySamplexis);

        //результат испытания
        GridPane resultPane = new GridPane();

        chartPane.addRow(0, chartLabel);
        chartPane.addRow(1, owenChartPane);
        chartPane.addRow(2, sampleChartPane);
        chartPane.addRow(3, resultPane);
        //chartPane.addColumn(0, chartLabel, owenChartPane, sampleChartPane, resultPane);
        chartPane.setGridLinesVisible(true);

        root.add(tablePane, 0, 1);
        root.add(chartPane, 1, 1);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("style/style.css");
        primaryStage.setTitle("Fire Retardant Efficiency Calculator");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();


        //ОБРАБОТЧИКИ СОБЫТИЙ И НАЗНАЧЕНИЕ ИХ КОНТРОЛЛЕРАМ
        //обработка событий меню
        //открытие файла из меню
        EventHandler<ActionEvent> onOpenFile = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();//Класс работы с диалогом выборки и сохранения
                fileChooser.setTitle("Открыть документ");//Заголовок диалога
                FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("Excel files (*.xlsx, *.xls)", "*.xlsx", "*.xls");//Расширение
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(primaryStage); //Указываем текущую сцену CodeNote.mainStage
                if (file != null) {
                    //Открытие файла
                    System.out.println("Процесс открытия файла");
                    fileAddress = file.getAbsolutePath();
                    testDescription = new TestDescription();
                    try{
                        getInformationFromFile(file, owenChart, tempTable);
                    } catch (IOException ex1) {
                        System.out.println("IO Exception while opening file");
                    } catch (InvalidFormatException ex2) {
                        System.out.println("Invalid format exception while opening file");
                    }
                }
            }
        };
        openMenuItem.setOnAction(onOpenFile);
    }

    //МЕТОДЫ

    //метод для создания ограничивающих линий на графике для печных термопар
    //эти линии ограничивают допустимые значения температуры в печи по ГОСТ
    public void createBorderLines(TestDescription testDescription, XYChart chart) {
        Double t0 = testDescription.getInitialValue();
        int time = testDescription.getScaleLength();

        XYChart.Series borderLineSeries1 = new XYChart.Series();
        XYChart.Series borderLineSeries2 = new XYChart.Series();
        ObservableList datas1 = FXCollections.observableArrayList();
        ObservableList datas2 = FXCollections.observableArrayList();

        for (int t = 0; t <= time; t++) {
            double x = (t0 + 345*(Math.log(8*t+1)))*0.85;
            double x1, x2;
            if (t<=10) {
                x1 = x * 0.85;
                x2 = x * 1.15;
            } else if (t<=30) {
                x1 = x * 0.9;
                x2 = x * 1.1;
            } else {
                x1 = x * 0.95;
                x2 = x * 1.05;
            }

            datas1.add(new XYChart.Data(t, x1));
            datas2.add(new XYChart.Data(t, x2));
        }

        borderLineSeries1.setData(datas1);
        borderLineSeries2.setData(datas2);
        chart.getData().addAll(borderLineSeries1,borderLineSeries2);
    }

    //метод для чтения данных из файла и построения таблицы
    //предполагем, что структура генерируемого файла всегда одна и та же:
    //всегда используется первый лист книги Excel,
    // заголовок - первая строка, в заголовке указаны date, time, номер канала в формате T1, T2...
    //todo рефакторинг - очень длинный метод
    public void getInformationFromFile(File file, XYChart chart, TableView tableView)
                                        throws IOException, InvalidFormatException {
        OPCPackage pkg = OPCPackage.open(new File(fileAddress));
        XSSFWorkbook workbook = new XSSFWorkbook(pkg);
        Sheet sheet = workbook.getSheetAt(0);
        //определяем номера столбцов
        int dateIndex = 0;
        int timeIndex = 1;
        //максимальное число каналов - 8
        int t1Index = -1;
        int t2Index = -1;
        int t3Index = -1;
        int t4Index = -1;
        int t5Index = -1;
        int t6Index = -1;
        int t7Index = -1;
        int t8Index = -1;
        Date date = new Date();
        Row headerRow = sheet.getRow(0);
        //TODO обработать NullPointerException если канала не существует
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().toLowerCase().contains("date")) {
                dateIndex = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("time")) {
                timeIndex = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t1")) {
                t1Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t2")) {
                t2Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t3")) {
                t3Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t4")) {
                t4Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t5")) {
                t5Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t6")) {
                t6Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t7")) {
                t7Index = cell.getColumnIndex();
            }
            if (cell.getStringCellValue().toLowerCase().contains("t8")) {
                t8Index = cell.getColumnIndex();
            }
        }
        //устанавливаем дату
        if (sheet.getRow(1).getCell(dateIndex).getCellType() == CellType.NUMERIC) {
            date = sheet.getRow(1).getCell(dateIndex).getDateCellValue();
        }
        this.testDescription.setTestDate(date);

        //название опыта по умолчанию - по имени файла
        this.testDescription.setTestName(fileAddress.substring(0, (fileAddress.length()-3)));

        System.out.println("t7"+t7Index);
        System.out.println("t1" + t1Index);

        //добавляем в таблицу строки из файла для каждого канала
        for (Row row : sheet) {
            System.out.println(row.getRowNum());
            if (row.getRowNum() >= 1 & row.getCell(timeIndex) != null) {
                TableEntity entityT1 = getTableEntityFromFile(row,"T1", timeIndex, t1Index);
                TableEntity entityT2 = getTableEntityFromFile(row, "T2", timeIndex, t2Index);
                TableEntity entityT3 = getTableEntityFromFile(row, "T3", timeIndex, t3Index);
                TableEntity entityT4 = getTableEntityFromFile(row, "T4", timeIndex, t4Index);
                TableEntity entityT5 = getTableEntityFromFile(row, "T5", timeIndex, t5Index);
                TableEntity entityT6 = getTableEntityFromFile(row, "T6", timeIndex, t6Index);
                TableEntity entityT7 = getTableEntityFromFile(row, "T7", timeIndex, t7Index);
                TableEntity entityT8 = getTableEntityFromFile(row, "T8", timeIndex, t8Index);

                addTableEntity(tableView, entityT1, entityT2, entityT3, entityT4, entityT5, entityT6, entityT7, entityT8);
            }
        }
        workbook.close();
    }

    //метод для получения сущности строки из файла Excel
    //принимает номер строки файла, имя канала, индексы колонок времени и канала
    public TableEntity getTableEntityFromFile (Row row, String channelName, int timeIndex, int channelIndex) {
        //обязательно &&, иначе "падает" вторая проверка
        if (channelIndex > 0 && row.getCell(channelIndex) != null) {
            TableEntity tableEntity = new TableEntity(this.entityCounter,
                channelName,
                row.getCell(timeIndex).getNumericCellValue(),
                row.getCell(channelIndex).getNumericCellValue());
            entityCounter++;
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

    //метод для отрисовки на графике линии для конкретного канала (термопары)
    public void addLineToChart(String channelName, String lineName, TableView tableView) {
        for (Object entity : tableView.getItems()) {
            TableEntity tableEntity = (TableEntity) entity;
            //todo
        }
    }

    public void init() {
    }

    public static void main(String[] args) {
        launch(args);
    }


}
