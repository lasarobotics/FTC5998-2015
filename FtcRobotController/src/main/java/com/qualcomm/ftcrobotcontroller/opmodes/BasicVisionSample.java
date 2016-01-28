package com.qualcomm.ftcrobotcontroller.opmodes;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Size;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class BasicVisionSample extends VisionOpMode {

    @Override
    public void init() {
        super.init();

        //Set the camera used for detection
        this.setCamera(Cameras.SECONDARY);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        //enableExtension(Extensions.QR);         //QR Code detection
        enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //You can do this for certain phones which switch red and blue
        //It will rotate the display and detection by 180 degrees, making it upright
        //rotation.setUnbiasedOrientation(ScreenOrientation.LANDSCAPE_WEST);

        //You can also do this when using the secondary camera
        //Sometimes it is necessary to ensure upright rotation
        rotation.setRotationInversion(true);
    }

    @Override
    public void loop() {
        super.loop();

        telemetry.addData("Beacon Color", beacon.getAnalysis().getColorString());
        telemetry.addData("Beacon Location (Center)", beacon.getAnalysis().getLocationString());
        telemetry.addData("Beacon Confidence", beacon.getAnalysis().getConfidenceString());
        //telemetry.addData("QR Error", qr.getErrorReason());
        //telemetry.addData("QR String", qr.getText());
        //telemetry.addData("Rotation Compensation", rotation.getRotationAngle());
        telemetry.addData("Frame Rate", fps.getFPSString() + " FPS");
        telemetry.addData("Frame Size", "Width: " + width + " Height: " + height);
    }

    @Override
    public void stop() {
        super.stop();
    }
}