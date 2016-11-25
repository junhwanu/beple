/**
 * 버지니아에서 온 편지 학습 단계 프레그먼트의 추상 클래스
 */
package kr.co.bsmsoft.beple_shop.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.Setting;


public abstract class AbFragment extends Fragment implements NetDefine {
	
	public Setting setting;

	public Callbacks mCallbacks = sDummyCallbacks;

	public interface Callbacks {
		public	void onAction(AbFragment sender, int target);
	}
	
	private static Callbacks sDummyCallbacks = new Callbacks() {

		@Override
		public void onAction(AbFragment sender, int target) {

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setting = new Setting(getActivity());
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
	
}
