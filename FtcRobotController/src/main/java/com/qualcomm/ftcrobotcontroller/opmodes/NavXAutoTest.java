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

import android.util.Log;

import com.kauailabs.navx.ftc.AHRS;
import com.kauailabs.navx.ftc.navXPIDController;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.Timers;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.text.DecimalFormat;

/*
 * An example linear op mode where the robot will drive in
 * a straight line (where the driving direction is guided by
 * the Yaw angle from a navX-Model device).
 *
 * This example uses a simple PID controller configuration
 * with a P coefficient, and will likely need tuning in order
 * to achieve optimal performance.
 *
 * Note that for the best accuracy, a reasonably high update rate
 * for the navX-Model sensor should be used.  This example uses
 * the default update rate (50Hz), which may be lowered in order
 * to reduce the frequency of the updates to the drive system.
 */

public class NavXAutoTest extends LinearOpMode {
    DcMotor frontLeft, frontRight, backLeft, backRight,intake;

    /* This is the port on the Core Device Interface Module        */
    /* in which the navX-Model Device is connected.  Modify this  */
    /* depending upon which I2C port you are using.               */
    private final int NAVX_DIM_I2C_PORT = 1;
    private AHRS navx_device;
    private ElapsedTime runtime = new ElapsedTime();

    private final byte NAVX_DEVICE_UPDATE_RATE_HZ = 50;

    private final double TARGET_ANGLE_DEGREES = 0.0;
    private final double TOLERANCE_DEGREES = 2.0;
    private final double MIN_MOTOR_OUTPUT_VALUE = -.5;
    private final double MAX_MOTOR_OUTPUT_VALUE = .5;
    private final double YAW_PID_P = .1;
    private final double YAW_PID_I = 0;
    private final double YAW_PID_D = 12;
    int DEVICE_TIMEOUT_MS = 500;
    Timers mTimers = new Timers();

    public double limit(double a) {
        return Math.min(Math.max(a, MIN_MOTOR_OUTPUT_VALUE), MAX_MOTOR_OUTPUT_VALUE);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        navx_device = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("dim"),
                NAVX_DIM_I2C_PORT,
                AHRS.DeviceDataType.kProcessedData,
                NAVX_DEVICE_UPDATE_RATE_HZ);

        waitForStart();
        driveForEncoderCounts(4500, .5);
        turnToDeg(-20,.3);
        blockForMs(500);
        driveForEncoderCounts(3900, .5);
        //turnToDeg(90);
        //blockForMs(500);
        //driveForEncoderCounts(500, .5);
        navx_device.close();
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);
    }
    private void blockForMs(int ms) throws InterruptedException {
        mTimers.startClock("delay");
        while (mTimers.getClockValue("delay")< ms){
            waitOneFullHardwareCycle();
        }
        waitOneFullHardwareCycle();
    }
    private void driveForEncoderCounts(int encoderCounts,double drive_speed) throws InterruptedException {

        Log.d("navx","started drive for " +encoderCounts);
        /* Create a PID Controller which uses the Yaw Angle as input. */
        while (backLeft.getCurrentPosition() != 0)
            backLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        navXPIDController yawPIDController = new navXPIDController( navx_device,
                navXPIDController.navXTimestampedDataSource.YAW);

        /* Configure the PID controller */
        yawPIDController.setSetpoint(TARGET_ANGLE_DEGREES);
        yawPIDController.setContinuous(true);
        yawPIDController.setOutputRange(MIN_MOTOR_OUTPUT_VALUE, MAX_MOTOR_OUTPUT_VALUE);
        yawPIDController.setTolerance(navXPIDController.ToleranceType.ABSOLUTE, TOLERANCE_DEGREES);
        yawPIDController.setPID(YAW_PID_P, YAW_PID_I, YAW_PID_D);
        yawPIDController.enable(true);
        navXPIDController.PIDResult yawPIDResult = new navXPIDController.PIDResult();
        DecimalFormat df = new DecimalFormat("#.##");

        try {
            while ( backLeft.getCurrentPosition() < encoderCounts &&
                    !Thread.currentThread().isInterrupted()){
                telemetry.addData("Encoder",backLeft.getCurrentPosition());
                if (yawPIDController.waitForNewUpdate(yawPIDResult, DEVICE_TIMEOUT_MS)) {
                    if (yawPIDResult.isOnTarget()) {
                        frontLeft.setPower(drive_speed);
                        frontRight.setPower(drive_speed);
                        backLeft.setPower(drive_speed);
                        backRight.setPower(drive_speed);
                        telemetry.addData("PIDOutput", df.format(drive_speed) + ", " +
                                df.format(drive_speed));
                    } else {
                        double output = yawPIDResult.getOutput();
                        backLeft.setPower(MathUtil.coerce(-1, 1, drive_speed - output));
                        frontLeft.setPower(MathUtil.coerce(-1, 1, drive_speed - output));
                        backRight.setPower(MathUtil.coerce(-1, 1, drive_speed + output));
                        frontRight.setPower(MathUtil.coerce(-1, 1, drive_speed + output));
                        telemetry.addData("PIDOutput", df.format(limit(drive_speed + output)) + ", " +
                                df.format(limit(drive_speed - output)));
                    }
                    telemetry.addData("Yaw", df.format(navx_device.getYaw()));
                } else{
			        /* A timeout occurred */
                    Log.w("navXDriveStraightOp", "Yaw PID waitForNewUpdate() TIMEOUT.");
                }
            }
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        yawPIDController.close();
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);
        Log.d("navx", "ended drive for " + encoderCounts);
    }
    private void turnToDeg(int deg,double power) throws InterruptedException {
        blockForMs(500);
        Log.d("navx", "started turn for " + deg);
        while ( Math.abs(navx_device.getYaw() - deg) > TOLERANCE_DEGREES){
            Log.d("navx", "current yaw " + navx_device.getYaw());
            if (deg > 0){
                frontLeft.setPower(-power);
                backLeft.setPower(-power);
                frontRight.setPower(power);
                backRight.setPower(power);
            }
            else{
                frontLeft.setPower(power);
                backLeft.setPower(power);
                frontRight.setPower(-power);
                backRight.setPower(-power);
            }
        }
        Log.d("navx", "ended turn for " + deg);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);
        blockForMs(500);
    }
//    private void turnToDeg(int deg) throws InterruptedException {
//        Log.d("navx","started turn for " +deg);
//        navx_device.zeroYaw();
//        /* Create a PID Controller which uses the Yaw Angle as input. */
//        navXPIDController yawPIDController = new navXPIDController( navx_device,
//                navXPIDController.navXTimestampedDataSource.YAW);
//
//        /* Configure the PID controller */
//        yawPIDController.setSetpoint(deg);
//        yawPIDController.setContinuous(false);
//        yawPIDController.setOutputRange(MIN_MOTOR_OUTPUT_VALUE, MAX_MOTOR_OUTPUT_VALUE);
//        yawPIDController.setTolerance(navXPIDController.ToleranceType.ABSOLUTE, TOLERANCE_DEGREES);
//        yawPIDController.setPID(YAW_PID_P, YAW_PID_I, YAW_PID_D);
//        yawPIDController.enable(true);
//        navXPIDController.PIDResult yawPIDResult = new navXPIDController.PIDResult();
//        DecimalFormat df = new DecimalFormat("#.##");
//
//        while ( Math.abs(navx_device.getYaw() - deg) > TOLERANCE_DEGREES && !Thread.currentThread().isInterrupted()) {
//            Log.d("navx", "current yaw " + navx_device.getYaw());
//            if (yawPIDController.waitForNewUpdate(yawPIDResult, DEVICE_TIMEOUT_MS)) {
//                if (yawPIDResult.isOnTarget()) {
//                    frontLeft.setPowerFloat();
//                    frontRight.setPowerFloat();
//                    backLeft.setPowerFloat();
//                    backRight.setPowerFloat();
//                    telemetry.addData("Motor Power", df.format(0.00));
//                    telemetry.addData("turning",navx_device.getYaw());
//                } else {
//                    double output = yawPIDResult.getOutput();
//                    Tank.motor4(frontLeft, frontRight, backLeft, backRight,
//                            MathUtil.coerce(-1, 1, output + .1), MathUtil.coerce(-1, 1, -output - .1));
//                    telemetry.addData("Motor Power", df.format(output));
//                }
//            } else {
//			    /* A timeout occurred */
//                Log.w("navXRotateOp", "Yaw PID waitForNewUpdate() TIMEOUT.");
//            }
//            telemetry.addData("Yaw", df.format(navx_device.getYaw()));
//        }
//        yawPIDController.close();
//        Log.d("navx", "ended turn for " + deg);
//    }
}
