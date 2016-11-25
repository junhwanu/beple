package kr.co.bsmsoft.beple_shop.model;

import java.util.ArrayList;

public class EventModel {

    private String[] STATUS_NM = {"", "발송대기", "발송완료"};

    public static final int STATUS_READY = 1;
    public static final int STATUS_COMPLETE = 2;

    private int id;
    private int shopId;
    private String eventNm;
    private String message;
    private int status;
    private int targetGroup;
    private int sendType;
    private String regDt;
    private String updDt;
    private String sendDt;
    private int cCount;
    private int times;
    private int numberOfLotto;

    private ArrayList<CustomerModel> customers = new ArrayList<CustomerModel>();

    public EventModel() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getEventNm() {
        return eventNm;
    }

    public void setEventNm(String eventNm) {
        this.eventNm = eventNm;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(int targetGroup) {
        this.targetGroup = targetGroup;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getRegDt() {
        return regDt;
    }

    public void setRegDt(String regDt) {
        this.regDt = regDt;
    }

    public String getUpdDt() {
        return updDt;
    }

    public void setUpdDt(String updDt) {
        this.updDt = updDt;
    }

    public String getSendDt() {
        return sendDt;
    }

    public void setSendDt(String sendDt) {
        this.sendDt = sendDt;
    }

    public int getcCount() {
        return cCount;
    }

    public void setcCount(int cCount) {
        this.cCount = cCount;
    }

    public String getStatusName() {

        return STATUS_NM[status];
    }

    public void addCustomer(CustomerModel customer) {
        customers.add(customer);
    }

    public ArrayList<CustomerModel> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<CustomerModel> customers) {
        this.customers = customers;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getNumberOfLotto() {
        return numberOfLotto;
    }

    public void setNumberOfLotto(int numberOfLotto) {
        this.numberOfLotto = numberOfLotto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
