package com.qualcomm.ftcrobotcontroller.opmodes;

import com.lasarobotics.library.controller.ButtonState;
import com.lasarobotics.library.controller.Controller;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.monkeyc.MonkeyC;
import com.lasarobotics.library.util.MathUtil;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class MonkeyCWriteTest extends OpMode {
    //basic FTC classes
    Controller firstController;
    Controller secondController;
    Controller one,two;

    MonkeyC writer;

    @Override
    public void init() {
        gamepad1.setJoystickDeadzone(.1F);
        gamepad1.setJoystickDeadzone(.1F);
        firstController = new Controller(gamepad1);
        secondController = new Controller(gamepad2);
        one = new Controller(gamepad1);
        two = new Controller(gamepad2);
    }
    @Override
    public void start(){
        writer = new MonkeyC();
    }
    @Override
    public void loop() {
        //update gamepads to controllers with events
        one.update(gamepad1);
        two.update(gamepad2);
        writer.add(one, two);

        telemetry.addData("Status", writer.getCommandsWritten() + " commands written");
        telemetry.addData("Time", writer.getTime() + " seconds");
    }

    @Override
    public void stop() {
        writer.write("test.txt", true);
    }
}
