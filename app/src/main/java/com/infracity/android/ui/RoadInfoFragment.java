package com.infracity.android.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRatingBar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonObject;
import com.infracity.android.Constants;
import com.infracity.android.R;
import com.infracity.android.model.RoadInfo;
import com.infracity.android.model.UploadResponse;
import com.infracity.android.rest.RestService;
import com.infracity.android.utils.PhotoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

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
    int key;
    String summary;
    boolean shouldReport;

    ImageAdapter imageAdapter;
    SharedPreferences preferences;
    ViewPager pager;
    ImageView placeHolder;
    private View cam;
    private View pick;

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
        preferences = getContext().getSharedPreferences(Constants.PREFERENCE, Context.MODE_PRIVATE);
        Dialog dialog = new Dialog(getContext(), R.style.InfoTheme);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.fragment_road_info);
        initRetrofit();
        Bundle bundle = getArguments();
        key = bundle.getInt("id");
        summary = bundle.getString("summary");
        shouldReport = bundle.getBoolean("report");
        FetchInfoTask task = new FetchInfoTask();
        task.execute(key);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1: {
                captureImage();
                cam.setVisibility(View.INVISIBLE);
                pick.setVisibility(View.INVISIBLE);
                break;
            }
            case R.id.button2: {
                pickImage();
                cam.setVisibility(View.INVISIBLE);
                pick.setVisibility(View.INVISIBLE);
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

    void enterReveal(final View view, int delay) {
        int cx = view.getMeasuredWidth() / 2;
        int cy = view.getMeasuredHeight() / 2;
        int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.setDuration(250);
        view.setOnClickListener(this);
        anim.setStartDelay(delay);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
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

    boolean isOpen = false;

    private void updateUI(final RoadInfo roadInfo) {
        MapsActivity activity = (MapsActivity) getActivity();
        if(activity != null) {
            activity.hideProgressBar();
        } else {
            return;
        }
        if(!isDetached() && getActivity() != null) {
            if(roadInfo != null) {
                final Dialog dialog = getDialog();
                View add = dialog.findViewById(R.id.add);
                final AppCompatRatingBar ratingEncroachment = (AppCompatRatingBar) dialog.findViewById(R.id.ratingEncroachment);
                final AppCompatRatingBar ratingSafety = (AppCompatRatingBar) dialog.findViewById(R.id.ratingSafety);
                final AppCompatRatingBar ratingQuality = (AppCompatRatingBar) dialog.findViewById(R.id.ratingQuality);
                final AppCompatRatingBar ratingPlatform = (AppCompatRatingBar) dialog.findViewById(R.id.ratingPlatform);

                ratingEncroachment.setIsIndicator(!shouldReport);
                ratingSafety.setIsIndicator(!shouldReport);
                ratingQuality.setIsIndicator(!shouldReport);
                ratingPlatform.setIsIndicator(!shouldReport);
                ratingEncroachment.setRating((!shouldReport && roadInfo.getEncroachments() <= 0) ? 3 : roadInfo.getEncroachments());
                ratingSafety.setRating((!shouldReport && roadInfo.getSafety() <= 0) ? 2 : roadInfo.getSafety());
                ratingQuality.setRating((!shouldReport && roadInfo.getRoadQuality() <= 0) ? 4 : roadInfo.getRoadQuality());
                ratingPlatform.setRating((!shouldReport && roadInfo.getPlatformUsability() <= 0) ? 1 : roadInfo.getPlatformUsability());

                if(shouldReport) {
                    add.setVisibility(View.VISIBLE);
                    cam = dialog.findViewById(R.id.button1);
                    pick = dialog.findViewById(R.id.button2);
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isOpen) {
                                cam.setVisibility(View.INVISIBLE);
                                pick.setVisibility(View.INVISIBLE);
                            } else {
                                ScaleAnimation animation = new ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                animation.setDuration(200);
                                animation.setInterpolator(new BounceInterpolator());
                                view.startAnimation(animation);
                                enterReveal(cam, 500);
                                enterReveal(pick, 250);
                            }
                            isOpen = !isOpen;
                        }
                    });
                    View submit = dialog.findViewById(R.id.submit);
                    submit.setVisibility(View.VISIBLE);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(imageAdapter != null && imageAdapter.getCount() > 0) {
                                UpdateRatingTask updateRatingTask = new UpdateRatingTask();
                                updateRatingTask.execute((int) ratingEncroachment.getRating(),
                                        (int) ratingSafety.getRating(),
                                        (int) ratingPlatform.getRating(),
                                        (int) ratingQuality.getRating());
                            } else {
                                Toast.makeText(getContext(), "Upload atleast one image before rating", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                TextView summaryText = (TextView) dialog.findViewById(R.id.summary);
                summaryText.setText(summary);
                pager = (ViewPager) dialog.findViewById(R.id.imagePager);
                placeHolder = (ImageView) dialog.findViewById(R.id.imagePlaceHolder);
                pager.setOffscreenPageLimit(3);
                imageAdapter = new ImageAdapter();
                imageAdapter.setImageUrls(roadInfo.getPhotos());
                pager.setAdapter(imageAdapter);
                if(roadInfo.getPhotos() == null || roadInfo.getPhotos().isEmpty()) {
                    pager.setVisibility(View.INVISIBLE);
                    placeHolder.setVisibility(View.VISIBLE);
                } else {
                    pager.setVisibility(View.VISIBLE);
                    placeHolder.setVisibility(View.INVISIBLE);
                }
            } else {
                Toast.makeText(getContext(), "Unable to load info", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    private class UpdateRatingTask extends AsyncTask<Integer, Void, Boolean> {

        private float average = 0f;

        @Override
        protected void onPreExecute() {
            MapsActivity activity = (MapsActivity) getActivity();
            if(activity != null) {
                activity.showProgressBar("Updating rating...");
            }
        }

        @Override
        protected Boolean doInBackground(Integer... ratings) {
            boolean result = false;
            try {
                int uid = preferences.getInt(Constants.PREFERENCE_UID, 0);
                Response<Object> response = service.updateInfo(key, uid, ratings[0], ratings[1], ratings[2], ratings[3]).execute();
                result = response.code() == 201;
                average =  (ratings[0] + ratings[1] + ratings[2] + ratings[3])/4;
            } catch (Exception e) {
                System.out.println("Update failed");
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            MapsActivity activity = (MapsActivity) getActivity();
            if(activity != null) {
                activity.hideProgressBar();
            }
            if(aBoolean) {
                Toast.makeText(getContext(), "Ratings registered", Toast.LENGTH_SHORT).show();
                if(average < 3.0f) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Report your concern")
                            .setMessage("Do you wish to file a complaint to the Greater corporation of Chennai?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FileComplaintTask fileComplaintTask = new FileComplaintTask();
                                    fileComplaintTask.execute(key);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    dismiss();
                                }
                            })
                            .show();
                } else {
                    dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Ratings failed", Toast.LENGTH_SHORT).show();
            }
        }
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

    private class FetchInfoTask extends AsyncTask<Integer, Void, RoadInfo> {

        @Override
        protected void onPreExecute() {
            MapsActivity activity = (MapsActivity) getActivity();
            if(activity != null) {
                activity.showProgressBar("Loading Info...");
            }
        }

        @Override
        protected RoadInfo doInBackground(Integer... ids) {
            RoadInfo roadInfo = null;
            try {
                Response<RoadInfo> response = service.getInfo(ids[0]).execute();
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

    private class UpdateInfoTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            MapsActivity activity = (MapsActivity) getActivity();
            if(activity != null) {
                activity.showProgressBar("Uploading photo...");
            }
        }

        @Override
        protected String doInBackground(Integer... ids) {
            String url = null;
            try {
                File file = new File(filePath);
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("image/jpeg"), file);
                RequestBody uid = RequestBody.create(MediaType.parse("text/plain"), "" + preferences.getInt(Constants.PREFERENCE_UID, 0));
                RequestBody roadId = RequestBody.create(MediaType.parse("text/plain"), "" + ids[0]);
                Response<UploadResponse> request = service.updatePhoto(roadId, uid, requestFile).execute();
                if(request.code() == 201) {
                    UploadResponse uploadResponse = request.body();
                    url = uploadResponse.getUrl();
                    System.out.println("upload Success " + uploadResponse.getUrl());
                }
            } catch (Exception e) {
                System.out.println("upload fail " + e.getMessage());
            }
            return url;
        }

        @Override
        protected void onPostExecute(String s) {
            if(TextUtils.isEmpty(s)) {
                Toast.makeText(getContext(), "Photo upload failed", Toast.LENGTH_SHORT).show();
            } else {
                imageAdapter.addUrl(s);
                Toast.makeText(getContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
            }
            MapsActivity activity = (MapsActivity) getActivity();
            if(activity != null) {
                activity.hideProgressBar();
            }
        }
    }

    private class ImageAdapter extends PagerAdapter {

        private ArrayList<String> urls;

        public void setImageUrls(ArrayList<String> urls) {
            this.urls = urls;
            if(urls == null || urls.isEmpty()) {
                pager.setVisibility(View.INVISIBLE);
                placeHolder.setVisibility(View.VISIBLE);
            } else {
                pager.setVisibility(View.VISIBLE);
                placeHolder.setVisibility(View.INVISIBLE);
            }
            notifyDataSetChanged();
        }

        public void addUrl(String url) {
            if(url == null) return;
            if(urls == null) urls = new ArrayList<>();
            urls.add(url);
            if(urls == null || urls.isEmpty()) {
                pager.setVisibility(View.INVISIBLE);
                placeHolder.setVisibility(View.VISIBLE);
            } else {
                pager.setVisibility(View.VISIBLE);
                placeHolder.setVisibility(View.INVISIBLE);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return urls == null ? 0 : urls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView view = new SimpleDraweeView(getContext());
            view.getHierarchy().setPlaceholderImage(getResources().getDrawable(R.drawable.image_placeholder), ScalingUtils.ScaleType.CENTER);
            container.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            String url = urls.get(0);
            if(!TextUtils.isEmpty(url)) {
                view.setImageURI(Uri.parse(urls.get(position)));
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
