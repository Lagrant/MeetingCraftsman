package io.github.lagrant.MeetingCraftsman.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;
import io.github.froger.instamaterial.ui.adapter.PhotoFiltersAdapter;
import io.github.froger.instamaterial.ui.view.RevealBackgroundView;


public class TakePhotoActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener,
        CameraHostProvider {
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_SETUP_PHOTO = 1;

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.vPhotoRoot)
    View vTakePhotoRoot;
    @BindView(R.id.vShutter)
    View vShutter;
    @BindView(R.id.ivTakenPhoto)
    ImageView ivTakenPhoto;
    @BindView(R.id.vUpperPanel)
    ViewSwitcher vUpperPanel;
    @BindView(R.id.vLowerPanel)
    ViewSwitcher vLowerPanel;
    @BindView(R.id.cameraView)
    CameraView cameraView;
    @BindView(R.id.rvFilters)
    RecyclerView rvFilters;
    @BindView(R.id.btnTakePhoto)
    Button btnTakePhoto;
    @BindView(R.id.btnSelectPhoto)
    ImageButton btnSelectPhoto;
    @BindView(R.id.btnVideoCam)
    ImageButton btnVideoCam;

    private boolean pendingIntro;
    private int currentState;

    private File photoPath;
    private Uri uri;
    private String path;

    private final String IMAGE_TYPE = "image/*";
    private final String TAG = "TAG";
    private final int IMAGE_CODE = 0; //这里的IMAGE_CODE是自己任意定义的
    private final int VIDEO = 72;
    private final int LOAD_VIDEO_CODE = 23;

    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, TakePhotoActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        updateStatusBarColor();
        updateState(STATE_TAKE_PHOTO);
        setupRevealBackground(savedInstanceState);
        setupPhotoFilters();

        vUpperPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                vUpperPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                pendingIntro = true;
                vUpperPanel.setTranslationY(-vUpperPanel.getHeight());
                vLowerPanel.setTranslationY(vLowerPanel.getHeight());
                return true;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (resultCode != RESULT_OK) { //此处的 RESULT_OK 是系统自定义得一个常量

            Log.e(TAG,"ActivityResult resultCode error");

            return;

        }

        Bitmap bm = null;

//外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口

        ContentResolver resolver = getContentResolver();

//此处的用于判断接收的Activity是不是你想要的那个

        if (requestCode == IMAGE_CODE) {

            try {

                uri = data.getData(); //获得图片的uri

                bm = MediaStore.Images.Media.getBitmap(resolver, uri); //显得到bitmap图片 这里开始的第二部分，获取图片的路径：

                PublishActivity.btm = bm;

                vUpperPanel.showNext();
                vLowerPanel.showNext();
                ivTakenPhoto.setImageBitmap(bm);
                updateState(STATE_SETUP_PHOTO);

            }catch (IOException e) {

                Log.e(TAG,e.toString());

            }

        } else if(requestCode == VIDEO){

            try {
                uri = data.getData();
                System.out.println("load video uri is "+uri);
                bm = ThumbnailUtils.createVideoThumbnail(getPath(uri),MediaStore.Images.Thumbnails.MINI_KIND);
                PublishActivity.btm = bm;

                vUpperPanel.showNext();
                vLowerPanel.showNext();
                ivTakenPhoto.setImageBitmap(bm);
                updateState(STATE_SETUP_PHOTO);

            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(requestCode == LOAD_VIDEO_CODE){

            try {
                uri = data.getData();
//                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
////获取网络视频
////                retriever.setDataSource(getPath(uri), new HashMap<String, String>());
////获取本地视频
//                retriever.setDataSource(getPath(uri));
//                bm = retriever.getFrameAtTime();
//                bm.compress();
                bm = ThumbnailUtils.createVideoThumbnail(getPath(uri),MediaStore.Images.Thumbnails.MINI_KIND);
                PublishActivity.btm = bm;
//                retriever.release();

                vUpperPanel.showNext();
                vLowerPanel.showNext();
                ivTakenPhoto.setImageBitmap(bm);
                updateState(STATE_SETUP_PHOTO);

            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff111111);
        }
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setFillPaintColor(0xFF16181a);
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    private void setupPhotoFilters() {
        PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(this);
        rvFilters.setHasFixedSize(true);
        rvFilters.setAdapter(photoFiltersAdapter);
        rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @OnClick(R.id.btnTakePhoto)
    public void onTakePhotoClick() {
        btnTakePhoto.setEnabled(false);
        cameraView.takePicture(true, true);
        animateShutter();
    }

//    @OnClick(R.id.btnSelectPhoto)
//    public void onSelectPhotoClick(){
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setType(IMAGE_TYPE);
//        startActivityForResult(intent, IMAGE_CODE);
//    }

    @OnClick(R.id.btnSelectPhoto)
    public void onSelectPhotoClick(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, LOAD_VIDEO_CODE);
    }

    @OnClick(R.id.btnVideoCam)
    public void onVideoCam(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        startActivityForResult(intent,VIDEO);
    }

    @OnClick(R.id.btnAccept)
    public void onAcceptClick() {
        PublishActivity.openWithPhotoUri(this, uri);
    }

    private void animateShutter() {
        vShutter.setVisibility(View.VISIBLE);
        vShutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                vShutter.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            vTakePhotoRoot.setVisibility(View.VISIBLE);
            if (pendingIntro) {
                startIntroAnimation();
            }
        } else {
            vTakePhotoRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void startIntroAnimation() {
        vUpperPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR);
        vLowerPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR).start();
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }

    class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        public MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
//            PublishActivity.btm = bitmap;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTakenPicture(bitmap);
                }
            });
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            super.saveImage(xact, image);
            photoPath = getPhotoPath();
            uri = Uri.fromFile(photoPath);
//            PublishActivity.btm = MediaStore.Images.Media.getBitmap(resolver, uri);;
        }
    }

    private void showTakenPicture(Bitmap bitmap) {
        vUpperPanel.showNext();
        vLowerPanel.showNext();
        ivTakenPhoto.setImageBitmap(bitmap);
        updateState(STATE_SETUP_PHOTO);
//        PublishActivity.btm = bitmap;
    }

    @Override
    public void onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
        } else {
            super.onBackPressed();
        }
    }

    private void updateState(int state) {
        currentState = state;
        if (currentState == STATE_TAKE_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                }
            }, 400);
        } else if (currentState == STATE_SETUP_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }
    }
}
