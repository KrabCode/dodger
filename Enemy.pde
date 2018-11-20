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

  void draw() {
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
      line(-0.5 * size, -1 * size, 0, 1 * size);
      line(0.5 * size, -1 * size, 0, 1 * size);
      line(-0.5 * size, -1 * size, 0, 0);
      line(0.5 * size, -1 * size, 0, 0);
      fill(255, 255, 255, 100);
      noStroke();
      ellipse(0, 0, size*15, size*15);
    } else if(type == "asteroid") {
      rotate(frameCount*0.01);
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
