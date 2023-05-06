package com.emureka.serialandbluetooth.mediapipe;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.emureka.serialandbluetooth.MyDataStore;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;


public class PoseTracking {
    static final String TAG = "Debug";
    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";

    // private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
    // corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private static final boolean FLIP_FRAMES_VERTICALLY = true;
    //temp const

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    private EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    private FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;
    // ApplicationInfo for retrieving metadata defined in the manifest.
    private ApplicationInfo applicationInfo;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;
    public static String mode = "auto";
    static String auto_mode = "";
    public static boolean reset = false;
    static int counter = 0;
    static boolean isLeft = false;
    static double ref_head_x = 0;
    static double ref_head_sh_dist = 0;
    static double ref_head_z=0;
    static double dist_correction =0;
    static double ref_fore = 0;
    public static int currStat = 0;
    static double[] movingAvg = {0,0,0};
    public static double[] poseOffset = {0, 0, 0};

    Activity context;
    public PoseTracking(Activity _context){
        context =_context;
    }

    public void onCreate(SurfaceView surfaceView){

        previewDisplayView = surfaceView;
        Log.d(TAG, "onCreate: previewDisplayView"+previewDisplayView.getHeight()+"  "+previewDisplayView.getWidth());
        Log.d(TAG, "onCreate: "+previewDisplayView);
        setupPreviewDisplayView();
        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(context);
        eglManager = new EglManager(null);
        eglManager = new EglManager(null);
        processor =
                new FrameProcessor(
                        context,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);
        PermissionHelper.checkAndRequestCameraPermissions(context);
        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        LandmarkProto.NormalizedLandmarkList landmarks = LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
                        if (landmarks == null) {
                            return;
                        }
                        getPoseValue(landmarks,mode,ref_head_x,ref_head_z,ref_head_sh_dist);
                    } catch (InvalidProtocolBufferException e) {
                        Log.e(TAG, "Couldn't Exception received - " + e);
                        return;
                    }
                });
//        }
    }

    public static void set_mode(int mode) {
        switch (mode) {
            case 1: {
                PoseTracking.mode = "side";
                break;
            }
            case 2: {
                PoseTracking.mode = "front";
                break;
            }
            case 0 : default: {
                PoseTracking.mode = "auto";
                break;
            }
        }
    }

    public static void set_ref(){
        reset = true;
        counter = 0;
        ref_head_x  = 0;
        ref_head_sh_dist = 0;
        ref_head_z = 0;
        ref_fore = 0;
    }

    private static void getPoseValue(LandmarkProto.NormalizedLandmarkList landmarks, String mode, double _ref_head_x, double _ref_head_z, double _ref_head_sh_dist) {

        if(counter!=0)Log.d(TAG, "getPoseValue: "+counter);
        double[] a = {0,0,0};
        if(mode.equals("auto")){
            // TODO: 2023/5/4 往前太多會變mode
            if(!auto_mode.equals((Math.abs(landmarks.getLandmark(11).getX()-landmarks.getLandmark(12).getX()))<0.25? "side":"front")){
                auto_mode  = (Math.abs(landmarks.getLandmark(11).getX()-landmarks.getLandmark(12).getX()))<0.25? "side":"front";
                Log.d(TAG, "Auto Mode Changed " +auto_mode);
            }

        }
        if(mode == "side"||auto_mode=="side"){
            // TODO: 2023/5/4  另外一肩好像看不太清楚
            double body_y = (landmarks.getLandmark(11).getY()+landmarks.getLandmark(12).getY())/2;
            double head_x = (landmarks.getLandmark(0).getX());
            double head_y = (landmarks.getLandmark(0).getY());
            isLeft = landmarks.getLandmark(11).getZ()>landmarks.getLandmark(12).getZ();
//            Log.d(TAG, "isLeft"+isLeft);
            double body_x = isLeft? landmarks.getLandmark(12).getX():landmarks.getLandmark(11).getX();
            double foreshortening = body_x-head_x;
            double shoulder_shrug = (head_y - body_y) - _ref_head_sh_dist;
            double head_dist =  head_x-_ref_head_x;

            a[0] = foreshortening;
            a[1] = shoulder_shrug;
            a[2] = head_dist;

            // reset
            if(reset){
                counter++;
                if(counter >= 20) {
                    reset = false;
                    counter = 0;
                }
                ref_head_x += head_x*0.05;
                ref_head_sh_dist += (head_y - body_y)*0.05;
            }

        } else {
            double body_z = (landmarks.getLandmark(11).getZ()+landmarks.getLandmark(12).getZ())/2;
            double body_y = (landmarks.getLandmark(11).getY()+landmarks.getLandmark(12).getY())/2;
            double head_z = (landmarks.getLandmark(0).getZ());
            double head_y = (landmarks.getLandmark(0).getY());
            double foreshortening = body_z-head_z;
            double shoulder_shrug = (head_y - body_y) - ref_head_sh_dist;
            double head_dist =  head_z-_ref_head_z;
            dist_correction = 0.5*(landmarks.getLandmark(11).getZ()+landmarks.getLandmark(12).getZ());
            a[0] = foreshortening;
            a[1] = shoulder_shrug;
            a[2] = head_dist;
            if(reset){
                counter++;
                if(counter >= 20) {
                    reset = false;
                    counter = 0;
                }
                    
                ref_head_z += head_z*0.05;
                ref_head_sh_dist += (head_y - body_y)*0.05;
                ref_fore += foreshortening*0.05;


            }
        }
        filter(a);
        //return a;
    }

    /**
     * return state
     */
    public static void update_current_state(MyDataStore dataStore){
        Log.d(TAG, "get_current_state: foreshortening:"+movingAvg[0]+" shoulder_shrug: "+movingAvg[1]+" head_dist"+movingAvg[2]);
        currStat = 0;

        for(int i = 0; i < poseOffset.length; ++i) poseOffset[i] = 0;

        // TODO: 2023/5/1  Mode 要手按很麻煩 感覺可以自動 
        if(mode.equals("side") || auto_mode.equals("side")){
            if(movingAvg[1]>=0.02){
                Log.d(TAG, "get_current_state: shoulder_shrug");
                poseOffset[1] = movingAvg[1] - 0.02;
                currStat = 2;
            }
            if(isLeft){
                if(movingAvg[0]<=-0.25){
                    Log.d(TAG, "Side: foreshortening");
                    poseOffset[0] = movingAvg[0] - (-0.25);
                    currStat = 1;
                    // TODO: 2023/5/1  會受到距離影響
                }
                if(movingAvg[2]>=0.3){
                    Log.d(TAG, "get_current_state: head_dist");
                    poseOffset[2] = movingAvg[2] - 0.3;
                    currStat = 3;
                }
            } else{
                if(movingAvg[0]>=0.25){
                    Log.d(TAG, "Side: foreshortening");
                    poseOffset[0] = movingAvg[0] - 0.25;
                    currStat = 1;
                }
                if(movingAvg[2]<=-0.15){
                    Log.d(TAG, "get_current_state: head_dist");
                    poseOffset[2] = movingAvg[2] - (-0.15);
                    currStat = 3;
                }
            }

        } else {
            if(movingAvg[1]>=0.02&&movingAvg[2]<0.4){
                Log.d(TAG, "get_current_state: shoulder_shrug");
                poseOffset[1] = movingAvg[1] - 0.02;
                currStat = 2;
            }
            double threshold =0.1*dist_correction/-0.6;
//            Log.d(TAG, "update_current_state: threshold"+(threshold));
            if(movingAvg[0]-ref_fore>=threshold){
                Log.d(TAG, "Front: foreshortening");
                poseOffset[0] = (movingAvg[0] - ref_fore) - threshold;
                currStat = 1;
            }
            if(movingAvg[2]<-0.3){
                Log.d(TAG, "get_current_state: head_dist");
                poseOffset[2] = movingAvg[2] - (-0.3);
                currStat = 3;
            }
        }
        Log.d(TAG, "update_current_state: EMU State" +currStat);
        dataStore.updateEmuState(currStat);
    }
    private static void filter(double[] _a){
        for(int i =0;i<3;i++)movingAvg[i] = 0.8*movingAvg[i]+0.2*_a[i];
    }
    public void onResume() {
        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(context)) {
            startCamera();
        }
    }

    public void onPause() {
        converter.close();
        // Hide preview display until we re-open the camera again.
        previewDisplayView.setVisibility(View.GONE);
    }

    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    protected void onCameraStarted(SurfaceTexture surfaceTexture) {
        previewFrameTexture = surfaceTexture;
        previewDisplayView.setVisibility(View.VISIBLE);
    }
    protected Size cameraTargetResolution() {
        return null;
    }
    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                this::onCameraStarted);
        cameraHelper.startCamera(
                context, CAMERA_FACING,  null, cameraTargetResolution());
    }
    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(
            int width, int height) {
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();
        previewFrameTexture.releaseTexImage();
        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                Log.d(TAG, "surfaceCreated: ");
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }
}