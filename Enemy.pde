class Enemy {
  //the dodger has x and y coordinates and an angle
  PVector pos;
  PVector move;
  PVector nPos;
  String type;
  //ship, kamikaze, asteroid
  int hp; // health points of circle
  float a;
  float size;
  float vel;
  float [] rndmAst = new float[16]; //random zahlen array fuer asteroid vertex
  boolean circleTouched = false;
  int spawnTimer = millis();

  //// construct the enemy
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
      vel *= 1.5;
      hp = int((30 + score/15) /changeVel);
    }
    if(type == "kamikaze"){
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      hp = int((25 + score/8) /changeVel);
    }
    if(type == "boss1"){
      size *= 2;
      size += modifier;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = int(400+modifier / changeVel);
        //+ int(score/8);
      }
    }
  }

  //// draw the aura of the enemy
  void drawCircle() {
    pushMatrix();
    translate(pos.x, pos.y);
    if(!circleTouched) {
      noStroke();
      if(type == "boss1") {
        fill(255, 255, 255, circleTransparency + hp/8);
        ellipse(0, 0, 2*size*bossCFactor, 2*size*bossCFactor);
        fill(0);
        ellipse(0, 0, (size+dodger.size), (size+dodger.size));
      } else {
        fill(255, 255, 255, circleTransparency + hp);
        ellipse(0, 0, 2*size*circleFactor + circleAdd, 2*size*circleFactor + circleAdd);
      }
      noStroke();
    }
    popMatrix();
  }

  //// draw the enemy
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
        vertex(-1 * size,   -1 * size);
        vertex(0          ,    1 * size);
        vertex(0.5 * size ,   -1 * size);
        vertex(0          , -0.3 * size);
        vertex(-1 * size,   -1 * size);
      endShape();
    } else if(type == "asteroid" || type == "boss1") {
      rotate(frameCount*0.01);
          beginShape();
            vertex(0, -rndmAst[1]);
            vertex(rndmAst[2], 0);
            vertex(0, rndmAst[3]);
            vertex(-rndmAst[4], 0);
            vertex(-12, -12);
            vertex(0, -rndmAst[1]);
          endShape();
      } else if(type == "kamikaze") {
        beginShape();
          vertex(-0.5 * size,   -1 * size);
          vertex(0          ,    1 * size);
          vertex(0.5 * size ,   -1 * size);
          vertex(0          , -0.3 * size);
          vertex(-0.5 * size,   -1 * size);
        endShape();
      }
    popMatrix();
  }

  //// update enemy position
  void update() {
    if(type == "kamikaze" && !circleTouched){
      //slowly turn towards the player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      PVector pointToPlayer = PVector.fromAngle(nPos.heading() - HALF_PI);
      PVector direction = PVector.fromAngle(a);
      direction.lerp(pointToPlayer, 0.05);
      a = direction.heading();
    }
    move = new PVector(0, vel);
    move = move.rotate(a);
    pos.add(move);
    //dodger moves
  }

  //// check if enemy is still inside bounds
  boolean bounds() {
    if(type == "boss1"){
    // put boss back into the field if aura was not broken. Also, increase it's velocity.
      if(pos.x < 0-bossCFactor && !circleTouched){
        pos.x += width + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.x > width+bossCFactor && !circleTouched){
        pos.x -= width + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.y < 0-bossCFactor && !circleTouched){
        pos.y += height + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.y > height+bossCFactor && !circleTouched){
        pos.y -= height + 7.9*bossCFactor;
        vel *= 1.02;
      } else if ( //if one of the above and circleTouched
        (pos.x < 0-3*bossCFactor) || (pos.x > width+3*bossCFactor && !circleTouched)
        || (pos.y < 0-3*bossCFactor) || (pos.y > height+3*bossCFactor && !circleTouched)) {
        if(circleTouched) return true;
      }
      return false;
    } else if(pos.x < 0-1.1*(circleFactor+circleAdd) || pos.x > width+1.1*(circleFactor+circleAdd)
           || pos.y < 0-1.1*(circleFactor+circleAdd) || pos.y > height+1.1*(circleFactor+circleAdd) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemy
  boolean collision() {
    if(pos.dist(dodger.pos) <= (0.5*(size+dodger.size)) ) {
      return true;
    } else {
      return false;
    }
  }

  //// check if dodger collides with the enemies aura
  boolean circleCollision() {
    if(pos.dist(dodger.pos) <= (size*circleFactor + circleAdd/2 + dodger.size) ) {
      return true;
    } else {
      return false;
    }
  }

}
