package com.zego.livedemo3.ui.activities.mixstream;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.biz.BizUser;
import com.zego.livedemo3.R;
import com.zego.livedemo3.ZegoApiManager;
import com.zego.livedemo3.presenters.BizLivePresenter;
import com.zego.livedemo3.ui.activities.LogListActivity;
import com.zego.livedemo3.ui.base.AbsShowActivity;
import com.zego.livedemo3.ui.widgets.PublishSettingsPannel;
import com.zego.livedemo3.ui.widgets.ViewLive;
import com.zego.livedemo3.utils.PreferenceUtil;
import com.zego.livedemo3.utils.ShareUtils;
import com.zego.livedemo3.utils.ZegoAVKitUtil;
import com.zego.zegoavkit2.AuxData;
import com.zego.zegoavkit2.ZegoAVKit;
import com.zego.zegoavkit2.ZegoAVKitCommon;
import com.zego.zegoavkit2.ZegoAvConfig;
import com.zego.zegoavkit2.ZegoConstants;
import com.zego.zegoavkit2.callback.ZegoLiveCallback;
import com.zego.zegoavkit2.entity.ZegoUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;

/**
 * des: 主页面
 */
public abstract class MixstreamBaseLiveActivity extends AbsShowActivity {

    public static final String KEY_CHANNEL = "KEY_CHANNEL";

    public static final String KEY_PUBLISH_TITLE = "KEY_PUBLISH_TITLE";

    public static final String KEY_PUBLISH_STREAM_ID = "KEY_PUBLISH_STREAM_ID";

    public static final String KEY_IS_PUBLISHING = "KEY_IS_PUBLISHING";

    public static final String KEY_ENABLE_CAMERA = "KEY_ENABLE_CAMERA";

    public static final String KEY_ENABLE_FRONT_CAM = "KEY_ENABLE_FRONT_CAM";

    public static final String KEY_ENABLE_TORCH = "KEY_ENABLE_TORCH";

    public static final String KEY_ENABLE_SPEAKER = "KEY_ENABLE_SPEAKER";

    public static final String KEY_ENABLE_MIC = "KEY_ENABLE_MIC";

    public static final String KEY_HAVE_LOGINNED_CHANNEL = "KEY_HAVE_LOGINNED_CHANNEL";

    public static final String KEY_SELECTED_BEAUTY = "KEY_SELECTED_BEAUTY";

    public static final String KEY_SELECTED_FILTER = "KEY_SELECTED_FILTER";

    public static final String KEY_PUBLISH_NUMBER = "KEY_PUBLISH_NUMBER";

    public static final String KEY_LIST_LIVEVIEW_TAG = "KEY_LIST_LIVEVIEW_TAG";

    public static final String KEY_LIST_LOG = "KEY_LIST_LOG";

    public static final String KEY_CAMERA_CAPTURE_ROTATION = "KEY_CAMERA_CAPTURE_ROTATION";

    public static final String KEY_ROOM_KEY = "KEY_ROOM_KEY";

    public static final String KEY_SETVER_KEY = "KEY_SERVER_KEY";

    public static final String MY_SELF = "MySelf";

    public static final String EMPTY_STREAM_ID = "EMPTY_STREAM_ID";

    protected ZegoAVKit mZegoAVKit;

    protected InputStream mIsBackgroundMusic;

    protected LinkedList<ViewLive> mListViewLive= new LinkedList<>();

    protected List<String> mListLiveViewTag = new ArrayList<>();

    protected List<String> mListLiveViewTagForCallComing = new ArrayList<>();

    protected LinkedHashMap<ZegoAVKitCommon.ZegoRemoteViewIndex, String> mMapFreeViewIndex;

    protected LinkedList<String> mListLog = new LinkedList<>();

    protected Map<String, Boolean> mMapReplayStreamID = new HashMap<>();

    public TextView tvPublisnControl;

    public TextView tvPublishSetting;

    public TextView tvSpeaker;

    public BottomSheetBehavior mBehavior;

    public RelativeLayout mRlytControlHeader;

    protected String mPublishTitle;

    protected String mPublishStreamID;

    protected String mChannel;

    protected boolean mIsPublishing = false;

    protected boolean mEnableSpeaker = true;

    protected boolean mEnableCamera = true;

    protected boolean mEnableFrontCam = true;

    protected boolean mEnableMic = true;

    protected boolean mEnableTorch = false;

    protected boolean mEnableBackgroundMusic = false;

    protected int mSelectedBeauty = 0;

    protected int mSelectedFilter = 0;

    protected int mPublishNumber = 0;

    protected boolean mHaveLoginedChannel = false;

    protected boolean mHostHasBeenCalled = false;

    protected String mMixStreamID;
    protected boolean mIsMixStreamMode = false;
    protected String mMixStreamMagic;
    protected BizUser mMixStreamRequestUser;


    protected boolean mIsDeviceOrientationPortrait = false;

    protected Map<String, Object> mMapStreamWidthAndHeight = new HashMap<>();

    protected int mCurrentOrientation = -1;

    protected DisplayManager.DisplayListener mDisplayListener;

    protected PhoneStateListener mPhoneStateListener;

    protected long mRoomKey;

    protected long mServerKey;

    protected abstract void doPublishOrPlay();

    protected abstract void initPublishControlText();

    protected abstract void hidePlayBackground();


    @Override
    protected int getContentViewLayout() {
        return R.layout.activity_live;
    }


    @Override
    protected void initExtraData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Activity 后台被回收后重新启动, 恢复数据
            mChannel = PreferenceUtil.getInstance().getStringValue(KEY_CHANNEL, null);
            mPublishTitle = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_TITLE, null);
            mPublishStreamID = PreferenceUtil.getInstance().getStringValue(KEY_PUBLISH_STREAM_ID, null);
            mIsPublishing = PreferenceUtil.getInstance().getBooleanValue(KEY_IS_PUBLISHING, false);
            mEnableFrontCam = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_FRONT_CAM, false);
            mEnableTorch = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_TORCH, false);
            mEnableSpeaker = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_SPEAKER, false);
            mEnableMic = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_MIC, false);
            mEnableCamera = PreferenceUtil.getInstance().getBooleanValue(KEY_ENABLE_CAMERA, false);
            mHaveLoginedChannel = PreferenceUtil.getInstance().getBooleanValue(KEY_HAVE_LOGINNED_CHANNEL, false);
            mSelectedBeauty = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_BEAUTY, 0);
            mSelectedFilter = PreferenceUtil.getInstance().getIntValue(KEY_SELECTED_FILTER, 0);
            mPublishNumber = PreferenceUtil.getInstance().getIntValue(KEY_PUBLISH_NUMBER, 0);
            mRoomKey = PreferenceUtil.getInstance().getLongValue(KEY_ROOM_KEY, 0);
            mServerKey = PreferenceUtil.getInstance().getLongValue(KEY_SETVER_KEY, 0);

            mListLiveViewTag = (List<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LIVEVIEW_TAG);
            if (mListLiveViewTag == null) {
                mListLiveViewTag = new ArrayList<>();
            }

            mListLog = (LinkedList<String>) PreferenceUtil.getInstance().getObjectFromString(KEY_LIST_LOG);
            if (mListLog == null) {
                mListLog = new LinkedList<>();
            }

        }
    }


    @Override
    protected void initVariables(final Bundle savedInstanceState) {

        mZegoAVKit = ZegoApiManager.getInstance().getZegoAVKit();

        mMapFreeViewIndex = new LinkedHashMap<>();
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.First, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Second, EMPTY_STREAM_ID);
        mMapFreeViewIndex.put(ZegoAVKitCommon.ZegoRemoteViewIndex.Third, EMPTY_STREAM_ID);

        // 初始化sdk回调
        initCallback();
        // 初始化电话监听器
        initPhoneCallingListener();

//        //  初始化手机朝向
//        mCurrentOrientation = getWindowManager().getDefaultDisplay().getRotation();
//        setRotateFromInterfaceOrientation(mCurrentOrientation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDisplayListener = new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    mCurrentOrientation =  getWindowManager().getDefaultDisplay().getRotation();
                    setRotateFromInterfaceOrientation(mCurrentOrientation);
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            };

            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayManager.registerDisplayListener(mDisplayListener, mHandler);
        }
    }

    /**
     * 设置设备朝向.
     */
    protected void setupDeviceOrientation(){

        // 判断手机是否垂直摆放
        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        mCurrentOrientation = orientation;
        mIsDeviceOrientationPortrait = true;
        switch (orientation){
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                mIsDeviceOrientationPortrait = false;
                break;
        }


        //  修正最终需要的输出分辨率, 保证：横屏姿势时，输出横屏视频，竖屏姿势时，输出竖屏视频
        ZegoAvConfig currentConfig = ZegoApiManager.getInstance().getZegoAvConfig();

        int width = currentConfig.getVideoEncodeResolutionWidth();
        int height = currentConfig.getVideoEncodeResolutionHeight();

        if((mIsDeviceOrientationPortrait &&  width > height) // 手机竖屏, 但是 宽 > 高
                || (!mIsDeviceOrientationPortrait && width < height)){ // 手机横屏, 但是 宽 < 高

            currentConfig.setVideoEncodeResolution(height, width);
            ZegoApiManager.getInstance().setZegoConfig(currentConfig);
        }

        setRotateFromInterfaceOrientation(orientation);
    }

    protected void setRotateFromInterfaceOrientation(int orientation){

        // 设置手机朝向
        mZegoAVKit.setAppOrientation(orientation);

        mZegoAVKit.setLocalViewRotation(calculatePreviewRotation(orientation));

        updateRemoteViewRotation();
    }

    protected ZegoAVKitCommon.ZegoCameraCaptureRotation calculatePreviewRotation(int orientation){

        ZegoAVKitCommon.ZegoCameraCaptureRotation zegoCameraCaptureRotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;

        switch (orientation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if(!mIsDeviceOrientationPortrait){
                    zegoCameraCaptureRotation = mEnableFrontCam ? ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270 : ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if(mIsDeviceOrientationPortrait){
                    zegoCameraCaptureRotation = mEnableFrontCam ? ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_270 : ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                }
                break;
        }

        return zegoCameraCaptureRotation;
    }

    protected void updateRemoteViewRotation(){

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < mListViewLive.size(); i++){
                    ViewLive viewLive = mListViewLive.get(i);
                    int streamOrdinal = viewLive.getStreamOrdinal();
                    switch (streamOrdinal){
                        case 0:
                        case 1:
                        case 2:
                            String streamID = viewLive.getStreamID();
                            int arr[] = (int[]) mMapStreamWidthAndHeight.get(streamID);
                            if(arr != null){
                                ZegoAVKitCommon.ZegoCameraCaptureRotation rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_0;

                                int width = viewLive.getTextureView().getWidth();
                                int height = viewLive.getTextureView().getHeight();

                                if((width < height && arr[0] > arr[1])
                                        || (width > height && arr[0] < arr[1])){

                                    rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                                }

                                if(i > 0 && (mCurrentOrientation == Surface.ROTATION_90 || mCurrentOrientation == Surface.ROTATION_270)){
                                    rotation = ZegoAVKitCommon.ZegoCameraCaptureRotation.Rotate_90;
                                }

                                mZegoAVKit.setRemoteViewRotation(rotation, ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
                            }
                            break;
                    }
                }
            }
        }, 500);

    }

    /**
     * 初始化设置面板.
     */
    private void initSettingPannel() {

        PublishSettingsPannel settingsPannel = (PublishSettingsPannel) findViewById(R.id.publishSettingsPannel);
        settingsPannel.initPublishSettings(mEnableCamera, mEnableFrontCam, mEnableMic, mEnableTorch, mEnableBackgroundMusic, mSelectedBeauty, mSelectedFilter);
        settingsPannel.setPublishSettingsCallback(new PublishSettingsPannel.PublishSettingsCallback() {
            @Override
            public void onEnableCamera(boolean isEnable) {
                mEnableCamera = isEnable;
                mZegoAVKit.enableCamera(isEnable);
            }

            @Override
            public void onEnableFrontCamera(boolean isEnable) {
                mEnableFrontCam = isEnable;
                mZegoAVKit.setFrontCam(isEnable);

                setRotateFromInterfaceOrientation(mCurrentOrientation);
            }

            @Override
            public void onEnableMic(boolean isEnable) {
                mEnableMic = isEnable;
                mZegoAVKit.enableMic(isEnable);
            }

            @Override
            public void onEnableTorch(boolean isEnable) {
                mEnableTorch = isEnable;
                mZegoAVKit.enableTorch(isEnable);
            }

            @Override
            public void onEnableBackgroundMusic(boolean isEnable) {
                mEnableBackgroundMusic = isEnable;
                mZegoAVKit.enableAux(isEnable);

                if(!isEnable){
                    if(mIsBackgroundMusic != null){
                        try {
                            mIsBackgroundMusic.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mIsBackgroundMusic = null;
                    }
                }
            }

            @Override
            public void onSetBeauty(int beauty) {
                mSelectedBeauty = beauty;
                mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(beauty));
            }

            @Override
            public void onSetFilter(int filter) {
                mSelectedFilter = filter;
                mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(filter));
            }
        });

        mBehavior = BottomSheetBehavior.from(settingsPannel);
        FrameLayout flytMainContent = (FrameLayout) findViewById(R.id.main_content);
        if (flytMainContent != null) {
            flytMainContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            });
        }
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        tvSpeaker = (TextView) findViewById(R.id.tv_speaker);
        tvPublishSetting = (TextView) findViewById(R.id.tv_publish_settings);
        tvPublisnControl = (TextView) findViewById(R.id.tv_publish_control);
        initPublishControlText();

        mRlytControlHeader = (RelativeLayout) findViewById(R.id.rlyt_control_header);

        initSettingPannel();

        final ViewLive vlBigView = (ViewLive) findViewById(R.id.vl_big_view);
        if (vlBigView != null) {
            mListViewLive.add(vlBigView);
        }
        vlBigView.setViewLiveCallback(new ViewLive.ViewLiveCallback() {
            @Override
            public void setLocalView(TextureView textureView) {
                mZegoAVKit.setLocalView(textureView);
            }

            @Override
            public void setRemoteView(ZegoAVKitCommon.ZegoRemoteViewIndex index, TextureView textureView) {
                mZegoAVKit.setRemoteView(index, textureView);
            }

            @Override
            public void setRemoteViewMode(ZegoAVKitCommon.ZegoRemoteViewIndex index, ZegoAVKitCommon.ZegoVideoViewMode mode) {
                mZegoAVKit.setRemoteViewMode(index, mode);
            }

            @Override
            public void shareToQQ(List<String> listShareUrls) {
                ShareUtils.getInstance().shareToQQ(MixstreamBaseLiveActivity.this, vlBigView.getListShareUrls(), mRoomKey, mServerKey, mPublishStreamID);
            }
        });

        final ViewLive vlSmallView1 = (ViewLive) findViewById(R.id.vl_small_view1);
        if (vlSmallView1 != null) {
            vlSmallView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vlSmallView1.toExchangeView(vlBigView);
                }
            });
            mListViewLive.add(vlSmallView1);
        }


        final ViewLive vlSmallView2 = (ViewLive) findViewById(R.id.vl_small_view2);
        if (vlSmallView2 != null) {
            vlSmallView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vlSmallView2.toExchangeView(vlBigView);
                }
            });
            mListViewLive.add(vlSmallView2);
        }

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            replayAndRepublish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 保存数据, 用于Activity在后台被回收后重新恢复
        PreferenceUtil.getInstance().setStringValue(KEY_CHANNEL, mChannel);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_TITLE, mPublishTitle);
        PreferenceUtil.getInstance().setStringValue(KEY_PUBLISH_STREAM_ID, mPublishStreamID);
        PreferenceUtil.getInstance().setBooleanValue(KEY_IS_PUBLISHING, mIsPublishing);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_CAMERA, mEnableCamera);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_FRONT_CAM, mEnableFrontCam);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_TORCH, mEnableTorch);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_SPEAKER, mEnableSpeaker);
        PreferenceUtil.getInstance().setBooleanValue(KEY_ENABLE_MIC, mEnableMic);
        PreferenceUtil.getInstance().setBooleanValue(KEY_HAVE_LOGINNED_CHANNEL, mHaveLoginedChannel);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_BEAUTY, mSelectedBeauty);
        PreferenceUtil.getInstance().setIntValue(KEY_SELECTED_FILTER, mSelectedFilter);
        PreferenceUtil.getInstance().setIntValue(KEY_PUBLISH_NUMBER, mPublishNumber);
        PreferenceUtil.getInstance().setLongValue(KEY_ROOM_KEY, mRoomKey);
        PreferenceUtil.getInstance().setLongValue(KEY_SETVER_KEY, mServerKey);

        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LOG, mListLog);

        mListLiveViewTag = new ArrayList<>();
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            mListLiveViewTag.add(mListViewLive.get(i).getLiveTag());
        }
        PreferenceUtil.getInstance().setObjectToString(KEY_LIST_LIVEVIEW_TAG, mListLiveViewTag);

    }

    /**
     * activity重建后, 恢复发布与播放.
     */
    protected void replayAndRepublish() {

        for (int i = 0, size = mListLiveViewTag.size(); i < size; i++) {
            int streamOrdinal = ViewLive.getStreamOrdinalFromLiveTag(mListLiveViewTag.get(i));
            String streamID = ViewLive.getStreamIDFromLiveTag(mListLiveViewTag.get(i));

            if(ViewLive.isPublishView(streamOrdinal)){
                startPublish();
            }else {
                startPlay(streamID, ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
            }
        }
    }

    /**
     * 挂断电话后, 恢复发布与播放.
     */
    protected void replayAndRepublishAfterRingOff() {
        for (int i = 0, size = mListLiveViewTagForCallComing.size(); i < size; i++) {
            int streamOrdinal = ViewLive.getStreamOrdinalFromLiveTag(mListLiveViewTagForCallComing.get(i));
            String streamID = ViewLive.getStreamIDFromLiveTag(mListLiveViewTagForCallComing.get(i));

            if(ViewLive.isPublishView(streamOrdinal)){
                BizLivePresenter.getInstance().createStream(mPublishTitle, mPublishStreamID);
            }else {
                startPlay(streamID, ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(streamOrdinal));
            }
        }
    }


    /**
     * 获取空闲的remoteViewIndex.
     * @return
     */
    protected ZegoAVKitCommon.ZegoRemoteViewIndex getFreeZegoRemoteViewIndex() {
        ZegoAVKitCommon.ZegoRemoteViewIndex freeIndex = null;
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (EMPTY_STREAM_ID.equals(mMapFreeViewIndex.get(index))) {
                freeIndex = index;
                break;
            }
        }
        return freeIndex;
    }

    /**
     * 获取空闲的View用于播放或者发布.
     *
     * @return
     */
    protected ViewLive getFreeViewLive() {
        ViewLive vlFreeView = null;
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            ViewLive viewLive = mListViewLive.get(i);
            if (viewLive.isFree()) {
                vlFreeView = viewLive;
                vlFreeView.setVisibility(View.VISIBLE);
                break;
            }
        }
        return vlFreeView;
    }

    /**
     * 释放View用于再次播放, 释放remoteViewIndex.
     *
     * @param streamID
     */
    protected void releaseTextureViewAndRemoteViewIndex(String streamID) {
        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            ViewLive currentViewLive = mListViewLive.get(i);
            if (currentViewLive.getStreamID().equals(streamID)) {
                int j = i;
                for (; j < size - 1; j++) {
                    ViewLive nextViewLive = mListViewLive.get(j + 1);
                    if (nextViewLive.isFree()) {
                        break;
                    }

                    int nextStreamOrdinal = nextViewLive.getStreamOrdinal();
                    if(ViewLive.isPublishView(nextStreamOrdinal)){
                        mZegoAVKit.setLocalView(currentViewLive.getTextureView());
                    }else {
                        mZegoAVKit.setRemoteView(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(nextStreamOrdinal), currentViewLive.getTextureView());

                    }

//                    currentViewLive.setLiveTag(nextViewLive.getLiveTag());
//                    currentViewLive.setLiveQuality(nextViewLive.getLiveQuality());
                    currentViewLive.toExchangeView(nextViewLive);
                    currentViewLive = nextViewLive;
                }
                // 标记最后一个View可用
                mListViewLive.get(j).setFree();
                break;
            }
        }

        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (mMapFreeViewIndex.get(index).equals(streamID)) {
                // 标记remoteViewIndex可用
                mMapFreeViewIndex.put(index, EMPTY_STREAM_ID);
                break;
            }
        }
    }

    /**
     * 通过streamID查找正在publish或者play的ViewLive.
     * @param streamID
     * @return
     */
    protected ViewLive getViewLiveByStreamID(String streamID){
        if(TextUtils.isEmpty(streamID)){
            return  null;
        }

        ViewLive viewLive = null;
        for(ViewLive vl : mListViewLive){
            if(streamID.equals(vl.getStreamID())){
                viewLive = vl;
                break;
            }
        }

        return viewLive;
    }


    /**
     * 初始化zego sdk回调.
     */
    protected void initCallback() {

        mZegoAVKit.setZegoLiveCallback(new ZegoLiveCallback() {
            @Override
            public void onLoginChannel(String liveChannel, int retCode) {
                if (retCode == 0) {
                    if (!mHaveLoginedChannel) {
                        doPublishOrPlay();
                        mHaveLoginedChannel = true;
                        recordLog(MY_SELF + ": onLoginChannel success(" + liveChannel + ")");
                    }

                    if (mHostHasBeenCalled) {
                        mHostHasBeenCalled = false;
                        // 挂断电话重新恢复
                        replayAndRepublishAfterRingOff();
                    }
                } else {
                    recordLog(MY_SELF + ": onLoginChannel fail(" + liveChannel + ") --errCode:" + retCode);
                }
            }

            @Override
            public void onPublishSucc(String streamID, String liveChannel, HashMap<String, Object> info) {
                mIsPublishing = true;
                recordLog(MY_SELF + ": onPublishSucc(" + streamID + ")");

                BizLivePresenter.getInstance().reportStreamState(true, streamID, PreferenceUtil.getInstance().getUserID());

                if(mIsMixStreamMode){
                    // 同意连麦请求
                    List<BizUser> listToUsers = new ArrayList<>();
                    listToUsers.add(mMixStreamRequestUser);
                    BizLivePresenter.getInstance().respondLiveTogether(listToUsers, mMixStreamMagic, true);
                  //  setMixStreamMode(false);
                    mMixStreamRequestUser = null;
                    mMixStreamMagic = null;
                }

                initPublishControlText();

                ViewLive viewLivePublish = getViewLiveByStreamID(streamID);
                if(viewLivePublish != null){
                    List<String> listUrls = new ArrayList<>();
                    if(info != null){
                        listUrls.add(((String [])info.get("hlsList"))[0]);
                        listUrls.add(((String [])info.get("rtmpList"))[0]);
                    }
                    viewLivePublish.setListShareUrls(listUrls);
                }

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPublishStop(int retCode, String streamID, String liveChannel) {
                mIsPublishing = false;
                recordLog(MY_SELF + ": onPublishStop(" + streamID + ") --errCode:" + retCode);
                // 停止预览
                mZegoAVKit.stopPreview();
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);
                mRlytControlHeader.bringToFront();

                BizLivePresenter.getInstance().reportStreamState(false, streamID, PreferenceUtil.getInstance().getUserID());

                // 重新创建流
                if(mIsMixStreamMode){
                    BizLivePresenter.getInstance().createStream(mPublishTitle, null);
                }

                initPublishControlText();
            }

            @Override
            public void onMixStreamConfigUpdate(int retCode, String mixStreamID, HashMap<String, Object> info) {
                recordLog("混流地址:" + ((String[])info.get(ZegoConstants.KEY_RTMP_URL_LIST))[0]);

                ViewLive viewLivePublish = getViewLiveByStreamID(mPublishStreamID);
                if(viewLivePublish != null){
                    List<String> listUrls = new ArrayList<>();
                    if(info != null){
                        listUrls.add(((String [])info.get("hlsList"))[0]);
                        listUrls.add(((String [])info.get("rtmpList"))[0]);
                    }
                    viewLivePublish.setListShareUrls(listUrls);
                }

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onPlaySucc(String streamID, String liveChannel) {
                recordLog(MY_SELF + ": onPlaySucc(" + streamID + ")");
                mRlytControlHeader.bringToFront();

                mPublishNumber++;
                setPublishEnabled();

                // 记录流ID用于play失败后重新play
                mMapReplayStreamID.put(streamID, false);
            }

            @Override
            public void onPlayStop(int retCode, String streamID, String liveChannel) {
                recordLog(MY_SELF + ": onPlayStop(" + streamID + ") --errCode:" + retCode);
                // 释放View
                releaseTextureViewAndRemoteViewIndex(streamID);
                mRlytControlHeader.bringToFront();

                mPublishNumber--;
                setPublishEnabled();

                // 当一条流play失败后重新play一次
                if(retCode == 2 && !TextUtils.isEmpty(streamID)){
                    if(!mMapReplayStreamID.get(streamID)){
                        mMapReplayStreamID.put(streamID, true);
                        startPlay(streamID, getFreeZegoRemoteViewIndex());
                    }
                }
            }

            @Override
            public void onVideoSizeChanged(String streamID, int width, int height) {
                hidePlayBackground();

                if(width > height){
                    ViewLive viewLivePlay = getViewLiveByStreamID(streamID);
                    if(viewLivePlay != null){
                        viewLivePlay.setZegoVideoViewMode(true, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                        mZegoAVKit.setRemoteViewMode(ZegoAVKitUtil.getZegoRemoteViewIndexByOrdinal(viewLivePlay.getStreamOrdinal()), ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFit);
                    }
                }


                int arr[] = new int[2];
                arr[0] = width;
                arr[1] = height;
                mMapStreamWidthAndHeight.put(streamID, arr);

                updateRemoteViewRotation();

                mRlytControlHeader.bringToFront();
            }

            @Override
            public void onTakeRemoteViewSnapshot(final Bitmap bitmap, ZegoAVKitCommon.ZegoRemoteViewIndex zegoRemoteViewIndex) {
            }

            @Override
            public void onTakeLocalViewSnapshot(final Bitmap bitmap) {
            }

            @Override
            public void onCaptureVideoSize(int width, int height) {
            }

            @Override
            public void onPlayQualityUpdate(String streamID, int quality) {
                ViewLive viewLive = getViewLiveByStreamID(streamID);
                if(viewLive != null){
                    viewLive.setLiveQuality(quality);
                }

            }

            @Override
            public void onPublishQulityUpdate(String streamID, int quality) {
                ViewLive viewLive = getViewLiveByStreamID(streamID);
                if(viewLive != null){
                    viewLive.setLiveQuality(quality);
                }
            }

            @Override
            public AuxData onAuxCallback(int dataLen) {
                // 开启伴奏后, sdk每20毫秒一次取数据
                if(!mEnableBackgroundMusic || dataLen <= 0){
                    return null;
                }

                AuxData auxData = new AuxData();
                auxData.dataBuf = new byte[dataLen];

                try{
                    AssetManager am = getAssets();
                    if(mIsBackgroundMusic == null){
                        mIsBackgroundMusic = am.open("a.pcm");
                    }
                    int len = mIsBackgroundMusic.read(auxData.dataBuf);

                    if(len <= 0){
                        // 歌曲播放完毕
                        mIsBackgroundMusic.close();
                        mIsBackgroundMusic = null;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

                auxData.channelCount = 2;
                auxData.sampleRate = 44100;


                return auxData;
            }
        });

    }


    /**
     * 电话状态监听.
     */
    protected void initPhoneCallingListener() {
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mHostHasBeenCalled) {
                            recordLog(MY_SELF + ": call state idle");
                            // 登陆频道
                            loginChannel();
                        }

                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        recordLog(MY_SELF + ": call state ringing");
                        mHostHasBeenCalled = true;
                        mListLiveViewTagForCallComing = new ArrayList<>();
                        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
                            mListLiveViewTagForCallComing.add(mListViewLive.get(i).getLiveTag());
                        }
                        // 来电停止发布与播放
                        stopAllStreamAndLogout();
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        break;
                }
            }
        };

        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    protected void recordLog(String msg) {
        mListLog.addFirst(msg);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                           publishStream();
                        }
                    });
                }else {


                    if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this, R.string.allow_camera_permission, Toast.LENGTH_LONG).show();
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(this, R.string.open_recorder_permission, Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
                break;
        }
    }

    protected void publishStream(){

        mZegoAVKit.setFrontCam(mEnableFrontCam);

        setupDeviceOrientation();

        ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记view已经被占用
        freeViewLive.setLiveTag(ViewLive.PUBLISH_STREAM_ORDINAL, mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publish " + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        // 开始播放
        mZegoAVKit.setLocalView(freeViewLive.getTextureView());
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();
        mZegoAVKit.startPublish(mPublishTitle, mPublishStreamID);

        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);
    }

    protected void publishMixStream(){

        ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记view已经被占用
        freeViewLive.setLiveTag(ViewLive.PUBLISH_STREAM_ORDINAL, mPublishStreamID);

        // 输出发布状态
        recordLog(MY_SELF + ": start publish " + mPublishStreamID);

        // 设置美颜 滤镜
        mZegoAVKit.enableBeautifying(ZegoAVKitUtil.getZegoBeauty(mSelectedBeauty));
        mZegoAVKit.setFilter(ZegoAVKitUtil.getZegoFilter(mSelectedFilter));

        // 开始播放
        mZegoAVKit.setLocalView(freeViewLive.getTextureView());
        mZegoAVKit.setLocalViewMode(ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.startPreview();

        ZegoAvConfig zegoAvConfig  = ZegoApiManager.getInstance().getZegoAvConfig();

        int width = zegoAvConfig.getVideoEncodeResolutionWidth();
        int height = zegoAvConfig.getVideoEncodeResolutionHeight();
        mZegoAVKit.startPublishMixStream(mPublishTitle, mPublishStreamID, mMixStreamID, width, height, 2);

        mZegoAVKit.setFrontCam(mEnableFrontCam);
        mZegoAVKit.enableTorch(mEnableTorch);
        mZegoAVKit.enableMic(mEnableMic);
    }

    /**
     * 开始发布.
     */
    protected void startPublish() {
        // 6.0及以上的系统需要在运行时申请CAMERA RECORD_AUDIO权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 101);
            } else {
                publishStream();
            }
        } else {
            publishStream();
        }
    }

    protected void stopPublish() {
        mZegoAVKit.stopPreview();
        mZegoAVKit.stopPublish();
        mZegoAVKit.setLocalView(null);
    }

    protected boolean isStreamExisted(String streamID){
        boolean isExisted = false;
        for(String value : mMapFreeViewIndex.values()){
            if(value.equals(streamID)){
                isExisted = true;
                break;
            }
        }
        return isExisted;
    }

    /**
     * 开始播放流.
     */
    protected void startPlay(String streamID, ZegoAVKitCommon.ZegoRemoteViewIndex remoteViewIndex) {

        if(isStreamExisted(streamID)){
            return ;
        }

        if (remoteViewIndex == null) {
            return;
        }

         ViewLive freeViewLive = getFreeViewLive();
        if (freeViewLive == null) {
            return;
        }

        // 标记remoteViewIndex已经被占用
        mMapFreeViewIndex.put(remoteViewIndex, streamID);

        // 标记view已经被占用
        freeViewLive.setLiveTag(remoteViewIndex.code, streamID);

        // 输出播放状态
        recordLog(MY_SELF + ": start play " + streamID);

        // 播放
        mZegoAVKit.setRemoteViewMode(remoteViewIndex, ZegoAVKitCommon.ZegoVideoViewMode.ScaleAspectFill);
        mZegoAVKit.setRemoteView(remoteViewIndex, freeViewLive.getTextureView());
        mZegoAVKit.startPlayStream(streamID, remoteViewIndex);
    }

    protected void stopPlay(String streamID) {
        for (ZegoAVKitCommon.ZegoRemoteViewIndex index : mMapFreeViewIndex.keySet()) {
            if (mMapFreeViewIndex.get(index).equals(streamID)) {
                mZegoAVKit.stopPlayStream(streamID);
                mZegoAVKit.setRemoteView(index, null);
                break;
            }
        }
    }


    protected void logout() {

        if(mIsPublishing){
            AlertDialog dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.do_you_really_want_to_leave)).setTitle(getString(R.string.hint)).setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setMixStreamMode(false);
                    stopAllStreamAndLogout();
                    BizLivePresenter.getInstance().leaveRoom();
                    dialog.dismiss();
                    finish();
                }
            }).setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();

            dialog.show();
        }else {

            stopAllStreamAndLogout();
            BizLivePresenter.getInstance().leaveRoom();
            finish();
        }

    }

    /**
     * 退出.
     */
    protected void stopAllStreamAndLogout() {

        for (int i = 0, size = mListViewLive.size(); i < size; i++) {
            if(ViewLive.isPublishView(mListViewLive.get(i).getStreamOrdinal())){
                stopPublish();
            }else {
                stopPlay(mListViewLive.get(i).getStreamID());
            }
        }

        mZegoAVKit.logoutChannel();
    }

    protected void setPublishEnabled() {
        if (!mIsPublishing) {
            if (mPublishNumber >= 3) {
                tvPublisnControl.setEnabled(false);
            } else {
                tvPublisnControl.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return false;
            } else {
                // 退出
                logout();
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.tv_log_list)
    public void openLogList() {
        LogListActivity.actionStart(this);
    }

    @OnClick(R.id.tv_publish_settings)
    public void publishSettings() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @OnClick(R.id.tv_speaker)
    public void doMute() {
        if (mEnableSpeaker) {
            mEnableSpeaker = false;
        } else {
            mEnableSpeaker = true;
        }

        mZegoAVKit.enableSpeaker(mEnableSpeaker);
        tvSpeaker.setSelected(!mEnableSpeaker);
    }

    @OnClick(R.id.tv_close)
    public void close() {
        logout();
    }

    @Override
    protected void onDestroy() {
        // 注销屏幕监听
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayManager.unregisterDisplayListener(mDisplayListener);
        }

        // 注销电话监听
        TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        // 注销回调, 避免内存泄漏
        mZegoAVKit.setZegoLiveCallback(null);

        super.onDestroy();
    }

    protected void setMixStreamMode(boolean isMixStreamMode){
        mIsMixStreamMode = isMixStreamMode;
    }

    protected void loginChannel(){
        ZegoUser zegoUser = new ZegoUser(PreferenceUtil.getInstance().getUserID(), PreferenceUtil.getInstance().getUserName());
        mZegoAVKit.loginChannel(zegoUser, mChannel);
    }
}
