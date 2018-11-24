## TODO
**GRAPHICS**

* make proper graphics for kamikaze and boss1
* create graphics for future enemies

**MENU**

* make a menu that is more than a splashscreen
* toggle music and fx on/off
* show highscore
* arrow key and mouse/touch input

**GAME LOGIC**

* persistent highscores with loadTable or loadStrings

* slow enemies down while harvesting their aura instead of after aura was harvested for that sucking-their-energy kinda feel
* make it possible that aura can shrink when harvested

**DIAGNOSTICS**

* make a line on top of screen that displays current velocities
* make the diagnostics togglable with a key
* include some hotkeys to modify values (score, velocities for balancing)

**BALANCE**

  * balance the gameplay more (increase beginning difficulty, slow down the difficulty ramp (currently 100+ is way to hard))
  * add different difficulties with diff parameters (three different init functions?)

**ENEMIES**

  * make boss2: many hp, slow but accelerating, follows player like kamikaze
  * enemy type that follows you for a bit after death
  * imagine a better logic for when the bosses appear

**SOUND**

  * find a longer sound to paulstretch a nicer background soundscape
  * improve the steering wobble sound
  * have higher variety of aura 'pop' sounds

**RANDOM IDEAS FOR LATER**

* tutorial
  * make a tutorial
  * begin with just auras, gradually introduce enemies (not all types needed)
  * death just respawns

* two player modes: 
  * **survival**: no PVP collisions, last dodger standing
  * **score**: first player to score X points wins (collision reduces score by 50%)
  * **slowdown**: score reduces velocity, collision reduces score, on PVP collision the player with higher score wins