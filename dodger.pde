int score, hiscore;
Dodger dodger;
boolean clockwise;
int rotVel;

void setup() {
  size(800,600);
  background(0);
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 4;
}

void draw() {
  background(0);
  dodger.update();
  dodger.bounds();
  dodger.draw();
}

void keyPressed() { //listen for user input
  if (keyCode ==  LEFT) {
    clockwise = true;
  } else if (keyCode == RIGHT) {
    clockwise = false;
  }
  rotVel++;
}
void keyReleased() { //listen for user input
  rotVel = 20;
}
