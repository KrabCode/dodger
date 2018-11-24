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
    if(type == "asteroid"){
      size = startSize + score*scESize;
      size *= random(0.7, 1.4); // RNG for enemy size
      vel = _vel * random(0.7, 1.3) + score * scEVel;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = int(50 / changeVel);
        //+ int(score/8);
      }
    }
    if(type == "ship"){
      size = startSize + score*scESize;
      size *= random(0.6, 1.2); // RNG for enemy size
      vel = _vel * random(0.8, 1.2) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      vel *= 1.5;
      hp = int((30 + score/15) /changeVel);
    }
    if(type == "kamikaze"){
      size = startSize + score*scESize;
      size *= random(0.6, 1.2); // RNG for enemy size
      vel = _vel * random(0.8, 1.2) + score * scEVel;
      //set angle to player
      PVector nPos = new PVector(-pos.x + dodger.pos.x, -pos.y + dodger.pos.y);
      a = nPos.heading() - HALF_PI;
      hp = int((25 + score/8) /changeVel);
    }
    if(type == "boss1"){
      size = (startSize + score*scESize) *2 + modifier;
      size *= random(0.9, 1.1); // RNG for enemy size
      vel = _vel * random(0.9, 1.1) + score * scEVel;
      for (int i=0; i < rndmAst.length; i++){
        rndmAst[i] = random(4, size);
        hp = int(400+modifier / changeVel);
        //+ int(score/8);
      }
    }
  }

  //// draw the aura of the enemy
  void drawCircle() {
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    if(!circleTouched) {
      pg.noStroke();
      if(type == "boss1") {
        pg.fill(255, 255, 255, circleTransparency + hp/8);
        pg.ellipse(0, 0, 2*size*bossCFactor, 2*size*bossCFactor);
        pg.fill(0);
        pg.ellipse(0, 0, (size+dodger.size), (size+dodger.size));
      } else {
        pg.fill(255, 255, 255, circleTransparency + hp);
        pg.ellipse(0, 0, 2*size*circleFactor + circleAdd, 2*size*circleFactor + circleAdd);
      }
      pg.noStroke();
    }
    pg.popMatrix();
  }

  //// draw the enemy
  void draw() {
    pg.fill(0);
    pg.rectMode(CENTER);
    pg.ellipseMode(CENTER);
    pg.pushMatrix();
    pg.translate(pos.x, pos.y);
    pg.rotate(a);
    if(!circleTouched) {
      pg.stroke(255);
      pg.fill(0);
    } else {
      pg.stroke(255);
      pg.fill(255);
    }
    pg.strokeWeight(3);
    if(type == "ship") {
      pg.beginShape();
        pg.vertex(-1 * size,   -1 * size);
        pg.vertex(0          ,    1 * size);
        pg.vertex(0.5 * size ,   -1 * size);
        pg.vertex(0          , -0.3 * size);
        pg.vertex(-1 * size,   -1 * size);
      pg.endShape();
    } else if(type == "asteroid" || type == "boss1") {
      pg.rotate(frameCount*0.01);
          pg.beginShape();
            pg.vertex(0, -rndmAst[1]);
            pg.vertex(rndmAst[2], 0);
            pg.vertex(0, rndmAst[3]);
            pg.vertex(-rndmAst[4], 0);
            pg.vertex(-12, -12);
            pg.vertex(0, -rndmAst[1]);
          pg.endShape();
      } else if(type == "kamikaze") {
        pg.beginShape();
          pg.vertex(-0.5 * size,   -1 * size);
          pg.vertex(0          ,    1 * size);
          pg.vertex(0.5 * size ,   -1 * size);
          pg.vertex(0          , -0.3 * size);
          pg.vertex(-0.5 * size,   -1 * size);
        pg.endShape();
      }
    pg.popMatrix();
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
        pos.x += pgWidth + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.x > pgWidth+bossCFactor && !circleTouched){
        pos.x -= pgWidth + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.y < 0-bossCFactor && !circleTouched){
        pos.y += pgHeight + 7.9*bossCFactor;
        vel *= 1.02;
      } else if(pos.y > pgHeight+bossCFactor && !circleTouched){
        pos.y -= pgHeight + 7.9*bossCFactor;
        vel *= 1.02;
      } else if ( //if one of the above and circleTouched
        (pos.x < 0-3*bossCFactor) || (pos.x > pgWidth+3*bossCFactor && !circleTouched)
        || (pos.y < 0-3*bossCFactor) || (pos.y > pgHeight+3*bossCFactor && !circleTouched)) {
        if(circleTouched) return true;
      }
      return false;
    } else if(pos.x < 0-1.1*(circleFactor+circleAdd) || pos.x > pgWidth+1.1*(circleFactor+circleAdd)
           || pos.y < 0-1.1*(circleFactor+circleAdd) || pos.y > pgHeight+1.1*(circleFactor+circleAdd) ) {
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
