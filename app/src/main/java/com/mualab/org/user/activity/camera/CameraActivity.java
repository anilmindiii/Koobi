package com.mualab.org.user.activity.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.Views.scaleview.ImageSource;
import com.mualab.org.user.Views.scaleview.ScaleImageView;
import com.mualab.org.user.activity.video_trim.VideoTrimmerActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.image.cropper.CropImage;
import com.mualab.org.user.image.cropper.CropImageView;
import com.mualab.org.user.image.picker.ImagePicker;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.constants.Constant;
import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.mualab.org.user.Views.*;

import static com.mualab.org.user.utils.media.ImageVideoUtil.generatePath;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_TAKE_VIDEO = 1;
    private static final int STATE_SETUP_PHOTO = 3;
    private static final int STATE_SETUP_VIDEO = 4;

    private View vShutter;
    private ScaleImageView ivTakenPhoto;
    private ViewSwitcher vUpperPanel;
    private ViewSwitcher vLowerPanel;
    private CameraView cameraView;
    private VideoView videoView;
    private boolean isCameraSession;
    private boolean isStartRecord;
    private Bitmap profileImageBitmap;

    private Button btnTakePhoto;
    private ImageButton  btnFlashLight;
    ImageView btnCameraMode;
    private RelativeLayout add_to_story;
    //private Chronometer mChronometer;

    private int currentState;
    private File photoPath;
    private Uri captureMediaUri;
    private ImageView img_gallery;
    private boolean isVideoUri;
    private RelativeLayout ly_report;
    private TextView tv_videos_pick,tv_images_pick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();

        updateState(STATE_TAKE_PHOTO);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE); //

        SizeSelector width = SizeSelectors.minWidth(1000);
        SizeSelector height = SizeSelectors.minHeight(2000);
        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.
        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(1, 1), 0); // Matches 1:1 sizes.
        SizeSelector result = SizeSelectors.or(
                SizeSelectors.and(ratio, dimensions), // Try to match both constraints
                ratio, // If none is found, at least try to match the aspect ratio
                SizeSelectors.biggest()); // If none is// found, take the biggest

        cameraView.setPictureSize(result);

        cameraView.addCameraListener(new CameraListener() {

            @Override
            public void onCameraOpened(CameraOptions options) {
                super.onCameraOpened(options);
            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                showToast(exception.getLocalizedMessage());
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                Progress.showProgressOnly(CameraActivity.this);
                CameraUtils.decodeBitmap(jpeg, 3000, 3000, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        img_gallery.setVisibility(View.GONE);
                        cameraView.stop();
                        showTakenPicture(bitmap);
                        isVideoUri = false;
                        Progress.hide(CameraActivity.this);
                        /*File f = new File(CameraActivity.this.getCacheDir(), "tmp.jpg");
                        try {

                            f.createNewFile();
                            //Convert bitmap to byte array
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80 *//*ignored for PNG*//*, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            //write the bytes in file
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            //mSelected = new ArrayList<>();
                           // mSelected.add(Uri.fromFile(f));
                            captureMediaUri = Uri.fromFile(f);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                });
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                cameraView.stop();
                captureMediaUri = Uri.fromFile(video);
                vUpperPanel.showNext();
                vLowerPanel.showNext();
                updateState(STATE_SETUP_VIDEO);
                videoView.setVideoURI(captureMediaUri);
                isVideoUri = true;
                isStartRecord = false;
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
                videoView.setLayoutParams(lp);
                playVideo();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    private void showToast(String str){
        if(!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showSmallCustomToast(str);
    }

    private void initView(){
        //vTakePhotoRoot = findViewById(R.id.vPhotoRoot);
        vShutter = findViewById(R.id.vShutter);
        btnFlashLight = findViewById(R.id.btnFlashLight);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        vUpperPanel = findViewById(R.id.vUpperPanel);
        vLowerPanel = findViewById(R.id.vLowerPanel);
        ImageView img_cancle = findViewById(R.id.img_cancle);
        ly_report = findViewById(R.id.ly_report);
        tv_images_pick = findViewById(R.id.tv_images_pick);
        tv_videos_pick = findViewById(R.id.tv_videos_pick);


        //rvFilters = findViewById(R.id.rvFilters);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnCameraMode = findViewById(R.id.btnCameraMode);
        img_gallery = findViewById(R.id.img_gallery);
        //ivBack = findViewById(R.id.ivBack);

        cameraView = findViewById(R.id.camera);
        videoView = findViewById(R.id.videoPreview);
        add_to_story = findViewById(R.id.add_to_story);
        //mChronometer = findViewById(R.id.tvVideoTimer);

        btnTakePhoto.setOnClickListener(this);
        img_gallery.setOnClickListener(this);
        img_gallery.setVisibility(View.VISIBLE);
        btnFlashLight.setOnClickListener(this);
        img_cancle.setOnClickListener(this);
        // findViewById(R.id.btnAccept).setOnClickListener(this);
        findViewById(R.id.retry).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);
        findViewById(R.id.btnCameraMode).setOnClickListener(this);
        add_to_story.setOnClickListener(this);
        findViewById(R.id.tv_videos_pick).setOnClickListener(this);
        findViewById(R.id.tv_images_pick).setOnClickListener(this);
        isCameraSession = true;
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
    public void onClick(View v) {
        // PopupVideoHintManager.getInstance().dismiss();
        switch (v.getId()){

            case R.id.btnTakePhoto:
                ly_report.setVisibility(View.GONE);

                if(currentState == STATE_TAKE_PHOTO){
                    btnTakePhoto.setEnabled(false);
                    cameraView.capturePicture();
                    animateShutter();

                }else if(currentState == STATE_TAKE_VIDEO){
                    img_gallery.setVisibility(View.GONE);
                    if(isStartRecord){
                        btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video);
                        if(countDownTimer!=null) countDownTimer.onFinish();
                    }else {
                        btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video_active_story);
                        btnTakePhoto.setEnabled(false);
                        cameraView.setSessionType(SessionType.VIDEO);
                        startTimear();
                    }
                }
                break;


            case R.id.img_gallery:
                if( ly_report.getVisibility() == View.VISIBLE){
                    ly_report.setVisibility(View.GONE);
                }else ly_report.setVisibility(View.VISIBLE);


                break;


            case R.id.tv_images_pick:
                ly_report.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* ");
                startActivityForResult(intent, 234);
                break;

            case R.id.tv_videos_pick:
                ly_report.setVisibility(View.GONE);

                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, Constant.REQUEST_CODE_PICK);

                break;




           /* case R.id.btnAccept:
                //PublishActivity.openWithPhotoUri(this, Uri.fromFile(photoPath));


                *//*Intent intent = null;
                if (mSelected != null && mSelected.size() > 0) {
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("feedType", Constant.IMAGE_STATE);
                    intent.putExtra("fromGallery", false);
                    intent.putParcelableArrayListExtra("imageUri", new ArrayList<Parcelable>(mSelected));
                } else if(videoUri!=null){
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("feedType", Constant.VIDEO_STATE);
                    intent.putExtra("videoUri", videoUri.toString());
                    intent.putExtra("fromGallery", false);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                }

                if (intent != null) {
                    // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                }*//*
                break;
*/

            case R.id.add_to_story:
                ly_report.setVisibility(View.GONE);

                addMyStory();
                break;


            case R.id.retry:
            case R.id.btnBack:
                onBackPressed();
                ly_report.setVisibility(View.GONE);
                img_gallery.setVisibility(View.VISIBLE);
                break;

            case R.id.switchCamera:
                ly_report.setVisibility(View.GONE);
                if(cameraView.getFacing()== Facing.BACK){
                    cameraView.setFacing(Facing.FRONT);
                }else {
                    cameraView.setFacing(Facing.BACK);
                }
                break;

            case R.id.btnFlashLight:
                ly_report.setVisibility(View.GONE);

                Flash flashMode = cameraView.getFlash();
                if(flashMode ==null){
                    flashMode = Flash.OFF;
                }

                if(isCameraSession){

                    if(flashMode == Flash.OFF){
                        flashMode = Flash.ON;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_on_white);

                    }else if(flashMode == Flash.ON){
                        flashMode = Flash.AUTO;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_auto_white);

                    }else if(flashMode == Flash.AUTO){
                        flashMode = Flash.OFF;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);
                    }
                }else {

                    if(flashMode == Flash.TORCH || flashMode == Flash.ON){
                        flashMode = Flash.OFF;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);

                    }else if(flashMode == Flash.OFF){
                        flashMode = Flash.TORCH;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_on_white);
                    }
                }
                break;

            case R.id.btnCameraMode:
                ly_report.setVisibility(View.GONE);
                changeCameraSessionMode();
                break;


            case R.id.img_cancle:
                ly_report.setVisibility(View.GONE);
                finish();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private synchronized void changeCameraSessionMode(){
        cameraView.setFlash(Flash.OFF);
        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);
        if(isCameraSession){
            isCameraSession = false;
            currentState = STATE_TAKE_VIDEO;
            cameraView.setSessionType(SessionType.VIDEO);
            btnTakePhoto.setText(R.string.rec);
            btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video_rec);
            btnCameraMode.setImageResource(R.drawable.ic_photo_camera_white);
            updateState(STATE_TAKE_VIDEO);
            //btnTakePhoto.setOnTouchListener(onTouchListener);
        }else {
            isCameraSession = true;
            btnCameraMode.setImageResource(R.drawable.video_player_ico);
            currentState = STATE_TAKE_PHOTO;
            btnTakePhoto.setText("");
            updateState(currentState);
            //btnTakePhoto.setOnTouchListener(null);
            btnTakePhoto.setOnClickListener(this);
            cameraView.setSessionType(SessionType.PICTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == Constant.POST_FEED_DATA){
            finish();
            img_gallery.setVisibility(View.VISIBLE);
        } else if (resultCode == RESULT_OK) {

            if(requestCode == Constant.GETVIDEOS){
                /*photoPath = new File(data.getStringExtra("new_uri"));
                isVideoUri = true;

                addMyStory();*/
                finish();
            }

            if(requestCode == Constant.REQUEST_CODE_PICK){
                Intent intent = new Intent(CameraActivity.this, VideoTrimmerActivity.class);
                MediaUri mediaUri = new MediaUri();
                mediaUri.uri = String.valueOf(data.getData());
                Uri selectedMediaUri = data.getData();


                mediaUri.uri = generatePath(selectedMediaUri,CameraActivity.this);

                mediaUri.isFromGallery = true;

                intent.putExtra("caption", "");
                intent.putExtra("mediaUri", mediaUri);
                intent.putExtra("feedType",90);

                startActivityForResult(intent,Constant.GETVIDEOS);

            }else if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    try {
                        if (imageUri != null)
                            profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                        if (profileImageBitmap != null) {
                            showTakenPicture(profileImageBitmap);

                            img_gallery.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    showToast(getString(R.string.msg_some_thing_went_wrong));
                }

            }
        }
    }

    void playVideo() {
        if (videoView.isPlaying()) return;
        videoView.start();
    }

    private void stopVideo(){
        videoView.setVisibility(View.GONE);
        if(videoView.isPlaying()){
            videoView.pause();
            videoView.stopPlayback();
        }
    }

    private void stopRecording(){
        if(!isCameraSession || isStartRecord){
            cameraView.stopCapturingVideo();
            //mChronometer.stop();\
            if(countDownTimer!=null)
                countDownTimer.cancel();
        }
    }

    private void startRecording(){
        if(cameraView.getSessionType()!= SessionType.PICTURE){
            try {
                File file = getExternalCacheDir();
                // File file = new File(getExternalCacheDir(), UUID.randomUUID() + ".mp4");
                if (file != null) {
                    isStartRecord = true;
                    // btnCameraMode.setVisibility(View.GONE);
                    btnCameraMode.setVisibility(View.GONE);
                    //mChronometer.setBase(SystemClock.elapsedRealtime());
                    photoPath = new File(file.getPath(), "tmp.mp4");
                    cameraView.startCapturingVideo(photoPath);
                    //mChronometer.start();
                    showToast("Start Recording");
                    Toast.makeText(CameraActivity.this,"Need to record at least 10 sec of video",Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                isStartRecord = false;
            }
        }
        else btnTakePhoto.setEnabled(true);
    }


    @Override
    public void onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
            cameraView.start();
            img_gallery.setVisibility(View.VISIBLE);
        } else if (currentState == STATE_SETUP_VIDEO) {
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            cameraView.start();
            updateState(STATE_TAKE_VIDEO);

            try{
                captureMediaUri = null;
                if(photoPath!=null)
                    photoPath.delete();
            } catch (Exception e){
                e.printStackTrace();
            }
        }else  {
            super.onBackPressed();
            img_gallery.setVisibility(View.VISIBLE);
        }
    }


    private void showTakenPicture(Bitmap bitmap) {
        vUpperPanel.showNext();
        vLowerPanel.showNext();
        ivTakenPhoto.setImage(ImageSource.bitmap(bitmap));
        updateState(STATE_SETUP_PHOTO);
    }


    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }


    private void updateState(int state) {
        currentState = state;
        if (currentState == STATE_TAKE_PHOTO) {
            //mChronometer.setVisibility(View.GONE);
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
        }else if(currentState==STATE_TAKE_VIDEO){
            //mChronometer.setVisibility(View.VISIBLE);
            //mChronometer.setBase(SystemClock.elapsedRealtime());
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                    stopVideo();
                }
            }, 400);

        }else if(currentState == STATE_SETUP_VIDEO){
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            videoView.setVisibility(View.VISIBLE);
            //videoView.setVideoPath(videoUri.getPath());

        }else if (currentState == STATE_SETUP_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            ivTakenPhoto.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
        }else {
            if(videoView.isPlaying()){
                videoView.pause();
                videoView.stopPlayback();
            }
            videoView.setVisibility(View.GONE);
        }
    }

    private void addMyStory(){

        if(ConnectionDetector.isConnected()){
            Progress.show(CameraActivity.this);
            add_to_story.setEnabled(false);

            Map<String,String> map = new HashMap<>();
            map.put("userId", ""+ Mualab.currentUser.id);
            map.put("type", isVideoUri?"video":"image");
            if(isVideoUri){

                ivTakenPhoto.setDrawingCacheEnabled(true);
                Bitmap bitmap = ivTakenPhoto.getDrawingCache();
              /* Bitmap bitmap = ImageVideoUtil.getVideoToThumbnil(photoPath., this,
                       MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);*/
                HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            Progress.hide(CameraActivity.this);
                            if (status.equalsIgnoreCase("success")) {
                                finish();
                            }
                            else showToast(message);
                        } catch (Exception e) {
                            add_to_story.setEnabled(true);
                            e.printStackTrace();
                            Progress.hide(CameraActivity.this);
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        Progress.hide(CameraActivity.this);
                        add_to_story.setEnabled(true);
                        Log.d("res:", ""+error.getLocalizedMessage());
                    }})
                        .setParam(map)
                        .setAuthToken(Mualab.currentUser.authToken)
                        .setProgress(true));
                task.postFile("myStory", photoPath, bitmap);
            }

            else {
                Bitmap bitmap = ivTakenPhoto.getBitmap();
                HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            Progress.hide(CameraActivity.this);
                            if (status.equalsIgnoreCase("success")) {
                                Mualab.isStoryUploaded = true;
                                finish();
                            }
                            else showToast(message);
                        } catch (Exception e) {
                            add_to_story.setEnabled(true);
                            Progress.hide(CameraActivity.this);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        add_to_story.setEnabled(true);
                        Progress.hide(CameraActivity.this);
                        Log.d("res:", ""+error.getLocalizedMessage());
                    }})
                        .setParam(map)
                        .setAuthToken(Mualab.currentUser.authToken)
                        .setProgress(true));
                task.postImage("myStory", bitmap);
            }

        }else showToast(getString(R.string.error_msg_network));
    }

    private CountDownTimer countDownTimer;

    private void startTimear() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnTakePhoto.setText(String.valueOf(millisUntilFinished / 1000));
                Log.e("Count", "count" + millisUntilFinished);
                if (57962 >= millisUntilFinished)
                    btnTakePhoto.setEnabled(true);
            }

            public void onFinish() {
                btnTakePhoto.setText("REC");
                btnTakePhoto.setEnabled(true);
                if (isStartRecord)
                    stopRecording();
            }
        }.start();
        startRecording();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(CameraActivity.this);
                } else showToast("YOUR  PERMISSION DENIED");
            }
            break;
        }
    }
}