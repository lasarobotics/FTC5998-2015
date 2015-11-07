package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * Created by ojas on 11/7/15.
 */
import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.util.MathUtil;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;



public class LiftAndIntake extends OpMode {

    DcMotor lift, intake;
    Controller firstController;

    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        intake = hardwareMap.dcMotor.get("intake");
        lift = hardwareMap.dcMotor.get("lift");

        firstController = new Controller(gamepad1);
    }

    public void loop() {
        firstController.update(gamepad1);
        if (firstController.dpad_up == ButtonState.PRESSED) {
            intake.setPower(1);
        }
        if (firstController.dpad_down == ButtonState.PRESSED) {
            intake.setPower(-1);
        }
        if(firstController.left_bumper == ButtonState.PRESSED){
            intake.setPower(0);
        }
        if (firstController.y == ButtonState.PRESSED) {
            lift.setPower(.4);
        }
        else if (firstController.a == ButtonState.PRESSED) {
            lift.setPower(-.4);
        }
        if (firstController.right_bumper == ButtonState.PRESSED){
            lift.setPower(0);
        }

    }

    public void stop() {
        intake.setPower(0);
        lift.setPower(0);
    }
}
