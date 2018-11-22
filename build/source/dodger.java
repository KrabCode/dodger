import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class dodger extends PApplet {



SoundFile pop, sLeft, sRight;
SoundFile snap0, snap1, snap2;
SoundFile bg, gameover;
float bgPosition; //saves the background sound position so that playback can resume at fixed position
boolean gameOverSoundPlayed;

PShape logo;

float score;
int hiscore = 0;

// Dodger
Dodger dodger;
int startVel = 5;                     // beginning velocity of dodger, increases by scVel for every score
float scVel = 0.03f;
float rotVel;                         // rotation velocity of dodger
float rotAcc = 2;                     // rotation acceleration of dodger, increases by scAcc for every score
float scAcc = 0.03f;
boolean clockwise;                    // is the player turning clockwise
boolean gameOver;


// Enemy
int maxE = 40;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 9;                      // enemies active at start
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel = 3;                  // beginning velocity of enemies, increases by scEVel for every score
float scEVel = 0.015f;
float startSize = 50;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.15f;
float shipChance;                     // chance to spawn ship instead of asteroid

// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;

public void setup() {
  // size(1000, 1000);
  
  frameRate(30);
  orientation(PORTRAIT);
  // fullScreen(P2D, SPAN);
  
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
  bg = new SoundFile(this, "bg^^.wav");
  //bg.rate(0.6);
  gameover = new SoundFile(this, "gameover.wav");
  gameover.amp(0.6f);

  //logo = loadShape("logo.svg");
  shapeMode(CORNERS);
  gameOver = false;
  score = 0;
  // dodger attributes
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 20;

  //enemy attributes
  limiter = 0.7f;
  eActive = sActive;
  shipChance = 0.15f; //starting chance for spawn to be ship, increases with score as well
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

public void runGame() {
  if(!bg.isPlaying()) {
     bg.play();
  }
  rotAcc = 1.5f + score*scAcc; //increase the rotation velocity by rotation acceleration
  background(0, 0, 0);
  textSize(30);
  fill(255);
  //adjust amount of enemies according to score
  if(PApplet.parseInt(score/25 + sActive) > eActive && eActive < maxE) {
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
          score += 1.5f;
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
  rotVel += rotAcc;
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
    setup();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      //dryKick2.stop();
      sRight.play();
    }
    clockwise = true;
    }
  }

public void touchStarted() {
  if(gameOver){
    gameOver = !gameOver;
    setup();
  } else {
    if(!clockwise){
      rotVel = 20;
      sRight.stop();
      //dryKick2.stop();
      sRight.play();
    }
    clockwise = true;
    }
}

public void keyReleased() { // listen for user input
  if(clockwise){
    //dryKick1.stop();
    sLeft.stop();
    sLeft.play();
  }
  clockwise = false;
  rotVel = 20;
}

public void touchEnded() {
  if(clockwise){
    //dryKick1.stop();
    sLeft.stop();
    sLeft.play();
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
        hp = 50;
        //+ int(score/8);
      }
    }
    if(type == "ship"){
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 2;
      hp = 25 + PApplet.parseInt(score/15);
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
