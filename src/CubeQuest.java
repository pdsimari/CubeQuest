import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
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
 *
 * James Conroy
 * Patricio Simari, PhD
 * Michael Monaghan
 * Tan Tran
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

        // position in the zx plane
        float x = 0.0f;
        float z = 0.0f;

        // direction of movement (+/- 1)
        float dx = 0.0f;
        float dz = 0.0f;

        // facing direction
        Direction facing = Direction.SOUTH;

        // shots
        final PlayerShot[] shots = new PlayerShot[PLAYER_SHOT_MAX];

        // age (in seconds)
        float t = 0.0f;

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
     * THe player.
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

    /**
     * Update player given dt, the number of seconds since last update.
     *
     * @param dt A float.
     */
    static void playerUpdate(float dt) {

        // update player position
        player.x += player.dx*PLAYER_SPEED*dt;
        player.z += player.dz*PLAYER_SPEED*dt;
        player.t += dt;

        // update player shots (if active)
        for (PlayerShot shot : player.shots) {
            if (shot.t < PLAYER_SHOT_DURATION) {

                shot.t += dt;
                shot.x += shot.dx*PLAYER_SHOT_SPEED*dt;
                shot.z += shot.dz*PLAYER_SHOT_SPEED*dt;

            }
        }

    }

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
            glTranslatef(0.0f, 0.5f, 0.0f);
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
                shot.dx = 0.0f;
                shot.dz = 0.0f;

                // set velocity according to player facing direction
                switch (player.facing) {
                    case EAST:  shot.dx = +1.0f;
                        break;
                    case WEST:  shot.dx = -1.0f;
                        break;
                    case NORTH: shot.dz = -1.0f;
                        break;
                    case SOUTH: shot.dz = +1.0f;
                        break;
                }

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
    static final int ENEMY_COUNT = 10;

    /**
     * Size of the enemies.
     */
    static final float ENEMY_SIZE = 0.5f;

    /**
     * Enemy speed in distance per second.
     */
    static final float ENEMY_SPEED = 1.0f;

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
                    glColor3f(0.0f, 1.0f, 0.0f);
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

    // =========================================================================
    // WORLD
    // =========================================================================

    /**
     * Bounds of the world where enemies can spawn.
     */
    static final float WORLD_RADIUS = 20.0f;

    /**
     * Scale factor used for rendering.
     */
    static float WORLD_SCALE = 0.25f;

    // -------------------------------------------------------------------------

    /**
     * Plot a grid on the ZX plane.
     */
    static void worldPlotFloor() {

        float lower = (float) floor(-camera.farPlane) - 0.5f;
        float upper = (float)  ceil(+camera.farPlane) + 0.5f;

        glDisable(GL_LIGHTING);
        {
            glColor4f(0.75f, 0.75f, 0.75f, 0.75f);
            glLineWidth(0.2f);
            glBegin(GL_LINES);
            {
                glNormal3f(0.0f, 1.0f, 0.0f);
                for (float x = lower; x <= upper; x += 1.0f) {
                    glVertex3f(x, 0.0f, -camera.farPlane);
                    glVertex3f(x, 0.0f, +camera.farPlane);
                }
                for (float z = lower; z <= upper; z += 1.0f) {
                    glVertex3f(-camera.farPlane, 0.0f, z);
                    glVertex3f(+camera.farPlane, 0.0f, z);
                }
            }
            glEnd();
        }
        glEnable(GL_LIGHTING);

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
        public float elevation   = -37.5f;
        public float distance    =  6.0f;

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

        glTranslatef(0.0f, 0.0f, -camera.distance);
        glRotatef(-camera.elevation, 1.0f, 0.0f, 0.0f);
        glRotatef(-camera.azimuth,   0.0f, 1.0f, 0.0f);

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

        // initialize the display
        Display.setTitle(APP_TITLE);
        Display.setFullscreen(false);
        Display.setVSyncEnabled(true);
        Display.setResizable(true);
        Display.create();

        // get display size
        int width = Display.getDisplayMode().getWidth();
        int height = Display.getDisplayMode().getHeight();

        // viewport
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glViewport(0, 0, width, height);

        // perspective transformation
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspectRatio = ((float) width)/height;
        gluPerspective(camera.fieldOfView, aspectRatio,
                       camera.nearPlane, camera.farPlane);

        // background color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

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

    /**
     * Handle input to the game.
     */
    static void gameHandleInput() {

        // arrow keys
        player.dx = 0.0f;
        player.dz = 0.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)||Keyboard.isKeyDown(Keyboard.KEY_A)) {
            player.dx = -1.0f;
            player.facing = Direction.WEST;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)||Keyboard.isKeyDown(Keyboard.KEY_S)) {
            player.dx = +1.0f;
            player.facing = Direction.EAST;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)||Keyboard.isKeyDown(Keyboard.KEY_W)) {
            player.dz = -1.0f;
            player.facing = Direction.NORTH;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)||Keyboard.isKeyDown(Keyboard.KEY_S)) {
            player.dz = +1.0f;
            player.facing = Direction.SOUTH;
        }

        // space bar
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            playerShoot();
        }

        // escape to quit
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState() &&
                Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {

                finished = true;

            }
        }

        // TODO: Add other game input handling.

    }

    // -------------------------------------------------------------------------

    /**
     * Handle input and update scene.
     */
    static void gameUpdate(float dt) {

        // TODO: add updates to all game elements.

        playerUpdate(dt);
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

        // clear the screen and depth buffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // viewing transformation (bottom of the model-view stack)
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
        {

            // go to 3rd person view of player
            cameraTransformation();
            glScalef(WORLD_SCALE, WORLD_SCALE, WORLD_SCALE);
            playerPlotAvatar();

            // undo player location
            glTranslatef(-player.x, 0.0f, -player.z);

            // TODO: plot all game elements

            worldPlotFloor();
            playerPlotShots();
            enemiesPlot();

        }
        glPopMatrix();

    }

    // -------------------------------------------------------------------------

    /**
     * Clean up before exit.
     */
    static void gameCleanup() {

        // Close the window
        Display.destroy();

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
