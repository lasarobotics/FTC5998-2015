package com.qualcomm.ftcrobotcontroller.opmodes;

import android.util.Log;

import com.kauailabs.navx.ftc.AHRS;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.Timers;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.LinearVisionOpMode;
import org.opencv.core.Size;

/**
 * Vision-enabled opmode
 */
public class Auto extends LinearVisionOpMode {
    private static final int TOLERANCE_DEGREES = 5;
    private final int NAVX_DIM_I2C_PORT = 1;
    private final byte NAVX_DEVICE_UPDATE_RATE_HZ = 50;
    private DcMotor frontLeft, frontRight, backLeft, backRight,intake;
    private GyroSensor gyro;
    private Servo slide, dump,carabiner,climber;
    private AHRS navx_device;

    private void visionSetup() throws InterruptedException {
        //Wait for vision to initialize - this should be the first thing you do
        waitForVisionStart();

        //Set the camera used for detection
        this.setCamera(Cameras.SECONDARY);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //UNCOMMENT THIS IF you're using a SECONDARY (facing toward screen) camera
        //or when you rotate the phone, sometimes the colors swap
        rotation.setRotationInversion(true);

        //You can do this for certain phones which switch red and blue
        //It will rotate the display and detection by 180 degrees, making it upright
        //rotation.setUnbiasedOrientation(ScreenOrientation.LANDSCAPE_WEST);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);
    }

    private void setup() {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        slide = hardwareMap.servo.get("slide");
        dump = hardwareMap.servo.get("dump");
        carabiner = hardwareMap.servo.get("carabiner");
        climber = hardwareMap.servo.get("climber");

        frontLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        slide.setPosition(.5);
        dump.setPosition(.5);
        climber.setPosition(1);
        carabiner.setPosition(.85);
        navx_device = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("dim"),
                NAVX_DIM_I2C_PORT,
                AHRS.DeviceDataType.kProcessedData,
                NAVX_DEVICE_UPDATE_RATE_HZ);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        visionSetup();
        setup();
        waitForStart();

        //Run
        //intake.setPower(1);
        final double power = 1;

        turnToDegNavX(45, -power);
        block(500);
        runForEncoderCounts(5000, power);
        block(500);
        turnToDegNavX(320, power);
        block(1000);
        runForEncoderCounts(2500, power);
        block(500);
        turnToDegNavX(270, .5);
        block(500);
        runForEncoderCounts(700, -power);

        int goodCount = 0;
        String lastString = null;
        Beacon.BeaconColor left;
        Beacon.BeaconColor right;

        //Goal is to get X good frames in a row
        while (goodCount < 10) {
            String colorString = beacon.getAnalysis().getColorString();

            telemetry.addData("Good count", goodCount);
            telemetry.addData("Color", colorString);

            if (!hasNewFrame()) continue;
            goodCount++;
            discardFrame();

            if (!beacon.getAnalysis().isBeaconFound()) {
                goodCount = 0;
                lastString = null;
                continue;
            }
            left = beacon.getAnalysis().getStateLeft();
            right = beacon.getAnalysis().getStateRight();

            if (lastString != null && !colorString.equals(lastString)) {
                goodCount = 0;
                lastString = null;
                continue;
            }
            lastString = colorString;
        }

        Log.w("BEACON STATE", beacon.getAnalysis().getColorString());

        //Dump
        //intake.setPower(0);
        climber.setPosition(0);
        waitOneFullHardwareCycle();

        //Shutdown
        climber.setPosition(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        waitOneFullHardwareCycle();
        telemetry.addData("status", "done");
    }

    private void block(int ms) throws InterruptedException {
        Timers mTimers = new Timers();
        mTimers.startClock("delay");
        while (mTimers.getClockValue("delay")< ms){
            waitOneFullHardwareCycle();
        }
        waitOneFullHardwareCycle();
    }

    private void runForEncoderCounts(int counts, double power) throws InterruptedException {
        backRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        block(100);
        waitOneFullHardwareCycle();
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);
        waitOneFullHardwareCycle();
        while ( Math.abs(backRight.getCurrentPosition()) < counts){
            waitOneFullHardwareCycle();
            Log.d("encoder", backLeft.getCurrentPosition() + "bl");
            Log.d("encoder",backRight.getCurrentPosition() + "br");
            Log.d("encoder",frontLeft.getCurrentPosition() + "fl");
            Log.d("encoder",frontRight.getCurrentPosition() + "fr");

        }

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
    public void turnToDeg(int deg, double power) throws InterruptedException {
        frontLeft.setPower(power);
        frontRight.setPower(-power);
        backLeft.setPower(power);
        backRight.setPower(-power);
        waitOneFullHardwareCycle();
        while (!MathUtil.inBounds(deg- TOLERANCE_DEGREES,deg+ TOLERANCE_DEGREES,gyro.getHeading())){
            Log.d("gyro",gyro.getHeading() + " ");
            waitOneFullHardwareCycle();
        }


        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
    public void turnToDegNavX(int deg, double power) throws InterruptedException{
        navx_device.zeroYaw();
        frontLeft.setPower(power);
        frontRight.setPower(-power);
        backLeft.setPower(power);
        backRight.setPower(-power);

        while (!MathUtil.inBounds(deg- TOLERANCE_DEGREES,deg+TOLERANCE_DEGREES, convertDegNavX(navx_device.getYaw()))){
            Log.d("gyro",navx_device.getYaw() + " ");
            telemetry.addData("gyro", navx_device.getYaw());
            waitOneFullHardwareCycle();
        }


        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
    public float convertDegNavX(float deg){
        if (deg < 0)
            deg = 360 - Math.abs(deg);
        return deg;
    }
    public int encoderAverage(){
        return (frontLeft.getCurrentPosition() +
                frontRight.getCurrentPosition() +
                backLeft.getCurrentPosition() +
                backRight.getCurrentPosition())/4;
    }

}
