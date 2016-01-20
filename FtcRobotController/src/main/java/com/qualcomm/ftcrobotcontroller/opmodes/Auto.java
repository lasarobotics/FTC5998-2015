package com.qualcomm.ftcrobotcontroller.opmodes;

import com.kauailabs.navx.ftc.AHRS;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.Timers;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;

import android.util.Log;

/**
 * Created by ehsan on 11/24/15.
 */
public class Auto extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight,intake;
    private GyroSensor gyro;
    private static final int TOLERANCE_DEGREES = 5;
    private Servo climber;
    private AHRS navx_device;
    private final int NAVX_DIM_I2C_PORT = 1;
    private final byte NAVX_DEVICE_UPDATE_RATE_HZ = 50;


    private void setup() {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        climber = hardwareMap.servo.get("climber");

        frontLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        climber.setPosition(1);

        navx_device = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("dim"),
                NAVX_DIM_I2C_PORT,
                AHRS.DeviceDataType.kProcessedData,
                NAVX_DEVICE_UPDATE_RATE_HZ);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        setup();
        waitForStart();

        //Run
        intake.setPower(1);
        runForEncoderCounts(5000, .5);
        block(1000);
        turnToDegNavX(290, .5);
        block(1000);

        //Dump
        intake.setPower(0);
        climber.setPosition(0);

        //Shutdown
        climber.setPosition(1);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
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

        while ( backRight.getCurrentPosition() < counts){
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
