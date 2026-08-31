/**
 * This is the source code of Cherrygram Next for Android.
 * It is a fork of Cherrygram, licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 * Copyright github.com/306bobby-android, 2026.
 */

package com.the306bobby.cherrygramnext.camera;

import android.graphics.SurfaceTexture;
import android.util.Range;

import org.telegram.ui.Components.InstantCameraView;

public class VideoMessagesHelper {

    public CameraXController cameraXController;

    public void createCameraX(InstantCameraView instantCameraView, final SurfaceTexture... surfaceTextures) {

    }

    public void switchCameraX(InstantCameraView instantCameraView) {

    }

    public void destroyCameraX(InstantCameraView instantCameraView) {

    }

    public void setZoom(float zoom) {

    }

    public boolean createFlashConfigurator(InstantCameraView instantCameraView) {
        return false;
    }

    public void checkFlash(InstantCameraView instantCameraView) {

    }

    public void updateCameraXFlash(InstantCameraView instantCameraView) {

    }

    public void showExposureControls(InstantCameraView instantCameraView, boolean show) {

    }

    public int getSliderW() {
        return 0;
    }

    public int getSliderH() {
        return 0;
    }

    public int getSliderBM() {
        return 0;
    }

    public static Range<Integer> getCameraXFpsRange() {
        return new Range<>(30, 30);
    }

}