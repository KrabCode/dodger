import ddf.minim.*;
Minim minim;

// Load the sound files
AudioSample pop, sLeft, sRight, snap0, snap1, snap2, gameover;
AudioPlayer bg;
boolean gameOverSoundPlayed;
boolean muted = false;

//prepare scaling screen to fixed resolution
PGraphics pg;
int pgWidth = 1920;
int pgHeight = 1080;
PGraphics pgfeedback;
float feedbackLevel = 0.9;

//PShape logo;

float score;
float highScore = 0;
int totalScore = 0;
int playTime;

// game states
boolean gameOver; // goes to main menu
boolean mainMenu; // starts a new game

int diagBar = 1;  // sets the state of  the diagnostics bar, can be changed by num keys
float scoreRate;

float changeVel = 1;                  // modifies all velocities
float nextScore;

/// Dodger
Dodger dodger;
int dodgerSize = 22;
float startVel;                       // beginning velocity of dodger, increases by scVel for every score
float scVel;
float rotVel;                         // rotation velocity of dodger
float rotAcc;                         // rotation acceleration of dodger, increases by scAcc for every score
float scAcc;
float rotDamp = 0.995;                // rotation velocity dampening
boolean clockwise;                    // is the player turning clockwise

/// Enemy
int maxE = 30;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 8;                      // enemies active at start
int enemiesPerScore = 40;             // amount of score necessary to increase sActive by one
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel;                      // beginning velocity of enemies, increases by scEVel for every score
float scEVel;
float startSize = 30;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.03;
float enemyDrain = 0.4;               // verlocity of enemy after aura was harvested
float shipChance;                     // chance to spawn ship instead of asteroid
float kamiChance;                     // chance to spawn kamikaze, starts at 0 increases with score
float chanceModifier;                 // number by which the chance for enemy types gets modified
boolean bossActive = false;            // tells us if there is a boss on the field
int bossNumber;                       // cycles through the bosses
int nextBossNumber;
float modifier;                       // used to modify some starting values

/// Aura
float circleFactor = 1.4;             // size of aura per enemy size
int circleAdd = 160;                  // added to size of aura
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
  gameover = minim.loadSample("pop.wav", bufferSize);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  score = 50;
  initGame(); // set up the variables for game initialisation
}

//// set up the variables for game initialisation
void initGame() {
  gameOver = false;
  playTime = second();
  score *= (4 + min(2, map(totalScore, 0, 1200, 0, 3) + min(2, map(highScore, 0, 500, 0, 3)))) /10;
  scoreRate = (4 + min(2, map(totalScore, 0, 1200, 0, 3) + min(2, map(highScore, 0, 500, 0, 3)))) /10;            // watch an ad or buy the game to keep 70% of your score
  // just kidding


  // dodger attributes
  rotVel = 20;
  startVel = 2.6 * changeVel;
  scVel = 0.002 * changeVel;
  rotAcc = random(0.4, 0.7) * changeVel;
  scAcc = 0.002 * changeVel;
  dodger = new Dodger(pgWidth/2, pgHeight/2, 0);

  //enemy attributes
  startEVel = 2 * changeVel;
  scEVel = 0.0025 * changeVel;
  limiter = 0.7;
  eActive = sActive;
  shipChance = 0.1; //starting chance for spawn to be ship, increases with score as well
  kamiChance = -0.2;
  bossNumber = 0;
  bossActive = false;

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

/////UP = SETUP//////////DOWN = UPDATE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// draw function with gamestates
void draw() {
  // draw everything to the canvas with pgWidth*pgHeight
  pg.beginDraw();
  pg.background(0);

  if(gameOver){
    showScore();
  } else if(mainMenu){
    showMenu();
  } else {
    runGame();
    drawBar();
  }

  // scale everything from the canvas to the actual screen
  pg.endDraw();
  image(pg, 0, 0, width, height);
}

//// perform a frame of the gameplay
void runGame() {
  // update next score
  nextScore = score * scoreRate;

  if(bg.position() == bg.length()) {
    bg.rewind();
  }
  if(muted) {
    bg.pause();
  } else if(!bg.isPlaying() && !muted) {
    //gameover.trigger();
    bg.pause();
  }
  pg.background(0, 0, 0);
  pg.textSize(30);
  pg.fill(255);
  // adjust amount of enemies according to score
  if(int(score/enemiesPerScore + sActive) > eActive && eActive < maxE) {
    eActive++;
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].update(); // update enemy position
    // if the enemy is out of bounds and not recently spawned make a new enemy appear
    if (enemies[eNum].bounds() && millis() - enemies[eNum].spawnTimer > 1000) {
      newEnemy();
    }
    // if dodger collides with an enemy
    if(enemies[eNum].collision()){
      gameOverSoundPlayed = false;
      //update score
      if (score > highScore){
        highScore = int(score);
      }
      totalScore += score;
      gameOver = true;
    }
    // if dodger is colliding with an aura decrease hp and if aura is harvested increase score
    if(enemies[eNum].circleCollision()){
      enemies[eNum].hp--; // decrease life of enemies aura
      if(enemies[eNum].circleTouched == false && enemies[eNum].hp < 0) {
        // increase score depending on enemy type (asteroid:1 ship:1.5 kamikaze:2.5 boss:5 )
        if(enemies[eNum].type == "boss1" || enemies[eNum].type == "boss2") {
          score += 3 + bossNumber*5;
          score += 3 + bossNumber*5;
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
        enemies[eNum].vel *= enemyDrain;         // reduce enemy velocity when circle disappears
      }
    }
    enemies[eNum].drawCircle();           // draws circle first so no overlap with enemies
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].draw();
  }

  dodger.update();
  dodger.bounds();                        // check if dodger is still in bounds (if not, put back)
  dodger.drawCircle();
  dodger.draw();
  rotAcc = (2 + score*scAcc) * changeVel; // increase the rotation velocity by rotation acceleration
  rotVel += rotAcc;                       // velocity increases by acceleration
  rotVel *= rotDamp;                      // dampen the rotation velocity
}

/////GAME LOGICKS/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// spawn a new enemy
void newEnemy() {
  String thisType = "asteroid";
  int border = (int) random(4);         // determine which edge enemies spawn from
  float type = random(1);               // determine which type the enemy is going to be
  // check if bosses get spawned
  nextBossNumber = int(30 + bossNumber*5);
  if(score > 5 && score % nextBossNumber <= 5 && !bossActive) {
    modifier = score;
    bossActive = true;
    chanceModifier = score / 450;
    switch(bossNumber % 2) {
      case 0:
        thisType = "boss1";
        break;
      case 1:
        thisType = "boss2";
        break;
    }
    bossNumber += 1;
  } else if(type > 1 -kamiChance - score / 550) {
    thisType = "kamikaze";
  } else if(type > 1 -shipChance - chanceModifier) {
    thisType = "ship";
  } else {
    thisType = "asteroid";
  }
  float enemyDiameter = (startSize + score*scESize) * circleFactor + circleAdd;
  if(thisType == "boss1"){
    enemies[eNum] = new Enemy(pgWidth/4 + random(pgWidth/2), pgHeight/4 + random(pgHeight/2), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  } else if(thisType == "boss2"){
    enemies[eNum] = new Enemy(random(pgWidth), random(pgHeight), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  } else if(border == 0) {
    //left border
    enemies[eNum] = new Enemy(0-enemyDiameter, random(pgHeight), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  }
  if(border == 1) {
    //top border
    enemies[eNum] = new Enemy(random(pgWidth), 0-enemyDiameter, random(HALF_PI + PI + limiter, 3*QUARTER_PI - limiter), startEVel, thisType);
  }
  if(border == 2) {
    //right border
    enemies[eNum] = new Enemy(pgWidth+enemyDiameter, random(pgHeight), random(TWO_PI + limiter, TWO_PI + PI - limiter), startEVel, thisType);
  }
  if(border == 3) {
    //bottom border
    enemies[eNum] = new Enemy(random(pgHeight), pgHeight+enemyDiameter, random(3*QUARTER_PI + limiter, HALF_PI + PI - limiter), startEVel, thisType);
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
  pg.fill(255);
  pg.stroke(255);
  pg.strokeWeight(6);
  pg.textAlign(CENTER, CENTER);
  // draw logo
  //int border = 15;
  //shape(logo, border, border+500, pgWidth-border, 750);
  // draw score & high score
  int playDiff = second() - playTime;
  pg.textSize(140);
  pg.text(int(score), pgWidth*2/4, pgHeight*1/4 -50);
  pg.textSize(30);
  pg.text(" || best "+ int(highScore) + " || total " + totalScore + " || rate "+ scoreRate + " || " + playDiff + "sec || next " + int(nextScore), pgWidth*2/4, pgHeight*2/5 -50);
  pg.text( "|| rate" + min(2, map(highScore, 0, 500, 0, 3)) /10 + " || rate " + (4 + min(2, map(totalScore, 0, 1200, 0, 3))) + "                                ", pgWidth*2/4, pgHeight*2.2/5 -50);
  // pg.textSize(100);
  // pg.text(int(score), pgWidth*2/4, pgHeight*3.3/4 -50);

  // wait for key input to show mainMenu
}

//// show the menu screen
void showMenu() {
  pg.background(0);
  pg.fill(255);
  pg.stroke(255);
  pg.strokeWeight(6);
  pg.textAlign(CENTER, CENTER);
  // draw logo
  //int border = 15;
  //shape(logo, border, border+500, pgWidth-border, 750);

  // draw menu & high score
  pg.textSize(60);
  pg.text(highScore, pgWidth*2/4, pgHeight*1/4 -50);
  pg.text("new game", pgWidth*2/4, pgHeight*3.3/4 -50);

  // wait for key input to start new game
}

boolean randomBool() {
  return random(1) > .5;
}

///////////////DIAGNOSE////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// display a bar with information
void drawBar() {
  pg.textSize(22);
  pg.fill(255, 255, 255, 150);
  pg.noStroke();
  pg.textAlign(LEFT, TOP);
  switch(diagBar) {
    case 0:
      break;
      // show just score
    case 1:
      int playDiff = second() - playTime;

      pg.text(" || "+ score + " || total"+ totalScore + " || rate"+ scoreRate + " || " + playDiff + "sec || next " + nextScore + "         || 0 = remove bar | 1 = this bar | 2 = Enemies | 3 = Dodger | 9 = hotkeys ||", 10, 10);

      break;
    // show enemy stats
    case 2:
      pg.text(" || "+score +
      " || Enemies || active:" + eActive +
      " || vel start:" + startEVel +
      "  +s* " + scEVel +
      "  current:" + nf(startEVel + score*scEVel, 0, 3) +
      " || size start:" + startSize +
      "  +s* " + scESize +
      "  current:" + nf(startSize + score*scESize, 0, 3) +
      " || chance for ship:" + nf(shipChance + chanceModifier, 0, 3) +
      " kami:" + nf(kamiChance + chanceModifier, 0, 3), 10, 10);
      break;
    // show dodger stats
    case 3:
      pg.text(" || "+score +
              " || dodger || vel start:" + startVel +
              "  +s* " + scVel +
              "  current:" + nf(startVel + score*scVel, 0, 3) +
              " || size:" + dodgerSize +
              "    || chance for ship:" + nf(shipChance + chanceModifier, 0, 3) +
              " kami:" + nf(kamiChance + chanceModifier, 0, 3), 10, 10);
    break;
    case 9:
      pg.text(" || "+ score +" || K = game over | arrow keys | modify score ||" + score % nextBossNumber, 10, 10);
      break;
  }

  float startVel;                       // beginning velocity of dodger, increases by scVel for every score
}

///////////////INPUTS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void keyPressed() { // listen for user input // touchStarted
  if(gameOver && !clockwise){
    gameOver = false;
    mainMenu = true;
    showMenu();
  } else if(mainMenu && !clockwise){
    mainMenu = false;
    initGame();
  } else {
    if(!clockwise){
      // sRight.trigger();
      rotVel = 20;
    }
    clockwise = true;
  }
  // change diagbar state with the num keys
  switch(keyCode) {
    case '0':
      diagBar = 0;
      break;
    case '1':
      diagBar = 1;
      break;
    case '2':
      diagBar = 2;
      break;
    case '3':
      diagBar = 3;
      break;
    case '4':
      diagBar = 4;
      break;
    case '5':
      diagBar = 5;
      break;
    case '6':
      diagBar = 6;
      break;
    case '7':
      diagBar = 7;
      break;
    case '8':
      diagBar = 8;
      break;
    case '9':
      diagBar = 9;
      break;
    case 'K':
      gameOver = true;
      break;
    case 'M':
      muted = !muted;
      break;
    case RIGHT:
      score++;
      break;
    case UP:
      score += 10;
      break;
    case LEFT:
      score--;
      break;
    case DOWN:
      score -= 10;
      break;
  }
}

void keyReleased() { // listen for user input // touchEnded
  if(clockwise){
    // sLeft.trigger();
    rotVel = 20;
  }
  clockwise = false;
}
