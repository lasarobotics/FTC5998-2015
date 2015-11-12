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
    Servo liftServo;
    Controller firstController;
    Controller secondController;

    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        gamepad2.setJoystickDeadzone(.1F);
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        lift = hardwareMap.dcMotor.get("lift");
        liftServo = hardwareMap.servo.get("liftServo");
        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);

        liftServo.setPosition(0.5);
    }

    public void loop() {
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        Tank.motor4(frontLeft, frontRight, backLeft, backRight, -firstController.left_stick_y, firstController.right_stick_y);

        if (firstController.dpad_up == ButtonState.PRESSED) {
            intake.setPower(1);
        }
        else if (firstController.dpad_down == ButtonState.PRESSED) {
            intake.setPower(-1);
        }
        else if (firstController.left_bumper == ButtonState.PRESSED){
            intake.setPower(0);
        }

        if (firstController.x == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() + 0.05));
        }
        else if (firstController.b == 1) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() - 0.05));
        }

        if (firstController.y == ButtonState.PRESSED) {
            lift.setPower(.25);
        }
        else if (firstController.a == ButtonState.PRESSED) {
            lift.setPower(-.25);
        }
        if (firstController.right_bumper == ButtonState.PRESSED){
            lift.setPower(0);
        }
                
    }

    public void stop() {
        intake.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
