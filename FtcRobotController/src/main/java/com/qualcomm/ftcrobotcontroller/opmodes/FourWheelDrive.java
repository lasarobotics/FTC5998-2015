package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class FourWheelDrive extends OpMode {

    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor lift, intake;
    Controller firstController;

    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        gamepad2.setJoystickDeadzone(.1F);
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        lift = hardwareMap.dcMotor.get("lift");
        intake = hardwareMap.dcMotor.get("intake");

        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);
    }

    public void loop() {
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        Tank.motor4(frontLeft, frontRight, backLeft, backRight, firstController.left_stick_y, firstController.right_stick_y);
        if (secondController.a == 1) {
            intake.setPower(1);
        }
        else if (secondController.x == 1) {
            intake.setPower(-1);
        }
        if (secondController.b == 1) {
            lift.setPower(1);
        }
        else if (secondController.y == 1) {
            lift.setPower(-1);
        }
        
    }

    public void stop() {
        
    }
}
