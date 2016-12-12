package kr.co.bsmsoft.beple_shop.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jinmo on 2016-12-07.
 */

public class LottoSetModel {
    private HashMap<Integer, ArrayList < LottoModel>> set;
    private int times;

    public LottoSetModel() {
        set = new HashMap<>();
    }

    public void addLotto(LottoModel lottoModel) {
        if(set == null) set = new HashMap<>();

        ArrayList<LottoModel> arrLottoModel;
        if(set.containsKey(lottoModel.getCustomer_id())) arrLottoModel = set.get(lottoModel.getCustomer_id());
        else arrLottoModel = new ArrayList<>();

        arrLottoModel.add(lottoModel);
        set.put(lottoModel.getCustomer_id(), arrLottoModel);
    }

    public ArrayList<LottoModel> getLottoSetByCustomerID(int customerID) {
        return set.get(customerID);
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public HashMap<Integer, ArrayList<LottoModel>> getSet() {
        return set;
    }
}
