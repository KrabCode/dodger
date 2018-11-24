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
  void draw() {
    pg.rectMode(CENTER);
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    pg.rotate(a);
    // rect(0, 0, sin(a)*30, 50);
    pg.stroke(255);
    pg.strokeWeight(6);
    pg.line(-0.5 * size, -1 * size, 0, 1 * size);
    pg.line(0.5 * size, -1 * size, 0, 1 * size);
    pg.line(-0.4 * size, -0.6 * size, 0.4 * size, -0.6 * size); //back line
    pg.popMatrix();
  }

  //// update dodger position
  void update() {
    //dodger moves
    move = new PVector(0, vel + score*scVel); // velocity adjust
    if(!clockwise){
      a -= 0.001 * rotVel;
    } else {
      a += 0.001 * rotVel;
    }
    move = move.rotate(a);
    pos.add(move);
  }

  //// check if dodger is inside the boundaries
  void bounds() {
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
