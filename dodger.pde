int score, hiscore;
Dodger dodger;

void setup() {
  size(800,600);
  background(0);
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
}

void draw() {
  background(0);
  dodger.update();
  dodger.draw();
}

void keyPressed() { //listen for user input
  if (key ==  LEFT) {

  } else if (key == RIGHT) {

  }
}
