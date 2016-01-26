/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.drive.Tank;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.opmode.VisionOpMode;
import org.opencv.core.Size;

/**
 * TeleOp Mode
 * <p/>
 * Enables control of the robot via the gamepad
 */
public class VisionTest extends VisionOpMode {
    public static final double correctionFactor = 0.05;
    DcMotor frontLeft, frontRight, backLeft, backRight;
    double leftSpeed, rightSpeed;
    DriveState currentState = DriveState.VISION_LOCATEBUTTON;

    @Override
    public void init() {
        super.init();
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        leftSpeed = 0.5; // set initial motor speeds
        rightSpeed = 0.5;


        //Set the camera used for detection
        this.setCamera(Cameras.PRIMARY);
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
    }

    @Override
    public void loop() {
        super.loop();

        switch (currentState) {
            case MOVE_FORWARD:

                break;
            case MOVE_LEFT:

                break;
            case MOVE_TOTARGET:

                break;
            case VISION_LOCATEBUTTON:
                Tank.motor4(frontLeft, frontRight, backLeft, backRight, -leftSpeed, rightSpeed);
                if (beacon.getAnalysis().getCenter().x > width / 2) { // *slowly* increment the compensation so that we don't mess everything up in the case of one bad frame analysis
                    leftSpeed += correctionFactor;
                    rightSpeed -= correctionFactor;
                } else if (beacon.getAnalysis().getCenter().x < width / 2) {
                    leftSpeed -= correctionFactor;
                    rightSpeed += correctionFactor;
                } else {
                    leftSpeed = 0.5;
                    rightSpeed = 0.5;
                }
                break;
            case PRESS_BUTTON:

                break;
            default:
                return;
        }

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

    public enum DriveState {
        MOVE_FORWARD,
        MOVE_LEFT,
        MOVE_TOTARGET,
        VISION_LOCATEBUTTON,
        PRESS_BUTTON
    }
}
