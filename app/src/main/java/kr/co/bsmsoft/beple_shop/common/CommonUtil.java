package kr.co.bsmsoft.beple_shop.common;

public class CommonUtil {
	/**
	 * 입력된 {@link String}이 <code>null</code> 또는 빈 {@link String}인지 확인하는 함수
	 * @param s
	 * @return
	 */
	public static boolean isStringNullOrEmptyCheck(String s) {
		if(s == null || s.length() == 0 || "null".equals(s)){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 입력된 {@link String}에서 전부 숫자인지 확인 하는 함수
	 * @param s
	 * @return
	 */
	public static boolean areAllNumbers(String s) {
	    if (s == null || s.length() == 0)
	        return false;
	    char c;
	    for (int i = 0; i < s.length(); i++) {
	        c = s.charAt(i);
	        if (c < '0' || c > '9')
	            return false;
	    }
	    return true;
	}

}
