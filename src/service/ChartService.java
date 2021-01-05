package service;

import com.sun.javafx.charts.Legend;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import domain.TableEntity;
import domain.TestDescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.poi.ss.formula.functions.T;

import java.util.Comparator;

//вспомогательный класс для отрисовки графика
public class ChartService {

    //запас по длине шкалы
    final int AXIS_LENGTH_RESERVE = 5;

    //метод для создания ограничивающих линий на графике для печных термопар
    //эти линии ограничивают допустимые значения температуры в печи по ГОСТ
    public void createBorderLines(XYChart chart, double initialTemp_T0, TableView<TableEntity> tableView, String owenChannels) {

        //будущие линии на графике
        //ограничительные линии
        XYChart.Series borderLineSeries1 = new XYChart.Series();
        XYChart.Series borderLineSeries2 = new XYChart.Series();
        //температура в печи по ГОСТу (Т)
        XYChart.Series normalTempSeries = new XYChart.Series();
        //средняя температура в печи
        XYChart.Series owenAverageSeries = new XYChart.Series();
        //данные для линий
        ObservableList datas1 = FXCollections.observableArrayList();
        ObservableList datas2 = FXCollections.observableArrayList();
        ObservableList owenAverageDatas = FXCollections.observableArrayList();
        ObservableList normalTempDatas = FXCollections.observableArrayList();

        //сортируем таблицу по времени измерения
        ObservableList<TableEntity> sortedTableEntities = tableView.getItems().sorted(TableEntity.sortByMinutes);

        //счетчик времени
        double currentTime_t = 0.0;
        //нормированная температура в печи
        double currentNormalTempereture_T = 0;
        //переменные для расчета средней температуры
        int averageTemp = 0;
        int channelsCount = 0;

        //читаем данные из отсортированного списка и выбираем нужные каналы
        for (Object entity : sortedTableEntities) {
            TableEntity tableEntity = (TableEntity) entity;

            if(owenChannels.contains(tableEntity.getChannel())) {
                if (currentTime_t == tableEntity.getMinutes()) {
                    averageTemp += tableEntity.getTemperature();
                    channelsCount++;
                } else {
                    //добавление на график среденей температуры в печи
                    if (channelsCount != 0) {
                        averageTemp = averageTemp / channelsCount;
                        owenAverageDatas.add(new XYChart.Data(currentTime_t, averageTemp));
                    }

                    //построение ограничивающих линий
                    //рассчитываем по формуле из ГОСТА требуемую температуру в печи
                    currentNormalTempereture_T = initialTemp_T0 + (345 * Math.log10((8*currentTime_t) + 1));

                    //рассчитываем верхнюю и нижнюю границу отклонений, добавляем на график
                    double x1;
                    double x2;
                    if (currentTime_t<=14) {
                        x1 = currentNormalTempereture_T * 0.85;
                        x2 = currentNormalTempereture_T * 1.15;
                    } else if (currentTime_t<=44) {
                        x1 = currentNormalTempereture_T * 0.9;
                        x2 = currentNormalTempereture_T * 1.1;
                    } else {
                        x1 = currentNormalTempereture_T * 0.95;
                        x2 = currentNormalTempereture_T * 1.05;
                    }
                    datas1.add(new XYChart.Data(currentTime_t, x1));
                    datas2.add(new XYChart.Data(currentTime_t, x2));
                    normalTempDatas.add(new XYChart.Data(currentTime_t, currentNormalTempereture_T));

                    //сброс значений для следующей итерации
                    averageTemp = 0;
                    channelsCount=0;
                    currentTime_t++;
                    averageTemp += tableEntity.getTemperature();
                    channelsCount++;
                }
            }
        }

        //добавляем линии на график
        borderLineSeries1.setData(datas1);
        borderLineSeries2.setData(datas2);
        owenAverageSeries.setData(owenAverageDatas);
        normalTempSeries.setData(normalTempDatas);
        chart.getData().addAll(borderLineSeries1,borderLineSeries2, owenAverageSeries, normalTempSeries);

        //добавляем легенду
        borderLineSeries1.setName("Граница темп. в печи");
        borderLineSeries2.setName("Граница темп. в печи");
        owenAverageSeries.setName("Средняя температура в печи");
        normalTempSeries.setName("Требуемая температура в печи");
    }


    //метод для отрисовки на графике линии для конкретного канала (термопары)
    //код дублируется, чтобы читать все данные за 1 проход (мы знаем, что максимум 8 каналов)
    public void addLinesToChart(TableView tableView, XYChart chart, TextField owenChannelsValue, TextField initialTempValue) {

        //проверяем, что в таблице есть данные
        if (tableView.getItems().size() > 0) {
            //создаем 8 линий
            XYChart.Series series1 = new XYChart.Series();
            XYChart.Series series2 = new XYChart.Series();
            XYChart.Series series3 = new XYChart.Series();
            XYChart.Series series4 = new XYChart.Series();
            XYChart.Series series5 = new XYChart.Series();
            XYChart.Series series6 = new XYChart.Series();
            XYChart.Series series7 = new XYChart.Series();
            XYChart.Series series8 = new XYChart.Series();

            ObservableList datas1 = FXCollections.observableArrayList();
            ObservableList datas2 = FXCollections.observableArrayList();
            ObservableList datas3 = FXCollections.observableArrayList();
            ObservableList datas4 = FXCollections.observableArrayList();
            ObservableList datas5 = FXCollections.observableArrayList();
            ObservableList datas6 = FXCollections.observableArrayList();
            ObservableList datas7 = FXCollections.observableArrayList();
            ObservableList datas8 = FXCollections.observableArrayList();

            //переменная для хранения максимального времени (для опред. длины шкалы Х)
            double maxTime = 0;
            //переменные для определения исходной температуры
            double initialTemp = 0;
            boolean isInitialTempStated = false;
            double initialTempsCount = 0;
            //проверяем, указана ли начальная температура в таблице

            try {
                initialTemp = Double.parseDouble(initialTempValue.getText());
                isInitialTempStated = true;
            } catch (Exception ex) {
                System.out.println("Неверно задана или не задана начальная температура");
            }

            //читаем данные из таблицы
            for (Object entity : tableView.getItems()) {
                TableEntity tableEntity = (TableEntity) entity;

                //находим длину шкалы (максимальное время)
                if (tableEntity.getMinutes() > maxTime) {
                    maxTime = tableEntity.getMinutes();
                }

                //находим начальное значение температуры
                if(!isInitialTempStated && tableEntity.getMinutes() == 0.0) {
                    initialTemp+=tableEntity.getTemperature();
                    initialTempsCount++;
                }

                //добавляем данные в соответсвующую линию в зависимости от названия
                switch (tableEntity.getChannel()) {
                    case "1":
                        datas1.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "2":
                        datas2.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "3":
                        datas3.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "4":
                        datas4.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "5":
                        datas5.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "6":
                        datas6.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "7":
                        datas7.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                    case "8":
                        datas8.add(new XYChart.Data(tableEntity.getMinutes(), tableEntity.getTemperature()));
                        break;
                }
            }

            //добавляем данные на график
            series1.setData(datas1);
            series2.setData(datas2);
            series3.setData(datas3);
            series4.setData(datas4);
            series5.setData(datas5);
            series6.setData(datas6);
            series7.setData(datas7);
            series8.setData(datas8);

            chart.getData().addAll(series1, series2, series3, series4, series5, series6, series7, series8);

            //добавляем легенду
            series1.setName("1");
            series2.setName("2");
            series3.setName("3");
            series4.setName("4");
            series5.setName("5");
            series6.setName("6");
            series7.setName("7");
            series8.setName("8");

            //добавляем запас по длине шкалы
            maxTime+=AXIS_LENGTH_RESERVE;
            //начальная температура
            if(!isInitialTempStated) {
                initialTemp = initialTemp / initialTempsCount;
                initialTempValue.setText(String.valueOf(initialTemp));
            }

            //строим ограничительные линии для температуры в печи
            String owenChannels = owenChannelsValue.getText();
            createBorderLines(chart, initialTemp, tableView, owenChannels);
        }
    }

    //метод для отрисовки на графике линии для конкретного канала (термопары)
    //код дублируется, чтобы читать все данные за 1 проход (мы знаем, что максимум 8 каналов)
    public void calculateResult(TableView tableView, XYChart chart, TextField sampleChannelsValue, TextField resultValue) {

        //проверяем, что таблица не пустая
        if (tableView.getItems().size() > 0) {

            //проверяем, что введены номера термопар
            if(sampleChannelsValue.getText().length() != 0) {
                //создаем линию
                XYChart.Series series = new XYChart.Series();
                ObservableList datas = FXCollections.observableArrayList();

                //переменные для расчета средней температуры
                int averageTemp = 0;
                int channelsCount = 0;
                double currentTime = 0.0;
                double result = 0;

                //сортируем таблицу по времени измерения
                ObservableList<TableEntity> sortedTableEntities = tableView.getItems().sorted(TableEntity.sortByMinutes);

                //читаем данные из таблицы
                for (Object entity : sortedTableEntities) {
                    TableEntity tableEntity = (TableEntity) entity;

                    if(sampleChannelsValue.getText().contains(tableEntity.getChannel())) {
                        if (currentTime == tableEntity.getMinutes()) {
                            averageTemp += tableEntity.getTemperature();
                            channelsCount++;
                        } else {
                            //добавление на график среденей температуры в печи
                            if (channelsCount != 0) {
                                averageTemp = averageTemp / channelsCount;
                                if (averageTemp >= 500.0) {
                                    result = currentTime;
                                    break;
                                }
                            }
                            //сброс значений для следующей итерации
                            averageTemp = 0;
                            channelsCount=0;
                            currentTime++;
                            averageTemp += tableEntity.getTemperature();
                            channelsCount++;
                        }
                    }
                }
                //добавляем на график вертикальную линию
                datas.add(new XYChart.Data(currentTime, 0));
                datas.add(new XYChart.Data(currentTime, 700));
                series.setData(datas);
                series.setName("Результат испытания - " + result + " мин");
                chart.getData().add(series);

                //вписываем значение в таблицу
                resultValue.setText(String.valueOf(result));

            } else {
                System.out.println("Не введены номера термопар на образце");
            }
        }
    }
}
