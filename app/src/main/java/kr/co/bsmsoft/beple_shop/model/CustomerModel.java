package kr.co.bsmsoft.beple_shop.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CustomerModel implements Parcelable {

    private int id;
    private int shopId;
    private String customerName;
    private String phone;
    private String birth;
    private String gender;
    private String address;
    private String calType;
    private String email;
    private int groupId;
    private String regDt;
    private String updDt;
    private int isSelected = 1; // 앱 안에서만 사용, 기본값은 1 (선택)

    public CustomerModel() {}

    public CustomerModel(Parcel src) {

        id = src.readInt();
        shopId = src.readInt();
        customerName = src.readString();
        phone = src.readString();
        birth = src.readString();
        gender = src.readString();
        address = src.readString();
        calType = src.readString();
        email = src.readString();
        groupId = src.readInt();
        regDt = src.readString();
        updDt = src.readString();
        isSelected = src.readInt();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeInt(shopId);
        dest.writeString(customerName);
        dest.writeString(phone);
        dest.writeString(birth);
        dest.writeString(gender);
        dest.writeString(address);
        dest.writeString(calType);
        dest.writeString(email);
        dest.writeInt(groupId);
        dest.writeString(regDt);
        dest.writeString(updDt);
        dest.writeInt(isSelected);

    }

    public static final Creator<CustomerModel> CREATOR = new Creator<CustomerModel>() {

        public CustomerModel createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new CustomerModel(source);
        }

        public CustomerModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return new CustomerModel[size];
        }
    };

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCalType() {
        return calType;
    }

    public void setCalType(String calType) {
        this.calType = calType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
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

    public int isSelected() {
        return isSelected;
    }

    public void isSelected(int isSelected) {
        this.isSelected = isSelected;
    }
}
