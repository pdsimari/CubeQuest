import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import org.lwjgl.util.WaveData;
import org.lwjgl.opengl.Display;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * CSC 322: Introduction to Computer Graphics, Fall 2016
 * Jan Lasota
 * James Conroy
 * Ciaran Cordial
 * Patricio Simari, PhD
 * Michael Monaghan
 * Tan Tran
 * Michael Hassoun
 * Shay Mento
 * Huong Pham
 * Pasham Adwani
 * Electrical Engineering and Computer Science
 * The Catholic University of America
 */
// =============================================================================
public class CubeQuest {
// =============================================================================

	/**
	 * Locked out constructor; this class is static.
	 */
	private CubeQuest() { }

	// =========================================================================
	// PLAYER
	// =========================================================================

	/**
	 * Direction player is currently facing.
	 */
	enum Direction {NORTH, SOUTH, EAST, WEST}

	/**
	 * Player speed (distance per second).
	 */
	static float PLAYER_SPEED = 100f;

	/**
	 * Player's shot speed (distance per second).
	 */
	static final float PLAYER_SHOT_SPEED = 30.0f;

	/**
	 * Player's shot damage (health decrease to enemy on collision).
	 */
	static final float PLAYER_SHOT_DAMAGE = 1.0f;

	/**
	 * Player's shot size.
	 */
	static final float PLAYER_SHOT_SIZE = 0.1f;

	/**
	 * Player's shot duration in seconds.
	 */
	static final float PLAYER_SHOT_DURATION = 1.0f;

	/**
	 * Maximum number of active shots player may have.
	 */
	static final int PLAYER_SHOT_MAX = 4;

	/**
	 * Enforced delay between consecutive player shots (in seconds).
	 */
	static final float PLAYER_SHOT_DELAY = PLAYER_SHOT_DURATION/
			PLAYER_SHOT_MAX;


//----------------------------------------------------------------------------------------------------------------------
	/**
	 * Player Model:
	 */
	private static void walle()
	{
		//head
		glEnable(GL_LIGHTING);
		glShadeModel(GL_SMOOTH);
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(0.0f,0.0f,0.0f);
			glScalef(1.8f,2.0f,1.4f);
			plotUnitHemisphere(20);
		}
		glPopMatrix();
		//face
		glColor3f(0.0f,0.0f,0.4f);
		glPushMatrix();
		{
			glTranslatef(0.0f,0.7f,1.1f);
			glScalef(1.0f,0.8f,0.4f);
			plotUnitSphere(10);
		}
		glPopMatrix();
		//neck
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(0.0f,0.0f,0.0f);
			glScalef(1.8f,1.0f,1.4f);
			plotUnitSphere(20);
		}
		glPopMatrix();
		//eyes1
		glColor3f(0.0f,0.5f,1.0f);
		glPushMatrix();
		{
			glTranslatef(0.45f,0.8f,1.3f);
			glScalef(0.2f,0.2f,0.2f);
			plotUnitSphere(20);
		}
		glPopMatrix();
		//eyes2
		glColor3f(0.0f,0.5f,1.0f);
		glPushMatrix();
		{
			glTranslatef(-0.45f,0.8f,+1.3f);
			glScalef(0.2f,0.2f,0.2f);
			plotUnitSphere(20);
		}
		glPopMatrix();
		//shoulder
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(0.0f,-2.0f,0.0f);
			glScalef(2.5f,0.8f,1.6f);
			plotUnitSphere(20);
		}
		glPopMatrix();
		//body
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(0.0f,-2.0f,0.0f);
			glRotatef(180.0f,1.0f,0.0f,0.0f);
			glScalef(2.5f,5.0f,1.6f);
			plotUnitHemisphere(20);
		}
		glPopMatrix();
		//Hand-R
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(+3.0f,-4.0f,0.0f);
			glRotatef(90.0f,1.0f,0.0f,0.0f);
			glScalef(0.3f,0.8f,1.6f);
			plotUnitSphere(10);
		}
		glPopMatrix();
		//Hand-L
		glColor3f(0.9f,0.95f,0.9f);
		glPushMatrix();
		{
			glTranslatef(-3.0f,-4.0f,0.0f);
			glRotatef(90.0f,1.0f,0.0f,0.0f);
			glScalef(0.3f,0.8f,1.6f);
			plotUnitSphere(10);
		}
		glPopMatrix();
	}
//-------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------


	/**
	 * Plot a regular polygon of n sides lying on the XZ plane and having vertices that are a distance of 1 from the
	 * origin.
	 *
	 * @param n An int.
	 */
	private static void plotUnitPolygon(int n) {

		final float inc = (float) ((Math.PI*2.0d)/n);
		float[] p = new float[3];
		glNormal3f(0.0f, 1.0f, 0.0f);
		glBegin(GL_POLYGON);
		{
			// generate n vertices
			float ang = 0.0f;
			for (int i = 0; i < n; i++) {
				setSpherical(ang, 0.0f, 1.0f, p);
				glVertex3f(p[0], p[1], p[2]);
				ang += inc;
			}
		}
		glEnd();

	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Plot a unit sphere with n bands of azimuth and n/2 bands of elevation.
	 *
	 * @param n Number of azimuth bands.
	 */
	private static void plotUnitSphere(int n) {

		// p->q will represent the current edge we are on
		float[] p = new float[3];
		float[] q = new float[3];

		float theta, phi;

		// north pole cap
		glBegin(GL_TRIANGLES);
		{
			phi = TURN/4 - TURN/n;
			setSpherical(0.0f,  phi, 1.0f, q);
			for (int i = 1; i <= n; i++) {

				// set up edge
				theta = (TURN*i)/n;
				set(q, p);
				setSpherical(theta, phi, 1.0f, q);

				// plot triangle
				glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
				glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
				glNormal3f(0.0f, 1.0f, 0.0f); glVertex3f(0.0f, 1.0f, 0.0f);

			}

		}
		glEnd();

		// middle bands
		glBegin(GL_QUADS);
		{

			float[] r = new float[3];
			float[] s = new float[3];
			for (int i = 2; i < (n/2); i++) {
				for (int j = 0; j < n; j++) {

					// update theta phi
					phi = TURN/4 - (TURN*i)/n;
					theta = (TURN*j)/n;

					// set point locations
					setSpherical(theta,          phi,          1.0f, p);
					setSpherical(theta + TURN/n, phi,          1.0f, q);
					setSpherical(theta + TURN/n, phi + TURN/n, 1.0f, r);
					setSpherical(theta,          phi + TURN/n, 1.0f, s);

					// plot quad
					glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
					glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
					glNormal3f(r[0], r[1], r[2]); glVertex3f(r[0], r[1], r[2]);
					glNormal3f(s[0], s[1], s[2]); glVertex3f(s[0], s[1], s[2]);

				}

			}

		}
		glEnd();

		// south pole cap
		glBegin(GL_TRIANGLES);
		{
			phi = -TURN/4 + TURN/n;
			setSpherical(0.0f,  phi, 1.0f, q);
			for (int i = 1; i <= n; i++) {

				// set up edge
				theta = (TURN*i)/n;
				set(q, p);
				setSpherical(theta, phi, 1.0f, q);

				// plot triangle
				glNormal3f(0.0f, -1.0f, 0.0f); glVertex3f(0.0f, -1.0f, 0.0f);
				glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
				glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);

			}

		}
		glEnd();

	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Set the coordinate values of dest to x, y, and z.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param dest The destination point.
	 */
	private static void set(float x, float y, float z, float[] dest) { dest[0] = x; dest[1] = y; dest[2] = z; }

	// -----------------------------------------------------------------------------------------------------------------


	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Float version of PI.
	 */
	private static final float PI = (float) Math.PI;

	/**
	 * A full turn around the unit circumference.
	 */
	private static final float TURN = (float) (2.0d*Math.PI);





//----------------------------------------------------------------------------------------------------------------------


	/**
	 * Player structure.
	 */
	static class Player {

		float health = 100;
		float maxHealth = 100;
		float Stamina= 100;
		float maxStamina=100;


		boolean isAlive() {
			return health > 0;
		}

		// position in the zx plane
		float x = 0.0f;
		float z = 0.0f;
		float rotation = 0.0f;

		// Jumping Mechanics.
		float y = 0;
		boolean jumping = false;
		boolean doubleJumping = false;
		float airTime = 0;
		float jumpStartHeight = 0;
		// Jumping Parameters.
		float jumpInitialSpeed = 8;
		float gravity = -20;

		// floating parameters
		float floatingPeriod = 4.0f;
		float floatingMagnitude = 0.1f;
		// floating mechanics
		float floatTime;
		float floatOffset;


		// direction of movement (+/- 1)
		float dx = 0.0f;
		float dz = 0.0f;

		float radius = 1.0f;

		// facing direction
		Direction facing = Direction.SOUTH;

		// shots
		final PlayerShot[] shots = new PlayerShot[PLAYER_SHOT_MAX];

		// age (in seconds)
		float t = 0.0f;

		public void jump() {
			if (jumping) {
				if (doubleJumping)
					return;
				doubleJumping = true;
			}

			// Kinematic equation for jump:
			// y = jumpStartHeight + jumpInitialSpeed*t + gravity*t/2

			jumping = true;
			airTime = 0;
			jumpStartHeight = y;
			floatTime = 0;
		}

		/**
		 * Update player given dt, the number of seconds since last update.
		 *
		 * @param dt A float.
		 */
		void update(float dt) {

			// update player position
			for (int i = 0; i < ENEMY_COUNT - 1; i++) {
				if (col.checkCollisionPlayer(player, enemies[i])) {
					dx = -1 * (enemies[i].dx * .01f);
					dz = -1 * (enemies[i].dz * .01f);

					x -= dx * 15.0f;
					z -= dz * 15.0f;

				}
			}

			for(int j = 0; j < TERRAIN_COUNT; j++){
				if (col.checkCollisionPlayerTerrain(player, columns) && (col.checkCollisionPlayerTerrainHeight(player, columns) == false)) {
					x -= 1.0f;
					z -= 1.0f;
					gravity = 0.0f;
					airTime = 0.0f;
					//System.out.println("COLLISION");
				}
				else{
					gravity = -20.0f;
					//System.out.println("NO COLLISION");
				}

				if (col.checkCollisionPlayerTerrainHeight(player, columns))
				{

					y = columns[j].Height * 2;
					x -= dz * PLAYER_SPEED * dt * cos(rotation * Math.PI / 180);
					z -= dz * PLAYER_SPEED * dt * sin(rotation * Math.PI / 180);

					x -= -dx * PLAYER_SPEED * dt * cos((rotation + 90) * Math.PI / 180);
					z -= -dx * PLAYER_SPEED * dt * sin((rotation + 90) * Math.PI / 180);
				}

				else {
					gravity = -20.f;
					airTime += dt;

					if (y <= 0) { // if below ground
						y = 0;
					}

				}
			}


			// update player shots (if active)
			for (PlayerShot shot : shots) {
				if (shot.t < PLAYER_SHOT_DURATION) {

					shot.t += dt;
					shot.x += shot.dx*PLAYER_SHOT_SPEED*dt;
					shot.z += shot.dz*PLAYER_SHOT_SPEED*dt;

				}
			}

			// Calculate jump height if above ground
			if (jumping) {
				airTime += dt;
				y = jumpStartHeight + jumpInitialSpeed*airTime + gravity*airTime*airTime/2.0f;
				// check for ground collision.
				if (y <= 0) { // if below ground
					jumping = doubleJumping = false;
					y = 0;
				}
			}

			x += -dz * PLAYER_SPEED * dt * sin(rotation * Math.PI / 180);
			z += -dz * PLAYER_SPEED * dt * cos(rotation * Math.PI / 180);

			x += dx * PLAYER_SPEED * dt * sin((rotation + 90) * Math.PI / 180);
			z += dx * PLAYER_SPEED * dt * cos((rotation + 90) * Math.PI / 180);

			// find float offset
			floatTime += (dt / floatingPeriod * 2*PI) % (2*PI);
			floatOffset = (float) (-sin(floatTime) * floatingMagnitude);

		}



	}

	static class Collision {

		public boolean checkCollisionPlayer(Player p, Enemy e) {
			//check radius of player to radius of enemies
			//if player.x + enemy.x < player.r + enemy.r && p.z + e.z < p.r + e.r
			//collision
			boolean collision = false;
			for (int i = 0; i < ENEMY_COUNT; i++) {
				if ((abs(p.x - e.x) < 2) && abs(p.z - e.z) < 2) {
					collision = true;
				}
				else{
					collision = false;
				}
			}
			return collision;
		}

		public boolean checkCollisionPlayerTerrain(Player p, Terrain[] t){
			boolean collision = false;
			for (int i = 0; i < TERRAIN_COUNT; i++) {
				if ((abs(p.x - t[i].x) < t[i].Width * 2) && (abs(p.z - t[i].z) < t[i].Width * 2 )) {
					collision = true;
				}
				else{
					collision = false;
				}
			}
			return collision;
		}

		public boolean checkCollisionPlayerTerrainHeight(Player p, Terrain[] t){
			boolean collision = false;
			for (int i = 0; i < TERRAIN_COUNT; i++) {
				if ((p.y > t[i].Height * 2) && checkCollisionPlayerTerrain(p, t)) {
					collision = true;
				} else {
					collision = false;
				}
			}
			return collision;
		}

		public boolean checkCollisionEnemies(Enemy[] a, Enemy[] b) {
			boolean collision = false;
			for (int i = 0; i < ENEMY_COUNT; i++) {
				if ((abs(a[i].x - b[i].x) < 2) && abs(a[i].z - b[i].z) < 2) {
					collision = true;
				}
				else{
					collision = false;
				}
			}
			return collision;

		}
	}
	final static Collision col = new Collision();
	/**
	 * Player shot structure.
	 */
	static class PlayerShot {

		// location in the zx plane
		public float x  = 0.0f;
		public float z  = 0.0f;

		// direction of movement (+/- 1)
		public float dx = 0.0f;
		public float dz = 0.0f;

		// age (in seconds); if >= PLAYER_SHOT_DURATION, shot is inactive
		public float t  = PLAYER_SHOT_DURATION;

	}

	/**
	 * The player.
	 */
	static final Player player = new Player();

	// -------------------------------------------------------------------------

	/**
	 * Initialize player.
	 */
	static void playerInit() {

		for (int i = 0; i < PLAYER_SHOT_MAX; i++) {
			player.shots[i] = new PlayerShot();
		}

	}

	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------

	/**
	 * Plot the player avatar. Plots at center of screen since we assume
	 * camera is following.
	 */
	static void playerPlotAvatar() {

		// plot player avatar
		glPushMatrix();
		{
			// glColor3f(1.0f, 0.0f, 0.0f);
			glTranslatef(player.x, player.y+player.floatOffset+2.0f, player.z);
			glRotatef(player.rotation, 0.0f, 1.0f, 0.0f);
			glScalef(0.25f, 0.25f, 0.25f);
			walle();
		}
		glPopMatrix();
		glPushMatrix();
		{
			glColor3f(0.25f,0.25f,0.25f);
			glTranslatef(player.x ,0.0f,player.z);
			glRotatef(180.0f,1.0f,0.0f,0.0f);
			glScalef(0.5f,0.0f,0.5f);
			plotUnitPolygon(20);
		}
		glPopMatrix();

	}

	// -------------------------------------------------------------------------

	/**
	 * Plot the player's currently active shots (those with a t <=
	 * PLAYER_SHOT_DURATION).
	 */
	static void playerPlotShots() {

		// for each shot...
		for (PlayerShot shot : player.shots) {

			// if it is active, plot it
			if (shot.t < PLAYER_SHOT_DURATION) {
				glPushMatrix();
				{
					glColor3f(1.0f, 1.0f, 0.0f);
					glTranslatef(shot.x, 0.5f, shot.z);
					glScalef(PLAYER_SHOT_SIZE, PLAYER_SHOT_SIZE,
							PLAYER_SHOT_SIZE);
					plotSolidCube();
				}
				glPopMatrix();
			}
		}

	}

	// -------------------------------------------------------------------------



	// -------------------------------------------------------------------------

	/**
	 * Set a new active shot given the player's position and
	 * facing direction. If all available shots are currently active, the call
	 * has no effect.
	 */
	static void playerShoot() {

		for (PlayerShot shot : player.shots) {

			// shot fired too recently to fire another
			if (shot.t < PLAYER_SHOT_DELAY) {
				return;
			}

			// found re-usable shot slot
			if (shot.t >= PLAYER_SHOT_DURATION) {

				// activate it
				shot.t = 0.0f;
				shot.x = player.x;
				shot.z = player.z;
				shot.dx = (float) (1 * sin(player.rotation * Math.PI / 180));
				shot.dz = (float) (1 * cos(player.rotation * Math.PI / 180));


				// if slot is found, we're done;
				return;

			}

		}

	}

	// =========================================================================
	// ENEMIES
	// =========================================================================

	/**
	 * Maximum number of enemies.
	 */
	static final int ENEMY_COUNT = 20;

	/**
	 * Size of the enemies.
	 */
	static final float ENEMY_SIZE = 0.5f;

	/**
	 * Enemy speed in distance per second.
	 */
	static final float ENEMY_SPEED = 1.5f;

	/**
	 * Time it takes for enemy to spawn in seconds.
	 */
	static final float ENEMY_SPAWN_TIME = 1.0f;

	/**
	 * Starting health of enemies.
	 */
	static final float ENEMY_MAX_HEALTH = 10.0f;

	/**
	 * Enemty structure.
	 */
	static class Enemy {

		// position in the zx plane
		float x;
		float z;
		float radius = 1.0f;

		// direction of movement (+/- 1)
		float dx;
		float dz;

		// age (in seconds)
		float t;

		// health remaining
		float health;

		//stamina remaining
		float stamina;
	}

	/**
	 * All enemies.
	 */
	static final Enemy[] enemies = new Enemy[ENEMY_COUNT];

	// -------------------------------------------------------------------------

	/**
	 * Initialize enemy locations.
	 */
	static void enemiesInit() {

		// for each enemy
		for (int i = 0; i < ENEMY_COUNT; i++) {

			// place it in a random world location
			enemies[i] = new Enemy();
			enemiesRespawn(enemies[i]);

		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Update enemies based on dt, the time transpired in seconds since the
	 * last enemty update.
	 *
	 * @param dt A float.
	 */
	static void enemiesUpdate(float dt) {

		// for each enemy...
		for (int i = 0; i < ENEMY_COUNT - 1; i++) {

			Enemy e = enemies[i];

			// update t
			e.t += dt;

			// if enemy is finished spawning...
			if (e.t >= 0.0f) {

				if(col.checkCollisionPlayer(player, enemies[i]) == true){
					// set direction of motion toward player
				}
				else {
					e.dx = signum(player.x - e.x);
					e.dz = signum(player.z - e.z);

					// update location
					e.x += e.dx * ENEMY_SPEED * dt;
					e.z += e.dz * ENEMY_SPEED * dt;
				}

			}

		}

	}

	// -----------------------------------------------------------------------------------------------------------------
	// ENEMIES' MODEL---------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static void plotEnemy(int gltype) {
		plotFeet(0.75f, 0.0f, 0.35f, 0.35f, 0.5f, gltype);
		//plot body
		plotBody(  0.0f, 3.0f, 0.0f, 1.5f,   1.0f, gltype);
		//Plot arms
		plotArms( -3.182f,0.565f, 0.0f, 0.25f,  0.5f, gltype);
		//Plot eyes
		plotEyes(  0.75f,3.5f, 1.125f, 0.15f);
	}

	private static void plotFeet(float cx, float cy, float cz, float r, float h, int gltype){
		glPushMatrix();
		{
			glTranslatef(cx,cy,cz);
			glScalef(r,0.0f,2*r);
			plotUHemisphere(20,gltype);
		}
		glPopMatrix();
		glPushMatrix();
		{
			glTranslatef(cx,cy,cz);
			glScalef(r,h,2*r);
			plotUHemisphere(20, gltype);
		}
		glPopMatrix();
		glPushMatrix();
		{
			glTranslatef(-cx,cy,cz);
			glScalef(r,0.0f,2*r);
			plotUHemisphere(20, gltype);
		}
		glPopMatrix();
		glPushMatrix();
		{
			glTranslatef(-cx,cy,cz);
			glScalef(r,h,2*r);
			plotUHemisphere(20, gltype);
		}
		glPopMatrix();
	}

	private static void plotEggShape(float cx, float cy, float cz, float r, float h,float deg, int gltype){
		glPushMatrix();
		{
			glRotatef(deg,0.0f,0.0f,1.0f);
			glTranslatef(cx,cy,cz);
			glScalef(r,-3.0f*h,r);
			plotUHemisphere(20,gltype);
		}
		glPopMatrix();
		glPushMatrix();
		{
			glRotatef(deg,0.0f,0.0f,1.0f);
			glTranslatef(cx,cy,cz);
			glScalef(r,h,r);
			plotUHemisphere(20,gltype);
		}
		glPopMatrix();

	}



	private static void plotEyes(float cx,float cy, float cz,float r) {
		glColor3f(1.0f,0.0f,0.0f);
		glPushMatrix();
		{
			glTranslatef(cx,cy,cz);
			glScalef(r,r,r);
			plotUnitSphere(20);
		}
		glPopMatrix();
		glPushMatrix();
		{
			glTranslatef(-cx,cy,cz);
			glScalef(r,r,r);
			plotUnitSphere(20);
		}
		glPopMatrix();

	}
	private static void plotBody(float cx,float cy, float cz,float r, float h, int gltype) {
		plotEggShape( cx, cy, cz, r, h, 0.0f, gltype);
	}

	private static void plotArms(float cx, float cy, float cz, float r, float h, int gltype){
		plotEggShape( cx, cy, cz, r, h, 315.0f, gltype);
		plotEggShape(-cx, cy, cz, r, h, -315.0f, gltype);
	}

	private static void plotUHemisphere(int n,int choice) {

		// p->q will represent the current edge we are on
		float[] p = new float[3];
		float[] q = new float[3];

		float theta, phi;

		// north pole cap
		glBegin(GL_TRIANGLES);
		{
			phi = TURN/4 - TURN/n;
			setSpherical(0.0f,  phi, 1.0f, q);
			for (int i = 1; i <= n; i++) {

				// set up edge
				theta = (TURN*i)/n;
				set(q, p);
				setSpherical(theta, phi, 1.0f, q);

				// plot triangle
				glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
				glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
				glNormal3f(0.0f, 1.0f, 0.0f); glVertex3f(0.0f, 1.0f, 0.0f);

			}

		}
		glEnd();

		if (choice == GL_LINES)
			glLineWidth(0.5f);
		// middle bands
		glBegin(choice);
		{

			float[] r = new float[3];
			float[] s = new float[3];
			for (int i = 2; i <= (n/4); i++) {
				for (int j = 0; j < n; j++) {

					// update theta phi
					phi = TURN/4 - (TURN*i)/n;
					theta = (TURN*j)/n;

					// set point locations
					setSpherical(theta,          phi,          1.0f, p);
					setSpherical(theta + TURN/n, phi,          1.0f, q);
					setSpherical(theta + TURN/n, phi + TURN/n, 1.0f, r);
					setSpherical(theta,          phi + TURN/n, 1.0f, s);

					// plot quad
					glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
					glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
					glNormal3f(r[0], r[1], r[2]); glVertex3f(r[0], r[1], r[2]);
					glNormal3f(s[0], s[1], s[2]); glVertex3f(s[0], s[1], s[2]);

				}

			}

		}
		glEnd();

	}
	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------


	/**
	 * Plot the current state of the enemies.
	 */
	static void enemiesPlot() {

		glShadeModel(GL_SMOOTH);
		// for each enemy...
		for (int i = 0; i < ENEMY_COUNT; i++) {

			// consider current enemy
			Enemy e = enemies[i];

			glPushMatrix();
			{

				// if enemy is spawning...
				if (e.t < 0) {
					// color is gray
					glColor3f(0.5f, 0.5f, 0.5f);
				}
				else {
					// color is green
					glColor3f(0.0f, 1.0f, 1.0f);
				}

				// plot cube at enemy location
				glTranslatef(e.x, 0.0f, e.z);
				float h = (ENEMY_SIZE*e.health)/ENEMY_MAX_HEALTH;
				glPushMatrix();
				{
					glScalef(ENEMY_SIZE, h, ENEMY_SIZE);
					glTranslatef(0.0f, 1.0f, 0.0f);
					plotEnemy(GL_QUADS);
				}
				glPopMatrix();
				glPushMatrix();
				{
					glScalef(ENEMY_SIZE, ENEMY_SIZE, ENEMY_SIZE);
					glTranslatef(0.0f, 1.0f, 0.0f);
					plotEnemy(GL_LINES);
				}
				glPopMatrix();

			}
			glPopMatrix();

		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Find the list of all enemies that intersect the axis-aligned box
	 * centered at (x, z) and having radius r.
	 *
	 * @param x A float.
	 * @param z A float.
	 * @param r A float.
	 * @return A list of enemies.
	 */
	static List<Enemy> enemiesFind(float x, float z, float r) {

		List<Enemy> list = new ArrayList<>(ENEMY_COUNT);

		float d = r + ENEMY_SIZE;
		for (Enemy e : enemies) {
			if (max(abs(x - e.x), abs(z - e.z)) < d) {
				list.add(e);
			}
		}

		return list;

	}

	// -------------------------------------------------------------------------

	/**
	 * Spawn enemy e to new location.
	 *
	 * @param e An enemy.
	 */
	static void enemiesRespawn(Enemy e) {

		e.x = random(-WORLD_RADIUS, +WORLD_RADIUS);
		e.z = random(-WORLD_RADIUS, +WORLD_RADIUS);
		e.t = -ENEMY_SPAWN_TIME;
		e.health = ENEMY_MAX_HEALTH;

	}
	// =========================================================================
	// SPARKS
	// =========================================================================

	/**
	 * Maximum number of sparks.
	 */
	static final int SPARK_COUNT = 100;

	/**
	 * Size of the enemies.
	 */
	static final float SPARK_SIZE = 0.1f;

	/**
	 * Enemy speed in distance per second.
	 */
	static final float SPARK_SPEED = 0.001f;

	/**
	 * Time it takes for enemy to spawn in seconds.
	 */
	static final float SPARK_SPAWN_TIME = 1.0f;

	/**
	 * Spark life time
	 */
	static final float SPARK_LIFE_TIME = 25.0f;

	/**
	 * Enemty structure.
	 */
	static class Spark {

		// position in the zx plane
		float x;
		float z;

		// direction of movement (+/- 1)
		float dx;
		float dz;

		// age (in seconds)
		float t;

	}

	/**
	 * All sparks.
	 */
	static final Spark[] sparks = new Spark[SPARK_COUNT];

	// -------------------------------------------------------------------------

	/**
	 * Initialize enemy locations.
	 */
	static void sparkInit() {

		// for each enemy
		for (int i = 0; i < SPARK_COUNT; i++) {

			// place it in a random world location
			sparks[i] = new Spark();
			sparkSpawn(sparks[i]);

		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Update sparks based on dt, the time transpired in seconds since the
	 * last update.
	 *
	 * @param dt a float
	 */
	static void sparkUpdate(float dt) {
		int turn = 0;
		int steps = 0;

		// for each spark...
		for (int i = 0; i < SPARK_COUNT; i++) {

			Spark s = sparks[i];

			// update t
			s.t += dt;

			if (turn == 0) {
				s.dx  = 0;
				s.dz -= 1;
			} else if (turn == 1) {
				s.dx  = 0;
				s.dz += 1;
			} else if (turn == 2) {
				s.dx += 1;
				s.dz  = 0;
			} else if (turn ==3) {
				s.dx -= 1;
				s.dz  = 0;
			}


			// update location
			s.x += s.dx * SPARK_SPEED * s.t;
			s.z += s.dz * SPARK_SPEED * s.t;
			steps += 1;


			if ( steps % 3 == 0 ) {
				turn = ((int) random(0, 3));
			}


			if (steps >= SPARK_LIFE_TIME) {
				sparkSpawn(s);
				steps = 0;
			}

		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Plot the current state of the sparks.
	 */
	static void sparkPlot() {

		// for each spark...
		for (int i = 0; i < SPARK_COUNT; i++) {

			// consider current enemy
			Spark s = sparks[i];

			glPushMatrix();
			{

				// color is yellow
				glColor3f(1.0f, 1.0f, 0.0f);

				// plot cube at spark location
				glTranslatef(s.x, -0.10f, s.z);
				glPushMatrix();
				{
					glScalef(SPARK_SIZE, SPARK_SIZE, SPARK_SIZE);
					glTranslatef(0.0f, 1.0f, 0.0f);
					plotSolidCube();
				}
				glPopMatrix();

			}
			glPopMatrix();

		}

	}



	/**
	 * Spawn spark s to new location.
	 * along the grid
	 * @param s A spark.
	 */
	static void sparkSpawn(Spark s) {

		s.x =  ((int) random(-WORLD_RADIUS, +WORLD_RADIUS));
		s.x += 0.5f;
		s.z =  ((int) random(-WORLD_RADIUS, +WORLD_RADIUS));
		s.z += 0.5f;
		s.t = 0.0f;

	}
	//==========================================================================
	// TERRAIN
	//==========================================================================


	/**
	 * Maximum number of Terrain instances.
	 */
	static final int   TERRAIN_COUNT = 1;


	/**
	 * Terrain structure.
	 */
	static class Terrain {

		// position in the zx plane
		float x;
		float z;

		// size
		float Width;
		float Height;

	}

	static void terrainArray() {
		float[][] arrayMap;
		int x = (int) WORLD_RADIUS;
		arrayMap = new float[x][x];
		System.out.println(Arrays.deepToString(arrayMap));

	}

	/**
	 * All terrain.
	 */
	static final Terrain[] columns  = new Terrain[TERRAIN_COUNT];

	// -------------------------------------------------------------------------

	/**
	 * Initialize terrain Instances
	 */
	static void TerrainInit() {

		// for each column
		for (int i = 0; i < TERRAIN_COUNT; i++) {

			// place it in a random world location
			columns[i] = new Terrain();
			terrainSpawn(columns[i]);


		}

	}
	// -------------------------------------------------------------------------

	/**
	 * Plot the terrain.
	 */
	static void terrainPlot() {

		// for each instance...
		for (int i = 0; i < TERRAIN_COUNT; i++) {

			// consider current instance
			Terrain c = columns[i];


			glPushMatrix();
			{
				glColor3f(1.0f/1.65f,1.0f* 0.42f,1.0f*0.42f);

				// plot cube at terrain location
				glTranslatef(c.x, 0.0f, c.z);
				glPushMatrix();
				{
					glScalef(c.Width, .5f, c.Width);
					glTranslatef(0.0f, 1.0f, 0.0f);
					plotSolidCube();
				}
				glPopMatrix();


			}
			glPopMatrix();

		}

	}
	// -------------------------------------------------------------------------
	/**
	 * Spawn a column c to a random location.
	 *
	 * @param c An enemy.
	 */
	static void terrainSpawn(Terrain c) {

		//size
		c.Width = random(0.5f,2.0f);
		c.Height = random(0.0f,3.0f);

		//position
		c.x = random(-WORLD_RADIUS, +WORLD_RADIUS);
		c.z = random(-WORLD_RADIUS, +WORLD_RADIUS);
	}

	// =========================================================================
	// WORLD
	// =========================================================================

	/**
	 * Bounds of the world where enemies can spawn.
	 */
	static final float WORLD_RADIUS = 100.0f;

	/**
	 * Scale factor used for rendering.
	 */
	static float WORLD_SCALE = 0.25f;

	// -------------------------------------------------------------------------

	/**
	 * Plot a grid on the ZX plane.
	 */

	static void worldPlotFloor(float elevation) {


		float lower = (float) floor(-camera.farPlane) - 0.5f;
		float upper = (float)  ceil(+camera.farPlane) + 0.5f;

		glDisable(GL_LIGHTING);
		{
			glColor4f(0.0f, 0.9f, 0.0f, 0.75f);
			glLineWidth(0.2f);
			glBegin(GL_LINES);
			{
				glNormal3f(0.0f, 1.0f, 0.0f);
				for (float x = lower; x <= upper; x += 1.0f) {
					glVertex3f(x, elevation, -camera.farPlane);
					glVertex3f(x, elevation, +camera.farPlane);
				}
				for (float z = lower; z <= upper; z += 1.0f) {
					glVertex3f(-camera.farPlane, elevation, z);
					glVertex3f(+camera.farPlane, elevation, z);
				}
			}
			glEnd();
		}
		glEnable(GL_LIGHTING);

	}

	static void worldPlotCeiling(float elevation2) {


		float lower = (float) floor(-camera.farPlane) - 0.5f;
		float upper = (float)  ceil(+camera.farPlane) + 0.5f;

		glDisable(GL_LIGHTING);
		{
			glColor4f(0.0f, 0.9f, 0.0f, 0.75f);
			glLineWidth(0.2f);
			glBegin(GL_LINES);
			{
				glNormal3f(0.0f, 1.0f, 0.0f);
				for (float x = lower; x <= upper; x += 1.0f) {
					glVertex3f(x, elevation2, -camera.farPlane);
					glVertex3f(x, elevation2, +camera.farPlane);
				}
				for (float z = lower; z <= upper; z += 1.0f) {
					glVertex3f(-camera.farPlane, elevation2, z);
					glVertex3f(+camera.farPlane, elevation2, z);
				}
			}
			glEnd();
		}
		glEnable(GL_LIGHTING);

	}
	// -------------------------------------------------------------------------
	/**
	 * PowerUp structure
	 */
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 Initialize function and variables
	 */
	private static void set(float[] src, float[] dest) { System.arraycopy(src, 0, dest, 0, src.length); }

	// size of the potion
	static final float ITEM_SIZE = 0.15f;

	// potion spawn time (in seconds)
	static final float ITEM_SPAWN_TIME = 1.0f;

	static final Item item = new Item();

// -----------------------------------------------------------------------------------------------------------------

	// =========================================================================
	// ITEMS
	// =========================================================================


	// -----------------------------------------------------------------------------------------------------------------
	private static class Item {

		Potion potion = new Potion();
		SpeedPotion potion2 = new SpeedPotion();
		Treasure treasure = new Treasure();
		private static class Potion{

			// number of potions allowed on the map.
			static final int POTION_COUNT = 1;

			// position in the zx plane
			static float x;
			static float z;

			// age (in seconds)
			static float time;

			//Respawn a potion
			static void potionsInit() {
				for (int i = 0; i <= POTION_COUNT; i++){
					potionRespawn();
				}
			}

			static int collisionPlayerandPickup() {
				//Calculate the distance between the Player and the Potion
				float dist = (float) sqrt((x - player.x)*(x - player. x)+(z - player.z)*(z - player.z));

				//If the distance < 1.0f then the player health regen to 100. Then the potion disappears and respawns on another place
				if(dist < 1.0f){
					player.health = 100;
					return 1;
				}
				return 0;
			}

			//Random a place for Potion to respawn
			static void potionRespawn() {
				x = random(-WORLD_RADIUS, WORLD_RADIUS);
				z = random(-WORLD_RADIUS, WORLD_RADIUS);
				time = -ITEM_SPAWN_TIME;
			}

			//Plot Potion
			static void plotPotion() {
				for (int i = 0; i <= POTION_COUNT; i++) {
					glPushMatrix();
					{
						glTranslatef(x, 0.0f, z);
						// Potion cap (hemisphere) brown color
						glColor3f(1.0f, 0.5f, 0.0f);
						glPushMatrix();
						{
							glScalef(2.5f, 3.0f, 2.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 0.4f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitHemisphere(16);
						}
						glPopMatrix();

						// Potion cap (cone) brown color
						glColor3f(1.0f, 0.5f, 0.0f);
						glPushMatrix();
						{
							glScalef(2.5f, 3.0f, 2.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 0.4f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCone(32);
						}
						glPopMatrix();

						// Potion neck (cylinder) clear color
						glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
						glPushMatrix();
						{
							glScalef(1.5f, 1.0f, 1.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 1.0f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCylinder(32);
						}
						glPopMatrix();

						// Potion flask (cone) red color
						glColor3f(1.0f, 0.0f, 0.0f);
						glPushMatrix();
						{
							glScalef(7.0f, 9.0f, 7.0f);
							glScalef(0.5f, 0.5f, 0.5f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCone(32);
						}
						glPopMatrix();
					}
					glPopMatrix();
				}
			}
		}

		private static class SpeedPotion {

			// number of speed potions allowed on the map.
			static final int POTION_COUNT = 1;

			// position in the zx plane
			static float x;
			static float z;

			// age (in seconds)
			static float time;

			//Respawn a potion
			static void speedPotionsInit() {
				for (int i = 0; i <= POTION_COUNT; i++){
					speedPotionRespawn();
				}
			}

			static int collisionPlayerandSpeedPickup() {
				//Calculate the distance between the Player and the Potion
				float dist = (float) sqrt((x - player.x)*(x - player. x)+(z - player.z)*(z - player.z));

				//If the distance < 1.0f then the player stamina regen to 100. Then the potion disappears and respawns on another place
				if(dist < 1.0f){
					player.Stamina = 100;
					return 1;
				}
				return 0;
			}

			//Random a place for Potion to respawn
			static void speedPotionRespawn() {
				x = random(-WORLD_RADIUS, WORLD_RADIUS);
				z = random(-WORLD_RADIUS, WORLD_RADIUS);
				time = -ITEM_SPAWN_TIME;
			}

			static void plotSpeedPotion() {
				for (int i = 0; i <= POTION_COUNT; i++) {
					glPushMatrix();
					{
						glTranslatef(x, 0.0f, z);
						// Potion cap (hemisphere) brown color
						glColor3f(1.0f, 0.5f, 0.0f);
						glPushMatrix();
						{
							glScalef(2.5f, 3.0f, 2.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 0.4f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitHemisphere(16);
						}
						glPopMatrix();

						// Potion cap (cone) brown color
						glColor3f(1.0f, 0.5f, 0.0f);
						glPushMatrix();
						{
							glScalef(2.5f, 3.0f, 2.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 0.4f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCone(32);
						}
						glPopMatrix();

						// Potion neck (cylinder) clear color
						glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
						glPushMatrix();
						{
							glScalef(1.5f, 1.0f, 1.5f);
							glScalef(0.5f, 0.5f, 0.5f);
							glTranslatef(0.0f, 1.0f, 0.0f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCylinder(32);
						}
						glPopMatrix();

						// Potion flask (cone) pink color
						glColor3f(1.0f, 0.0f, 1.0f);
						glPushMatrix();
						{
							glScalef(7.0f, 9.0f, 7.0f);
							glScalef(0.5f, 0.5f, 0.5f);
							glScalef(ITEM_SIZE, ITEM_SIZE, ITEM_SIZE);
							plotUnitCone(32);
						}
						glPopMatrix();
					}
					glPopMatrix();
				}
			}
		}


		private static class Treasure {
			// number of potions allowed on the map.
			static final int TREASURE_COUNT = 1;

			// position in the zx plane
			static float x;
			static float z;

			// age (in seconds)
			static float time;

			//Respawn a potion
			static void treasureInit() {
				for (int i = 0; i <= TREASURE_COUNT; i++){
					TreasureRespawn();
				}
			}

			static int collisionPlayerandPickup() {
				//Calculate the distance between the Player and the Potion
				float dist = (float) sqrt((x - player.x)*(x - player. x)+(z - player.z)*(z - player.z));

				System.out.println(x+ " " + player.x);
				System.out.println(z+ " " +player.z);
				System.out.println(dist);
				//If the distance < 1.0f then the player health regen to 100. Then the potion disappears and respawns on another place
				if(dist < 50.0f){
					player.health = 100;
					return 1;
				}
				return 0;
			}

			//Random a place for Potion to respawn
			static void TreasureRespawn() {
				x = random(-WORLD_RADIUS, WORLD_RADIUS);
				z = random(-WORLD_RADIUS, WORLD_RADIUS);
				time = -ITEM_SPAWN_TIME;
			}


			//Plot the Treasure Chest
			private static void plotTreasureChest() {
				//Set the x,y,z,h position for the Treasure

				glPushMatrix();
				{
					//Set the white color
					glColor3f(1.0f, 1.0f, 1.0f);
					glTranslatef(x, 0.5f, z);

					//Plot base
					glPushMatrix();
					{
						glTranslatef(0.0f, 0.5f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(6.0f, 0.5f, 6.0f);
						plotUnitCube();
					}
					glPopMatrix();

					//Draw the front
					plotCylinder(2.5f, 3.0f, 2.5f, 2.5f, 0.3f);
					plotCylinder(2.5f / 2, 3.0f, 2.5f, 2.5f, 0.3f);
					plotCylinder(0, 3.0f, 2.5f, 2.5f, 0.3f);
					plotCylinder(-2.5f / 2, 3.0f, 2.5f, 2.5f, 0.3f);
					plotCylinder(-2.5f, 3.0f, 2.5f, 2.5f, 0.3f);

					//Draw the middle
					plotCylinder(2.5f, 3.0f, 0.0f, 2.5f, 0.3f);

					plotCylinder(2.5f, 3.0f, 2.5f / 2, 2.5f, 0.3f);

					plotCylinder(2.5f, 3.0f, -2.5f / 2, 2.5f, 0.3f);

					plotCylinder(-2.5f, 3.0f, 0.0f, 2.5f, 0.3f);

					plotCylinder(-2.5f, 3.0f, 2.5f / 2, 2.5f, 0.3f);

					plotCylinder(-2.5f, 3.0f, -2.5f / 2, 2.5f, 0.3f);

					//Draw the back
					plotCylinder(2.5f, 3.0f, -2.5f, 2.5f, 0.3f);
					plotCylinder(2.5f / 2, 3.0f, -2.5f, 2.5f, 0.3f);
					plotCylinder(0, 3.0f, -2.5f, 2.5f, 0.3f);
					plotCylinder(-2.5f / 2, 3.0f, -2.5f, 2.5f, 0.3f);
					plotCylinder(-2.5f, 3.0f, -2.5f, 2.5f, 0.3f);

					//Draw the roof
					glPushMatrix();
					{
						glTranslatef(0.0f, 3.0f + 2.5f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(6.0f, 0.5f, 6.0f);
						plotUnitCube();
					}
					glPopMatrix();
				}
				glPopMatrix();
			}

			// -----------------------------------------------------------------------------------------------------------------
			private static void plotSword() {

				glPushMatrix();
				{
					//Set the black color
					glColor3f(1.0f, 0.0f, 0.0f);

					glTranslatef(x, 0.2f, z);

					//Plot the body of the Sword along the y axis
					glPushMatrix();
					{
						glTranslatef(0.0f, 1.0f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(0.5f, 3.5f, 0.3f);
						plotUnitCube();
					}
					glPopMatrix();

					//plot the middle of the Sword along the x and z axis
					glPushMatrix();
					{
						glTranslatef(0.0f, 0.5f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(2.0f, 0.5f, 0.3f);
						plotUnitCube();
					}
					glPopMatrix();

					//Plot the right of the Sword
					glPushMatrix();
					{
						glTranslatef(0.75f, 0.9f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(0.5f, 1.0f, 0.3f);
						plotUnitCube();
					}
					glPopMatrix();

					//Plot the left of the Sword
					glPushMatrix();
					{
						glTranslatef(-0.75f, 0.9f, 0.0f);
						glScalef(0.5f, 0.5f, 0.5f);
						glScalef(0.5f, 1.0f, 0.3f);
						plotUnitCube();
					}
					glPopMatrix();
				}
				glPopMatrix();
			}
		}
	}
	// =========================================================================
	// SHAPE MODELS
	// =========================================================================

	/**
	 * Render a Cylinder.
	 */
	private static void plotCylinder(float cx, float cy, float cz, float h, float r) {

		// Plot the Cylinder
		glPushMatrix();
		{
			glTranslatef(cx, cy, cz);
			glScalef(r, h, r);
			plotUnitCylinder(16);
		}
		glPopMatrix();

	}
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Render a Unit cube.
	 */
	private static void plotUnitCube() {

		// drawing quads (squares)
		glBegin(GL_QUADS);

		// front x face
		glNormal3f(1.0f, 0.0f, 0.0f);
		glVertex3f(1.0f, -1.0f, -1.0f);
		glVertex3f(1.0f, 1.0f, -1.0f);
		glVertex3f(1.0f, 1.0f, 1.0f);
		glVertex3f(1.0f, -1.0f, 1.0f);

		// back x face
		glNormal3f(-1.0f, 0.0f, 0.0f);
		glVertex3f(-1.0f, 1.0f, 1.0f);
		glVertex3f(-1.0f, -1.0f, 1.0f);
		glVertex3f(-1.0f, -1.0f, -1.0f);
		glVertex3f(-1.0f, 1.0f, -1.0f);

		// front y face
		glNormal3f(0.0f, 1.0f, 0.0f);
		glVertex3f(-1.0f, 1.0f, -1.0f);
		glVertex3f(1.0f, 1.0f, -1.0f);
		glVertex3f(1.0f, 1.0f, 1.0f);
		glVertex3f(-1.0f, 1.0f, 1.0f);

		// back y face
		glNormal3f(0.0f, -1.0f, 0.0f);
		glVertex3f(1.0f, -1.0f, 1.0f);
		glVertex3f(-1.0f, -1.0f, 1.0f);
		glVertex3f(-1.0f, -1.0f, -1.0f);
		glVertex3f(1.0f, -1.0f, -1.0f);

		// front z face
		glNormal3f(0.0f, 0.0f, 1.0f);
		glVertex3f(-1.0f, -1.0f, 1.0f);
		glVertex3f(1.0f, -1.0f, 1.0f);
		glVertex3f(1.0f, 1.0f, 1.0f);
		glVertex3f(-1.0f, 1.0f, 1.0f);

		// back z face
		glNormal3f(0.0f, 0.0f, -1.0f);
		glVertex3f(1.0f, 1.0f, -1.0f);
		glVertex3f(-1.0f, 1.0f, -1.0f);
		glVertex3f(-1.0f, -1.0f, -1.0f);
		glVertex3f(1.0f, -1.0f, -1.0f);

		glEnd();

	}
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * Plot a cone of height and radius 1 made up of n triangular faces.
	 */
	private static void plotUnitCone(int n) {

		// p->q will represent the current base edge we are on
		final float angleIncrement = (float) ((Math.PI*2.0d)/n);
		float angle = angleIncrement;
		float[] p = new float[3];
		float[] q = new float[3];
		setSpherical(0.0f, 0.0f, 1.0f, p);
		setSpherical(angle, 0.0f, 1.0f, q);

		// plot triangle faces
		glShadeModel(GL_SMOOTH);
		glBegin(GL_TRIANGLES);
		{
			for (int i = 0; i < n; i++) {

				// plot current triangle
				glNormal3f(p[0], p[1], p[2]);
				glVertex3f(p[0], p[1], p[2]);
				glNormal3f(q[0], q[1], q[2]);
				glVertex3f(q[0], q[1], q[2]);
				glNormal3f(0.0f, 1.0f, 0.0f);
				glVertex3f(0.0f, 1.0f, 0.0f);

				// go to next base edge
				set(q, p);
				angle += angleIncrement;
				setSpherical(angle, 0.0f, 1.0f, q);

			}
		}
		glEnd();

	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Plot an uncapped unit cylinder with n sides. The extrema of the cylinder will be at Y = +/- 1.
	 */
	private static void plotUnitCylinder(int n) {

		// p->q will represent the current base edge we are on
		float[] p = new float[3];
		float[] q = new float[3];
		setSpherical(0.0f, 0.0f, 1.0f, q);

		// plot triangle faces
		glBegin(GL_QUADS);
		for (int i = 1; i <= n; i++) {

			// go to next base edge
			set(q, p);
			setSpherical((TURN*i)/n, 0.0f, 1.0f, q);

			// plot current quad
			glNormal3f(p[0], 0.0f, p[2]); glVertex3f(p[0], -1.0f, p[2]);
			glNormal3f(q[0], 0.0f, q[2]); glVertex3f(q[0], -1.0f, q[2]);
			glNormal3f(q[0], 0.0f, q[2]); glVertex3f(q[0], +1.0f, q[2]);
			glNormal3f(p[0], 0.0f, p[2]); glVertex3f(p[0], +1.0f, p[2]);

		}
		glEnd();

	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Plot a unit sphere with n bands of azimuth and n/2 bands of elevation.
	 *
	 * @param n Number of azimuth bands.
	 */
	private static void plotUnitHemisphere(int n) {

		// p->q will represent the current edge we are on
		float[] p = new float[3];
		float[] q = new float[3];

		float theta, phi;

		// north pole cap
		glBegin(GL_TRIANGLES);
		{
			phi = TURN/4 - TURN/n;
			setSpherical(0.0f,  phi, 1.0f, q);
			for (int i = 1; i <= n; i++) {

				// set up edge
				theta = (TURN*i)/n;
				set(q, p);
				setSpherical(theta, phi, 1.0f, q);

				// plot triangle
				glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
				glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
				glNormal3f(0.0f, 1.0f, 0.0f); glVertex3f(0.0f, 1.0f, 0.0f);

			}

		}
		glEnd();

		// middle bands
		glBegin(GL_QUADS);
		{

			float[] r = new float[3];
			float[] s = new float[3];
			for (int i = 2; i <= (n/4); i++) {
				for (int j = 0; j < n; j++) {

					// update theta phi
					phi = TURN/4 - (TURN*i)/n;
					theta = (TURN*j)/n;

					// set point locations
					setSpherical(theta,          phi,          1.0f, p);
					setSpherical(theta + TURN/n, phi,          1.0f, q);
					setSpherical(theta + TURN/n, phi + TURN/n, 1.0f, r);
					setSpherical(theta,          phi + TURN/n, 1.0f, s);

					// plot quad
					glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
					glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
					glNormal3f(r[0], r[1], r[2]); glVertex3f(r[0], r[1], r[2]);
					glNormal3f(s[0], s[1], s[2]); glVertex3f(s[0], s[1], s[2]);

				}

			}
			// Equator
			for (int j = 0; j < n; j++) {
				int i = (n/4);

				// update theta phi
				phi = TURN/4 - (TURN*i)/n;
				theta = (TURN*j)/n;

				// set point locations
				setSpherical(theta,          phi,          1.0f, p);
				setSpherical(theta + TURN/n, phi,          1.0f, q);
				setSpherical(theta + TURN/n, 0,            1.0f, r);
				setSpherical(theta,          0,            1.0f, s);

				// plot quad
				glNormal3f(p[0], p[1], p[2]); glVertex3f(p[0], p[1], p[2]);
				glNormal3f(q[0], q[1], q[2]); glVertex3f(q[0], q[1], q[2]);
				glNormal3f(r[0], r[1], r[2]); glVertex3f(r[0], r[1], r[2]);
				glNormal3f(s[0], s[1], s[2]); glVertex3f(s[0], s[1], s[2]);

			}



		}
		glEnd();

	}

	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Set the components of point dest based on the spherical parameters theta, phi, and r.
	 *
	 * @param theta The azimuth about the Y axis in radians.
	 * @param phi   The elevation above the XZ plane in radians.
	 * @param r     The distance from the origin.
	 * @param dest  Destination point.
	 */
	private static void setSpherical(float theta, float phi, float r, float[] dest) {

		dest[1] =    (float) sin(phi)*r;
		float r_xz = (float) cos(phi)*r;
		dest[0] =    (float) cos(theta)*r_xz;
		dest[2] =    (float) sin(theta)*r_xz;

	}

	// -----------------------------------------------------------------------------------------------------------------


	// =========================================================================
	// CAMERA
	// =========================================================================

	/**
	 * Camera structure.
	 */
	static class Camera {

		// camera's spherical coordinates about the player
		public float azimuth     =  0.0f;
		public float elevation   =  -12.5f;
		public float distance    =  1.5f;

		// clipping planes
		public float nearPlane   =  0.1f;
		public float farPlane    =  100.0f;
		public float fieldOfView =  45.0f;

	}


	/**
	 * The game camera.
	 */
	static final Camera camera = new Camera();

	// -------------------------------------------------------------------------

	/**
	 * Apply camera transformation.
	 */
	static void cameraTransformation() {

		glRotatef(-camera.elevation, 1.0f, 0.0f, 0.0f);
		glRotatef(180, 0.0f, 1.0f, 0.0f);
		glTranslatef(0.0f, -1.0f, +2.0f);
		glRotatef(player.rotation, 0.0f, -1.0f, 0.0f);
		glTranslatef(-player.x * .25f, -player.y * .25f, -player.z * .25f);

	}

	// =========================================================================
	// GAME FRAMEWORK
	// =========================================================================

	/**
	 * Application title (shown on window bar).
	 */
	static final String APP_TITLE = CubeQuest.class.getName();

	/**
	 * Target frame rate.
	 */
	static final int FRAME_RATE = 60;

	/**
	 * Light position (in camera space).
	 */
	static final FloatBuffer lightPosition =
			floatBuffer(3.0f, 4.0f, 5.0f, 1.0f);

	/**
	 * Ambient component of light.
	 */
	static final FloatBuffer lightAmbient  =
			floatBuffer(0.2f, 0.2f, 0.2f, 1.0f);

	/**
	 * Diffuse component of light.
	 */
	static final FloatBuffer lightDiffuse  =
			floatBuffer(0.5f, 0.5f, 0.5f, 1.0f);

	/**
	 * Specular component of light.
	 */
	static final FloatBuffer lightSpecular =
			floatBuffer(0.1f, 0.1f, 0.1f, 1.0f);

	/**
	 * Ambient component of material.
	 */
	static final FloatBuffer materialAmbient  =
			floatBuffer( 1.0f, 1.0f, 1.0f, 1.0f);

	/**
	 * Diffuse component of material.
	 */
	static final FloatBuffer materialDiffuse  =
			floatBuffer( 1.0f, 1.0f, 1.0f, 1.0f);

	/**
	 * Specular component of material.
	 */
	static final FloatBuffer materialSpecular =
			floatBuffer( 1.0f, 1.0f, 1.0f, 1.0f);

	/**
	 * Material shininess (specular exponent).
	 */
	static final float materialShininess = 8.0f;

	/**
	 * Exit flag (application will finish when set to true).
	 */
	static boolean finished;

	// -------------------------------------------------------------------------


	/**
	 * Initialize display and OpenGL properties.
	 *
	 * @throws Exception
	 */
	static void gameInit() throws Exception {

		// Enable support for High DPI displays.
		System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");

		// initialize the display
		Display.setTitle(APP_TITLE);
		Display.setFullscreen(false);
		Display.setVSyncEnabled(true);
		Display.setResizable(true);
		Display.create();

		updateOpenGLProjectionMatrix();

		//Mouse, sets it to be hidden
		Mouse.create();
		Mouse.setGrabbed(true);

		// background color
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// lighting
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_AMBIENT, lightAmbient);
		glLight(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
		glLight(GL_LIGHT0, GL_SPECULAR, lightSpecular);
		glLight(GL_LIGHT0, GL_POSITION, lightPosition);
		glEnable(GL_NORMALIZE);
		glEnable(GL_AUTO_NORMAL);

		// material
		glMaterial(GL_FRONT, GL_AMBIENT, materialAmbient);
		glMaterial(GL_FRONT, GL_DIFFUSE, materialDiffuse);
		glMaterial(GL_FRONT, GL_SPECULAR, materialSpecular);
		glMaterialf(GL_FRONT, GL_SHININESS, materialShininess);

		// allow changing colors while keeping the above material
		glEnable(GL_COLOR_MATERIAL);

		// depth testing
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);

		// transparency
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// antialiasing
		glEnable(GL_POINT_SMOOTH);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_POLYGON_SMOOTH);

		// fog
		glEnable(GL_FOG);
		glFog(GL_FOG_COLOR, floatBuffer(1.0f, 1.0f, 1.0f, 1.0f));
		glFogi(GL_FOG_MODE, GL_EXP2);
		glFogf(GL_FOG_DENSITY, 0.01f);

		// TODO: initialize game elements

		playerInit();
		enemiesInit();
		item.potion.potionsInit();
		item.potion2.speedPotionsInit();
		item.treasure.treasureInit();
		TerrainInit();
		sparkInit();

		AL.create();
		int source = alGenSources();
		int buffer = alGenBuffers();
		WaveData wd = WaveData.create(new BufferedInputStream(new FileInputStream("music.wav")));
		alBufferData(buffer,wd.format,wd.data,wd.samplerate);
        wd.dispose();
		alSourcei(source,AL_LOOPING,AL_TRUE);
		alSourceQueueBuffers(source,buffer);
		alSourcePlay(source);

	}

	// -------------------------------------------------------------------------

	/**
	 * Main loop of the application. Repeats until finished variable takes on
	 * true.
	 */
	static void gameRun() {



		long last = System.currentTimeMillis();
		long current = last;
		while (!finished) {

			// perform time step and render
			float dt = 0.001f*(current - last);
			{
				gameHandleInput();
				gameUpdate(dt);
				gameHandleCollisions();
				gameRenderFrame();
			}
			Display.sync(FRAME_RATE);

			// make sure display is updated
			Display.update();
			if (Display.isCloseRequested()) {
				finished = true;
			}
			last = current;
			current = System.currentTimeMillis();
		}

	}

	// -------------------------------------------------------------------------

	//Initial X and Y for Camera
	public static int previousX = -1;
	public static int previousY = -1;
	/**
	 * Handle input to the game.
	 */
	static void gameHandleInput() {

		// arrow keys
		player.dx = 0.0f;
		player.dz = 0.0f;

		int mouseDifferenceX = Mouse.getX() - previousX;
		int mouseDifferenceY = Mouse.getY() - previousY;


		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			player.dx += +1.0f;
			//player.facing = Direction.WEST;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			player.dx += -1.0f;
			//player.facing = Direction.EAST;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			player.dz += -1.0f;
			//player.facing = Direction.NORTH;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			player.dz += +1.0f;
			//player.facing = Direction.SOUTH;
		}
		if(!(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
			PLAYER_SPEED=10.0f;
		}

		// space bar
		if (Mouse.isButtonDown(0)) {
			playerShoot();
		}

		//Updates camera based on Mouse position
		player.rotation -= (mouseDifferenceX * 0.25f);
		Mouse.setCursorPosition(Display.getDisplayMode().getWidth() / 2, Display.getDisplayMode().getHeight() / 2);
		previousX = Mouse.getX();



		// escape to quit
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {

					finished = true;

				}
				if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
					toggleFullscreen();
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
					player.jump();
				}
			}
		}

		// TODO: Add other game input handling.

	}



	private static void toggleFullscreen() {
		try {
			Display.setFullscreen(!Display.isFullscreen());
			updateOpenGLProjectionMatrix();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Handle input and update scene.
	 */
	static void gameUpdate(float dt) {

		// TODO: add updates to all game elements.

		player.update(dt);
		enemiesUpdate(dt);
		sparkUpdate(dt);

	}

	// -------------------------------------------------------------------------

	/**
	 * Check for relevant collisions and handle them.
	 */
	static void gameHandleCollisions() {

		// TODO: add necessary collision checks and behaviors.

		collisionShotsAndEnemies();
		item.potion.collisionPlayerandPickup();
		item.potion2.collisionPlayerandSpeedPickup();
		item.treasure.collisionPlayerandPickup();

	}

	// -------------------------------------------------------------------------

	/**
	 * Check for collisions between player shots and enemies.
	 */
	static void collisionShotsAndEnemies() {

		// for each active shot...
		for (PlayerShot shot : player.shots) {
			if (shot.t < PLAYER_SHOT_DURATION) {

				// check for shot collision with enemy
				List<Enemy> list = enemiesFind(shot.x, shot.z, PLAYER_SHOT_SIZE);
				for (Enemy e : list) {

					// register damage to enemy
					e.health -= PLAYER_SHOT_DAMAGE;
					if (e.health <= 0) {
						enemiesRespawn(e);
					}

					// disable shot
					shot.t = PLAYER_SHOT_DURATION;

				}

			}
		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Render the scene from the current view
	 */
	static void gameRenderFrame() {

		// The viewport and projection matrices must be updated when the window size changes
		if (Display.wasResized()) {
			updateOpenGLProjectionMatrix();
		}

		// clear the screen and depth buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Render the UI
		renderUI();

		// viewing transformation (bottom of the model-view stack)
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glPushMatrix();
		{

			// go to 3rd person view of player
			cameraTransformation();
			glScalef(WORLD_SCALE, WORLD_SCALE, WORLD_SCALE);
			playerPlotAvatar();

			// TODO: plot all game elements

			worldPlotFloor(0);
			worldPlotFloor(elevation);
			playerPlotShots();
			enemiesPlot();

			//Design a movement of items according to a sin wave
			float height = (float) Math.sin(((System.currentTimeMillis()) % (1000 * 4)) * (2 * PI) / 1000 / 4);

			//Plot health regen Potion move along the y axis
			glPushMatrix();
			{
				glTranslatef(0.0f, 1.0f + height, 0.0f);
				item.potion.plotPotion();
				glPopMatrix();
			}

			//Respawn the Potion in a different place on the surface once picked up by the player
			if (item.potion.collisionPlayerandPickup() == 1) {
				item.potion.potionsInit();
			}

			glPushMatrix();
			{
				glTranslatef(0.0f, 1.0f + height, 0.0f);
				item.potion2.plotSpeedPotion();
				glPopMatrix();
			}

			//Respawn the Speed Potion in a different place on the surface once picked up by the player
			if (item.potion2.collisionPlayerandSpeedPickup() == 1) {
				item.potion2.speedPotionsInit();
			}

			glPushMatrix();
			{
				glScalef(0.5f, 0.5f, 0.5f);

				//Plot a Treasure Chest on a surface
				item.treasure.plotTreasureChest();
				//Plot a Sword inside the Treasure Chest and make it move following the sin wave according to the y axis

				glScalef(1.0f, 0.8f, 1.0f);
				glTranslatef(0.0f, 3.1f + height, 0.0f);
				item.treasure.plotSword();
				glPopMatrix();
			}

			if (item.treasure.collisionPlayerandPickup()==1){
				item.treasure.treasureInit();
			}

			glPushMatrix();//elevator
			{
				glColor3f(0.0f, 1.0f, 0.0f);
				glScalef(3.0f, 0.0f, 3.0f);
				plotUnitPolygon(128);
			}
			glPopMatrix();

			//terrainPlot();
			//sparkPlot();
		}
		glPopMatrix();


	}




	static void updateOpenGLProjectionMatrix() {

		// Spit out window dimensions for debug
		System.out.println("Width:\t" + Display.getWidth());
		System.out.println("Height:\t" + Display.getHeight());
		System.out.println("HiDPI Scaling:\t" + Display.getPixelScaleFactor());

		// get display size
		int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
		int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

		// viewport
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glViewport(0, 0, width, height);

		// perspective transformation
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float aspectRatio = ((float) width) / height;
		gluPerspective(camera.fieldOfView, aspectRatio,
				camera.nearPlane, camera.farPlane);
	}

	/**
	 * Renders all the UI
	 * (0,0) is at the bottom left corner of the display.
	 */
	static void renderUI() {
		// Make everything in density independent screen coordinates.
		float width = Display.getWidth();
		float height = Display.getHeight();


		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		{
			// make everything in screen coordinates.
			glLoadIdentity();
			glTranslatef(-1.0f,-1.0f,-1.0f);
			glScalef(1/(width/2.0f),1/(height/2.0f),1.0f);



			// No shading required for UI elements.
			glDisable(GL_LIGHTING);

			// Test Pattern
            /*glBegin(GL_QUADS);
            {
                glColor4f(1.0f,1.0f, 1.0f, 1.0f);
                glVertex2f(0f, 0f);
                glVertex2f(width/2, 0f);
                glVertex2d(width/2, height/2);
                glVertex2f(0f, height/2);
                glColor4f(1.0f,0.0f, 0.0f, 1.0f);
                glVertex2f(width/2, height/2);
                glVertex2f(width-10, height/2);
                glVertex2d(width-10, height-10);
                glVertex2f(width/2, height-10);
            }
            glEnd();*/

			renderHealth(width, height);
			renderStamina(width, height);
		}
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
	}

	static void renderHealth(float width, float height) {
		float margin = 50.0f;
		float maxBarHeight = 200;
		float barWidth = 50;

		float barHeight = maxBarHeight * player.health / player.maxHealth;
		glPushMatrix();
		glTranslatef(margin, margin, 0.0f);
		glBegin(GL_QUADS);
		{
			glColor3f(0.5f,0.5f,0.5f);
			glVertex2d(0.0f,barHeight);
			glVertex2d(barWidth,barHeight);
			glVertex2d(barWidth,maxBarHeight);
			glVertex2d(0.0f,maxBarHeight);

			glColor3f(1.0f,0.0f,0.0f);
			glVertex2d(0.0f,0.0f);
			glVertex2d(barWidth,0.0f);
			glVertex2d(barWidth,barHeight);
			glVertex2d(0.0f,barHeight);
		}
		glEnd();
		glPopMatrix();
	}
	private static void renderStamina(float width, float height) { //the stamina bar needs additional coding to decrease as the player sprints

		float margin = 150.0f;
		float maxBarHeight = 200;
		float barWidth = 50;

		float barHeight = maxBarHeight * player.Stamina / player.maxStamina;
		glPushMatrix();
		glTranslatef(margin-25, margin-100, 0.0f);

		glBegin(GL_QUADS);
		{
			glColor3f(0.5f,0.5f,0.5f);
			glVertex2d(0.0f,barHeight);
			glVertex2d(barWidth,barHeight);
			glVertex2d(barWidth,maxBarHeight);
			glVertex2d(0.0f,maxBarHeight);

			glColor3f(0.0f,0.0f,1.0f);
			glVertex2d(0.0f,0.0f);
			glVertex2d(barWidth,0.0f);
			glVertex2d(barWidth,barHeight);
			glVertex2d(0.0f,barHeight);
		}
		glEnd();
		glPopMatrix();

		// if shift is being pressed, stamina goes down.
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_A) ||
				Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_S) ||
				Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_D) ||
				Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_W)){
			if(player.Stamina > 0){
				player.Stamina--;
			}
		}
		// if shift is being pressed, the player starts running. if stamina is zero, the player moves at normal speed.
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && player.Stamina > 0){
			PLAYER_SPEED = 25.0f;
		}
		else {
			PLAYER_SPEED = 10.0f;
		}

		// if shift is not being pressed, stamina starts to regen.
		if(!(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) && player.Stamina < player.maxStamina){
			player.Stamina += 0.05f;
		}
	}

	// -------------------------------------------------------------------------

	/**
	 * Clean up before exit.
	 */
	static void gameCleanup() {

		// Close the window
		Display.destroy();
		Mouse.destroy();

		AL.destroy();
	}

	// =========================================================================
	// UTILITY AND MISC.
	// =========================================================================

	/**
	 * Random number generator.
	 */
	static final Random random = new Random(System.currentTimeMillis());

	// -------------------------------------------------------------------------

	/**
	 * A random value uniformly distributed in the interval [lower, upper).
	 *
	 * @param lower A float.
	 * @param upper A float.
	 * @return A float.
	 */
	static float random(float lower, float upper) {

		return random.nextFloat()*(upper - lower) + lower;

	}

	// -------------------------------------------------------------------------
	static float elevation = random (10, 15);
	//--------------------------------------------------------------------------

	/**
	 * Plot a unit cube (i.e, a cube spanning the [-1, 1] interval on the X, Y,
	 * and Z axes).
	 */
	static void plotSolidCube() {

		// set flat shading
		glShadeModel(GL_FLAT);

		// drawing quads (squares)
		glBegin(GL_QUADS);
		{

			// front x face

			glNormal3f( 1.0f, 0.0f, 0.0f);
			glVertex3f( 1.0f, -1.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, 1.0f);
			glVertex3f( 1.0f, -1.0f, 1.0f);

			// back x face

			glNormal3f(-1.0f, 0.0f, 0.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);

			// front y face

			glNormal3f(0.0f, 1.0f, 0.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);

			// back y face

			glNormal3f(0.0f, -1.0f, 0.0f);
			glVertex3f( 1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f( 1.0f, -1.0f, -1.0f);

			// front z face

			glNormal3f(0.0f, 0.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f( 1.0f, -1.0f, 1.0f);
			glVertex3f( 1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);

			// back z face

			glNormal3f(0.0f, 0.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, -1.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f( 1.0f, -1.0f, -1.0f);

		}
		glEnd();

	}

	// -------------------------------------------------------------------------

	/**
	 * Plot a unit cube (i.e, a cube spanning the [-1, 1] interval on the X, Y,
	 * and Z axes).
	 */
	static void plotWireFrameCube() {

		// set flat shading
		glShadeModel(GL_FLAT);

		// front x face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(1.0f, 0.0f, 0.0f);
			glVertex3f(1.0f, -1.0f, -1.0f);
			glVertex3f(1.0f, 1.0f, -1.0f);
			glVertex3f(1.0f, 1.0f, 1.0f);
			glVertex3f(1.0f, -1.0f, 1.0f);
		}
		glEnd();

		// back x face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(-1.0f, 0.0f, 0.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);
		}
		glEnd();

		// front y face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(0.0f, 1.0f, 0.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);
			glVertex3f(1.0f, 1.0f, -1.0f);
			glVertex3f(1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);
		}
		glEnd();

		// back y face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(0.0f, -1.0f, 0.0f);
			glVertex3f(1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f(1.0f, -1.0f, -1.0f);
		}
		glEnd();

		// front z face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(0.0f, 0.0f, 1.0f);
			glVertex3f(-1.0f, -1.0f, 1.0f);
			glVertex3f(1.0f, -1.0f, 1.0f);
			glVertex3f(1.0f, 1.0f, 1.0f);
			glVertex3f(-1.0f, 1.0f, 1.0f);
		}
		glEnd();

		// back z face
		glBegin(GL_LINE_LOOP);
		{
			glNormal3f(0.0f, 0.0f, -1.0f);
			glVertex3f( 1.0f, 1.0f, -1.0f);
			glVertex3f(-1.0f, 1.0f, -1.0f);
			glVertex3f(-1.0f, -1.0f, -1.0f);
			glVertex3f( 1.0f, -1.0f, -1.0f);
		}
		glEnd();

	}

	// -------------------------------------------------------------------------

	/**
	 * Utility function to easily create float buffers.
	 *
	 * @param f1 A float.
	 * @param f2 A float.
	 * @param f3 A float.
	 * @param f4 A float.
	 * @return A float buffer.
	 */
	static FloatBuffer floatBuffer(float f1, float f2,
								   float f3, float f4) {

		FloatBuffer fb = BufferUtils.createFloatBuffer(4);
		fb.put(f1).put(f2).put(f3).put(f4).flip();
		return fb;

	}

	// =========================================================================
	//                                  MAIN
	// =========================================================================

	public static void main(String[] args) {

		try {
			gameInit();
			gameRun();
		} catch (Exception e) {
			System.out.println("Fatal error: " + e.getMessage());
		} finally {
			gameCleanup();
		}

	}

// =============================================================================
}
// =============================================================================