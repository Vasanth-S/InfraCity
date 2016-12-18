package com.infracity.android.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.infracity.android.Constants;
import com.infracity.android.R;
import com.infracity.android.rest.RestService;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class ReportFragment extends DialogFragment {

    SharedPreferences preferences;
    RestService service;
    int key;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        preferences = getContext().getSharedPreferences(Constants.PREFERENCE, Context.MODE_PRIVATE);
        initRetrofit();
        Bundle bundle = getArguments();
        key = bundle.getInt("id");
        Dialog dialog = new Dialog(getContext(), R.style.InfoTheme);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.fragment_report);
        StringBuilder sb = new StringBuilder();
        TextView detail = (TextView) dialog.findViewById(R.id.details);
        sb.append("Name : ").append(preferences.getString(Constants.PREFERENCE_DISPLAY_NAME, "")).append("\n\n")
            .append("Email : ").append(preferences.getString(Constants.PREFERENCE_EMAIL, "")).append("\n\n");
        detail.setText(sb.toString());

        final EditText editText = (EditText) dialog.findViewById(R.id.comment);

        Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new String[] {
                "Request to relay the road",
                "Poor Quality of Work",
                "Pot hole fill up/Repairs to the damaged surface",
                "Removal of fallen trees",
                "Request to provide foot path",
                "Shifting of garbage bin",
                "Repairs to existing footpath",
                "Removal of shops in the footpath"
        }));

        View submit = dialog.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editText.getText().toString())) {
                    Toast.makeText(getContext(), "Please enter comment before submitting", Toast.LENGTH_SHORT).show();
                } else {
                    FileComplaintTask fileComplaintTask = new FileComplaintTask();
                    fileComplaintTask.execute(key);
                }
            }
        });
        return dialog;
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);
    }

    private class FileComplaintTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... ids) {
            String result = null;
            try {
                Response<JsonObject> response = service.fileComplaint(ids[0], preferences.getInt(Constants.PREFERENCE_UID, 0)).execute();
                if(response.code() == 201) {
                    JsonObject object = response.body();
                    if(object != null && object.has("id")) {
                        result = object.get("id").getAsString();
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String complaintId) {
            if(TextUtils.isEmpty(complaintId)) {
                Toast.makeText(getContext(), "Unable to file complaint, Please try later", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Complaint filed - " + complaintId, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }
}
