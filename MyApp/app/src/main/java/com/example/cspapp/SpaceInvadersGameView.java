package com.example.cspapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceInvadersGameView extends SurfaceView implements Runnable {

    private String imageUrl;
    private Bitmap backgroundImage;
    private boolean isBackgroundLoaded = false;

    private Thread gameThread;
    private volatile boolean isPlaying;
    private boolean isGameOver;
    private boolean isGameWon;

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Game objects
    private PlayerShip playerShip;
    private List<Invader> invaders = new ArrayList<>();
    private List<Bullet> playerBullets = new ArrayList<>();
    private List<Bullet> invaderBullets = new ArrayList<>();
    private DefenseBunker[] bunkers = new DefenseBunker[4];

    // Game settings
    private int score = 0;
    private int lives = 3;
    private final int INVADER_ROWS = 4; // Reduced from 5 to 4
    private final int INVADER_COLS = 9; // Reduced from 10 to 9
    private int invaderMovementDirection = 1;  // 1 for right, -1 for left
    private int movementSpeed;
    private long lastInvaderBulletTime = 0;
    private long invaderBulletDelay = 1000; // ms
    private Random random = new Random();

    // Movement controls
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    private long lastPlayerShotTime = 0;
    private final long PLAYER_SHOT_COOLDOWN = 400; // milliseconds between shots

    // Drawing objects
    private final SurfaceHolder surfaceHolder;
    private final Paint paint;

    // Timing
    private long nextFrameTime;
    private long fps;
    private long MILLIS_PER_SECOND = 1000;

    public SpaceInvadersGameView(Context context, int gameSpeed) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();

        // Set FPS based on the game speed (5-15 scale)
        fps = 60;
        movementSpeed = gameSpeed;

        // Get screen dimensions
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Initialize game
        initializeGame();
    }

    public void setBackgroundImageUrl(String url) {
        this.imageUrl = url;
        if (url != null && !url.isEmpty()) {
            loadBackgroundImage();
        }
    }

    // Add method to load background image using Glide
    private void loadBackgroundImage() {
        Glide.with(getContext())
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        // Scale the bitmap to fill the screen
                        backgroundImage = Bitmap.createScaledBitmap(
                                resource,
                                screenWidth,
                                screenHeight,
                                false);
                        isBackgroundLoaded = true;
                    }
                });
    }

    private void initializeGame() {
        // Create player ship
        playerShip = new PlayerShip(screenWidth, screenHeight);

        // Create invaders
        createInvaders();

        // Create defense bunkers
        createBunkers();

        // Reset game state
        score = 0;
        lives = 3;
        isGameOver = false;
        isGameWon = false;
        isMovingLeft = false;
        isMovingRight = false;
    }

    private void createInvaders() {
        invaders.clear();

        int invaderWidth = screenWidth / 20;
        int invaderHeight = screenHeight / 25;
        int spacingX = invaderWidth / 2;
        int spacingY = invaderHeight;
        int startX = screenWidth / 8;
        int startY = screenHeight / 8;

        for (int row = 0; row < INVADER_ROWS; row++) {
            int invaderType = row / 2; // Different types for visual variety
            for (int col = 0; col < INVADER_COLS; col++) {
                int x = startX + col * (invaderWidth + spacingX);
                int y = startY + row * (invaderHeight + spacingY);
                invaders.add(new Invader(x, y, invaderWidth, invaderHeight, invaderType));
            }
        }
    }

    private void createBunkers() {
        int bunkerWidth = screenWidth / 8;
        int bunkerHeight = screenHeight / 16;
        int startY = screenHeight - (screenHeight / 4);

        for (int i = 0; i < bunkers.length; i++) {
            int startX = (screenWidth / 5) * (i + 1) - (bunkerWidth / 2);
            bunkers[i] = new DefenseBunker(startX, startY, bunkerWidth, bunkerHeight);
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
    }

    private boolean updateRequired() {
        if (nextFrameTime <= System.currentTimeMillis()) {
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / fps;
            return true;
        }
        return false;
    }

    private void update() {
        if (isGameOver || isGameWon) {
            return;
        }

        // Update player ship with new movement controls
        if (isMovingLeft) {
            playerShip.moveLeft();
        }
        if (isMovingRight) {
            playerShip.moveRight();
        }

        // Update player bullets
        updatePlayerBullets();

        // Update invader bullets
        updateInvaderBullets();

        // Update invaders
        updateInvaders();

        // Check if all invaders are destroyed
        if (invaders.isEmpty()) {
            isGameWon = true;
        }

        // Chance for invaders to shoot
        if (System.currentTimeMillis() - lastInvaderBulletTime > invaderBulletDelay) {
            invaderShoot();
            lastInvaderBulletTime = System.currentTimeMillis();
            // Adjust bullet delay based on number of invaders left
            invaderBulletDelay = Math.max(300, 1000 - (INVADER_ROWS * INVADER_COLS - invaders.size()) * 10);
        }
    }

    private void updatePlayerBullets() {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : playerBullets) {
            bullet.update();

            // Check if bullet is off screen
            if (bullet.getY() < 0) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // Check for collision with invaders
            List<Invader> invadersToRemove = new ArrayList<>();
            for (Invader invader : invaders) {
                if (bullet.collidesWith(invader.getRect())) {
                    bulletsToRemove.add(bullet);
                    invadersToRemove.add(invader);
                    score += (INVADER_ROWS - invader.getType()) * 10;
                    break;
                }
            }
            invaders.removeAll(invadersToRemove);

            // Check for collision with bunkers
            for (DefenseBunker bunker : bunkers) {
                if (bunker.isActive() && bunker.getRect().intersect(bullet.getRect())) {
                    bunker.takeDamage();
                    bulletsToRemove.add(bullet);
                    break;
                }
            }
        }

        playerBullets.removeAll(bulletsToRemove);
    }

    private void updateInvaderBullets() {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : invaderBullets) {
            bullet.update();

            // Check if bullet is off screen
            if (bullet.getY() > screenHeight) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // Check for collision with player
            if (bullet.collidesWith(playerShip.getRect())) {
                bulletsToRemove.add(bullet);
                lives--;
                if (lives <= 0) {
                    isGameOver = true;
                }
                continue;
            }

            // Check for collision with bunkers
            for (DefenseBunker bunker : bunkers) {
                if (bunker.isActive() && bunker.getRect().intersect(bullet.getRect())) {
                    bunker.takeDamage();
                    bulletsToRemove.add(bullet);
                    break;
                }
            }
        }

        invaderBullets.removeAll(bulletsToRemove);
    }

    private void updateInvaders() {
        boolean moveDown = false;
        float maxX = 0;
        float minX = screenWidth;

        // Find leftmost and rightmost invaders
        for (Invader invader : invaders) {
            float right = invader.getX() + invader.getWidth();
            if (right > maxX) maxX = right;
            if (invader.getX() < minX) minX = invader.getX();
        }

        // Check if invaders need to change direction
        if (maxX >= screenWidth - 10 && invaderMovementDirection > 0) {
            invaderMovementDirection = -1;
            moveDown = true;
        } else if (minX <= 10 && invaderMovementDirection < 0) {
            invaderMovementDirection = 1;
            moveDown = true;
        }

        // Update all invaders - REDUCED SPEED by dividing by 4 instead of 2
        for (Invader invader : invaders) {
            if (moveDown) {
                invader.moveDown(screenHeight / 30);

                // Check if invaders reached the bottom
                if (invader.getY() + invader.getHeight() > playerShip.getY()) {
                    isGameOver = true;
                    return;
                }
            } else {
                invader.move(invaderMovementDirection * (movementSpeed / 4));
            }
        }
    }

    private void invaderShoot() {
        if (invaders.isEmpty()) return;

        // Choose a random invader to shoot from the lowest row of each column
        int[] lowestInvaderInColumn = new int[INVADER_COLS];
        for (int i = 0; i < INVADER_COLS; i++) {
            lowestInvaderInColumn[i] = -1;
        }

        for (int i = 0; i < invaders.size(); i++) {
            Invader invader = invaders.get(i);
            int column = i % INVADER_COLS;
            if (lowestInvaderInColumn[column] == -1 ||
                    invaders.get(lowestInvaderInColumn[column]).getY() < invader.getY()) {
                lowestInvaderInColumn[column] = i;
            }
        }

        List<Integer> shootingCandidates = new ArrayList<>();
        for (int i = 0; i < INVADER_COLS; i++) {
            if (lowestInvaderInColumn[i] != -1) {
                shootingCandidates.add(lowestInvaderInColumn[i]);
            }
        }

        if (!shootingCandidates.isEmpty()) {
            int shooterIndex = shootingCandidates.get(random.nextInt(shootingCandidates.size()));
            Invader shooter = invaders.get(shooterIndex);

            invaderBullets.add(new Bullet(
                    shooter.getX() + shooter.getWidth() / 2,
                    shooter.getY() + shooter.getHeight(),
                    false
            ));
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();

            // Draw background - either image or solid color
            if (isBackgroundLoaded && backgroundImage != null) {
                // Draw the background image
                canvas.drawBitmap(backgroundImage, 0, 0, null);

                // Add a semi-transparent overlay to make game elements more visible
                paint.setColor(Color.BLACK);
                paint.setAlpha(100); // 40% opacity
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
                paint.setAlpha(255); // Reset to full opacity
            } else {
                // Default black background
                canvas.drawColor(Color.BLACK);
            }

            // Draw player ship
            playerShip.draw(canvas, paint);

            // Draw invaders
            for (Invader invader : invaders) {
                invader.draw(canvas, paint);
            }

            // Draw player bullets
            paint.setColor(Color.GREEN);
            for (Bullet bullet : playerBullets) {
                bullet.draw(canvas, paint);
            }

            // Draw invader bullets
            paint.setColor(Color.RED);
            for (Bullet bullet : invaderBullets) {
                bullet.draw(canvas, paint);
            }

            // Draw bunkers
            for (DefenseBunker bunker : bunkers) {
                if (bunker.isActive()) {
                    bunker.draw(canvas, paint);
                }
            }

            // Draw score and lives
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Score: " + score, 20, 60, paint);
            canvas.drawText("Lives: " + lives, screenWidth - 200, 60, paint);

            // Draw game over or win message
            if (isGameOver) {
                displayGameOverMessage(canvas);
            } else if (isGameWon) {
                displayGameWonMessage(canvas);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void displayGameOverMessage(Canvas canvas) {
        paint.setTextSize(100);
        paint.setColor(Color.RED);
        canvas.drawText("GAME OVER", screenWidth / 2f - 250, screenHeight / 2f, paint);
        paint.setTextSize(60);
        paint.setColor(Color.WHITE);
        canvas.drawText("Final Score: " + score, screenWidth / 2f - 180, screenHeight / 2f + 100, paint);
        canvas.drawText("Tap to play again", screenWidth / 2f - 200, screenHeight / 2f + 200, paint);
    }

    private void displayGameWonMessage(Canvas canvas) {
        paint.setTextSize(100);
        paint.setColor(Color.GREEN);
        canvas.drawText("YOU WIN!", screenWidth / 2f - 200, screenHeight / 2f, paint);
        paint.setTextSize(60);
        paint.setColor(Color.WHITE);
        canvas.drawText("Score: " + score, screenWidth / 2f - 100, screenHeight / 2f + 100, paint);
        canvas.drawText("Tap to play again", screenWidth / 2f - 200, screenHeight / 2f + 200, paint);
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            // Error handling
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Set movement flag when button is pressed
    public void setMovingLeft(boolean moving) {
        isMovingLeft = moving;
    }

    // Set movement flag when button is pressed
    public void setMovingRight(boolean moving) {
        isMovingRight = moving;
    }

    // Fire a bullet when button is pressed
    public void firePlayerBullet() {
        if (!isGameOver && !isGameWon) {
            long currentTime = System.currentTimeMillis();
            // Check if cooldown period has elapsed
            if (currentTime - lastPlayerShotTime > PLAYER_SHOT_COOLDOWN) {
                playerBullets.add(new Bullet(
                        playerShip.getX() + playerShip.getWidth() / 2,
                        playerShip.getY(),
                        true
                ));
                lastPlayerShotTime = currentTime;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver || isGameWon) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Reset the game on tap if game over
                initializeGame();
                return true;
            }
        }
        return true;
    }

    // Game objects
    class PlayerShip {
        private float x;
        private float y;
        private float width;
        private float height;
        private final float SPEED = 7.5f; // 50% slower (was 15f)

        public PlayerShip(int screenWidth, int screenHeight) {
            width = screenWidth / 15;
            height = screenHeight / 25;
            x = screenWidth / 2f - width / 2;
            y = screenHeight - height * 3;
        }

        public void moveLeft() {
            x -= SPEED;
            if (x < 0) x = 0;
        }

        public void moveRight() {
            x += SPEED;
            if (x + width > screenWidth) x = screenWidth - width;
        }

        public void update() {
            // No longer needs auto-movement update as we're using direct controls
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(Color.WHITE);

            // Draw the ship as a rectangle with a triangle on top
            RectF rect = new RectF(x, y, x + width, y + height);
            canvas.drawRect(rect, paint);

            // Draw cannon
            float cannonWidth = width / 5;
            float cannonHeight = height / 2;
            RectF cannon = new RectF(
                    x + width / 2 - cannonWidth / 2,
                    y - cannonHeight,
                    x + width / 2 + cannonWidth / 2,
                    y
            );
            canvas.drawRect(cannon, paint);
        }

        public RectF getRect() {
            return new RectF(x, y, x + width, y + height);
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
    }

    class Invader {
        private float x;
        private float y;
        private float width;
        private float height;
        private int type; // 0, 1, 2 for different types

        public Invader(float x, float y, float width, float height, int type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public void move(float dx) {
            x += dx;
        }

        public void moveDown(float dy) {
            y += dy;
        }

        public void draw(Canvas canvas, Paint paint) {
            // Different colors based on type
            switch (type % 3) {
                case 0:
                    paint.setColor(Color.RED);
                    break;
                case 1:
                    paint.setColor(Color.GREEN);
                    break;
                case 2:
                    paint.setColor(Color.BLUE);
                    break;
            }

            canvas.drawRect(x, y, x + width, y + height, paint);

            // Draw eyes
            paint.setColor(Color.WHITE);
            float eyeSize = width / 5;
            canvas.drawCircle(x + width / 3, y + height / 3, eyeSize, paint);
            canvas.drawCircle(x + width * 2 / 3, y + height / 3, eyeSize, paint);
        }

        public RectF getRect() {
            return new RectF(x, y, x + width, y + height);
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public int getType() { return type; }
    }

    class Bullet {
        private float x;
        private float y;
        private final float width = 6f;
        private final float height = 20f;
        private final float speed;
        private final boolean isPlayerBullet;

        public Bullet(float x, float y, boolean isPlayerBullet) {
            this.x = x - width / 2;
            this.y = y;
            this.isPlayerBullet = isPlayerBullet;

            // Player bullets move up, invader bullets move down
            this.speed = isPlayerBullet ? -20f : 10f;
        }

        public void update() {
            y += speed;
        }

        public void draw(Canvas canvas, Paint paint) {
            canvas.drawRect(x, y, x + width, y + height, paint);
        }

        public boolean collidesWith(RectF rect) {
            return RectF.intersects(getRect(), rect);
        }

        public RectF getRect() {
            return new RectF(x, y, x + width, y + height);
        }

        public float getY() { return y; }
    }

    class DefenseBunker {
        private float x;
        private float y;
        private float width;
        private float height;
        private int health = 3;

        public DefenseBunker(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void takeDamage() {
            health--;
        }

        public boolean isActive() {
            return health > 0;
        }

        private void loadBackgroundImage() {
            Glide.with(getContext())
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            // Scale the bitmap to fill the screen
                            backgroundImage = Bitmap.createScaledBitmap(
                                    resource,
                                    screenWidth,
                                    screenHeight,
                                    false);
                            isBackgroundLoaded = true;
                        }
                    });
        }
        public void draw(Canvas canvas, Paint paint) {
            // Change color based on health
            switch (health) {
                case 3:
                    paint.setColor(0xFF00FF00); // Bright green
                    break;
                case 2:
                    paint.setColor(0xFFFFFF00); // Yellow
                    break;
                case 1:
                    paint.setColor(0xFFFF7F00); // Orange
                    break;
            }

            // Draw the bunker
            canvas.drawRect(x, y, x + width, y + height, paint);

            // Draw a cutout in the middle top
            float cutoutWidth = width / 3;
            float cutoutHeight = height / 2;
            paint.setColor(Color.BLACK);
            canvas.drawRect(
                    x + width / 2 - cutoutWidth / 2,
                    y,
                    x + width / 2 + cutoutWidth / 2,
                    y + cutoutHeight,
                    paint
            );
        }

        public RectF getRect() {
            return new RectF(x, y, x + width, y + height);
        }
    }
}