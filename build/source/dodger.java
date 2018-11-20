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

int score;
int hiscore = 0;
Dodger dodger;

Enemy[] enemies = new Enemy[99];
int eNum;
int eActive;
float limiter; // makes the arrow more narrow
// Enemy enemy;
int startVel;

boolean clockwise;
int rotVel, rotAcc;
boolean gameOver;
boolean gameStart;

public void setup() {
  
  background(0);
  logo = loadShape("logo.svg");
  shapeMode(CORNERS);
  gameOver = false;
  gameStart = true;
  score = 0;
  // dodger attributes
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 20;
  rotAcc = 1;
  //enemy attributes
  startVel = 3;
  limiter = 0.5f; // makes the arrow more narrow
  eActive = 3;
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

public void draw() {
  if(!gameOver || gameStart){
    background(0, 0, 0, 20);
    dodger.update();
    dodger.bounds();
    dodger.draw();
    //adjust amount of enemies according to score
    if(PApplet.parseInt(score/200*pow(1.02f, eActive)) > eActive && eActive < 99) {
      eActive++;
    }
    for(eNum = 0; eNum < eActive; eNum++){
      enemies[eNum].update();
      if (enemies[eNum].bounds()) {
        score++;
        newEnemy();
      }
      if(enemies[eNum].collision()){
        gameOver = true;
      }
      enemies[eNum].draw();
    }
    rotVel += rotAcc;
  } else {
    showScore();
  }
}



public void newEnemy() {
  int border;
  border = (int) random(4);
  println(border);

  if(border == 0 || border == 1) {
    enemies[eNum] = new Enemy(0, random(height), random(PI + limiter, TWO_PI - limiter), startVel, "asteroid");
  }
  if(border == 2 || border == 3) {
    enemies[eNum] = new Enemy(width, random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startVel, "ship");
  }

  startVel += 0.1f;
}

public void showScore() {
  background(0);
  textSize(100);
  stroke(255);
  strokeWeight(6);
  textAlign(CENTER, CENTER);
  // draw logo
  int border = 15;
  shape(logo, border, border+120, width-border, 250);
  //update score
  if (score > hiscore){
    hiscore = score;
  }
  // draw menu, score & high score
  text(score, width*1/4, height*2/3 -50);
  text(hiscore, width*3/4, height*2/3 -50);
  // wait for key input to start new game
}

public void keyPressed() { // listen for user input
  if(gameOver || gameStart){
    gameOver = !gameOver;
    gameStart = false;
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
  float size = 25;
  float vel = 5;

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
    move = new PVector(0, vel);
    if(clockwise){
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
  String type;
  //ship, asteroid
  float a;
  float size = 18;
  float vel;
  float [] rndmAst = new float[16]; //random zahlen array fuer asteroid vertex

  Enemy (float _x, float _y, float _a, float _vel, String _type) {
    pos = new PVector(_x, _y);
    a = _a;
    type = _type;
    vel = _vel;
    for (int i=0; i < rndmAst.length; i++){
      rndmAst[i] = random(4, size);
    }
  }

  public void draw() {
    fill(0);
    rectMode(CENTER);
    ellipseMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    stroke(255);
    strokeWeight(6);
    if(type == "ship") {
      line(-0.5f * size, -1 * size, 0, 1 * size);
      line(0.5f * size, -1 * size, 0, 1 * size);
      line(-0.5f * size, -1 * size, 0, 0);
      line(0.5f * size, -1 * size, 0, 0);
      fill(255, 255, 255, 100);
      noStroke();
      ellipse(0, 0, size*15, size*15);
    } else if(type == "asteroid") {
      rotate(frameCount*0.01f);
          beginShape();
            vertex(0, -rndmAst[1]);//oben
            vertex(rndmAst[0], -12);
            vertex(rndmAst[2], 0);//rechts
            vertex(12, rndmAst[4]);
            vertex(0, rndmAst[3]);//unte
            vertex(-12, 12);
            vertex(-rndmAst[4], 0);//links
            vertex(-12, -12);
            vertex(0, -rndmAst[1]);//oben
          endShape();
          fill(255, 255, 255, 100);
          noStroke();
          ellipse(0, 0, size*15, size*15);
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
    if(pos.x < 0-size*2/3 || pos.x > width+size*2/3 || pos.y < 0-size*2/3 || pos.y > height+size*2/3) {
      return true;
    } else {
      return false;
    }
  }

  public boolean collision() {
    if(pos.dist(dodger.pos) <= (size+dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }
}
  public void settings() {  size(1200, 900); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dodger" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
