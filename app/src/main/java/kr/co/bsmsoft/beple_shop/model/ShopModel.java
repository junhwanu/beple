package kr.co.bsmsoft.beple_shop.model;

import android.util.Log;

public class ShopModel {

    private int id;
    private String shopName;
    private String ownerName;
    private String phone1;
    private String phone2;
    private String phone3;
    private String mobile;
    private String imageSub1;
    private String imageSub2;
    private String imageSub3;
    private String address1;
    private String address2;
    private String address3;
    private String email;
    private int pointLotto;
    private int pointSms;
    private String regDt;
    private int orgId;
    private String image;
    private int cCount;
    private String expired;
    private String expiredDt;
    private String type;
    private String sign;

    public ShopModel() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone3() {
        return phone3;
    }

    public void setPhone3(String phone3) {
        this.phone3 = phone3;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getImageSub1() {
        return imageSub1;
    }

    public void setImageSub1(String imageSub1) {
        this.imageSub1 = imageSub1;
    }

    public String getImageSub2() {
        return imageSub2;
    }

    public void setImageSub2(String imageSub2) {
        this.imageSub2 = imageSub2;
    }

    public String getImageSub3() {
        return imageSub3;
    }

    public void setImageSub3(String imageSub3) {
        this.imageSub3 = imageSub3;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPointLotto() {
        return pointLotto;
    }

    public void setPointLotto(int pointLotto) {
        this.pointLotto = pointLotto;
    }

    public int getPointSms() {
        return pointSms;
    }

    public void setPointSms(int pointSms) {
        this.pointSms = pointSms;
    }

    public String getRegDt() {
        return regDt;
    }

    public void setRegDt(String regDt) {
        this.regDt = regDt;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getcCount() {
        return cCount;
    }

    public void setcCount(int cCount) {
        this.cCount = cCount;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getExpiredDt() {
        return expiredDt;
    }

    public void setExpiredDt(String expiredDt) {
        this.expiredDt = expiredDt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        if(sign == null || sign.equals("null") || sign.isEmpty() || sign.length() == 0) {
            this.sign = "";
        } else {
            this.sign = sign;
        }
    }
}
