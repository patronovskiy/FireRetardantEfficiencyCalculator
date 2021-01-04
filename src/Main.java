
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
import service.ChartService;
import service.ReadingFileService;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//главный класс приложения с настройкой интерфейса и управлением логикой, событиями
public class Main extends Application {
    //ВСПОМОГАТЕЛЬНЫЕ ПЕРЕМЕННЫЕ
    private TestDescription testDescription;                        //сущность-описание опыта
    private int entityCounter = 1;                                  //счетчик строк в таблице
    private int channelsCounter = 0;                                //счетчик количества термопар
    private  ArrayList<String> owenChannels = new ArrayList<>();    //список печных термопар
    private ArrayList<String> sampleChannels = new ArrayList<>();   //список термопар на образце

    //объекты вспомогательных классов
    ChartService chartService = new ChartService();
    ReadingFileService readingFileService = new ReadingFileService();

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

        //график температур с термопар
        GridPane owenChartPane = new GridPane();
        owenChartPane.getStyleClass().add("chart-wrapper");
        NumberAxis xOwenAxis = new NumberAxis();
        NumberAxis yOwenAxis = new NumberAxis();
        XYChart owenChart = new LineChart<Number, Number>(xOwenAxis, yOwenAxis);
        owenChart.getStyleClass().add("owen-chart");
        owenChartPane.add(owenChart,0,1,2,1);
        chartService.createBorderLines(new TestDescription("1", new Date(), 20, 500, 150, "test"), owenChart);

        //результат испытания
        GridPane resultPane = new GridPane();

        chartPane.addRow(0, chartLabel);
        chartPane.addRow(1, owenChartPane);

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
                    testDescription = new TestDescription();
                    try{
                        readingFileService.getInformationFromFile(file, owenChart, tempTable, testDateValue, testNameValue, entityCounter, channelsCounter);
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

    public void init() {
    }

    public static void main(String[] args) {
        launch(args);
    }


}
