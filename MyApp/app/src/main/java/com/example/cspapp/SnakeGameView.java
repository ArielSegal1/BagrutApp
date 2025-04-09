package com.example.cspapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private volatile boolean isPlaying;
    private boolean isGameOver;

    // Game objects
    private final ArrayList<Point> snakeBody = new ArrayList<>();
    private Point food = new Point();

    // Direction constants
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    private int direction = RIGHT;

    // Game settings
    private final int blockSize = 50;
    private final int NUM_BLOCKS_WIDE = 20;
    private final int NUM_BLOCKS_HIGH;

    // Drawing objects
    private final SurfaceHolder surfaceHolder;
    private final Paint paint;

    // Game state
    private int score = 0;
    private long nextFrameTime;
    private final long FPS = 10;
    private final Random random = new Random();

    public SnakeGameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();

        // Calculate how many blocks high based on the height of the screen
        NUM_BLOCKS_HIGH = getResources().getDisplayMetrics().heightPixels / blockSize;

        // Initialize the snake
        snakeBody.clear();
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2, NUM_BLOCKS_HIGH / 2));
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2 - 1, NUM_BLOCKS_HIGH / 2));
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2 - 2, NUM_BLOCKS_HIGH / 2));

        // First food
        spawnFood();
    }

    private void spawnFood() {
        food.x = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        food.y = random.nextInt(NUM_BLOCKS_HIGH - 1) + 1;

        // Check if food spawned on snake body
        for (Point segment : snakeBody) {
            if (segment.x == food.x && segment.y == food.y) {
                spawnFood(); // Try again
                break;
            }
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

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            // Error handling
        }
    }

    public void reset() {
        // Reset the game
        snakeBody.clear();
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2, NUM_BLOCKS_HIGH / 2));
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2 - 1, NUM_BLOCKS_HIGH / 2));
        snakeBody.add(new Point(NUM_BLOCKS_WIDE / 2 - 2, NUM_BLOCKS_HIGH / 2));

        // Reset direction and score
        direction = RIGHT;
        score = 0;
        spawnFood();
        isGameOver = false;
    }

    private void update() {
        if (isGameOver) {
            return;
        }

        // Update snake position (move head)
        Point head = snakeBody.get(0);
        Point newHead = new Point(head);

        // Move based on direction
        switch (direction) {
            case UP:
                newHead.y--;
                break;
            case RIGHT:
                newHead.x++;
                break;
            case DOWN:
                newHead.y++;
                break;
            case LEFT:
                newHead.x--;
                break;
        }

        // Check for collision with walls
        if (newHead.x < 0 || newHead.y < 0 || newHead.x >= NUM_BLOCKS_WIDE || newHead.y >= NUM_BLOCKS_HIGH) {
            isGameOver = true;
            return;
        }

        // Check for collision with self
        for (int i = 1; i < snakeBody.size(); i++) {
            if (newHead.x == snakeBody.get(i).x && newHead.y == snakeBody.get(i).y) {
                isGameOver = true;
                return;
            }
        }

        // Add new head
        snakeBody.add(0, newHead);

        // Check for eating food
        if (newHead.x == food.x && newHead.y == food.y) {
            // Grow snake (don't remove tail)
            score++;
            spawnFood();
        } else {
            // Remove tail if not growing
            snakeBody.remove(snakeBody.size() - 1);
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();

            // Clear screen and set background
            canvas.drawColor(Color.BLACK);

            // Draw snake
            paint.setColor(Color.GREEN);
            for (Point segment : snakeBody) {
                canvas.drawRect(
                        segment.x * blockSize,
                        segment.y * blockSize,
                        (segment.x + 1) * blockSize,
                        (segment.y + 1) * blockSize,
                        paint);
            }

            // Draw food
            paint.setColor(Color.RED);
            canvas.drawRect(
                    food.x * blockSize,
                    food.y * blockSize,
                    (food.x + 1) * blockSize,
                    (food.y + 1) * blockSize,
                    paint);

            // Draw score
            paint.setColor(Color.WHITE);
            paint.setTextSize(70);
            canvas.drawText("Score: " + score, 10, 70, paint);

            // Draw game over message
            if (isGameOver) {
                paint.setTextSize(90);
                paint.setColor(Color.RED);
                canvas.drawText("GAME OVER", canvas.getWidth() / 2f - 240, canvas.getHeight() / 2f, paint);
                paint.setTextSize(70);
                canvas.drawText("Tap to Play Again", canvas.getWidth() / 2f - 220, canvas.getHeight() / 2f + 100, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private boolean updateRequired() {
        if (nextFrameTime <= System.currentTimeMillis()) {
            nextFrameTime = System.currentTimeMillis() + 1000 / FPS;
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isGameOver) {
                reset();
                return true;
            }

            // Handle direction changes
            float x = event.getX();
            float y = event.getY();

            Point head = snakeBody.get(0);
            float centerX = head.x * blockSize + blockSize / 2f;
            float centerY = head.y * blockSize + blockSize / 2f;

            // Calculate angle from head to touch
            float dx = x - centerX;
            float dy = y - centerY;

            // Determine new direction based on angle
            if (Math.abs(dx) > Math.abs(dy)) {
                // Horizontal movement
                if (dx > 0 && direction != LEFT) {
                    direction = RIGHT;
                } else if (dx < 0 && direction != RIGHT) {
                    direction = LEFT;
                }
            } else {
                // Vertical movement
                if (dy > 0 && direction != UP) {
                    direction = DOWN;
                } else if (dy < 0 && direction != DOWN) {
                    direction = UP;
                }
            }
        }
        return true;
    }
}