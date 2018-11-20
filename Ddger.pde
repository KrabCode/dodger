class Dodger {

  //the dodger has x and y coordinates and an angle
  PVector pos;

  float a;
  float size = 25;

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
    popMatrix();
    a += 0.01;
  }

  void update() {
    //dodger moves
  }

}
