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

  void draw() {
    rectMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    stroke(255);
    strokeWeight(6);
    line(-0.5 * size, -1 * size, 0, 1 * size);
    line(0.5 * size, -1 * size, 0, 1 * size);
    line(-0.4 * size, -0.6 * size, 0.4 * size, -0.6 * size); //back line
    // line(0, 0, move.x, move.y);
    popMatrix();

  }

  void update() {
    move = new PVector(0, vel);
    if(clockwise){
      a -= 0.02;
      println("plus");
    } else {
      a += 0.02;
      println("minus");
    }
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

void bounds() {
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
