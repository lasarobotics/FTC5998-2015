package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.util.MathUtil;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class FourWheelDrive extends OpMode {

    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor intake, lift;
    Servo liftServo, hanger;
    Controller firstController;
    Controller secondController;

    public void init() {
        gamepad1.setJoystickDeadzone(.1F); // make sure we don't get fake values
        gamepad2.setJoystickDeadzone(.1F);
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        lift = hardwareMap.dcMotor.get("lift");
        liftServo = hardwareMap.servo.get("liftServo");
        hanger = hardwareMap.servo.get("hanger");
        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);
        liftServo.setPosition(0.5); // set the servo halfway in between 0 and 1, so there can be
        // 10 increments on either side
    }

    public void loop() {
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        Tank.motor4(frontLeft, frontRight, backLeft, backRight, -firstController.left_stick_y,
                firstController.right_stick_y);

        if (firstController.x == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() + 0.20));
        } else if (firstController.b == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() - 0.20));
        }

        if (secondController.dpad_up == ButtonState.PRESSED) {
            intake.setPower(1);
        } else if (secondController.dpad_down == ButtonState.PRESSED) {
            intake.setPower(-1);
        } else if (secondController.left_bumper == ButtonState.PRESSED) {
            intake.setPower(0);
        }

        if (secondController.x == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() + 0.05));
        } else if (secondController.b == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() - 0.05));
        }

        if (secondController.right_bumper == ButtonState.HELD) {
            lift.setPower(1);
        } else if (secondController.right_trigger == ButtonState.HELD) {
            lift.setPower(-1);
        } else {
            lift.setPower(0);
        }
        if (secondController.y == ButtonState.PRESSED){
            hanger.setPosition(1.0);
        }
        else if (secondController.a == ButtonState.PRESSED){
            hanger.setPosition(0.0);
        }

    }

    public void stop() { // make sure nothing moves after the end of the match
        intake.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        lift.setPower(0);
    }
}
