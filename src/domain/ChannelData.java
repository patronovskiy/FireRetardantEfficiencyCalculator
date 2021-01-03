package domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//Класс для описания данных одного канала
public class ChannelData {
    private String channelName;                     //название канала
    private ObservableList<XYChart.Data> datas;     //список данных - точек "время, температура"
    private ChannelType channelType;                //тип термопары - печная или на образце (enum)
                                                    // (чтобы можно было отобразить на разных графиках)

    //КОНСТРУКТОРЫ
    public ChannelData(String channelName, ObservableList datas, ChannelType channelType) {
        this.channelName = channelName;
        this.datas = datas;
        this.channelType = channelType;
    }

    public ChannelData() {
    }

    //ГЕТТЕРЫ И СЕТТЕРЫ
    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ObservableList getDatas() {
        return datas;
    }

    public void setDatas(ObservableList datas) {
        this.datas = datas;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }
}
