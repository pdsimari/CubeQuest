import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.nio.FloatBuffer;
import java.util.ArrayList;
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
    static final float PLAYER_SPEED = 10.0f;

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

    /**
     * Player structure.
     */
    static class Player {

        float health = 100;
        float maxHealth = 100;

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
        float jumpInitialSpeed = 20;
        float gravity = -100;

        // direction of movement (+/- 1)
        float dx = 0.0f;
        float dz = 0.0f;

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
        }

        /**
         * Update player given dt, the number of seconds since last update.
         *
         * @param dt A float.
         */
        void playerUpdate(float dt) {

            // update player position

            x += -dz * PLAYER_SPEED * dt * sin(rotation * Math.PI / 180);
            z += -dz * PLAYER_SPEED * dt * cos(rotation * Math.PI / 180);

            x += dx * PLAYER_SPEED * dt * sin((rotation + 90) * Math.PI / 180);
            z += dx * PLAYER_SPEED * dt * cos((rotation + 90) * Math.PI / 180);


            t += dt;


            // update player shots (if active)
            for (PlayerShot shot : shots) {
                if (shot.t < PLAYER_SHOT_DURATION) {

                    shot.t += dt;
                    shot.x += shot.dx*PLAYER_SHOT_SPEED*dt;
                    shot.z += shot.dz*PLAYER_SHOT_SPEED*dt;

                }
            }

            // Calculate jump height
            if (jumping) {
                airTime += dt;
                y = jumpStartHeight + jumpInitialSpeed*airTime + gravity*airTime*airTime/2.0f;
                // check for ground collision.
                if (y <= 0) {
                    jumping = doubleJumping = false;
                    y = 0;
                }
            }

        }

    }

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
            glColor3f(1.0f, 0.0f, 0.0f);
            glTranslatef(player.x, 0.5f + player.y, player.z);
            glRotatef(player.rotation, 0.0f, 1.0f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            plotSolidCube();
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
    static final int ENEMY_COUNT = 100;

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

        // direction of movement (+/- 1)
        float dx;
        float dz;

        // age (in seconds)
        float t;

        // health remaining
        float health;

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
        for (int i = 0; i < ENEMY_COUNT; i++) {

            Enemy e = enemies[i];

            // update t
            e.t += dt;

            // if enemy is finished spawning...
            if (e.t >= 0.0f) {

                // set direction of motion toward player
                e.dx = signum(player.x - e.x);
                e.dz = signum(player.z - e.z);

                // update location
                e.x += e.dx*ENEMY_SPEED*dt;
                e.z += e.dz*ENEMY_SPEED*dt;

            }

        }

    }

    // -------------------------------------------------------------------------

    /**
     * Plot the current state of the enemies.
     */
    static void enemiesPlot() {

        // for each enemy...
        for (int i = 0; i < ENEMY_COUNT; i++) {

            // consider current enemy
            Enemy e = enemies[i];

            glPushMatrix();
            {

                // if enemy is spawning...
                if (e.t < 0) {
                    // color is blue
                    glColor3f(0.0f, 0.0f, 1.0f);
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
                    plotSolidCube();
                }
                glPopMatrix();
                glPushMatrix();
                {
                    glScalef(ENEMY_SIZE, ENEMY_SIZE, ENEMY_SIZE);
                    glTranslatef(0.0f, 1.0f, 0.0f);
                    plotWireFrameCube();
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
    //==========================================================================
    // TERRAIN
    //==========================================================================


    /**
     * Maximum number of Terrain instances.
     */
    static final int   TERRAIN_COUNT = 1000;


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
                    glScalef(c.Width, c.Height, c.Width);
                    glTranslatef(0.0f, 0.0f, 0.0f);
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
    // ITEMS
    // =========================================================================

    // size of the potion
    static final float ITEM_SIZE = 0.15f;
    // time before potion despawns
    static final float ITEM_DESPAWN_TIME = 10.0f;
    // potion spawn time (in seconds)
    static final float ITEM_SPAWN_TIME = 1.0f;

    /**
     * Potion structure
     */
    static class Potion {

        // amount the potion heals
        static final float POTION_HEAL = 3.0f;
        // number of potions allowed on the map.
        static final int POTION_COUNT = 1;

        // position in the zx plane
        float x;
        float z;

        // age (in seconds)
        float time;

        static void applyPickup() {

            // while collision occurs
            // if(player health = 9)
            // player health = player health + 1
            // elseif(player health = 8)
            // player health = player health + 2
            // elseif(player health <= 7 && health < max health && health > 0)
            // player health = player health + 3

        }

        static void potionsInit() {

            for (int i = 0; i <= POTION_COUNT; i++){
                potionRespawn();
            }
        }

        static void collisionPlayerandPickup() {

        }

        static void potionRespawn() {

            p.x = random(-WORLD_RADIUS, WORLD_RADIUS);
            p.z = random(-WORLD_RADIUS, WORLD_RADIUS);
            p.time = -ITEM_SPAWN_TIME;
        }

        static void plotPotion() {

            for (int i = 0; i <= POTION_COUNT; i++) {

                glPushMatrix();
                {
                    glTranslatef(p.x, 0.0f, p.z);
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

    // The potion object
    static final Potion p = new Potion();
    // =========================================================================
    // SHAPE MODELS
    // =========================================================================

    /**
     * Plot a cone of height and radius 1 made up of n triangular faces.
     *
     * @param n An int.
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
     *
     * @param n An int.
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

    /**
     * A full turn around the unit circumference.
     */
    private static final float TURN = (float) (2.0d*Math.PI);

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

    static void worldPlotFloor2(float elevation2) {


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
    static void worldPlotFloor3(float elevation3) {


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
                    glVertex3f(x, elevation3, -camera.farPlane);
                    glVertex3f(x, elevation3, +camera.farPlane);
                }
                for (float z = lower; z <= upper; z += 1.0f) {
                    glVertex3f(-camera.farPlane, elevation3, z);
                    glVertex3f(+camera.farPlane, elevation3, z);
                }
            }
            glEnd();
        }
        glEnable(GL_LIGHTING);

    }
    // -------------------------------------------------------------------------
    // Power Up
    // -----------------------------------------------------------------------------------------------------------------

    /**
    Initialize function and variables
     */
    private static void set(float[] src, float[] dest) { System.arraycopy(src, 0, dest, 0, src.length); }

    /**
     * PowerUp structure
     */
// -----------------------------------------------------------------------------------------------------------------
    private static void plotTreasureChest() {

        float x= (float) 2.5;
        float y= (float) 3.0;
        float z= (float) 2.5;
        float h= (float) 2.5;

        glColor3f(1.0f, 1.0f, 1.0f);

        // plot base
        glPushMatrix();
        {
            glTranslatef(0.0f, 0.5f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(6.0f, 0.5f, 6.0f);
            plotUnitCube();
        }
        glPopMatrix();

        //Draw the front
        plotCylinder(x,y,z,h,0.3f);   //cx cy cz
        plotCylinder(x/2,y,z,h,0.3f);
        plotCylinder(0,y,z,h,0.3f);
        plotCylinder(-x/2,y,z,h,0.3f);
        plotCylinder(-x,y,z,h,0.3f);

        //Draw the middle
        plotCylinder(x,y,0.0f,h,0.3f);

        plotCylinder(x,y,z/2,h,0.3f);

        plotCylinder(x,y,-z/2,h,0.3f);

        plotCylinder(-x,y,0.0f,h,0.3f);

        plotCylinder(-x,y,z/2,h,0.3f);

        plotCylinder(-x,y,-z/2,h,0.3f);

        //Draw the back
        plotCylinder(x,y,-z,h,0.3f);
        plotCylinder(x/2,y,-z,h,0.3f);
        plotCylinder(0,y,-z,h,0.3f);
        plotCylinder(-x/2,y,-z,h,0.3f);
        plotCylinder(-x,y,-z,h,0.3f);

        //Draw the roof
        glPushMatrix();
        {
            glTranslatef(0.0f, y+2.5f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(6.0f, 0.5f, 6.0f);
            plotUnitCube();
        }
        glPopMatrix();

    }

// -----------------------------------------------------------------------------------------------------------------
    private static void plotSword() {

        glColor3f(0.0f, 0.0f, 0.0f);

        // plot base
        glPushMatrix();
        {
            glTranslatef(0.0f, 1.0f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(0.5f, 3.5f, 0.3f);
            plotUnitCube();
        }
        glPopMatrix();

        glPushMatrix();
        {
            glTranslatef(0.0f, 0.5f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(2.0f, 0.5f, 0.3f);
            plotUnitCube();
        }
        glPopMatrix();

        glPushMatrix();
        {
            glTranslatef(0.75f, 0.9f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(0.5f, 1.0f, 0.3f);
            plotUnitCube();
        }
        glPopMatrix();

        glPushMatrix();
        {
            glTranslatef(-0.75f, 0.9f, 0.0f);
            glScalef(0.5f, 0.5f, 0.5f);
            glScalef(0.5f, 1.0f, 0.3f);
            plotUnitCube();
        }
        glPopMatrix();
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static void plotCylinder(float cx, float cy, float cz, float h, float r) {

        // plot cone 1
        glPushMatrix();
        {
            glTranslatef(cx, cy, cz);
            glScalef(r, h, r);
            plotUnitCylinder(16);
        }
        glPopMatrix();

    }
    // -----------------------------------------------------------------------------------------------------------------// -----------------------------------------------------------------------------------------------------------------

    /**
     * Render a unit cube.
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
        glTranslatef(-player.x * .25f, 0.0f, -player.z * .25f);

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
        p.potionsInit();
        TerrainInit();

    }

    // -------------------------------------------------------------------------

    /**
     * Main loop of the application. Repeats until finished variable takes on
     * true.
     */
    static void gameRun() {

        long timeStamp = System.currentTimeMillis();
        while (!finished) {

            // perform time step and render
            float dt = 0.001f*(System.currentTimeMillis() - timeStamp);
            {
                gameHandleInput();
                gameUpdate(dt);
                gameHandleCollisions();
                gameRenderFrame();
            }
            timeStamp = System.currentTimeMillis();
            Display.sync(FRAME_RATE);

            // make sure display is updated
            Display.update();
            if (Display.isCloseRequested()) {
                finished = true;
            }

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

        player.playerUpdate(dt);
        enemiesUpdate(dt);

    }

    // -------------------------------------------------------------------------

    /**
     * Check for relevant collisions and handle them.
     */
    static void gameHandleCollisions() {

        // TODO: add necessary collision checks and behaviors.

        collisionShotsAndEnemies();

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
            worldPlotFloor2(0);
            worldPlotFloor2(elevation2);
            worldPlotFloor3(0);
            worldPlotFloor3(elevation3);
            playerPlotShots();
            enemiesPlot();
            p.plotPotion();

            glPushMatrix();{
            glScalef(0.2f,0.2f,0.2f);
            plotTreasureChest();
            float height = (float) Math.sin((float)(System.currentTimeMillis() % (200 * 2*PI)) / 200f);
            glTranslatef(0.0f,2.0f+height,0.0f);
            plotSword();
            glPopMatrix();
            }
            terrainPlot();
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

    // -------------------------------------------------------------------------

    /**
     * Clean up before exit.
     */
    static void gameCleanup() {

        // Close the window
        Display.destroy();
        Mouse.destroy();
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
    static float elevation = random (10, 20);
    static float elevation2= random  (20,30);
    static float elevation3= random (30,40);
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
