package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class FourWheelDrive extends OpMode {

    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor intake;
    Servo lift;
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
        lift = hardwareMap.servo.get("lift");

        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);
    }

    public void loop() {
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        Tank.motor4(frontLeft, frontRight, backLeft, backRight, -firstController.left_stick_y, firstController.right_stick_y);

        if (firstController.dpad_up == 1) {
            intake.setPower(1);
        }
        else if (firstController.dpad_down == 1) {
            intake.setPower(-1);
        }
        else if (firstController.dpad_right == 1) {
            intake.setPower(0);
        }
        else if (firstController.dpad_left == 1) {
            intake.setPower(0);
        }

        if (firstController.y == 1) {
            lift.setPosition(lift.getPosition() + 5);
        }
        else if (firstController.a == 1) {
            lift.setPosition(lift.getPosition() - 5);
        }
                
    }

    public void stop() {
        
    }
}
