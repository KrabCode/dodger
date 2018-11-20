int score, hiscore;
Dodger dodger;
Enemy enemy;
boolean clockwise;
int rotVel;
boolean gameOver;

void setup() {
  size(800,600);
  background(0);
  gameOver = false;
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 4;
  newEnemy();
}

void draw() {
  if(!gameOver){
    background(0);
    dodger.update();
    dodger.bounds();
    dodger.draw();
    enemy.update();
    if (enemy.bounds()) {
      newEnemy();
    }
    println(enemy.collision());
    if(enemy.collision()){
      gameOver = true;
    }
    enemy.draw();
    score++;
  } else {
    showScore();
  }
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

void newEnemy() {
  enemy = new Enemy(0, height/2, random(PI, TWO_PI), "asteroid");
}

void showScore() {
  // draw score & high score
  //wait for key input to start new game
  background(0);
  textSize(100);
  textAlign(CENTER, CENTER);
  text(score, width/2, height/2 -50);
}
