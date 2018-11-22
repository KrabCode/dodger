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
float rotDamp = 0.995f;                // rotation velocity dampening
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
float scESize = 0.15f;
float shipChance;                     // chance to spawn ship instead of asteroid

// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;

public void setup() {
  // setup screen
  // size(1000, 1000);
  
  orientation(PORTRAIT);
  frameRate(30);
  
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
public void initGame() {
  gameOver = false;
  score = 0;

  // dodger attributes
  rotVel = 20;
  startVel = 3.1f * changeVel;
  scVel = 0.015f * changeVel;
  rotAcc = 1.8f * changeVel;
  scAcc = 0.01f * changeVel;
  dodger = new Dodger(width/2, height/2, 0);

  //enemy attributes
  startEVel = 3 * changeVel;
  scEVel = 0.01f * changeVel;
  limiter = 0.7f;
  eActive = sActive;
  shipChance = 0.1f; //starting chance for spawn to be ship, increases with score as well

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

public void draw() {
  if(!gameOver){
    runGame();
  } else {
    showScore();
  }
}

// perform a frame of the gameplay
public void runGame() {
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
  if(PApplet.parseInt(score/25 + sActive) > eActive && eActive < maxE) {
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
          score += 1.5f;
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
        enemies[eNum].vel *= 0.8f; //reduce enemy velocity when circle disappears
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

public void newEnemy() {
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

  startVel += 0.1f;
}

public void showScore() {
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
    hiscore = PApplet.parseInt(score);
  }
  // draw menu, score & high score
  text(hiscore, width*2/4, height*1/4 -50);
  text(PApplet.parseInt(score), width*2/4, height*3.3f/4 -50);
  // wait for key input to start new game
}

public void keyPressed() { // listen for user input
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

public void touchStarted() {
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

public void keyReleased() { // listen for user input
  if(clockwise){
    sLeft.stop();
    sRight.stop();
    sLeft.trigger();
  }
  clockwise = false;
  rotVel = 20;
}

public void touchEnded() {
  if(clockwise){
    sLeft.stop();
    sRight.stop();
    sLeft.trigger();
  }
  clockwise = false;
  rotVel = 20;
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

  public void draw() {
    rectMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    stroke(255);
    strokeWeight(6);
    line(-0.5f * size, -1 * size, 0, 1 * size);
    line(0.5f * size, -1 * size, 0, 1 * size);
    line(-0.4f * size, -0.6f * size, 0.4f * size, -0.6f * size); //back line
    // line(0, 0, move.x, move.y);
    popMatrix();

  }

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

  public void bounds() {
    if(pos.x < 0+size*2/3) {
      pos.x = 0+size*2/3;
    } else if(pos.x > width-size*2/3) {
      pos.x = width-size*2/3;
    }
    if(pos.y < 0+size*2/3) {
      pos.y = 0+size*2/3;
    } else if(pos.y > height-size*2/3) {
      pos.y = height-size*2/3;
    }
  }
}
class Enemy {
  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  PVector nPos;
  String type;
  //ship, asteroid
  int hp; // health points of circle
  float a;
  float size;
  float vel;
  float [] rndmAst = new float[16]; //random zahlen array fuer asteroid vertex
  boolean circleTouched = false;

  Enemy (float _x, float _y, float _a, float _vel, String _type) {
    pos = new PVector(_x, _y);
    a = _a;
    type = _type;
    size = startSize + score*scESize;
    size *= random(0.6f, 1.4f); // RNG for enemy size
    vel = _vel * random(0.7f, 1.3f) + score * scEVel;
    if(type == "asteroid"){
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = PApplet.parseInt(50 / changeVel);
        //+ int(score/8);
      }
    }
    if(type == "ship"){
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 2;
      hp = PApplet.parseInt((25 + score/15) /changeVel);
    }
  }

  public void drawCircle() {
    pushMatrix();
    translate(pos.x, pos.y);
    if(!circleTouched) {
      noStroke();
      fill(255, 255, 255, circleTransparency + hp);
      ellipse(0, 0, 2*size*circleFactor + circleAdd, 2*size*circleFactor + circleAdd);
      noStroke();
      // //comment to remove lag
      // fill(255, 255, 255, min(255, 5 + 2*hp));
      // ellipse(0, 0, 0.3*(size*circleFactor + circleAdd), 0.3*(size*circleFactor + circleAdd));
    }
    popMatrix();
  }

  public void draw() {
    fill(0);
    rectMode(CENTER);
    ellipseMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    if(!circleTouched) {
      stroke(255);
      fill(0);
    } else {
      stroke(255);
      fill(255);
    }
    strokeWeight(3);
    if(type == "ship") {
      beginShape();
        vertex(-0.5f * size,   -1 * size);
        vertex(0          ,    1 * size);
        vertex(0.5f * size ,   -1 * size);
        vertex(0          , -0.3f * size);
        vertex(-0.5f * size,   -1 * size);
      endShape();
    } else if(type == "asteroid") {
      rotate(frameCount*0.01f);
          beginShape();
            vertex(0, -rndmAst[1]);
            vertex(rndmAst[2], 0);
            vertex(0, rndmAst[3]);
            vertex(-rndmAst[4], 0);
            vertex(-12, -12);
            vertex(0, -rndmAst[1]);
          endShape();
      }

    // line(0, 0, move.x, move.y);
    popMatrix();

  }

  public void update() {
    move = new PVector(0, vel);
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

  public boolean bounds() {
    if(pos.x < 0-2*(circleFactor+circleAdd) || pos.x > width+2*(circleFactor+circleAdd)
    || pos.y < 0-2*(circleFactor+circleAdd) || pos.y > height+2*(circleFactor+circleAdd) ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean collision() {
    if(pos.dist(dodger.pos) <= (0.5f*(size+dodger.size)) ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean circleCollision() {
    if(pos.dist(dodger.pos) <= (size*circleFactor + circleAdd/2 + dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }

}
  public void settings() {  fullScreen();  smooth(5); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dodger" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
