//PShape logo;

float score;
int hiscore = 0;
boolean gameOver;

float changeVel = 1;                  // modifies all velocities

// Dodger
Dodger dodger;
float startVel;                       // beginning velocity of dodger, increases by scVel for every score
float scVel;
float rotVel;                         // rotation velocity of dodger
float rotAcc;                         // rotation acceleration of dodger, increases by scAcc for every score
float scAcc;
float rotDamp = 0.995;                // rotation velocity dampening
boolean clockwise;                    // is the player turning clockwise

// Enemy
int maxE = 20;
Enemy[] enemies = new Enemy[maxE];
int eNum;                             // index of current enemy
int sActive = 9;                      // enemies active at start
int eActive;                          // enemies currently active
float limiter;                        // makes the arrow more narrow
float startEVel;                      // beginning velocity of enemies, increases by scEVel for every score
float scEVel;
float startSize = 30;                 // beginning size of enemies, increases by scESize for every score
float scESize = 0.15;
float shipChance;                     // chance to spawn ship instead of asteroid
float kamiChance;                     // chance to spawn kamikaze, starts at 0 increases with score
boolean bossActive;                   // tells us if there is a boss on the field
float modifier;                       // used to modify some starting values

// Aura
int circleFactor = 2;                 // size of aura per enemy size
int circleAdd = 220;                  // added to size of aura
int circleTransparency = 20;
float bossCFactor = 1.5;                // boss has smaller circle and no add

void setup() {
  // setup screen
  // size(1000, 1000);
  fullScreen(P2D);
  orientation(PORTRAIT);
  frameRate(60);
  smooth(5);
  background(0);

  //logo = loadShape("logo.svg");
  //shapeMode(CORNERS);

  initGame(); // set up the variables for game initialisation
}

// set up the variables for game initialisation
void initGame() {
  gameOver = false;
  score = 0;

  // dodger attributes
  rotVel = 20;
  startVel = 3.1 * changeVel;
  scVel = 0.015 * changeVel;
  rotAcc = 1.8 * changeVel;
  scAcc = 0.01 * changeVel;
  dodger = new Dodger(width/2, height/2, 0);

  //enemy attributes
  startEVel = 3 * changeVel;
  scEVel = 0.01 * changeVel;
  limiter = 0.7;
  eActive = sActive;
  shipChance = 0.1; //starting chance for spawn to be ship, increases with score as well
  kamiChance = 0;
  bossActive = false;

  // generate new enemies
  for(eNum = 0; eNum < enemies.length; eNum++) {
    newEnemy();
  }
}

/////UP = SETUP//////////DOWN = UPDATE///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void draw() {
  if(!gameOver){
    runGame();
  } else {
    showScore();
  }
}

// perform a frame of the gameplay
void runGame() {
  background(0, 0, 0);
  textSize(30);
  fill(255);
  //adjust amount of enemies according to score
  if(int(score/25 + sActive) > eActive && eActive < maxE) {
    //eActive++;
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].update();
    if (enemies[eNum].bounds()) {
      newEnemy();
    }
    if(enemies[eNum].collision()){
      gameOver = true;
    }
    if(enemies[eNum].circleCollision()){
      enemies[eNum].hp--;
      if(enemies[eNum].circleTouched == false && enemies[eNum].hp < 0) {
        if(enemies[eNum].type == "boss1") {
          score += 5;
          bossActive = false;
        } else if(enemies[eNum].type == "kamikaze") {
          score += 2.5;
        } else if(enemies[eNum].type == "ship") {
          score += 1.5;
        } else {
          score++;
        }
        enemies[eNum].circleTouched = true;
        enemies[eNum].vel *= 0.8; //reduce enemy velocity when circle disappears
      }
    }
    enemies[eNum].drawCircle();
  }
  for(eNum = 0; eNum < eActive; eNum++){
    enemies[eNum].draw();
  }

  dodger.update();
  dodger.bounds();
  dodger.draw();
  rotAcc = (2 + score*scAcc) * changeVel; //increase the rotation velocity by rotation acceleration
  rotVel += rotAcc;
  rotVel *= rotDamp;
}

void newEnemy() {
  int border;
  border = (int) random(4);
  float type;
  type = random(1);
  String thisType;
  if(score > 5 && score % 60 <= 5 && !bossActive) {
    thisType = "boss1";
    bossActive = true;
    modifier = score;
  } else if(type > 1 -kamiChance -score/450) {
    thisType = "kamikaze";
  } else if(type > 1 -shipChance -score/450) {
    thisType = "ship";
  } else {
    thisType = "asteroid";
  }

  if(border == 0) {
    //left border
    enemies[eNum] = new Enemy(0-((10 + score*scESize)*circleFactor + circleAdd), random(height), random(PI + limiter, TWO_PI - limiter), startEVel, thisType);
  }
  if(border == 1) {
    //top border
    enemies[eNum] = new Enemy(random(width), 0-((10 + score*scESize)*circleFactor + circleAdd), random(HALF_PI + PI + limiter, 3*QUARTER_PI - limiter), startEVel, thisType);
  }
  if(border == 2) {
    //right border
    enemies[eNum] = new Enemy(width+((10 + score*scESize)*circleFactor + circleAdd), random(height), random(TWO_PI + limiter, TWO_PI + PI - limiter), startEVel, thisType);
  }
  if(border == 3) {
    //bottom border
    enemies[eNum] = new Enemy(random(height), height+((10 + score*scESize)*circleFactor + circleAdd), random(3*QUARTER_PI + limiter, HALF_PI + PI - limiter), startEVel, thisType);
  }
  modifier = 0;
  startVel += 0.1;
}

void showScore() {
  background(0);
  textSize(150);
  fill(255);
  stroke(255);
  strokeWeight(6);
  textAlign(CENTER, CENTER);
  // draw logo
  //int border = 15;
  //shape(logo, border, border+500, width-border, 750);
  //update score
  if (score > hiscore){
    hiscore = int(score);
  }
  // draw menu, score & high score
  text(hiscore, width*2/4, height*1/4 -50);
  text(int(score), width*2/4, height*3.3/4 -50);
  // wait for key input to start new game
}


///////////////INPUTS///////////////////////////////////////////////////////////////////////////////////////////////////////////////

void touchStarted() {
  if(gameOver){
    gameOver = !gameOver;
    initGame();
  } else {
    if(!clockwise){
      rotVel = 20;
    }
    clockwise = true;
    }
}

void touchEnded() {
  if(clockwise){
    rotVel = 20;
  }
  clockwise = false;
}
