package kr.co.bsmsoft.beple_shop.model;

import java.util.ArrayList;

/**
 * Created by Jinmo on 2016-12-08.
 */

public class LottoModel {
    private int id;
    private int shop_id;
    private int event_id;
    private int customer_id;
    private ArrayList<Integer> lotto_num;
    private int times;
    private String reg_dt;
    private int send_result;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShop_id() {
        return shop_id;
    }

    public void setShop_id(int shop_id) {
        this.shop_id = shop_id;
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public ArrayList<Integer> getLotto_num() {
        return lotto_num;
    }

    public void setLotto_num(String strLotto) {
        String[] arrLotto = strLotto.split(",");

        lotto_num = new ArrayList<>();
        for(int i=0;i<arrLotto.length;i++)
            lotto_num.add(Integer.valueOf(arrLotto[i].trim()));
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getReg_dt() {
        return reg_dt;
    }

    public void setReg_dt(String reg_dt) {
        this.reg_dt = reg_dt;
    }

    public int getSend_result() {
        return send_result;
    }

    public void setSend_result(int send_result) {
        this.send_result = send_result;
    }
}
