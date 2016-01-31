package com.qualcomm.ftcrobotcontroller.opmodes;

import android.util.Log;

import com.kauailabs.navx.ftc.AHRS;
import com.kauailabs.navx.ftc.navXPIDController;
import com.lasarobotics.library.drive.Tank;
import com.lasarobotics.library.util.MathUtil;
import com.lasarobotics.library.util.RollingAverage;
import com.lasarobotics.library.util.Timers;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.lasarobotics.vision.ftc.resq.Beacon;

import java.text.DecimalFormat;

/**
 * Vision-enabled opmode
 */
public class AutoBlue extends LinearOpMode {
    private static final int TOLERANCE_DEGREES = 5;
    private final int NAVX_DIM_I2C_PORT = 1;
    private final byte NAVX_DEVICE_UPDATE_RATE_HZ = 50;
    DecimalFormat df = new DecimalFormat("#.##");
    private DcMotor frontLeft, frontRight, backLeft, backRight, intake;
    private GyroSensor gyro;
    private Servo slide, dump, carabiner, climber;
    private AHRS navx;

    public double limit(double a) {
        return MathUtil.deadband(0.02, MathUtil.coerce(-1, 1, a));
    }

    /*private void visionSetup() throws InterruptedException {
        //Wait for vision to initialize - this should be the first thing you do
        waitForVisionStart();

        //Set the camera used for detection
        this.setCamera(Cameras.SECONDARY);
        //Set the frame size
        //Larger = sometimes more accurate, but also much slower
        //For Testable OpModes, this might make the image appear small - it might be best not to use this
        this.setFrameSize(new Size(900, 900));

        //Enable extensions. Use what you need.
        enableExtension(Extensions.BEACON);     //Beacon detection
        enableExtension(Extensions.ROTATION);   //Automatic screen rotation correction

        //UNCOMMENT THIS IF you're using a SECONDARY (facing toward screen) camera
        //or when you rotate the phone, sometimes the colors swap
        rotation.setRotationInversion(true);

        //You can do this for certain phones which switch red and blue
        //It will rotate the display and detection by 180 degrees, making it upright
        //rotation.setUnbiasedOrientation(ScreenOrientation.LANDSCAPE_WEST);

        //Set the beacon analysis method
        //Try them all and see what works!
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);
    }*/

    private void setup() throws InterruptedException {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        intake = hardwareMap.dcMotor.get("intake");
        slide = hardwareMap.servo.get("slide");
        dump = hardwareMap.servo.get("dump");
        carabiner = hardwareMap.servo.get("carabiner");
        climber = hardwareMap.servo.get("climber");

        frontLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        block(200);

        frontLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        slide.setPosition(.5);
        dump.setPosition(.5);
        climber.setPosition(1);
        carabiner.setPosition(.85);
        navx = AHRS.getInstance(hardwareMap.deviceInterfaceModule.get("dim"),
                NAVX_DIM_I2C_PORT,
                AHRS.DeviceDataType.kProcessedData,
                NAVX_DEVICE_UPDATE_RATE_HZ);
        block(500);
        navx.zeroYaw();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        //visionSetup();
        setup();
        waitForStart();

        //Run
        //intake.setPower(1);
        final double powerForward = 1;
        final double powerRot = 0.5; //.789

        //NavX2 untested, not using - but power must be set to 1 for v2
        //runForEncoderCounts(250, powerForward);
        intake.setPower(1);
        block(250);
        turnToDegNavX(35, powerRot);
        block(500);
        runForEncoderCounts(5000, powerForward);
        block(500);
        turnToDegNavX(-35, -powerRot);
        block(1000);
        runForEncoderCounts(2200, powerForward);
        block(500);
        turnToDegNavX(-80, -powerRot);
        block(500);
        runForEncoderCounts(1000, -powerForward);

        int goodCount = 0;
        String lastString = null;
        Beacon.BeaconColor left;
        Beacon.BeaconColor right;

        //Goal is to get X good frames in a row
        /*(while (goodCount < 10) {
            String colorString = beacon.getAnalysis().getColorString();

            telemetry.addData("Good count", goodCount);
            telemetry.addData("Color", colorString);

            if (!hasNewFrame()) continue;
            goodCount++;
            discardFrame();

            if (!beacon.getAnalysis().isBeaconFound()) {
                goodCount = 0;
                lastString = null;
                continue;
            }
            left = beacon.getAnalysis().getStateLeft();
            right = beacon.getAnalysis().getStateRight();

            if (lastString != null && !colorString.equals(lastString)) {
                goodCount = 0;
                lastString = null;
                continue;
            }
            lastString = colorString;
        }

        Log.w("BEACON STATE", beacon.getAnalysis().getColorString());*/

        //Dump
        //intake.setPower(0);
        climber.setPosition(0);
        waitOneFullHardwareCycle();

        //Shutdown
        climber.setPosition(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        intake.setPower(0);
        waitOneFullHardwareCycle();
        telemetry.addData("status", "done");
    }

    private void block(int ms) throws InterruptedException {
        Timers mTimers = new Timers();
        mTimers.startClock("delay");
        while (mTimers.getClockValue("delay") < ms) {
            waitOneFullHardwareCycle();
        }
        waitOneFullHardwareCycle();
    }

    private void runForEncoderCounts(int counts, double power) throws InterruptedException {
        backRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        block(100);
        waitOneFullHardwareCycle();
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setPower(power);
        frontRight.setPower(power);
        backLeft.setPower(power);
        backRight.setPower(power);
        while (Math.abs(backRight.getCurrentPosition()) < counts) {
            waitOneFullHardwareCycle();
            Log.d("encoder", backLeft.getCurrentPosition() + "bl");
            Log.d("encoder", backRight.getCurrentPosition() + "br");
            Log.d("encoder", frontLeft.getCurrentPosition() + "fl");
            Log.d("encoder", frontRight.getCurrentPosition() + "fr");
        }
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }

    public void turnToDegEncoder(double counts, double power) throws InterruptedException {
        frontLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        block(100);
        waitOneFullHardwareCycle();
        frontLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        frontRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backLeft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        backRight.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        frontLeft.setPower(-power);
        frontRight.setPower(power);
        backLeft.setPower(-power);
        backRight.setPower(power);
        waitOneFullHardwareCycle();

        while (Math.abs(encoderAverage()) < counts) {
            waitOneFullHardwareCycle();
            Log.d("encoder", backLeft.getCurrentPosition() + "bl");
            Log.d("encoder", backRight.getCurrentPosition() + "br");
            Log.d("encoder", frontLeft.getCurrentPosition() + "fl");
            Log.d("encoder", frontRight.getCurrentPosition() + "fr");
        }

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }

    public void turnToDegNavX(int deg, double power) throws InterruptedException {
        navx.zeroYaw();
        block(500);
        frontLeft.setPower(-power);
        frontRight.setPower(power);
        backLeft.setPower(-power);
        backRight.setPower(power);
        waitOneFullHardwareCycle();

        float yaw = 0.0f;

        //while (!MathUtil.inBounds(deg - TOLERANCE_DEGREES, deg + TOLERANCE_DEGREES, convertDegNavX(navx.getYaw()))) {
        boolean arrived = false;
        do {
            waitOneFullHardwareCycle();
            yaw = navx.getYaw();
            Log.d("Yaw", yaw + "");
            telemetry.addData("Yaw", yaw + "");
            if (MathUtil.inBounds(deg - TOLERANCE_DEGREES, deg + TOLERANCE_DEGREES, yaw))
                arrived = true;
            if (power > 0 && yaw > deg - TOLERANCE_DEGREES) //clockwise
                arrived = true;
            if (power < 0 && yaw < deg + TOLERANCE_DEGREES) //counterclockwise
                arrived = true;
        } while (!arrived);
        //while((power > 0 && convertDegNavX(navx.getYaw()) > deg + TOLERANCE_DEGREES ) ||(power < 0 && convertDegNavX(navx.getYaw()) < deg-TOLERANCE_DEGREES)) {

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        waitOneFullHardwareCycle();
    }

    public float convertDegNavX(float deg) {
        if (deg < 0)
            deg = 360 - Math.abs(deg);
        return deg;
    }

    public void turnToDegNavX2(int deg, double power) throws InterruptedException {
        Log.d("navx", "started turn for " + deg);

        navXPIDController yawPIDController = new navXPIDController(navx, navXPIDController.navXTimestampedDataSource.YAW);
        navx.zeroYaw(); //reset the NavX yaw

        /* Configure the PID controller */
        yawPIDController.setSetpoint(deg);
        yawPIDController.setContinuous(true);
        yawPIDController.setOutputRange(-1, 1);
        yawPIDController.setTolerance(navXPIDController.ToleranceType.ABSOLUTE, TOLERANCE_DEGREES);
        yawPIDController.setPID(0.05, 0, 0);
        yawPIDController.enable(true);
        navXPIDController.PIDResult yawPIDResult = new navXPIDController.PIDResult();

        RollingAverage<Double> average = new RollingAverage<>(100);
        double lastValue = 0.0;

        while ((Math.abs(navx.getYaw() - deg) > TOLERANCE_DEGREES ||
                !(average.getAverage() < 0.01 * power && average.getSize() >= 20)) //&& abs(power) < some value
                && opModeIsActive()) {
            Log.d("navx", "current yaw " + navx.getYaw());

            if (yawPIDController.waitForNewUpdate(yawPIDResult, 500)) {
                double output = yawPIDResult.getOutput();
                Tank.motor4(frontLeft, frontRight, backLeft, backRight, limit(-output * power), limit(output * power));
                telemetry.addData("Yaw", df.format(navx.getYaw()));
                telemetry.addData("PID Power", df.format(output));
                telemetry.addData("PID Average", df.format(average.getAverage()));

                average.addValue(Math.abs(output - lastValue));
                lastValue = output;
            } else {
                /* A timeout occurred */
                Log.w("navXDriveStraightOp", "Yaw PID waitForNewUpdate() TIMEOUT.");
            }
        }
        Log.d("navx", "ended turn for " + deg);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);
    }

    public int encoderAverage() {
        double left = (frontLeft.getCurrentPosition() + backLeft.getCurrentPosition()) / 2;
        double right = (frontRight.getCurrentPosition() + backRight.getCurrentPosition()) / 2;

        //clockwise = positive
        //forward = positive
        return (int) ((Math.abs(left) + Math.abs(right)) / 2 * Math.signum(left));
    }
}
