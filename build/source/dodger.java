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

//prepare scaling screen to fixed resolution
PGraphics pg;
int pgWidth = 640;
// 1920;
int pgHeight = 480;
//1080;
PGraphics pgfeedback;
float feedbackLevel = 0.9f;

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
float rotDamp = 0.995f;                // rotation velocity dampening
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
float scESize = 0.15f;
float shipChance;                     // chance to spawn ship instead of asteroid
float kamiChance;                     // chance to spawn kamikaze, starts at 0 increases with score
boolean bossActive;                   // tells us if there is a boss on the field
float modifier;                       // used to modify some starting values

/// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
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
  bg = minim.loadFile("bg.wav", bufferSize);
  gameover = minim.loadSample("gameover.wav", bufferSize);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  initGame(); // set up the variables for game initialisation
}

//// set up the variables for game initialisation
public void initGame() {
  gameOver = false;
  score = 40;

  // dodger attributes
  rotVel = 20;
  startVel = 3.1f * changeVel;
  scVel = 0.015f * changeVel;
  rotAcc = 1.6f * changeVel;
  scAcc = 0.007f * changeVel;
  dodger = new Dodger(pgWidth/2, height/2, 0);

  //enemy attributes
  startEVel = 3 * changeVel;
  scEVel = 0.01f * changeVel;
  limiter = 0.7f;
  eActive = sActive;
  shipChance = 0.1f; //starting chance for spawn to be ship, increases with score as well
  kamiChance = 0;
  bossActive = false;

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

/////UP = SETUP//////////DOWN = UPDATE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//// draw function with gamestates
public void draw() {
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
public void runGame() {
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
  if(PApplet.parseInt(score/25 + sActive) > eActive && eActive < maxE) {
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
        enemies[eNum].vel *= 0.6f;         // reduce enemy velocity when circle disappears
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
public void newEnemy() {
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
public void showScore() {
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
    hiscore = PApplet.parseInt(score);
  }
  // draw menu, score & high score
  pg.text(hiscore, pgWidth*2/4, pgHeight*1/4 -50);
  pg.text(PApplet.parseInt(score), pgWidth*2/4, pgHeight*3.3f/4 -50);

  // wait for key input to start new game
}

///////////////GRAPHICS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Feedback Functions

public void feedbackCapture(PGraphics output) {
  pg.loadPixels();
  output.loadPixels();

  arrayCopy(pg.pixels, output.pixels);

  output.updatePixels();
}

///////////////INPUTS////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public void keyPressed() { // listen for user input // touchStarted
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

public void keyReleased() { // listen for user input // touchEnded
  if(clockwise){
    sLeft.trigger();
    rotVel = 20;
  }
  clockwise = false;
}
class Dodger {

  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  float a;
  float size = 28;
  float vel = startVel;

  Dodger (float _x, float _y, float _a) {
    pos = new PVector(_x, _y);
    a = _a;
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
        rndmAst[i] = random(4, size);
        hp = PApplet.parseInt(50 / changeVel);
        //+ int(score/8);
      }
    }
    if(type == "ship"){
      size = startSize + score*scESize;
      size *= random(0.6f, 1.2f); // RNG for enemy size
      vel = _vel * random(0.8f, 1.2f) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 1.5f;
      hp = PApplet.parseInt((30 + score/15) /changeVel);
    }
    if(type == "kamikaze"){
      size = startSize + score*scESize;
      size *= random(0.6f, 1.2f); // RNG for enemy size
      vel = _vel * random(0.8f, 1.2f) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      hp = PApplet.parseInt((25 + score/8) /changeVel);
    }
    if(type == "boss1"){
      size = (startSize + score*scESize) *2 + modifier;
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
      if(type == "boss1") {
        pg.fill(255, 255, 255, circleTransparency + hp/8);
        pg.ellipse(0, 0, 2*size*bossCFactor, 2*size*bossCFactor);
        pg.fill(0);
        pg.ellipse(0, 0, (size+dodger.size), (size+dodger.size));
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
    } else if(type == "asteroid" || type == "boss1") {
      pg.rotate(frameCount*0.01f);
          pg.beginShape();
            pg.vertex(0, -rndmAst[1]);
            pg.vertex(rndmAst[2], 0);
            pg.vertex(0, rndmAst[3]);
            pg.vertex(-rndmAst[4], 0);
            pg.vertex(-12, -12);
            pg.vertex(0, -rndmAst[1]);
          pg.endShape();
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
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      PVector pointToPlayer = PVector.fromAngle(nPos.heading() - HALF_PI);
      PVector direction = PVector.fromAngle(a);
      direction.lerp(pointToPlayer, 0.05f);
      a = direction.heading();
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
      if(pos.x < 0-bossCFactor && !circleTouched){
        pos.x += pgWidth + 7.9f*bossCFactor;
        vel *= 1.02f;
      } else if(pos.x > pgWidth+bossCFactor && !circleTouched){
        pos.x -= pgWidth + 7.9f*bossCFactor;
        vel *= 1.02f;
      } else if(pos.y < 0-bossCFactor && !circleTouched){
        pos.y += pgHeight + 7.9f*bossCFactor;
        vel *= 1.02f;
      } else if(pos.y > pgHeight+bossCFactor && !circleTouched){
        pos.y -= pgHeight + 7.9f*bossCFactor;
        vel *= 1.02f;
      } else if ( //if one of the above and circleTouched
        (pos.x < 0-3*bossCFactor) || (pos.x > pgWidth+3*bossCFactor && !circleTouched)
        || (pos.y < 0-3*bossCFactor) || (pos.y > pgHeight+3*bossCFactor && !circleTouched)) {
        if(circleTouched) return true;
      }
      return false;
    } else if(pos.x < 0-1.1f*(circleFactor+circleAdd) || pos.x > pgWidth+1.1f*(circleFactor+circleAdd)
           || pos.y < 0-1.1f*(circleFactor+circleAdd) || pos.y > pgHeight+1.1f*(circleFactor+circleAdd) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemy
  public boolean collision() {
    if(pos.dist(dodger.pos) <= (0.5f*(size+dodger.size)) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemies aura
  public boolean circleCollision() {
    if(pos.dist(dodger.pos) <= (size*circleFactor + circleAdd/2 + dodger.size) ) {
      return true;
    } else {
      return false;
    }
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
