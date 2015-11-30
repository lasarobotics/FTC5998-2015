package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.monkeyc.MonkeyC;
import com.lasarobotics.library.util.MathUtil;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class MonkeyCWrite extends OpMode {
    //basic FTC classes
    DcMotor frontLeft, frontRight, backLeft, backRight;
    DcMotor intake, lift;
    Servo liftServo;
    Controller firstController;
    Controller secondController;
    Servo hanger;

    MonkeyC writer;

    @Override
    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        gamepad1.setJoystickDeadzone(.1F);
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
    }
    @Override
    public void start(){
        writer = new MonkeyC();
    }
    @Override
    public void loop() {
        //update gamepads to controllers with events
        firstController.update(gamepad1);
        secondController.update(gamepad2);
        writer.add(firstController, secondController);

        telemetry.addData("Status", writer.getCommandsWritten() + " commands written");
        telemetry.addData("Time", writer.getTime() + " seconds");

        //Drive commands go here (must match when playing back)
        Tank.motor4(frontLeft, frontRight, backLeft, backRight, -firstController.left_stick_y,
                firstController.right_stick_y);

        if (secondController.dpad_up == ButtonState.PRESSED) {
            intake.setPower(1);
        } else if (secondController.dpad_down == ButtonState.PRESSED) {
            intake.setPower(-1);
        } else if (secondController.left_bumper == ButtonState.PRESSED) {
            intake.setPower(0);
        }

        if (secondController.x == ButtonState.PRESSED) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() + 0.05));
        } else if (secondController.b == ButtonState.PRESSED) {
            liftServo.setPosition(MathUtil.coerce(0.0, 1.0, liftServo.getPosition() - 0.05));
        }

        if (secondController.right_bumper == ButtonState.HELD){
            lift.setPower(.2);
        }
        else if (secondController.right_trigger == 1.0){
            lift.setPower(-.2);
        }
        else{
            lift.setPower(0);
        }

        if (secondController.a == ButtonState.PRESSED){
            hanger.setPosition(1);
        }
        else if (secondController.y == ButtonState.PRESSED){
            hanger.setPosition(0);
        }
    }

    @Override
    public void stop() {
        intake.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        lift.setPower(0);
        writer.write("test.txt", true);
    }
}
