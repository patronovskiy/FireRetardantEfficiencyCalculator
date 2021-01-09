
import java.io.File;
import java.io.IOException;
import java.net.URL;

import domain.EntityCounter;
import domain.TableEntity;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import service.ChartService;
import service.ReadingFileService;
import service.SavingFileService;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//главный класс приложения с настройкой интерфейса и управлением логикой, событиями
public class Main extends Application {

    //ВСПОМОГАТЕЛЬНЫЕ ПЕРЕМЕННЫЕ
    private EntityCounter entityCounter = new EntityCounter();      //счетчик строк в таблице
    private int channelsCounter = 0;                                //счетчик количества термопар

    final String DEFAULT_SAMPLE_CHANNELS = "1,2,3";                 //номера термопар на образце по умолчанию
    final String DEFAULT_OWEN_CHANNELS = "4,5,6,7,8";               //номера термопар в печи по умолчанию

    boolean isDataLoaded = false;                                   //флаг, отслеживающий загружена ли информация

    //объекты вспомогательных классов
    ChartService chartService = new ChartService();
    ReadingFileService readingFileService = new ReadingFileService();
    SavingFileService savingFileService = new SavingFileService();

    //ГЛАВНЫЙ МЕТОД ПРОГРАММЫ, СОЗДАЮЩИЙ ПРИЛОЖЕНИЕ
    @Override
    public void start(Stage primaryStage) throws Exception {
        //настройка компоновщика
        //корневой компоновщик
        GridPane root = new GridPane();

        //МЕНЮ
        //разметка меню
        MenuBar mainMenuBar = new MenuBar();
        Menu fileMenu = new Menu("Файл");
        MenuItem openMenuItem = new MenuItem("Открыть файл");
        MenuItem saveMenuItem = new MenuItem("Сохранить отчет");
        fileMenu.getItems().addAll(openMenuItem, saveMenuItem);
        Menu helpMenu = new Menu("Справка");
        MenuItem helpMenuItem = new MenuItem("Показать справку");
        helpMenu.getItems().add(helpMenuItem);
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

        Label sampleChannels = new Label("№ термопар на образце:");
        TextField sampleChannelsValue = new TextField(DEFAULT_SAMPLE_CHANNELS);

        Label owenChannels = new Label("№ термопар в печи:");
        TextField owenChannelsValue = new TextField(DEFAULT_OWEN_CHANNELS);

        Label result = new Label("Результат испытания, мин:");
        TextField resultValue = new TextField();

        Button calculateResultsButton = new Button("Рассчитать результат");
        calculateResultsButton.getStyleClass().add("tableview-button");

        Button rebuildChartButton = new Button("Перестроить график");
        rebuildChartButton.getStyleClass().add("tableview-button");

        infoTable.setGridLinesVisible(true);
        infoTable.addColumn(0, testName, testDate, initialTemp, criticalTemp, notes, sampleChannels, owenChannels, result);
        infoTable.addColumn(1,
            testNameValue, testDateValue, initialTempValue, criticalTempValue, notesValue, sampleChannelsValue, owenChannelsValue, resultValue);
        infoTable.add(rebuildChartButton, 0, 8, 2, 1);
        infoTable.add(calculateResultsButton, 0, 9, 2, 1);

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
        TextField chartLabel = new TextField("Графики зависимости температуры от времени");
        chartLabel.getStyleClass().addAll("chart-label", "main-chart-label");

        //график температур с термопар
        GridPane owenChartPane = new GridPane();
        owenChartPane.getStyleClass().add("chart-wrapper");
        NumberAxis xOwenAxis = new NumberAxis();
        NumberAxis yOwenAxis = new NumberAxis();
        XYChart owenChart = new LineChart<Number, Number>(xOwenAxis, yOwenAxis);
        //добавляем подписи осей
        owenChart.getXAxis().setLabel("Время, мин");
        owenChart.getYAxis().setLabel("Температура, ᵒС");
        owenChart.getStyleClass().add("owen-chart");
        owenChartPane.addRow(0, owenChart);

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
                //Класс работы с диалогом выборки и сохранения
                FileChooser fileChooser = new FileChooser();
                //Заголовок диалога
                fileChooser.setTitle("Открыть документ");
                FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");//Расширение
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(primaryStage); //Указываем текущую сцену
                if (file != null) {
                    //Открытие файла
                    System.out.println("Процесс открытия файла");
                    try{
                        //удаление информации после предыдущих загруженных файлов
                        owenChart.getData().clear();
                        tempTable.getItems().clear();
                        entityCounter.clear();
                        readingFileService.clearInfoTable(testDateValue, testNameValue, initialTempValue, resultValue);
                        //загрузка новой информации из файла
                        readingFileService.getInformationFromFile(  file,
                                                                    tempTable, testDateValue, testNameValue,
                                                                    entityCounter, channelsCounter);
                        //устанавливаем флаг "информация загружена"
                        isDataLoaded = true;
                        chartService.addLinesToChart(tempTable, owenChart, owenChannelsValue, initialTempValue);
                    } catch (IOException ex1) {
                        System.out.println("IO Exception while opening file");
                    } catch (InvalidFormatException ex2) {
                        System.out.println("Invalid format exception while opening file");
                    } catch (Exception ex) {
                        System.out.println("Ошибка при чтении файла. Попробуйте открыть другой файл");
                    }
                }
            }
        };
        //назначаем обработчик пункту меню "Открыть файл"
        openMenuItem.setOnAction(onOpenFile);

        //сохранение отчета
        EventHandler<ActionEvent> onSaveReport = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                savingFileService.saveReport(   primaryStage,
                                                testNameValue,
                                                testDateValue,
                                                notesValue,
                                                sampleChannelsValue,
                                                owenChannelsValue,
                                                resultValue,
                                                chartPane);
            }
        };
        //назначаем обработчик пункту меню "Сохранить отчет"
        saveMenuItem.setOnAction(onSaveReport);

        //обработка событий на кнопках
        //перестроение графика по введенным номерам термопар
        EventHandler<ActionEvent> onRebuildChart = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isDataLoaded) {
                    owenChart.getData().clear();
                    chartService.addLinesToChart(tempTable, owenChart, owenChannelsValue, initialTempValue);
                }
            }
        };
        //назначаем обработчик кнопке "перестроить график"
        rebuildChartButton.setOnAction(onRebuildChart);

        //расчет результата по введенным номерам термопар
        EventHandler<ActionEvent> onCalculateResult = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(isDataLoaded) {
                    owenChart.getData().clear();
                    chartService.addLinesToChart(tempTable, owenChart, owenChannelsValue, initialTempValue);
                    chartService.calculateResult(tempTable, owenChart, sampleChannelsValue, resultValue);
                }
            }
        };
        //назначаем обработчик кнопке "рассчитать результат"
        calculateResultsButton.setOnAction(onCalculateResult);

        //открытие справки
        EventHandler<ActionEvent> onHelp = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Загружается справка");
                //создаем компоновщик
                GridPane helpPane = new GridPane();
                //справку загружаем в "браузер" как веб-страницу
                //создаем браузер
                WebView helpWebView = new WebView();
                WebEngine helpWebEngine = helpWebView.getEngine();
                //загружаем страницу
                try{
                    File file = new File("src/resource/help.html");
                    URL url= file.toURI().toURL();
                    helpWebEngine.load(url.toString());
                } catch (Exception ex) {
                    System.out.println("Ошибка при открытиии файла справки" + ex);
                }
                //добавляем браузер в компоновщик
                helpPane.getChildren().add(helpWebView);
                //создаем и показываем окно
                Scene helpScene = new Scene(helpPane);
                Stage helpStage = new Stage();
                helpStage.setScene(helpScene);
                helpStage.setTitle("Справка");
                helpStage.show();
            }
        };
        //назначаем обработчик пункту меню "Показать справку"
        helpMenuItem.setOnAction(onHelp);
    }

    //точка входа в приложение
    public static void main(String[] args) {
        launch(args);
    }
}
