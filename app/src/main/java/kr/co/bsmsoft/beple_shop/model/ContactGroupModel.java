package kr.co.bsmsoft.beple_shop.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ContactGroupModel implements Parcelable {

    private int id;
    private String groupName;
    private String groupId;
    private ArrayList<CustomerModel> groupMember = new ArrayList<>();
    private int isSelected = 0; // 앱 안에서만 사용, 기본값은 1 (선택)

    public ContactGroupModel() {}

    public ContactGroupModel(Parcel src) {
        id = src.readInt();
        groupName = src.readString();
        groupId = src.readString();
        groupMember = src.readArrayList(CustomerModel.class.getClassLoader());
        isSelected = src.readInt();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(groupName);
        dest.writeString(groupId);
        dest.writeList(groupMember);
        dest.writeInt(isSelected);
    }

    public static final Creator<ContactGroupModel> CREATOR = new Creator<ContactGroupModel>() {

        public ContactGroupModel createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new ContactGroupModel(source);
        }

        public ContactGroupModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return new ContactGroupModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getGroupMemberCount() {
        return groupMember.size();
    }

    public ArrayList<CustomerModel> getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(ArrayList<CustomerModel> groupMember) {
        this.groupMember = groupMember;
    }

    public int isSelected() {
        return isSelected;
    }

    public void isSelected(int isSelected) {
        this.isSelected = isSelected;
    }
}
