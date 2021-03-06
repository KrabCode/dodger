class Dodger {

  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  float a;
  float size = dodgerSize;
  float vel = startVel;

  Dodger (float _x, float _y, float _a) {
    pos = new PVector(_x, _y);
    a = _a;
  }

  //// draw the aura of the enemy
  void drawCircle() {
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);

    // float auraSize = 1 + map(score, 0, highScore + 10, 0, 2);
    int scale = 20;

    pg.noStroke();
    pg.fill(255, 255, 255, 10);
    pg.ellipse(0, 0, 2*size*(score%  9/  9 * scale/4), 2*size*(score%  9/  9 * scale/4) );
    pg.ellipse(0, 0, 2*size*(score% (9*9)/ (9*9) * scale/3), 2*size*(score% (9*9)/ (9*9) * scale/3) );

    pg.stroke(255, 255, 255, 100);
    pg.strokeWeight(4);
    pg.ellipse(0, 0, 2*size*(score%(9*9*9)/(9*9*9) * scale/2), 2*size*(score%(9*9*9)/(9*9*9) * scale/2) );

    pg.noStroke();
    pg.fill(0);
    pg.ellipse(0, 0, size, size);

    pg.noStroke();
    pg.popMatrix();
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
