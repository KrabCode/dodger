import ddf.minim.*;
Minim minim;

// Load the sound files
AudioSample pop, sLeft, sRight, snap0, snap1, snap2, gameover;
AudioPlayer bg;
boolean gameOverSoundPlayed;

//PShape logo;

float score;
int hiscore = 0;
boolean gameOver;

float changeVel = 2;                  // modifies all velocities

// Dodger
Dodger dodger;
float startVel;                       // beginning velocity of dodger, increases by scVel for every score
float scVel;
float rotVel;                         // rotation velocity of dodger
float rotAcc;                         // rotation acceleration of dodger, increases by scAcc for every score
float scAcc;
float rotDamp = 0.995;                // rotation velocity dampening
boolean clockwise;                    // is the player turning clockwise

// Enemy
int maxE = 20;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 9;                      // enemies active at start
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel;                      // beginning velocity of enemies, increases by scEVel for every score
float scEVel;
float startSize = 30;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.15;
float shipChance;                     // chance to spawn ship instead of asteroid

// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;

void setup() {
  // setup screen
  // size(1000, 1000);
  fullScreen();
  orientation(PORTRAIT);
  frameRate(30);
  smooth(5);
  background(0);

  // Create a Sound object for controlling the synthesis engine sample rate.

  //sounds
  minim = new Minim(this);
  int bufferSize = 512;
  pop = minim.loadSample("pop.wav", bufferSize);
  sLeft = minim.loadSample("perc1.wav", bufferSize);
  sRight = minim.loadSample("perc2.wav", bufferSize);
  snap0 = minim.loadSample("snap0.wav", bufferSize);
  snap1 = minim.loadSample("snap1.wav", bufferSize);
  snap2 = minim.loadSample("snap2.wav", bufferSize);
  bg = minim.loadFile("bg.wav", bufferSize);
  gameover = minim.loadSample("gameover.wav", bufferSize);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  initGame(); // set up the variables for game initialisation
}

// set up the variables for game initialisation
void initGame() {
  gameOver = false;
  score = 0;

  // dodger attributes
  rotVel = 20;
  startVel = 3.1 * changeVel;
  scVel = 0.015 * changeVel;
  rotAcc = 1.8 * changeVel;
  scAcc = 0.01 * changeVel;
  dodger = new Dodger(width/2, height/2, 0);

  //enemy attributes
  startEVel = 3 * changeVel;
  scEVel = 0.01 * changeVel;
  limiter = 0.7;
  eActive = sActive;
  shipChance = 0.1; //starting chance for spawn to be ship, increases with score as well

  // generate new enemies
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

// perform a frame of the gameplay
void runGame() {
  if(bg.position() == bg.length()) {
    bg.rewind();
  }
  if(!bg.isPlaying()) {
    gameover.stop();
    bg.play();
  }
  background(0, 0, 0);
  textSize(30);
  fill(255);
  //adjust amount of enemies according to score
  if(int(score/25 + sActive) > eActive && eActive < maxE) {
    //eActive++;
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
            snap1.trigger();
            println("snap 1");
            break;
          case 1:
            snap1.trigger();
            println("snap 2");
            break;
          case 2:
            snap2.trigger();
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
  rotAcc = (2 + score*scAcc) * changeVel; //increase the rotation velocity by rotation acceleration
  rotVel += rotAcc;
  rotVel *= rotDamp;
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
    bg.pause();
    gameover.trigger();
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
    initGame();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      sRight.trigger();
    }
    clockwise = true;
    }
  }

void touchStarted() {
  if(gameOver){
    gameOver = !gameOver;
    initGame();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      sRight.trigger();
    }
    clockwise = true;
    }
}

void keyReleased() { // listen for user input
  if(clockwise){
    sLeft.stop();
    sRight.stop();
    sLeft.trigger();
  }
  clockwise = false;
  rotVel = 20;
}

void touchEnded() {
  if(clockwise){
    sLeft.stop();
    sRight.stop();
    sLeft.trigger();
  }
  clockwise = false;
  rotVel = 20;
}
