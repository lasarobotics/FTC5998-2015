package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.Timers;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import java.util.concurrent.TimeUnit;

public class Teleop extends OpMode {

    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor intake, lift;
    Servo slide, dump,carabiner,climber;
    Controller firstController;
    Controller secondController;
    DcMotor goalOne, goalTwo;
    private enum DriftStatus {
        STOPPED,
        MOVING,
        DRIFTING
    }
    DriftStatus drift;
    Timers mainTimer;
    public static final double controllerThreshold = 0.2;
    double lastLeftSpeed, lastRightSpeed;


    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        gamepad2.setJoystickDeadzone(.1F);
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        goalOne = hardwareMap.dcMotor.get("goalOne");
        goalTwo = hardwareMap.dcMotor.get("goalTwo");
        lift = hardwareMap.dcMotor.get("lift");
        slide = hardwareMap.servo.get("slide");
        dump = hardwareMap.servo.get("dump");
        carabiner = hardwareMap.servo.get("carabiner");
        climber = hardwareMap.servo.get("climber");
        mainTimer = new Timers();
        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);
        slide.setPosition(.5);
        dump.setPosition(.5);
        climber.setPosition(1);
        carabiner.setPosition(.85);
        drift = DriftStatus.STOPPED;
        lift.setDirection(DcMotor.Direction.REVERSE);
        mainTimer.startClock("matchTimer");
    }

    public void loop() {
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        if ((firstController.left_stick_y > controllerThreshold) || (firstController.right_stick_y > controllerThreshold)) {
            drift = DriftStatus.MOVING;
        } else if (((firstController.left_stick_y <= controllerThreshold) && (firstController.right_stick_y <= controllerThreshold)) && (drift == DriftStatus.MOVING)) {
            drift = DriftStatus.DRIFTING;
            mainTimer.startClock("driftTimer");
        } else if ((drift == DriftStatus.DRIFTING) && (mainTimer.getClockValue("driftTimer", TimeUnit.SECONDS) > 1)) {
            drift = DriftStatus.STOPPED;
            mainTimer.resetClock("driftTimer");
        }

        if (drift == DriftStatus.MOVING) {
            Tank.motor4(frontLeft, frontRight, backLeft, backRight, -firstController.left_stick_y,
                    firstController.right_stick_y);
            lastLeftSpeed = firstController.left_stick_y;
            lastRightSpeed = firstController.right_stick_y;

        } else if (drift == DriftStatus.DRIFTING) {
            Tank.motor4(frontLeft, frontRight, backLeft, backRight, -reduceSpeedLinear(lastLeftSpeed),
                    reduceSpeedLinear(lastRightSpeed));
            lastLeftSpeed = reduceSpeedLinear(lastLeftSpeed);
            lastRightSpeed = reduceSpeedLinear(lastRightSpeed);

        } else if (drift == DriftStatus.STOPPED) {
            frontLeft.setPower(0);
            frontRight.setPower(0);
            backLeft.setPower(0);
            backRight.setPower(0);
        }


        //Hanging
        if (firstController.right_bumper == ButtonState.PRESSED) {
            goalOne.setPower(1);
        } else if (firstController.right_trigger > controllerThreshold) {
            goalOne.setPower(-1);
        } else {
            goalOne.setPower(0);
        }

        if (firstController.left_bumper == ButtonState.PRESSED) {
            goalTwo.setPower(1);
        } else if (firstController.left_trigger > controllerThreshold) {
            goalTwo.setPower(-1);
        } else {
            goalTwo.setPower(0);
        }

        if (firstController.a == ButtonState.HELD){
            carabiner.setPosition(MathUtil.coerce(0, 1, carabiner.getPosition() + .005));
        }
        else if (firstController.y == ButtonState.HELD){
            carabiner.setPosition(MathUtil.coerce(0, 1, carabiner.getPosition() - .005));
        }

        //Intake
        if (secondController.y == ButtonState.PRESSED) {
            intake.setPower(1);
        } else if (secondController.a == ButtonState.PRESSED) {
            intake.setPower(-1);
        } else if (secondController.b == ButtonState.PRESSED) {
            intake.setPower(0);
        }

        //Lift
        if (secondController.dpad_up == ButtonState.HELD){
            lift.setPower(.3);
        }
        else if (secondController.dpad_down == ButtonState.HELD){
            lift.setPower(-.3);
        }
        else {
            lift.setPower(0);
        }

        if (secondController.right_stick_x > controllerThreshold) {
            slide.setPosition(MathUtil.coerce(0, 1, slide.getPosition() + .02));
        }
        else if (secondController.right_stick_x < -controllerThreshold){
            slide.setPosition(MathUtil.coerce(0, 1, slide.getPosition() - .02));
        }
        else if (secondController.right_stick_y > controllerThreshold){
            slide.setPosition(.5);
        }

        if (secondController.left_stick_x > controllerThreshold) {
            dump.setPosition(MathUtil.coerce(0, 1, dump.getPosition() + .005));
        }
        else if (secondController.left_stick_x < -controllerThreshold){
            dump.setPosition(MathUtil.coerce(0, 1, dump.getPosition() - .005));
        }
        else if (secondController.left_stick_y > controllerThreshold){
            dump.setPosition(.5);
        }
        //Climbers
        if (secondController.start == ButtonState.PRESSED){
            climber.setPosition(0);
        }
        else if (secondController.back == ButtonState.PRESSED){
            climber.setPosition(1);
        }

        if (mainTimer.getClockValue("matchTimer", TimeUnit.SECONDS) >= 119.5) // auto stop at end
        {
            stop();
        }
    }

    public double reduceSpeedLinear(double speed) {
        if (speed > 0) {
            return (speed - 0.1);
        } else if (speed < 0) {
            return (speed + 0.1);
        } else {
            return speed;
        }
    }

    public void stop() {
        super.stop();
        intake.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        goalOne.setPower(0);
        goalTwo.setPower(0);
        lift.setPower(0);
    }
}
