package domain;

import java.time.LocalDate;
import java.util.Date;
import javafx.scene.control.Label;

/**
 * @author patronovskiy
 * @link https://github.com/patronovskiy
 */

//класс, представляющий описание опыта
public class TestDescription {
    private String testName;            //название опыта (по умолчанию это имя файла)
    private Date testDate;              //дата проведения опыта
    private double initialValue;        //начальная температура todo среднее первое показание термопар соотв. типа
    private double criticalTemperature; //температура окончания опыта, по ГОСТ это 500 °С (среднее на образце)
    private int scaleLength;            //длина шкалы/время проведения опыта
    private String notes;               //примечания

    //КОНСТРУКТОРЫ
    public TestDescription(String testName, Date testDate, double initialValue, double criticalTemperature,
                           int scaleLength, String notes) {
        this.testName = testName;
        this.testDate = testDate;
        this.initialValue = initialValue;
        this.criticalTemperature = criticalTemperature;
        this.scaleLength = scaleLength;
        this.notes = notes;
    }

    public TestDescription() {
    }

    //ГЕТТЕРЫ И СЕТТЕРЫ
    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }

    public double getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(double initialValue) {
        this.initialValue = initialValue;
    }

    public double getCriticalTemperature() {
        return criticalTemperature;
    }

    public void setCriticalTemperature(double criticalTemperature) {
        this.criticalTemperature = criticalTemperature;
    }

    public int getScaleLength() {
        return scaleLength;
    }

    public void setScaleLength(int scaleLength) {
        this.scaleLength = scaleLength;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
