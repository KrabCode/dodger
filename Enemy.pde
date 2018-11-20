class Enemy {

  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  String type;
  //ship, asteroid
  float a;
  float size = 18;
  float vel = 0.8;

  Enemy (float _x, float _y, float _a, String _type) {
    pos = new PVector(_x, _y);
    a = _a;
    type = _type;
  }

  void draw() {
    rectMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    stroke(255);
    strokeWeight(6);
    if(type == "ship") {
      line(-0.5 * size, -1 * size, 0, 1 * size);
      line(0.5 * size, -1 * size, 0, 1 * size);
      line(-0.5 * size, -1 * size, 0, 0);
      line(0.5 * size, -1 * size, 0, 0);
    } else if(type == "asteroid") {
      ellipse(0, 0, size*2, size*2);
    }
    // line(0, 0, move.x, move.y);
    popMatrix();

  }

  void update() {
    move = new PVector(0, vel);
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

  boolean bounds() {
    if(pos.x < 0-size*2/3 || pos.x > width+size*2/3 || pos.y < 0-size*2/3 || pos.y > height+size*2/3) {
      return true;
    } else {
      return false;
    }
  }

  boolean collision() {
    if(pos.dist(dodger.pos) <= (size+dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }
}
