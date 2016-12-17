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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.infracity.android.Constants;
import com.infracity.android.R;
import com.infracity.android.model.RoadInfo;
import com.infracity.android.rest.RestService;
import com.infracity.android.utils.PhotoUtils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class RoadInfoFragment extends DialogFragment implements View.OnClickListener {

    RestService service;
    String key;
    String summary;

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
        key = bundle.getString("id");
        summary = bundle.getString("summary");
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
            UpdateInfoTask updateInfoTask = new UpdateInfoTask();
            updateInfoTask.execute(key);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateUI(RoadInfo roadInfo) {
        if(!isDetached() && getActivity() != null) {
            if(roadInfo != null) {
                Dialog dialog = getDialog();
                enterReveal(dialog.findViewById(R.id.button1), 1000);
                enterReveal(dialog.findViewById(R.id.button2), 2000);
                enterReveal(dialog.findViewById(R.id.button3), 3000);
                enterReveal(dialog.findViewById(R.id.button4), 4000);
                TextView summaryText = (TextView) dialog.findViewById(R.id.summary);
                summaryText.setText(summary);
                ViewPager pager = (ViewPager) dialog.findViewById(R.id.imagePager);
                ImageAdapter imageAdapter = new ImageAdapter();
                imageAdapter.setImageUrls(roadInfo.getPhotos());
                pager.setAdapter(imageAdapter);
            } else {
                Toast.makeText(getContext(), "Unable to load info", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    private class FetchInfoTask extends AsyncTask<String, Void, RoadInfo> {

        @Override
        protected RoadInfo doInBackground(String... strings) {
            RoadInfo roadInfo = null;
            try {
                Response<RoadInfo> response = service.getInfo(strings[0]).execute();
                if(response.code() == 200) {
                    roadInfo = response.body();
                    System.out.println("road info " + roadInfo);
                }
            } catch (Exception e) {
                System.out.println("road info fail " + e.getMessage());
            }
            return roadInfo;
        }

        @Override
        protected void onPostExecute(RoadInfo roadInfo) {
            updateUI(roadInfo);
        }
    }

    private class UpdateInfoTask extends AsyncTask<String, Void, RoadInfo> {
        @Override
        protected RoadInfo doInBackground(String... strings) {
            try {
                File file = new File(filePath);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
                Response<Object> response = service.updateInfo(strings[0], requestFile).execute();
                if(response.code() == 201) {
                    System.out.println("upload Success");
                }
            } catch (Exception e) {
                System.out.println("upload fail " + e.getMessage());
            }
            return null;
        }
    }

    private class ImageAdapter extends PagerAdapter {

        private String[] urls;

        public void setImageUrls(String[] urls) {
            this.urls = urls;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return urls == null ? 0 : urls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView view = new SimpleDraweeView(getContext());
            container.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setImageURI(Uri.parse(urls[position]));
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
