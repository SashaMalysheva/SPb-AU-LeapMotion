#include <Arduino.h>


class StepperMotor {
public:
    StepperMotor(int In1, int In2, int In3, int In4);    // Constructor that will set the inputs
    void step(int noOfSteps);    // Step a certain number of steps. + for one way and - for the other
    int stepsPerRevolution;  // change this to fit the number of steps per revolution
    int duration;    // Step duration in ms
    int num_of_loop = 0;
};


StepperMotor::StepperMotor(int stepsPerRevolution, int duration, int In3, int In4){
    this->stepsPerRevolution = stepsPerRevolution;
    this->duration = duration;
}

StepperMotor motor(8,9,10,11);

void setup(){
  Serial.begin(9600);
  motor.setStepDuration(1);
}

void loop(){
  while (!Serial.available());
  angle = Serial.parseInt();
  sign = angle >= 0;
  angle = angle >= 0 ? angle : -angle;

  for (int i = 0; i < angle; ++i)
    sign ? motor.step(735) : motor.step(-735);
  Serial.println(sign ? angle : -angle);
//  delay(2000);
}


