package com.infracity.android.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.infracity.android.Constants;
import com.infracity.android.R;
import com.infracity.android.rest.RestService;
import com.infracity.android.utils.PhotoUtils;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class RoadInfoFragment extends DialogFragment implements View.OnClickListener {

    RestService service;

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = new Dialog(getContext(), R.style.InfoTheme);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.fragment_road_info);
        initRetrofit();
        Bundle bundle = getArguments();
        String key = bundle.getString("id");
        FetchInfoTask task = new FetchInfoTask();
        task.execute(key);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1: {
                captureImage();
                break;
            }
            case R.id.button2: {
                pickImage();
                break;
            }
        }
    }
    private Uri fileUri;
    private String filePath;

    protected void pickImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(galleryIntent, 1001);
    }

    protected void captureImage() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = Uri.fromFile(PhotoUtils.getOutputMediaFile(getContext()));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        startActivityForResult(cameraIntent, 1001);
    }

    private class FetchInfoTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                Response<Object> response = service.getInfo(strings[0]).execute();
                if(response.code() == 200) {

                }
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            enterReveal(getDialog().findViewById(R.id.button1), 1000);
            enterReveal(getDialog().findViewById(R.id.button2), 2000);
            enterReveal(getDialog().findViewById(R.id.button3), 3000);
            enterReveal(getDialog().findViewById(R.id.button4), 4000);
        }
    }

    void enterReveal(final View view, int startOffset) {
        int cx = view.getMeasuredWidth() / 2;
        int cy = view.getMeasuredHeight() / 2;
        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(this);
        anim.setInterpolator(new BounceInterpolator());
        anim.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null){
                Uri selectedImageURI = data.getData();
                fileUri = selectedImageURI;
                filePath = PhotoUtils.getPath(getContext(), selectedImageURI);
            } else if(fileUri != null) {
                filePath = fileUri.getPath();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
