PShape logo;

int score;
int hiscore = 0;
Dodger dodger;

Enemy[] enemies = new Enemy[99];
int eNum;
int sActive;
int eActive;
float limiter; // makes the arrow more narrow
// Enemy enemy;
int startVel;
int circleFactor;
float shipChance;

boolean clockwise;
int rotVel;
float rotAcc;
boolean gameOver;

void setup() {
  size(1900, 1200);
  background(0);
  logo = loadShape("logo.svg");
  shapeMode(CORNERS);
  gameOver = false;
  score = 0;
  // dodger attributes
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 20;
  rotAcc = 2.4;
  //enemy attributes
  startVel = 2;
  limiter = 0.9; // makes the arrow more narrow
  sActive = 5;
  eActive = 5;
  shipChance = 0.1;
  circleFactor = 6;
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

void draw() {
  if(!gameOver){
    rotAcc = 1.5 + score/50;
    background(0, 0, 0, 20);
    textSize(30);
    fill(255);
    text(score, 30, 30);
    //adjust amount of enemies according to score
    if(int(score/10 + sActive) > eActive) {
      eActive++;
    }
    for(eNum = 0; eNum < eActive; eNum++){
      enemies[eNum].update();
      if (enemies[eNum].bounds()) {
        newEnemy();
      }
      if(enemies[eNum].collision()){
        gameOver = true;
      }
      if(enemies[eNum].circleCollision()){
        if(enemies[eNum].circleTouched == false) {
          score++;
        }
        enemies[eNum].circleTouched = true;
      }

      enemies[eNum].draw();
    }
    dodger.update();
    dodger.bounds();
    dodger.draw();
    rotVel += rotAcc;
  } else {
    showScore();
  }
}



void newEnemy() {
  int border;
  border = (int) random(4);
  float type;
  type = random(1);
  String thisType;
  if(type > 1 -shipChance - score/300) {
    thisType = "ship";
  } else {
    thisType = "asteroid";
  }

  if(border == 0) {
    //left border
    enemies[eNum] = new Enemy(0-180, random(height), random(PI + limiter, TWO_PI - limiter), startVel, thisType);
  }
  if(border == 1) {
    //top border
    enemies[eNum] = new Enemy(random(width), 0-180, random(HALF_PI + PI + limiter, 3*QUARTER_PI - limiter), startVel, thisType);
  }
  if(border == 2) {
    //right border
    enemies[eNum] = new Enemy(width+180, random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startVel, thisType);
  }
  if(border == 3) {
    //bottom border
    enemies[eNum] = new Enemy(random(height), height+180, random(3*QUARTER_PI + limiter, HALF_PI + PI - limiter), startVel, thisType);
  }

  startVel += 0.1;
}

void showScore() {
  background(0);
  textSize(100);
  fill(255);
  stroke(255);
  strokeWeight(6);
  textAlign(CENTER, CENTER);
  // draw logo
  int border = 15;
  shape(logo, border, border+500, width-border, 750);
  //update score
  if (score > hiscore){
    hiscore = score;
  }
  // draw menu, score & high score
  text(score, width*1/4, height*3/4 -50);
  text(hiscore, width*3/4, height*3/4 -50);
  // wait for key input to start new game
}

void keyPressed() { // listen for user input
  if(gameOver && keyCode == ' '){
    gameOver = !gameOver;
    setup();
  } else {
    if(!clockwise){
      rotVel = 20;
    }
    clockwise = true;
  }
  // if (keyCode ==  LEFT) {
  //   clockwise = true;
  // } else if (keyCode == RIGHT) {
  //   clockwise = false;
  // }
  }

  void keyReleased() { // listen for user input
    clockwise = false;
    rotVel = 20;
  }
