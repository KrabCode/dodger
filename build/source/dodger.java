import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class dodger extends PApplet {


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
float feedbackLevel = 0.9f;

//PShape logo;

float score;
float highScore = 0;
int totalScore = 0;
int playTime;

// game states
boolean gameOver; // goes to main menu
boolean mainMenu; // starts a new game

int diagBar = 6;  // sets the state of  the diagnostics bar, can be changed by num keys
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
float rotDamp = 0.99f;                // rotation velocity dampening
boolean clockwise;                    // is the player turning clockwise

/// Enemy
int maxE = 30;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 8;                      // enemies active at start
int enemiesPerScore = 60;             // amount of score necessary to increase sActive by one
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel;                      // beginning velocity of enemies, increases by scEVel for every score
float scEVel;
float startSize = 25;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.015f;
float enemyDrain = 0.7f;               // verlocity of enemy after aura was harvested
float enemyRDrain = 0.5f;              // rotation of enemy after aura was harvested
float shipChance, shipVal;            // chance to spawn ship instead of asteroid
float kamiChance, kamiVal;            // chance to spawn kamikaze, starts at 0 increases with score
float chanceModifier = 1/500;         // number by which the chance for enemy types gets modified
boolean bossActive = false;           // tells us if there is a boss on the field
int bossNumber;                       // cycles through the bosses
int nextBossNumber;
float modifier;                       // used to modify some starting values

/// Aura
float circleFactor = 1.4f;             // size of aura per enemy size
int circleAdd = 160;                  // added to size of aura
int circleTransparency = 20;
float bossCFactor = 1.5f;              // boss has smaller circle and no add

public void setup() {
  // setup screen
  // size(1000, 1000);
  
  orientation(PORTRAIT);
  //prepare scaling screen to fixed resolution
  pg = createGraphics(pgWidth, pgHeight, P2D);
  pgfeedback = createGraphics(pgWidth, pgHeight, P2D);
  noCursor();
  frameRate(60);
  
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
  bg = minim.loadFile("bg1.wav", bufferSize);
  gameover = minim.loadSample("pop.wav", bufferSize);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  score = 0;
  totalScore = 0;
  initGame(); // set up the variables for game initialisation
}

//// set up the variables for game initialisation
public void initGame() {
  gameOver = false;
  playTime = second();
  scoreRate = (4 + min(1, map(totalScore, 0, 2000, 0, 1)) + min(1.5f, map(totalScore, 0, 15000, 0, 1)) + min(1, map(score, 0, 500, 0, 1)) + min(1, map(highScore, 0, 1500, 0, 1))) /10;            // watch an ad or buy the game to keep 70% of your score
  score *= (4 + min(1, map(totalScore, 0, 2000, 0, 1)) + min(1.5f, map(totalScore, 0, 15000, 0, 1)) + min(1, map(score, 0, 500, 0, 1)) + min(1, map(highScore, 0, 1500, 0, 1))) /10;            // watch an ad or buy the game to keep 70% of your score
  // just kidding
  // println(" 0-2000: " + min(1, map(totalScore, 0, 2000, 0, 1)) + " / 0-15000:" + min(1, map(totalScore, 0, 15000, 0, 1)) + " / h 0-1000" + min(2, map(highScore, 0, 1000, 0, 2)) );

  // dodger attributes
  rotVel = 20;
  startVel = 2 * changeVel;
  scVel = 0.002f * changeVel;
  rotAcc = random(0.06f, 0.10f) * changeVel;
  scAcc = 0.0009f * changeVel;
  dodger = new Dodger(pgWidth/2, pgHeight/2, 0);

  //enemy attributes
  startEVel = 1.6f * changeVel;
  scEVel = 0.0025f * changeVel;
  limiter = 0.7f;
  eActive = sActive;
  shipChance = 0.25f; //starting chance for spawn to be ship, increases with score as well
  kamiChance = 0.1f;
  bossNumber = 0;
  bossActive = false;

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

/////UP = SETUP//////////DOWN = UPDATE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// draw function with gamestates
public void draw() {
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
public void runGame() {
  // update next score
  nextScore = score * scoreRate;

  if(bg.position() == bg.length()) {
    bg.rewind();
  }
  // if(muted) {
  //   bg.pause();
  // } else && !muted
  if(!bg.isPlaying()) {
    gameover.trigger();
    bg.play();
  }
  pg.background(0, 0, 0);
  pg.textSize(30);
  pg.fill(255);
  // adjust amount of enemies according to score
  if(PApplet.parseInt(score/enemiesPerScore + sActive) > eActive && eActive < maxE) {
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
        highScore = PApplet.parseInt(score);
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
          score += 2.5f;
        } else if(enemies[eNum].type == "ship") {
          score += 1.5f;
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
        enemies[eNum].vel *= enemyDrain;                                           // reduce enemy velocity when circle disappears
        enemies[eNum].rotation = (enemies[eNum].rotation % TWO_PI) *enemyRDrain;  // reduce enemy rotation when circle disappears
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
public void newEnemy() {
  String thisType = "asteroid";
  int border = (int) random(4);         // determine which edge enemies spawn from
  float typeR = random(1);               // determine which type the enemy is going to be

  // check if bosses get spawned
  nextBossNumber = PApplet.parseInt(30 + bossNumber*5);
  if(score > 5 && score % nextBossNumber <= 5 && !bossActive) {
    modifier = score;
    bossActive = true;
    float bossSeed = random(1,5);
    println("making boss", PApplet.parseInt(bossNumber+bossSeed));
    switch(PApplet.parseInt(bossNumber+bossSeed) % 2) {
      case 0:
        thisType = "boss1";
        break;
      case 1:
        thisType = "boss2";
        break;
    }
    bossNumber += 1;
    kamiVal = (-score*chanceModifier + kamiChance);
    shipVal = (-score*chanceModifier + shipChance);
    } else if( (typeR) > 1-kamiVal ) {
    thisType = "kamikaze";
    } else if( (typeR) > 1-shipVal ) {
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
public void showScore() {
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
  pg.text(PApplet.parseInt(score), pgWidth*2/4, pgHeight*1/4 -50);
  pg.textSize(30);
  // pg.text(" || best "+ int(highScore) + " || total " + totalScore + " || rate "+ scoreRate + " || " + playDiff + "sec || next " + int(nextScore), pgWidth*2/4, pgHeight*2/5 -50);
  pg.text(" || best "+ PApplet.parseInt(highScore) + " || total " + totalScore + " || rate "+ scoreRate + " || next " + PApplet.parseInt(nextScore) + " || ", pgWidth*2/4, pgHeight*2/5 -50);
  // pg.text( "|| rate" + min(2, map(highScore, 0, 500, 0, 3)) /10 + " || rate " + (4 + min(2, map(totalScore, 0, 1200, 0, 3))) + "                                ", pgWidth*2/4, pgHeight*2.2/5 -50);
  // pg.textSize(100);
  // pg.text(int(score), pgWidth*2/4, pgHeight*3.3/4 -50);

  // wait for key input to show mainMenu
}

//// show the menu screen
public void showMenu() {
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
  pg.text("new game", pgWidth*2/4, pgHeight*3.3f/4 -50);

  // wait for key input to start new game
}

public boolean randomBool() {
  return random(1) > .5f;
}

///////////////DIAGNOSE////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// display a bar with information
public void drawBar() {
  scoreRate = (4 + min(1, map(totalScore, 0, 2000, 0, 1)) + min(1.5f, map(totalScore, 0, 15000, 0, 1)) + min(1, map(score, 0, 500, 0, 1)) + min(1, map(highScore, 0, 1500, 0, 1))) /10;            // watch an ad or buy the game to keep 70% of your score
  pg.textSize(22);
  pg.fill(255, 255, 255, 150);
  pg.noStroke();
  pg.textAlign(LEFT, TOP);
  switch(diagBar) {
    case 0:
    // show just score
      pg.text(" || "+ score + " ||", 10, 10);
      break;
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
      " || chance for ship:" + shipVal +
      " kami:" + kamiVal, 10, 10);
      break;
    // show dodger stats
    case 3:
      pg.text(" || "+score +
              " || dodger || vel start:" + startVel +
              "  +s* " + scVel +
              "  current:" + nf(startVel + score*scVel, 0, 3) +
              " || size:" + dodgerSize +
              " || chance for ship:" + shipVal +
              " kami:" + kamiVal, 10, 10);
    break;
    case 9:
      pg.text(" || "+ score +" || (case sensitive) K = game over | WASD = modify score ||" + score % nextBossNumber, 10, 10);
      break;
  }

  float startVel;                       // beginning velocity of dodger, increases by scVel for every score
}

///////////////INPUTS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public void keyPressed() { // listen for user input // touchStarted
  if(gameOver && !clockwise && keyCode == ' '){
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
    case 'D':
      score++;
      break;
    case 'W':
      score += 10;
      break;
    case 'A':
      score--;
      break;
    case 'S':
      score -= 10;
      break;
  }
}

public void keyReleased() { // listen for user input // touchEnded
  if(clockwise){
    // sLeft.trigger();
    rotVel = 20;
  }
  clockwise = false;
}
class Dodger {

  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  float a;
  float size = dodgerSize;
  float vel = startVel;

  Dodger (float _x, float _y, float _a) {
    pos = new PVector(_x, _y);
    a = _a;
  }

  //// draw the aura of the enemy
  public void drawCircle() {
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);

    // float auraSize = 1 + map(score, 0, highScore + 10, 0, 2);
    int scale = 20;

    pg.noStroke();
    pg.fill(255, 255, 255, 10);
    pg.ellipse(0, 0, 2*size*(score%  9/  9 * scale/4), 2*size*(score%  9/  9 * scale/4) );
    pg.ellipse(0, 0, 2*size*(score% (9*9)/ (9*9) * scale/3), 2*size*(score% (9*9)/ (9*9) * scale/3) );

    pg.stroke(255, 255, 255, 100);
    pg.strokeWeight(4);
    pg.ellipse(0, 0, 2*size*(score%(9*9*9)/(9*9*9) * scale/2), 2*size*(score%(9*9*9)/(9*9*9) * scale/2) );

    pg.noStroke();
    pg.fill(0);
    pg.ellipse(0, 0, size, size);

    pg.noStroke();
    pg.popMatrix();
  }

  //// draw dodger
  public void draw() {
    pg.rectMode(CENTER);
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    pg.rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    pg.stroke(255);
    pg.strokeWeight(6);
    pg.line(-0.5f * size, -1 * size, 0, 1 * size);
    pg.line(0.5f * size, -1 * size, 0, 1 * size);
    pg.line(-0.4f * size, -0.6f * size, 0.4f * size, -0.6f * size); //back line
    pg.popMatrix();
  }

  //// update dodger position
  public void update() {
    //dodger moves
    move = new PVector(0, vel + score*scVel); // velocity adjust
    if(!clockwise){
      a -= 0.001f * rotVel;
    } else {
      a += 0.001f * rotVel;
    }
    move = move.rotate(a);
    pos.add(move);
  }

  //// check if dodger is inside the boundaries
  public void bounds() {
    if(pos.x < 0+size*2/3) {
      pos.x = 0+size*2/3;
    } else if(pos.x > pgWidth-size*2/3) {
      pos.x = pgWidth-size*2/3;
    }
    if(pos.y < 0+size*2/3) {
      pos.y = 0+size*2/3;
    } else if(pos.y > pgHeight-size*2/3) {
      pos.y = pgHeight-size*2/3;
    }
  }
}
class Enemy {
  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  PVector nPos;
  String type;
  //ship, kamikaze, asteroid
  int hp; // health points of circle
  float a;
  float size;
  float vel;
  float [] rndmAst = new float[16]; //random zahlen array fuer asteroid vertex
  boolean circleTouched = false;
  int spawnTimer = millis();
  int untouchable = 6000; // time the bosses are untouchable
  float transparency;
  float rotation = random(-1, 1);

  //// construct the enemy
  Enemy (float _x, float _y, float _a, float _vel, String _type) {
    pos = new PVector(_x, _y);
    a = _a;
    type = _type;
    if(type == "asteroid"){
      size = startSize + score*scESize;
      size *= random(0.7f, 1.4f); // RNG for enemy size
      vel = _vel * random(0.7f, 1.3f) + score * scEVel;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(size/4, size*5/4);
      }
      hp = PApplet.parseInt((50 + score/50) / changeVel);
    }
    if(type == "ship"){
      size = startSize + score*scESize;
      size *= random(0.6f, 1.2f); // RNG for enemy size
      vel = _vel * random(0.8f, 1.2f) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 1.5f;
      hp = PApplet.parseInt((30 + score/40) /changeVel);
    }
    if(type == "kamikaze"){
      size = startSize + score*scESize;
      size *= random(0.6f, 1.2f); // RNG for enemy size
      vel = _vel * random(0.8f, 1.2f) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      hp = PApplet.parseInt((25 + score/15) /changeVel);
    }
    if(type == "boss1"){
      size = 60 + (startSize + score*scESize)*0.4f + modifier/4;
      size *= random(0.9f, 1.1f); // RNG for enemy size
      vel = _vel * random(0.9f, 1.1f) + score * scEVel;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = PApplet.parseInt(400+modifier / changeVel);
        //+ int(score/8);
      }
    }
    if(type == "boss2"){
      size = 70 + (startSize + score*scESize/2)/5 + modifier/5;
      size *= random(0.9f, 1.1f); // RNG for enemy size
      vel = _vel * random(0.9f, 1.1f) + score * scEVel;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = PApplet.parseInt(400+modifier / changeVel);
        //+ int(score/8);
      }
    }
  }

  //// draw the aura of the enemy
  public void drawCircle() {
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    if(!circleTouched) {
      pg.noStroke();
      if(type == "boss1" || type == "boss2") {
        pg.fill(255, 255, 255, circleTransparency + hp/8);
        pg.ellipse(0, 0, 2*size*bossCFactor, 2*size*bossCFactor);
        pg.fill(0);
        pg.ellipse(0, 0, size, size);
      } else {
        pg.fill(255, 255, 255, circleTransparency + hp);
        pg.ellipse(0, 0, 2*size*circleFactor + circleAdd, 2*size*circleFactor + circleAdd);
      }
      pg.noStroke();
    }
    pg.popMatrix();
  }

  //// draw the enemy
  public void draw() {
    pg.fill(0);
    pg.rectMode(CENTER);
    pg.ellipseMode(CENTER);

    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    pg.rotate(a);
    if(!circleTouched) {
      pg.stroke(255);
      pg.fill(0);
    } else {
      pg.stroke(255);
      pg.fill(255);
    }
    pg.strokeWeight(3);
    if(type == "ship") {
      pg.beginShape();
        pg.vertex(-1 * size,   -1 * size);
        pg.vertex(0          ,    1 * size);
        pg.vertex(0.5f * size ,   -1 * size);
        pg.vertex(0          , -0.3f * size);
        pg.vertex(-1 * size,   -1 * size);
      pg.endShape();
    } else if(type == "asteroid") {
      pg.rotate(frameCount*0.03f*rotation);
      pg.beginShape();
        pg.vertex(0, -rndmAst[1]);
        pg.vertex(rndmAst[2], 0);
        pg.vertex(0, rndmAst[3]);
        pg.vertex(-rndmAst[4], 0);
        pg.vertex(-12, -12);
        pg.vertex(0, -rndmAst[1]);
      pg.endShape();
    } else if(type == "boss1") {
      transparency = map(millis() - spawnTimer, 0, untouchable, 55, 255);
      if(!circleTouched) {
        pg.stroke(255, 255, 255, transparency);
        pg.fill(255-transparency, 255-transparency, 255-transparency);
      } else {
        pg.stroke(255);
        pg.fill(255);
      }
      // draws the spawnTimer ellipse
      pg.ellipse(0, 0, size, size);
      pg.stroke(0);
      pg.fill(0);
      pg.ellipse(0, size, size/3, size/3);
    } else if(type == "boss2") {
      transparency = map(millis() - spawnTimer, 0, untouchable, 55, 255);
      if(!circleTouched) {
        pg.stroke(255, 255, 255, transparency);
        pg.fill(255-transparency, 255-transparency, 255-transparency);
      } else {
        pg.stroke(255);
        pg.fill(255);
      }
      // draws the spawnTimer ellipse
      pg.ellipse(0, 0, size, size);
      pg.stroke(0);
      pg.fill(0);
      pg.ellipse(0, size, size/3, size/3);
    } else if(type == "kamikaze") {
      pg.beginShape();
        pg.vertex(-0.5f * size,   -1 * size);
        pg.vertex(0          ,    1 * size);
        pg.vertex(0.5f * size ,   -1 * size);
        pg.vertex(0          , -0.3f * size);
        pg.vertex(-0.5f * size,   -1 * size);
      pg.endShape();
  }
    pg.popMatrix();
  }

  //// update enemy position
  public void update() {
    if(type == "kamikaze" && !circleTouched){
      //slowly turn towards the player
      a = turnTowardsPlayer(0.05f);
    }
    if(type == "boss2" && !circleTouched){
      //slowly turn towards the player
      a = turnTowardsPlayer(0.02f);
    }
    move = new PVector(0, vel);
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

  //// check if enemy is still inside bounds
  public boolean bounds() {
    if(type == "boss1"){
      // put boss back into the field if aura was not broken. Also, increase it's velocity.
      boolean bounded = false; // went against boundary?
      // left
      if(pos.x < 0-bossCFactor && !circleTouched){
        pos.x += 7.9f*bossCFactor;
        if(randomBool()) {
          a += PI + 0;
        } else {
          a += PI + 0;
        }
        bounded = true;
      //right
      } else if(pos.x > pgWidth+bossCFactor && !circleTouched){
        pos.x -= 7.9f*bossCFactor;
        if(randomBool()) {
          a += PI + 0;
        } else {
          a += PI + 0;
        }
        bounded = true;
      //top
      } else if(pos.y < 0-bossCFactor && !circleTouched) {
        pos.y += 7.9f*bossCFactor;
        if(randomBool()) {
          a += PI + 0;
        } else {
          a += PI + 0;
        }
        bounded = true;
      //bottom
      } else if(pos.y > pgHeight+bossCFactor && !circleTouched){
        pos.y -= 7.9f*bossCFactor;
        if(randomBool()) {
          a += PI + 0;
        } else {
          a += PI + 0;
        }
        bounded = true;
      //if one of the above and circleTouched
      } else if (bounded && !circleTouched) {
          if(circleTouched) return true;
          vel += 0.1f;
      }
      return false;
    } else if(type == "boss2"){
      return false;
    } else if( pos.x < 0-1.1f*(circleFactor+circleAdd) || pos.x > pgWidth+1.1f*(circleFactor+circleAdd)|| pos.y < 0-1.1f*(circleFactor+circleAdd) || pos.y > pgHeight+1.1f*(circleFactor+circleAdd) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemy
  public boolean collision() {
    // don't collide with boss if it just spawned
    if (type == "boss1"  || type == "boss2") {
      if(millis() - spawnTimer < 6000) return false;
      if(pos.dist(dodger.pos) <= (0.5f*size + dodger.size) ) {
        return true;
      } else {
        return false;
      }
    }
    if(pos.dist(dodger.pos) <= (0.5f*(size + dodger.size)) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemies aura
  public boolean circleCollision() {
    if (type == "boss1"  || type == "boss2") {
      if(millis() - spawnTimer < 6000) return false;
      pg.pushMatrix();
      pg.translate(pos.x, pos.y);
      // pg.ellipse(0, 0, (size+dodger.size), (size+dodger.size));
      pg.popMatrix();

      if(pos.dist(dodger.pos) <= size*bossCFactor) {
        return true;
      } else {
        return false;
      }
    }
    if(pos.dist(dodger.pos) <= (size*circleFactor + circleAdd/2 + dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }

  public float turnTowardsPlayer(float lerpFactor) {
    PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
    PVector pointToPlayer = PVector.fromAngle(nPos.heading() - HALF_PI);
    PVector direction = PVector.fromAngle(a);
    direction.lerp(pointToPlayer, lerpFactor);
    return direction.heading();
  }

}
  public void settings() {  fullScreen(P2D);  smooth(2); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dodger" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
