package kr.co.bsmsoft.beple_shop.model;

import java.util.ArrayList;

public class MmsModel {

    private int id;
    private int shopId;
    private String type;
    private String message;
    private String image_url;
    private ArrayList<String> phoneList;
    private int c_cnt;
    private String reg_dt;

    public MmsModel() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.replaceAll("\\n", "<br>");
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public ArrayList<String> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(ArrayList<String> phoneList) {
        this.phoneList = phoneList;
    }

    public int getC_cnt() {
        return c_cnt;
    }

    public void setC_cnt(int c_cnt) {
        this.c_cnt = c_cnt;
    }

    public String getReg_dt() {
        return reg_dt;
    }

    public void setReg_dt(String reg_dt) {
        this.reg_dt = reg_dt;
    }
}
