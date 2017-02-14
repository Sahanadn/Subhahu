package com.house.security;

/**
 * Created by user on 20-12-2016.
 */
public class Visitor {
    int id;
    String name;
    String phone;
    String time;
    String date;
    String vehicleNumber;
    String status;
    String note;
    String dbId;
    String inTime;
    String outTime;

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Visitor() {

    }
    public Visitor(int id, String name, String phone, String time, String date, String vehicleNumber, String status, String note) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.time = time;
        this.date = date;
        this.vehicleNumber = vehicleNumber;
        this.status = status;
        this.note = note;
    }

    @Override
    public String toString() {
        return "Visitor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                ", dbId='" + dbId + '\'' +
                ", inTime='" + inTime + '\'' +
                ", outTime='" + outTime + '\'' +
                '}';
    }
}
