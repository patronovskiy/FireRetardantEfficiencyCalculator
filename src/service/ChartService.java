package service;

import domain.TableEntity;
import domain.TestDescription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;

//вспомогательный класс для отрисовки графика
public class ChartService {

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


    //метод для отрисовки на графике линии для конкретного канала (термопары)
    public void addLineToChart(String channelName, String lineName, TableView tableView) {
        for (Object entity : tableView.getItems()) {
            TableEntity tableEntity = (TableEntity) entity;
            //todo
        }
    }

}
