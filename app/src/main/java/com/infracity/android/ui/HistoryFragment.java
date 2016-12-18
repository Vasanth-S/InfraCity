package com.infracity.android.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infracity.android.R;

/**
 * Created by pragadeesh on 18/12/16.
 */
public class HistoryFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.InfoTheme);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.fragment_history);
        Bundle bundle = getArguments();
        String summary = bundle.getString("summary");
        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(summary);
        return dialog;
    }
}
