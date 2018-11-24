import ddf.minim.*;
Minim minim;

// Load the sound files
AudioSample pop, sLeft, sRight, snap0, snap1, snap2, gameover;
AudioPlayer bg;
boolean gameOverSoundPlayed;

//prepare scaling screen to fixed resolution
PGraphics pg;
int pgWidth = 1920;
int pgHeight = 1080;
PGraphics pgfeedback;
float feedbackLevel = 0.9;

//PShape logo;

float score;
int hiscore = 0;
boolean gameOver;

float changeVel = 1;                  // modifies all velocities

/// Dodger
Dodger dodger;
float startVel;                       // beginning velocity of dodger, increases by scVel for every score
float scVel;
float rotVel;                         // rotation velocity of dodger
float rotAcc;                         // rotation acceleration of dodger, increases by scAcc for every score
float scAcc;
float rotDamp = 0.995;                // rotation velocity dampening
boolean clockwise;                    // is the player turning clockwise

/// Enemy
int maxE = 20;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 5;                          // enemies active at start
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel;                      // beginning velocity of enemies, increases by scEVel for every score
float scEVel;
float startSize = 30;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.15;
float shipChance;                     // chance to spawn ship instead of asteroid
float kamiChance;                     // chance to spawn kamikaze, starts at 0 increases with score
boolean bossActive;                   // tells us if there is a boss on the field
float modifier;                       // used to modify some starting values

/// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;
float bossCFactor = 1.5;              // boss has smaller circle and no add

void setup() {
  // setup screen
  // size(1000, 1000);
  fullScreen(P2D);
  orientation(PORTRAIT);
  //prepare scaling screen to fixed resolution
  pg = createGraphics(pgWidth, pgHeight, P2D);
  pgfeedback = createGraphics(pgWidth, pgHeight, P2D);
  noCursor();
  frameRate(60);
  smooth(2);
  background(0);

  //sounds
  minim = new Minim(this);
  int bufferSize = 512;
  pop = minim.loadSample("pop.wav", bufferSize);
  sLeft = minim.loadSample("perc1l.wav", bufferSize);
  sRight = minim.loadSample("perc2l.wav", bufferSize);
  snap0 = minim.loadSample("snap0.wav", bufferSize);
  snap1 = minim.loadSample("snap1.wav", bufferSize);
  snap2 = minim.loadSample("snap2.wav", bufferSize);
  bg = minim.loadFile("bg.wav", bufferSize);
  gameover = minim.loadSample("gameover.wav", bufferSize);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  initGame(); // set up the variables for game initialisation
}

//// set up the variables for game initialisation
void initGame() {
  gameOver = false;
  score = 40;

  // dodger attributes
  rotVel = 20;
  startVel = 3.1 * changeVel;
  scVel = 0.015 * changeVel;
  rotAcc = 1.6 * changeVel;
  scAcc = 0.007 * changeVel;
  dodger = new Dodger(pgWidth/2, height/2, 0);

  //enemy attributes
  startEVel = 3 * changeVel;
  scEVel = 0.01 * changeVel;
  limiter = 0.7;
  eActive = sActive;
  shipChance = 0.1; //starting chance for spawn to be ship, increases with score as well
  kamiChance = 0;
  bossActive = false;

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

/////UP = SETUP//////////DOWN = UPDATE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// draw function with gamestates
void draw() {
  pg.beginDraw();
  pg.background(0);

  if(!gameOver){
    runGame();
  } else {
    showScore();
  }

  pg.endDraw();
  image(pg, 0, 0, width, height);
}

//// perform a frame of the gameplay
void runGame() {
  if(bg.position() == bg.length()) {
    bg.rewind();
  }
  if(!bg.isPlaying()) {
    gameover.stop();
    bg.play();
  }
  pg.background(0, 0, 0);
  pg.textSize(30);
  pg.fill(255);
  // adjust amount of enemies according to score
  if(int(score/25 + sActive) > eActive && eActive < maxE) {
    eActive++;
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].update(); // update enemy position
    // if the enemy is out of bounds and not recently spawned
    if (enemies[eNum].bounds() && millis() - enemies[eNum].spawnTimer > 1000) {
      newEnemy();
    }
    // if dodger collides with an enemy
    if(enemies[eNum].collision()){
      gameOverSoundPlayed = false;
      gameOver = true;
    }
    // if dodger is colliding with a circle
    if(enemies[eNum].circleCollision()){
      enemies[eNum].hp--; // decrease life of enemies aura
      if(enemies[eNum].circleTouched == false && enemies[eNum].hp < 0) {
        // increase score depending on enemy type
        if(enemies[eNum].type == "boss1") {
          score += 5;
          bossActive = false;
        } else if(enemies[eNum].type == "kamikaze") {
          score += 2.5;
        } else if(enemies[eNum].type == "ship") {
          score += 1.5;
        } else {
          score++;
        }
        //play a random snap sound
        switch(frameCount % 3) {
          case 0:
            snap0.trigger();
            break;
          case 1:
            snap1.trigger();
            break;
          case 2:
            snap2.trigger();
            break;
        }
        enemies[eNum].circleTouched = true;
        enemies[eNum].vel *= 0.6;         // reduce enemy velocity when circle disappears
      }
    }
    enemies[eNum].drawCircle();           // draws circle first so no overlap with enemies
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].draw();
  }

  dodger.update();
  dodger.bounds();                        // check if dodger is still in bounds (if not, put back)
  dodger.draw();
  rotAcc = (2 + score*scAcc) * changeVel; // increase the rotation velocity by rotation acceleration
  rotVel += rotAcc;                       // velocity increases by acceleration
  rotVel *= rotDamp;                      // dampen the rotation velocity
}

/////GAME LOGICKS/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// spawn a new enemy
void newEnemy() {
  String thisType;
  int border = (int) random(4);         // determine which edge enemies spawn from
  float type = random(1);               // determine which type the enemy is going to be
  if(score > 5 && score % 60 <= 5 && !bossActive) {
    thisType = "boss1";
    bossActive = true;
    modifier = score;
  } else if(type > 1 -kamiChance -score/450) {
    thisType = "kamikaze";
  } else if(type > 1 -shipChance -score/450) {
    thisType = "ship";
  } else {
    thisType = "asteroid";
  }

  float enemyDiameter = (startSize + score*scESize) * circleFactor + circleAdd;
  if(border == 0) {
    //left border
    enemies[eNum] = new Enemy(0-enemyDiameter, random(height), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  }
  if(border == 1) {
    //top border
    enemies[eNum] = new Enemy(random(pgWidth), 0-enemyDiameter, random(HALF_PI + PI + limiter, 3*QUARTER_PI - limiter), startEVel, thisType);
  }
  if(border == 2) {
    //right border
    enemies[eNum] = new Enemy(pgWidth+enemyDiameter, random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startEVel, thisType);
  }
  if(border == 3) {
    //bottom border
    enemies[eNum] = new Enemy(random(height), height+enemyDiameter, random(3*QUARTER_PI + limiter, HALF_PI + PI - limiter), startEVel, thisType);
  }
  modifier = 0; // reset the modifier (used in enemy class for special enemies)
}

//// show the game over screen
void showScore() {
  if(!gameOverSoundPlayed){
    bg.pause(); //pause background sound
    gameover.trigger(); //play the game over sample
    gameOverSoundPlayed = true; //so it doesnt play again
  }
  pg.background(0);
  pg.textSize(150);
  pg.fill(255);
  pg.stroke(255);
  pg.strokeWeight(6);
  pg.textAlign(CENTER, CENTER);
  // draw logo
  //int border = 15;
  //shape(logo, border, border+500, pgWidth-border, 750);
  //update score
  if (score > hiscore){
    hiscore = int(score);
  }
  // draw menu, score & high score
  pg.text(hiscore, pgWidth*2/4, pgHeight*1/4 -50);
  pg.text(int(score), pgWidth*2/4, pgHeight*3.3/4 -50);

  // wait for key input to start new game
}

///////////////GRAPHICS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Feedback Functions

void feedbackCapture(PGraphics output) {
  pg.loadPixels();
  output.loadPixels();

  arrayCopy(pg.pixels, output.pixels);

  output.updatePixels();
}

///////////////INPUTS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void keyPressed() { // listen for user input // touchStarted
  if(gameOver && keyCode == ' '){
    gameOver = !gameOver;
    initGame();
  } else {
    if(!clockwise){
      sRight.trigger();
      rotVel = 20;
    }
    clockwise = true;
    }
  }

void keyReleased() { // listen for user input // touchEnded
  if(clockwise){
    sLeft.trigger();
    rotVel = 20;
  }
  clockwise = false;
}
