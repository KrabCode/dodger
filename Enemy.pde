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
    size *= random(0.6, 1.4); // RNG for enemy size
    vel = _vel * random(0.7, 1.3) + score * scEVel;
    if(type == "asteroid"){
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = int(50 / changeVel);
        //+ int(score/8);
      }
    }
    if(type == "ship"){
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 2;
      hp = int((25 + score/15) /changeVel);
    }
  }

  void drawCircle() {
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

  void draw() {
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
        vertex(-0.5 * size,   -1 * size);
        vertex(0          ,    1 * size);
        vertex(0.5 * size ,   -1 * size);
        vertex(0          , -0.3 * size);
        vertex(-0.5 * size,   -1 * size);
      endShape();
    } else if(type == "asteroid") {
      rotate(frameCount*0.01);
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

  void update() {
    move = new PVector(0, vel);
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

  boolean bounds() {
    if(pos.x < 0-2*(circleFactor+circleAdd) || pos.x > width+2*(circleFactor+circleAdd)
    || pos.y < 0-2*(circleFactor+circleAdd) || pos.y > height+2*(circleFactor+circleAdd) ) {
      return true;
    } else {
      return false;
    }
  }

  boolean collision() {
    if(pos.dist(dodger.pos) <= (0.5*(size+dodger.size)) ) {
      return true;
    } else {
      return false;
    }
  }

  boolean circleCollision() {
    if(pos.dist(dodger.pos) <= (size*circleFactor + circleAdd/2 + dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }

}
