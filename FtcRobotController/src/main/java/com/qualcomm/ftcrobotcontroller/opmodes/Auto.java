package com.qualcomm.ftcrobotcontroller.opmodes;

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

    private void setup() {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        climber = hardwareMap.servo.get("climber");

        frontLeft.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_WITHOUT_ENCODERS);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        climber.setPosition(1);
        //gyro.calibrate();
        //gyro.resetZAxisIntegrator();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        setup();
        waitForStart();
        Log.d("gyro", "turn complete");
        intake.setPower(1);
        runForEncoderCounts(5000, .5);
        intake.setPower(0);
        climber.setPosition(0);
        //turnToDeg(290, 1);
        block(1000);
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
        backLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        waitOneFullHardwareCycle();
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);

        while (backLeft.getCurrentPosition() < counts){
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

}
