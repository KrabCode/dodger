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

int score, hiscore;
Dodger dodger;
Enemy enemy;
boolean clockwise;
int rotVel;
boolean gameOver;

public void setup() {
  
  background(0);
  gameOver = false;
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 4;
  newEnemy();
}

public void draw() {
  if(!gameOver){
    background(0);
    dodger.update();
    dodger.bounds();
    dodger.draw();
    enemy.update();
    if (enemy.bounds()) {
      newEnemy();
    }
    println(enemy.collision());
    if(enemy.collision()){
      gameOver = true;
    }
    enemy.draw();
    score++;
  } else {
    showScore();
  }
}

public void keyPressed() { //listen for user input
  if (keyCode ==  LEFT) {
    clockwise = true;
  } else if (keyCode == RIGHT) {
    clockwise = false;
  }
  rotVel++;
}

public void keyReleased() { //listen for user input
  rotVel = 20;
}

public void newEnemy() {
  enemy = new Enemy(0, height/2, random(PI, TWO_PI), "asteroid");
}

public void showScore() {
  // draw score & high score
  //wait for key input to start new game
  background(0);
  textSize(100);
  textAlign(CENTER, CENTER);
  text(score, width/2, height/2 -50);
}
class Dodger {

  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  float a;
  float size = 25;
  float vel = 2;

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
    println(a);
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
  float vel = 0.8f;

  Enemy (float _x, float _y, float _a, String _type) {
    pos = new PVector(_x, _y);
    a = _a;
    type = _type;
  }

  public void draw() {
    rectMode(CENTER);
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
    } else if(type == "asteroid") {
      ellipse(0, 0, size*2, size*2);
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
  public void settings() {  size(800,600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "dodger" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
