int score, hiscore;
Dodger dodger;
boolean clockwise;
Enemy enemy;

void setup() {
  size(800,600);
  background(0);
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
  newEnemy();
}

void draw() {
  background(0);
  dodger.update();
  dodger.bounds();
  dodger.draw();
  enemy.update();
  if (enemy.bounds()) {
    newEnemy();
  }
  println(enemy.collision());
  enemy.draw();
}

void keyPressed() { //listen for user input
  if (keyCode ==  LEFT) {
    clockwise = true;
  } else if (keyCode == RIGHT) {
    clockwise = false;
  }
}

void newEnemy() {
  enemy = new Enemy(0, height/2, random(PI, TWO_PI), "asteroid");
}
