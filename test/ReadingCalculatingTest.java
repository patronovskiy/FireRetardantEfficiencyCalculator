import domain.EntityCounter;
import domain.TableEntity;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import service.ChartService;
import service.ReadingFileService;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

//тестовое приложение для проверки методов чтения файла и расчета результата
public class ReadingCalculatingTest extends Application {

    //переменная, хранящая результат испытания
    private static Double result = 0D;

    //создаем небольшое приложение javafx с неполным функционалом
    //оно должно загрузить тестовый файл и рассчитать результат испытания
    @Override
    public void start(Stage primaryStage) throws Exception {
        //создаем необходимые элементы приложения
        TableView<TableEntity> tableView = new TableView<>();
        TextField testDateValue = new TextField();
        TextField testNameValue = new TextField();
        EntityCounter entityCounter = new EntityCounter();
        int channelsCounter = 0;
        NumberAxis xOwenAxis = new NumberAxis();
        NumberAxis yOwenAxis = new NumberAxis();
        XYChart chart = new LineChart<Number, Number>(xOwenAxis, yOwenAxis);
        TextField sampleChannelsValue = new TextField("1,2,3");
        TextField resultValue = new TextField();

        //создаем сервисы, предоставляющие приложению методы чтения файла и расчета результата
        ReadingFileService readingFileService = new ReadingFileService();
        ChartService chartService = new ChartService();

        //указываем путь к тестовому файлу
        File file = new File("test/resource/testFile.xlsx");

        try {
            //читаем тестовый файл
            readingFileService.getInformationFromFile(file,
                    tableView, testDateValue, testNameValue, entityCounter, channelsCounter);
            //рассчитываем результат
            chartService.calculateResult(tableView,chart, sampleChannelsValue, resultValue);
            //выгружаем результат в глобальную переменную
            result = Double.parseDouble(resultValue.getText());
        } catch (Exception ex) {
            System.out.println("Ошибка в тесте 1 " + ex);
        }
        //закрываем приложение
        Platform.exit();
    }

    @Test
    public void readingCalculatingTest() {
        //запускаем тестовое приложение
        launch(null);
        //сравниваем полученный результат с заранее известным результатом
        assertEquals(44.0, result);
    }
}
