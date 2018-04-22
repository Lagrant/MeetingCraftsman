package io.github.lagrant.MeetingCraftsman.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.Utils;

public class PublishActivity extends BaseActivity {
    public static final String ARG_TAKEN_PHOTO_URI = "arg_taken_photo_uri";

    @BindView(R.id.tbFollowers)
    ToggleButton tbFollowers;
    @BindView(R.id.tbDirect)
    ToggleButton tbDirect;
    @BindView(R.id.ivPhoto)
    ImageView ivPhoto;
    @BindView(R.id.etDescription)
    EditText description;

    private boolean propagatingToggleState = false;
    private static Uri photoUri;
    private int photoSize;
    public static Bitmap btm;
    public static String dcp = "Lagrant: ";

    public static void openWithPhotoUri(Activity openingActivity, Uri photoUri) {
        Intent intent = new Intent(openingActivity, PublishActivity.class);
        intent.putExtra(ARG_TAKEN_PHOTO_URI, photoUri);
        openingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_grey600_24dp);
        photoSize = getResources().getDimensionPixelSize(R.dimen.publish_photo_thumbnail_size);

        if (savedInstanceState == null) {
            photoUri = getIntent().getParcelableExtra(ARG_TAKEN_PHOTO_URI);
        } else {
            photoUri = savedInstanceState.getParcelable(ARG_TAKEN_PHOTO_URI);
        }
        updateStatusBarColor();

        ivPhoto.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivPhoto.getViewTreeObserver().removeOnPreDrawListener(this);
                loadThumbnailPhoto();
                return true;
            }
        });
//        description.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//
//            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
//
//                if (arg1 == EditorInfo.IME_ACTION_DONE) {
//                    Toast.makeText(PublishActivity.this, String.valueOf(arg1), Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//
//                return false;
//
//            }
//
//        });
        description.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    System.out.println("手指弹起时执行确认功能");
                    dcp ="Lagrant: " + description.getText().toString();
                    System.out.println("dcp = "+dcp);
                    return true;
                }

                return false;
            }
        });
        description.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
// TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
// TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
// TODO Auto-generated method stub
                dcp ="Lagrant: " + s.toString();
            }
        });

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (Utils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff888888);
        }
    }

//    private Runnable delayRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//
//        }
//    };

    private void loadThumbnailPhoto() {
        ivPhoto.setScaleX(0);
        ivPhoto.setScaleY(0);
        Picasso.with(this)
                .load(photoUri)
                .centerCrop()
                .resize(photoSize, photoSize)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        ivPhoto.animate()
                                .scaleX(1.f).scaleY(1.f)
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(400)
                                .setStartDelay(200)
                                .start();
                    }

                    @Override
                    public void onError() {
                    }
                });
        Picasso.with(this)
                .load(photoUri)
                .centerCrop()
                .resize(photoSize, photoSize)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        btm = bitmap;
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_publish) {
            bringMainActivityToTop();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void bringMainActivityToTop() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(MainActivity.ACTION_SHOW_LOADING_ITEM);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_TAKEN_PHOTO_URI, photoUri);
    }

    @OnCheckedChanged(R.id.tbFollowers)
    public void onFollowersCheckedChange(boolean checked) {
        if (!propagatingToggleState) {
            propagatingToggleState = true;
            tbDirect.setChecked(!checked);
            propagatingToggleState = false;
        }
    }

    @OnCheckedChanged(R.id.tbDirect)
    public void onDirectCheckedChange(boolean checked) {
        if (!propagatingToggleState) {
            propagatingToggleState = true;
            tbFollowers.setChecked(!checked);
            propagatingToggleState = false;
        }
    }


}
