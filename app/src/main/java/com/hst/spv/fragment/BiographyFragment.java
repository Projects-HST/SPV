package com.hst.spv.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hst.spv.R;
import com.hst.spv.activity.YourSpv;
import com.hst.spv.helper.AlertDialogHelper;
import com.hst.spv.helper.ProgressDialogHelper;
import com.hst.spv.servicehelpers.ServiceHelper;
import com.hst.spv.serviceinterfaces.IServiceListener;
import com.hst.spv.utils.SPVConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.util.Log.d;

public class BiographyFragment extends Fragment implements View.OnClickListener, IServiceListener {

    private static final String TAG = YourSpv.class.getName();
    private View rootView;
    private TextView personal, political, career_cont;
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper dialogHelper;
    private String resString;

    public static BiographyFragment newInstance(int position) {
        BiographyFragment fragment = new BiographyFragment();
        Bundle args = new Bundle();
        args.putInt("biography", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_biography, container, false);
        initView();
        personalCareer();
        return rootView;
    }

    private void initView(){

        personal = (TextView)rootView.findViewById(R.id.personal);
        personal.setOnClickListener(this);
        political = (TextView)rootView.findViewById(R.id.political);
        political.setOnClickListener(this);
        career_cont = (TextView) rootView.findViewById(R.id.viewText);

        serviceHelper = new ServiceHelper(getActivity());
        serviceHelper.setServiceListener(this);

        dialogHelper = new ProgressDialogHelper(getActivity());
    }

    @Override
    public void onClick(View v) {

        if (v == personal){

                personal.setEnabled(true);
                personal.setBackground(getResources().getDrawable(R.drawable.bt_enabled));
                personal.setTextColor(getResources().getColor(R.color.white));
                political.setBackground(getResources().getDrawable(R.drawable.bt_disabled));
                political.setTextColor(getResources().getColor(R.color.btn_st_grad));

                personalCareer();
        }

        if (v == political) {

            political.setEnabled(true);
            political.setBackground(getResources().getDrawable(R.drawable.bt_enabled));
            political.setTextColor(getResources().getColor(R.color.white));
            personal.setTextColor(getResources().getColor(R.color.btn_st_grad));
            personal.setBackground(getResources().getDrawable(R.drawable.bt_disabled));

            politicalCareer();
        }
    }

    private void personalCareer(){

        resString = "personal_career";

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(SPVConstants.KEY_USER_ID, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dialogHelper.showProgressDialog(getResources().getString(R.string.progress_bar));
        String serverUrl = SPVConstants.Base_Url + SPVConstants.PERSONAL_URL;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), serverUrl);
    }

    private void politicalCareer(){

        resString = "political_career";

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(SPVConstants.KEY_USER_ID, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dialogHelper.showProgressDialog(getResources().getString(R.string.progress_bar));
        String serverUrl = SPVConstants.Base_Url + SPVConstants.POLITICAL_URL;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), serverUrl);
    }

    private boolean validateSignInResponse(JSONObject response){

        boolean signInSuccess = false;

        if ((response != null)) {

            try {
                String status = response.getString("status");
                String msg = response.getString(SPVConstants.PARAM_MESSAGE);
                d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(getActivity(), msg);

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(JSONObject response) {

        dialogHelper.hideProgressDialog();

        try {

            if (validateSignInResponse(response)){

                if (resString.equalsIgnoreCase("personal_career")) {

                    JSONArray personalDetails = response.getJSONArray("personal_result");
                    JSONObject object = personalDetails.getJSONObject(0);

                    Log.d(TAG, object.toString());

                    String career = "";

                    for (int i = 0; i < personalDetails.length(); i++) {

                        career = personalDetails.getJSONObject(i).getString("personal_life_text_en");
                        career_cont.setText(career);
                    }
                }
                if (resString.equalsIgnoreCase("political_career")){

                    JSONArray politicalDetails = response.getJSONArray("political_result");
                    JSONObject object = politicalDetails.getJSONObject(0);

                    Log.d(TAG, object.toString());

                    String career = "";

                    for (int i=0; i<politicalDetails.length(); i++){

                        career = politicalDetails.getJSONObject(i).getString("political_career_text_en");
                        career_cont.setText(career);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String error) {

        dialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(getActivity(), error);
    }
}