import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class dodger extends PApplet {

PShape logo;

float score;
int hiscore = 0;

// Dodger
Dodger dodger;
int startVel = 4; // beginning velocity of dodger, increases by scVel for every score
float scVel = 0.05f;
float rotVel; // rotation velocity of dodger
float rotAcc = 2; // rotation acceleration of dodger, increases by scAcc for every score
float scAcc = 0.04f;

// Enemy
int maxE = 40;
Enemy[] enemies = new Enemy[maxE];
int eNum;
int sActive = 8; // enemies active at start
int eActive; // enemies currently active
float limiter; // makes the arrow more narrow
float startEVel = 2; // beginning velocity of enemies, increases by scEVel for every score
float scEVel = 0.02f;
float scESize = 0.2f;

int circleFactor = 2;
int circleAdd = 220;
float shipChance;

boolean clockwise;
boolean gameOver;

public void setup() {
  
  
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
  limiter = 0.7f;
  eActive = sActive;
  shipChance = 0.15f; //starting chance for spawn to be ship, increases with score as well
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

public void draw() {
  if(!gameOver){
    rotAcc = 1.5f + score*scAcc;
    background(0, 0, 0, 20);
    textSize(30);
    fill(255);
    // text(int(score), 30, 30);
    //adjust amount of enemies according to score
    if(PApplet.parseInt(score/11 + sActive) > eActive && eActive < maxE) {
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
          enemies[eNum].vel *= 0.7f;
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



public void newEnemy() {
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

  startVel += 0.1f;
}

public void showScore() {
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
    hiscore = PApplet.parseInt(score);
  }
  // draw menu, score & high score
  text(PApplet.parseInt(score), width*2/4, height*3/4 -50);
  text(hiscore, width*2/4, height*1/4 -50);
  // wait for key input to start new game
}

public void keyPressed() { // listen for user input
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

  public void keyReleased() { // listen for user input
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
    size = 50 + score*scESize;
    size *= random(0.6f, 1.4f); // RNG for enemy size
    vel = _vel * random(0.8f, 1.2f) + score * scEVel;
    if(type == "asteroid"){
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = 40 + PApplet.parseInt(score/2);
      }
    }
    if(type == "ship"){
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 2.4f;
      hp = 20 + PApplet.parseInt(score/3);
    }
  }

  public void drawCircle() {
    pushMatrix();
    translate(pos.x, pos.y);
    if(!circleTouched) {
      noStroke();
      fill(255, 255, 255, 5 + hp);
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
      // line(-0.5 * size, -1 * size, 0, 1 * size);
      // line(0, 1 * size, 0.5 * size, -1 * size);
      // line(0.5 * size, -1 * size, 0, 0);
      // line(0, 0, -0.5 * size, -1 * size);
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
    if(pos.x < 0-0.1f*size*(circleFactor+circleAdd) || pos.x > width+0.1f*size*(circleFactor+circleAdd)
    || pos.y < 0-0.1f*size*(circleFactor+circleAdd) || pos.y > height+0.1f*size*(circleFactor+circleAdd) ) {
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
  public void settings() {  fullScreen();  smooth(1000); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dodger" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
