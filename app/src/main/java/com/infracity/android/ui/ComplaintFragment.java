package com.infracity.android.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infracity.android.R;
import com.infracity.android.model.ComplaintResponse;
import com.infracity.android.model.Message;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class ComplaintFragment extends DialogFragment {

    ComplaintResponse complaintResponse;

    public void setComplaintResponse(ComplaintResponse complaintResponse) {
        this.complaintResponse = complaintResponse;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.InfoTheme);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.fragment_complaint);
        TextView detail = (TextView) dialog.findViewById(R.id.details);
        StringBuilder sb = new StringBuilder();
        sb.append(complaintResponse.getComplaint().getEmail()).append("\n\n")
                .append(complaintResponse.getComplaint().getAddress()).append("\n")
                .append(complaintResponse.getComplaint().getPhone()).append("\n\n")
                .append(complaintResponse.getComplaint().getDate()).append("\n");
        detail.setText(sb.toString());
        LinearLayout messages = (LinearLayout) dialog.findViewById(R.id.messages);
        if(complaintResponse.getMessages() != null && !complaintResponse.getMessages().isEmpty()) {
            messages.setVisibility(View.VISIBLE);
            for(Message message : complaintResponse.getMessages()) {
                TextView textView = new TextView(getContext());
                textView.setPadding(10, 10, 10, 10);
                textView.setTextSize(16);
                StringBuilder builder = new StringBuilder();
                builder.append(message.getMessage()).append("\n\n")
                        .append("From ").append(message.getUser())
                        .append(" On ").append(message.getDate()).append("\n");
                textView.setText(builder.toString());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT);
                params.topMargin = 10;
                params.bottomMargin = 10;
                params.leftMargin = 10;
                params.rightMargin = 10;
                messages.addView(textView, params);
            }
        } else {
            messages.setVisibility(View.GONE);
        }
        return dialog;
    }
}
