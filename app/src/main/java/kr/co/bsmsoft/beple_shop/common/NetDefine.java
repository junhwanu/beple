package kr.co.bsmsoft.beple_shop.common;


public interface NetDefine {

    public static final String SERVER_URL = "http://www.beple.or.kr";

    public final static String GCM_SENDER_ID = "239085231209";

    public final static String KEY_PLATFORM = "platform";
    public final static String ANDROID = "ANDROID";
    public final static String KEY_ID = "id";
    public final static String KEY_APP_VERSION = "app_version";
    public final static String KEY_UPDATE_MSG = "update_msg";
    public final static String KEY_UPDATE_ACTION = "update_action";
    public final static String KEY_SYSTEM_MSG = "system_msg";
    public final static String KEY_SYSTEM_ACTION = "system_action";
    public final static String KEY_USER_NAME = "user_name";
    public final static String KEY_USER_PASSWORD = "user_pwd";
    public final static String KEY_LOGIN_INFO = "login_info";
    public final static String KEY_MARKET_URL = "market_url";
    public final static String KEY_ITEM_ID = "item_id";
    public final static String KEY_AUTO_LOGIN = "auto_login";
    public final static String KEY_SHOP_NAME = "shop_name";
    public final static String KEY_OWNER_NAME = "owner_name";
    public final static String KEY_PHONE1 = "phone1";
    public final static String KEY_PHONE2 = "phone2";
    public final static String KEY_PHONE3 = "phone3";
    public final static String KEY_MOBILE = "mobile";
    public final static String KEY_IMAGE_SUB1 = "image_sub1";
    public final static String KEY_IMAGE_SUB2 = "image_sub2";
    public final static String KEY_IMAGE_SUB3 = "image_sub3";
    public final static String KEY_ADDRESS1 = "address1";
    public final static String KEY_ADDRESS2 = "address2";
    public final static String KEY_ADDRESS3 = "address3";
    public final static String KEY_EMAIL = "email";
    public final static String KEY_POINT_LOTTO = "point_lotto";
    public final static String KEY_POINT_SMS = "point_sms";
    public final static String KEY_ORG_ID = "org_id";
    public final static String KEY_IMAGE = "image";
    public final static String KEY_C_COUNT = "c_count";
    public final static String KEY_SHOP_ID = "shop_id";
    public final static String KEY_NOTICE_URL = "notice_url";
    public final static String KEY_FAQ_URL = "faq_url";
    public final static String KEY_EVENT_LIST = "event_list";
    public final static String KEY_CUSTOMER_LIST = "customer_list";
    public final static String KEY_CUSTOMER_LIST_SELECTED = "customer_list_selected";
    public final static String KEY_CUSTOMER_GROUP_LIST = "customer_group_list";

    public final static String KEY_EVENT = "event";
    public final static String KEY_CUSTOMER_NM = "customer_nm";
    public final static String KEY_BIRTH = "birth";
    public final static String KEY_GENDER = "gender";
    public final static String KEY_ADDRESS = "address";
    public final static String KEY_CAL_TYPE = "cal_type";
    public final static String KEY_TIMES = "times";
    public final static String KEY_NUM_OF_LOTTO = "num_of_lotto";
    public final static String KEY_LOTTO_URL= "lotto_url";
    public final static String KEY_LOTTO_MSG= "lotto_msg";
    public final static String KEY_EXPIRED= "expired";
    public final static String KEY_EXPIRED_DT= "expired_dt";

    public final static String KEY_USER = "user";
    public final static String KEY_TIMESTAMP = "timestamp";
    public final static String KEY_TOKEN = "token";
    public final static String KEY_VERSION = "version";
    public final static String KEY_DEVICE_KEY = "device_key";
    public final static String KEY_ACCESS_TOKEN = "access_token";
    public final static String KEY_PHONE = "phone";
    public final static String KEY_URL = "url";
    public final static String KEY_ALLOW_PUSH = "allow_push";
    public final static String KEY_PUSH_ID = "push_id";
    public final static String KEY_REG_DT= "reg_dt";
    public final static String KEY_EVENT_NAME= "event_nm";
    public final static String KEY_MESSAGE= "message";
    public final static String KEY_STATUS= "status";
    public final static String KEY_TARGET_GROUP= "target_group";
    public final static String KEY_SEND_TYPE= "send_type";
    public final static String KEY_UPD_DT= "upd_dt";
    public final static String KEY_SEND_DT= "send_dt";
    public final static String KEY_IMAGE_LIST= "image_list";
    public final static String KEY_FILE_URL= "path";
    public final static String KEY_FILE_DESC= "file_desc";
    public final static String KEY_FILE_NAME= "file_name";
    public final static String KEY_IS_USE= "is_use";
    public final static String KEY_IMAGE_PATH = "image_path";
    public final static String KEY_IMAGE_URL = "image_url";
    public final static String KEY_CATEGORY_LIST = "category_list";
    public final static String KEY_CATEGORY_NAME = "cate_nm";
    public final static String KEY_CATEGORY_ID = "cate_id";
    public final static String KEY_COUNT = "count";
    public final static String KEY_PAGE = "page";
    public final static String KEY_SERVER_ADDR= "server_addr";
    public final static String KEY_MMS= "mms";
    public final static String KEY_MMS_TODAY= "today";
    public final static String KEY_MMS_MONTH= "month";
    public final static String KEY_ADMIN_ID= "admin_id";
    public final static String KEY_ADMIN_PWD= "admin_pwd";
    public final static String KEY_SALES_NAME= "sales_name";
    public final static String KEY_TYPE_CODE= "type_code";
    public final static String KEY_USER_ID= "user_id";
    public final static String KEY_USER_TYPE= "user_type";
    public final static String KEY_KIND= "kind";


    public final static String MOBILE_URL = "/api/mobile/";
    public final static String SMS_URL = "/api/sms/";
    public final static String LOTTO_URL = "/api/lotto/";
    public final static String USER_URL = "/api/user/";
    public final static String LOGIN_URL = "/api/login";
    public final static String IMAGE_URL = "/api/image";
    public final static String CATEGORY_URL = "/api/category";
    public final static String MMS_URL = "/api/mms";
    public final static String SHOP_URL = "/shop";
    public final static String ADMIN_URL = "/admin";

    public final static int RESPONSE_OK = 0;
    public final static int ERROR_INVALID_LOGIN_INFO = -103;
    public final static int ERROR_EXIST_USERID = -104;
    public final static int ERROR_NOT_EXIST_USERID = -106;

    public final static int REQUEST_CODE_IMAGE_SELECT_ACTIVITY = 1000;
    public final static int REQUEST_CODE_CUSTOMER_LIST_ACTIVITY = 1001;
    public final static int REQUEST_CODE_PHONE_LIST_ACTIVITY = 1002;
    public final static int REQUEST_CODE_CONTACTS_ACTIVITY = 1003;
    public final static int REQUEST_CODE_CONTACTS_GROUP_ACTIVITY = 1004;

    public static final int USER_TYPE_SHOP = 1;        // 가맹점
    public static final int USER_TYPE_AGENCY = 2;      // 영업점

}
