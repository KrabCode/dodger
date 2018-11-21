PShape logo;

float score;
int hiscore = 0;

// Dodger
Dodger dodger;
int startVel = 4; // beginning velocity of dodger, increases by scVel for every score
float scVel = 0.05;
float rotVel; // rotation velocity of dodger
float rotAcc = 2; // rotation acceleration of dodger, increases by scAcc for every score
float scAcc = 0.04;

// Enemy
int maxE = 40;
Enemy[] enemies = new Enemy[maxE];
int eNum;
int sActive = 8; // enemies active at start
int eActive; // enemies currently active
float limiter; // makes the arrow more narrow
float startEVel = 2; // beginning velocity of enemies, increases by scEVel for every score
float scEVel = 0.02;
float scESize = 0.2;

int circleFactor = 2;
int circleAdd = 220;
float shipChance;

boolean clockwise;
boolean gameOver;

void setup() {
  fullScreen();
  smooth(1000);
  noCursor();
  background(0);
  logo = loadShape("logo.svg");
  shapeMode(CORNERS);
  gameOver = false;
  score = 0;
  // dodger attributes
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 20;

  //enemy attributes
  limiter = 0.7;
  eActive = sActive;
  shipChance = 0.15; //starting chance for spawn to be ship, increases with score as well
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

void draw() {
  if(!gameOver){
    rotAcc = 1.5 + score*scAcc;
    background(0, 0, 0, 20);
    textSize(30);
    fill(255);
    // text(int(score), 30, 30);
    //adjust amount of enemies according to score
    if(int(score/11 + sActive) > eActive && eActive < maxE) {
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
        enemies[eNum].hp--;
        if(enemies[eNum].circleTouched == false && enemies[eNum].hp < 0) {
          if(enemies[eNum].type == "ship") {
            score++;
          } else {
            score++;
          }
          enemies[eNum].circleTouched = true;
          enemies[eNum].vel *= 0.7;
        }
      }
      enemies[eNum].drawCircle();
    }
    for(eNum = 0; eNum < eActive; eNum++){
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
  if(type > 1 -shipChance -score/150) {
    thisType = "ship";
  } else {
    thisType = "asteroid";
  }

  if(border == 0) {
    //left border
    enemies[eNum] = new Enemy(0-((10 + score*scESize)*circleFactor + circleAdd), random(height), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  }
  if(border == 1) {
    //top border
    enemies[eNum] = new Enemy(random(width), 0-((10 + score*scESize)*circleFactor + circleAdd), random(HALF_PI + PI + limiter, 3*QUARTER_PI - limiter), startEVel, thisType);
  }
  if(border == 2) {
    //right border
    enemies[eNum] = new Enemy(width+((10 + score*scESize)*circleFactor + circleAdd), random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startEVel, thisType);
  }
  if(border == 3) {
    //bottom border
    enemies[eNum] = new Enemy(random(height), height+((10 + score*scESize)*circleFactor + circleAdd), random(3*QUARTER_PI + limiter, HALF_PI + PI - limiter), startEVel, thisType);
  }

  startVel += 0.1;
}

void showScore() {
  background(0);
  textSize(150);
  fill(255);
  stroke(255);
  strokeWeight(6);
  textAlign(CENTER, CENTER);
  // draw logo
  int border = 15;
  shape(logo, border, border+500, width-border, 750);
  //update score
  if (score > hiscore){
    hiscore = int(score);
  }
  // draw menu, score & high score
  text(int(score), width*2/4, height*3/4 -50);
  text(hiscore, width*2/4, height*1/4 -50);
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
