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
boolean clockwise;

public void setup() {
  
  background(0);
  score = 0;
  hiscore = 0;
  dodger = new Dodger(width/2, height/2, 0);
}

public void draw() {
  background(0);
  dodger.update();
  dodger.draw();
}

public void keyPressed() { //listen for user input
  if (keyCode ==  LEFT) {
    clockwise = true;
  } else if (keyCode == RIGHT) {
    clockwise = false;
  }
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
    move = new PVector(0, vel);
    if(clockwise){
      a += 0.02f;
      println("plus");
    } else {
      a -= 0.02f;
      println("minus");
    }
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
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
