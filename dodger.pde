PShape logo;

int score;
int hiscore = 0;
Dodger dodger;

Enemy[] enemies = new Enemy[99];
int eNum;
int eActive;
float limiter; // makes the arrow more narrow
// Enemy enemy;
int startVel;

boolean clockwise;
int rotVel, rotAcc;
boolean gameOver;

void setup() {
  size(1200, 900);
  background(0);
  logo = loadShape("logo.svg");
  shapeMode(CORNERS);
  gameOver = false;
  score = 0;
  // dodger attributes
  dodger = new Dodger(width/2, height/2, 0);
  rotVel = 20;
  rotAcc = 2;
  //enemy attributes
  startVel = 3;
  limiter = 0.5; // makes the arrow more narrow
  eActive = 3;
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

void draw() {
  if(!gameOver){
    background(0, 0, 0, 20);
    dodger.update();
    dodger.bounds();
    dodger.draw();
    //adjust amount of enemies according to score
    if(int(score/200*pow(1.02, eActive)) > eActive && eActive < 99) {
      eActive++;
    }
    for(eNum = 0; eNum < eActive; eNum++){
      enemies[eNum].update();
      if (enemies[eNum].bounds()) {
        newEnemy();
      }
      if(enemies[eNum].collision()){
        gameOver = true;
      }
      enemies[eNum].draw();
    }
    score++;
  } else {
    showScore();
  }
}


void keyReleased() { // listen for user input
  rotVel = 20;
}

void newEnemy() {
  int border;
  border = (int) random(4);
  println(border);

  if(border == 0 || border == 1) {
    enemies[eNum] = new Enemy(0, random(height), random(PI + limiter, TWO_PI - limiter), startVel, "asteroid");
  }
  if(border == 2 || border == 3) {
    enemies[eNum] = new Enemy(width, random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startVel, "ship");
  }

  startVel += 0.1;
}

void showScore() {
  background(0);
  textSize(100);
  textAlign(CENTER, CENTER);
  // draw logo
  int border = 15;
  shape(logo, border, border+120, width-border, 250);
  //update score
  if (score > hiscore){
    hiscore = score;
  }
  // draw menu, score & high score
  text(score, width*1/4, height*2/3 -50);
  text(hiscore, width*3/4, height*2/3 -50);
  // wait for key input to start new game
}

void keyPressed() { // listen for user input
  if(gameOver){
    gameOver = !gameOver;
    setup();
  } else if (keyCode ==  LEFT) {
    clockwise = true;
  } else if (keyCode == RIGHT) {
    clockwise = false;
    }
    rotVel += rotAcc;
  }
