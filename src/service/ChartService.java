package service;

import com.sun.javafx.charts.Legend;
import domain.TableEntity;
import domain.TestDescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import org.apache.poi.ss.formula.functions.T;

import java.util.Comparator;

//вспомогательный класс для отрисовки графика
public class ChartService {

    final int AXIS_LENGTH_RESERVE = 5;

    //метод для создания ограничивающих линий на графике для печных термопар
    //эти линии ограничивают допустимые значения температуры в печи по ГОСТ
    public void createBorderLines(XYChart chart, double time, double t0, TableView<TableEntity> tableView, String owenChannels) {

        //будущие линии на графике
        //ограничительные линии
        XYChart.Series borderLineSeries1 = new XYChart.Series();
        XYChart.Series borderLineSeries2 = new XYChart.Series();
        //средняя температура в печи
        XYChart.Series owenAverageSeries = new XYChart.Series();
        //данные для линий
        ObservableList datas1 = FXCollections.observableArrayList();
        ObservableList datas2 = FXCollections.observableArrayList();
        ObservableList owenAverageDatas = FXCollections.observableArrayList();

        //сортируем таблицу по времени измерения
        ObservableList<TableEntity> sortedTableEntities = tableView.getItems().sorted(TableEntity.sortByMinutes);

        //переменные для определения исходной температуры
        double initialTemp = 0;
        double initialTempsCount = 0;
        //счетчик времени
        double currentTime = 0;

        //читаем данные из отсортированного списка и выбираем нужные каналы
        for (Object entity : sortedTableEntities) {
            TableEntity tableEntity = (TableEntity) entity;
            int averageTemp = 0;
            int channelsCount = 0;
            if(owenChannels.contains(tableEntity.getChannel())) {
                if (currentTime == tableEntity.getMinutes()) {
                    averageTemp += tableEntity.getTemperature();
                    channelsCount++;
                }
            }
            //добавление на график среденей темппературы в печи
            averageTemp = averageTemp / channelsCount;
            owenAverageDatas.add(new XYChart.Data(currentTime, averageTemp));

            //построение ограничивающих линий
            

        }




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

        //добавляем легенду
        borderLineSeries1.setName("Граница темп. в печи");
        borderLineSeries2.setName("Граница темп. в печи");
    }


    //метод для отрисовки на графике линии для конкретного канала (термопары)
    //код дублируется, чтобы читать все данные за 1 проход (мы знаем, что максимум 8 каналов)
    public void addLinesToChart(TableView tableView, XYChart chart) {

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
        double initialTempsCount = 0;

        //читаем данные из таблицы
        for (Object entity : tableView.getItems()) {
            TableEntity tableEntity = (TableEntity) entity;

            //находим длину шкалы (максимальное время)
            if (tableEntity.getMinutes() > maxTime) {
                maxTime = tableEntity.getMinutes();
            }

            //находим начальное значение температуры
            if(tableEntity.getMinutes() == 0.0) {
                initialTemp+=tableEntity.getMinutes();
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

        //запас шкалы 10 минут
        maxTime+=AXIS_LENGTH_RESERVE;
        //начальная температура
        initialTemp = initialTemp / initialTempsCount;
        //строим ограничительные линии для температуры в печи
        createBorderLines(chart, maxTime, initialTemp);
    }



}
