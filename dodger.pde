import processing.sound.*;

SoundFile pop, sLeft, sRight;
SoundFile snap0, snap1, snap2;
SoundFile bg, gameover;
boolean gameOverSoundPlayed;

//PShape logo;

float score;
int hiscore = 0;
int changeVel = 2;

// Dodger
Dodger dodger;
int startVel = 5 * changeVel;                     // beginning velocity of dodger, increases by scVel for every score
float scVel = 0.03 * changeVel;
float rotVel;                         // rotation velocity of dodger
float rotAcc = 2 * changeVel;                     // rotation acceleration of dodger, increases by scAcc for every score
float scAcc = 0.03 * changeVel;
boolean clockwise;                    // is the player turning clockwise
boolean gameOver;


// Enemy
int maxE = 40;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 9;                      // enemies active at start
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel = 3 * changeVel;      // beginning velocity of enemies, increases by scEVel for every score
float scEVel = 0.015 * changeVel;
float startSize = 50;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.15;
float shipChance;                     // chance to spawn ship instead of asteroid

// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;

void setup() {
  // size(1000, 1000);
  fullScreen();
  orientation(PORTRAIT);
  frameRate(30);
  // fullScreen(P2D, SPAN);
  smooth(5);
  background(0);

  //sounds
  pop = new SoundFile(this, "pop.wav");
  sLeft = new SoundFile(this, "perc1.wav");
  sLeft.pan(-1);
  sRight = new SoundFile(this, "perc2.wav");
  sRight.pan(1);
  snap0 = new SoundFile(this, "snap0.wav");
  snap1 = new SoundFile(this, "snap1.wav");
  snap2 = new SoundFile(this, "snap2.wav");
  bg = new SoundFile(this, "bg.wav");
  //bg.rate(0.6);
  gameover = new SoundFile(this, "gameover.wav");
  gameover.amp(0.6);

  //logo = loadShape("logo.svg");
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
    runGame();
  } else {
    showScore();
  }
}

void runGame() {
  if(!bg.isPlaying()) {
     bg.play();
  }
  rotAcc = 1.5 + score*scAcc; //increase the rotation velocity by rotation acceleration
  background(0, 0, 0);
  textSize(30);
  fill(255);
  //adjust amount of enemies according to score
  if(int(score/25 + sActive) > eActive && eActive < maxE) {
    eActive++;
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].update();
    if (enemies[eNum].bounds()) {
      newEnemy();
    }
    if(enemies[eNum].collision()){
      gameOverSoundPlayed = false;
      gameOver = true;
    }
    if(enemies[eNum].circleCollision()){
      enemies[eNum].hp--;
      if(enemies[eNum].circleTouched == false && enemies[eNum].hp < 0) {
        if(enemies[eNum].type == "ship") {
          score += 1.5;
        } else {
          score++;
        }
        switch(frameCount % 3) {
          case 0:
            snap1.play();
            println("snap 1");
            break;
          case 1:
            snap1.play();
            println("snap 2");
            break;
          case 2:
            snap2.play();
            println("snap 3");
            break;
        }
        enemies[eNum].circleTouched = true;
        enemies[eNum].vel *= 0.8; //reduce enemy velocity when circle disappears
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
}

void newEnemy() {
  int border;
  border = (int) random(4);
  float type;
  type = random(1);
  String thisType;
  if(type > 1 -shipChance -score/350) {
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
  if(!gameOverSoundPlayed){
    bg.stop();
    gameover.play();
    gameOverSoundPlayed = true;
    println("gameOverSoundPlayed");
  }
  background(0);
  textSize(150);
  fill(255);
  stroke(255);
  strokeWeight(6);
  textAlign(CENTER, CENTER);
  // draw logo
  //int border = 15;
  //shape(logo, border, border+500, width-border, 750);
  //update score
  if (score > hiscore){
    hiscore = int(score);
  }
  // draw menu, score & high score
  text(hiscore, width*2/4, height*1/4 -50);
  text(int(score), width*2/4, height*3.3/4 -50);
  // wait for key input to start new game
}

void keyPressed() { // listen for user input
  if(gameOver && keyCode == ' '){
    gameOver = !gameOver;
    setup();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      sRight.play();
    }
    clockwise = true;
    }
  }

void touchStarted() {
  if(gameOver){
    gameOver = !gameOver;
    setup();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      sRight.play();
    }
    clockwise = true;
    }
}

void keyReleased() { // listen for user input
  if(clockwise){
    sLeft.stop();
    sLeft.play();
  }
  clockwise = false;
  rotVel = 20;
}

void touchEnded() {
  if(clockwise){
    sLeft.stop();
    sLeft.play();
  }
  clockwise = false;
  rotVel = 20;
}
