package com.survivor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Array;

public class Main implements ApplicationListener {

    private Texture backgroundTexture;
    private Texture spriteSheet;
    private Texture slimeSheet;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Rectangle dinoRectangle;
    private Rectangle slimeRectangle;

    // Dinosaur animation
    private Animation<TextureRegion> animation;
    private float elapsedTime = 0;

    // Slime animation
    private Animation<TextureRegion> slimeAnimation;

    // Store slime positions and individual animation timers
    private Array<Rectangle> slimes;
    private Array<Float> slimeTimers;

    private static final int SLIME_COUNT = 20;
    private static final int SPRITE_SIZE = 16;
    private float spawnTimer = 0f;
    private float spawnInterval = 3f;
    private int slimesSpawned = 0;
    // Dinosaur position
    private float dinoX = 357;
    private float dinoY = 370;

    private boolean facingLeft = false;
    private int health = 10;

    @Override
    public void create() {

        // Load images
        backgroundTexture = new Texture("World.png");
        spriteSheet = new Texture("Dino.png");
        slimeSheet = new Texture("Slime.png");

        // Initialize drawing and viewport
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(694, 740);

        dinoRectangle = new Rectangle();
        slimeRectangle = new Rectangle();
        // -----------------------------
        // Dinosaur animation
        // -----------------------------

        TextureRegion[][] dinoFrames = TextureRegion.split(
            spriteSheet,
            SPRITE_SIZE,
            SPRITE_SIZE
        );

        animation = new Animation<>(
            0.15f,
            dinoFrames[0]
        );

        animation.setPlayMode(Animation.PlayMode.LOOP);

        // -----------------------------
        // Slime animation
        // -----------------------------

        TextureRegion[][] slimeFrames = TextureRegion.split(
            slimeSheet,
            SPRITE_SIZE,
            SPRITE_SIZE
        );

        slimeAnimation = new Animation<>(
            0.15f,
            slimeFrames[0]
        );

        slimeAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Initialize slime arrays
        slimes = new Array<>();
        slimeTimers = new Array<>();

        // Spawn slimes


    }


    private void spawnOneSlime() {

        Rectangle slime = new Rectangle(0, 0, 16, 16);

        boolean overlapping;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            slime.x = MathUtils.random(97, 594 - 16);
            slime.y = MathUtils.random(118, 616 - 16);

            overlapping = slime.overlaps(dinoRectangle);

            for (Rectangle other : slimes) {
                if (slime.overlaps(other)) {
                    overlapping = true;
                    break;
                }
            }

            attempts++;

        } while (overlapping && attempts < maxAttempts);

        // Only add the slime if a valid position was found
        if (!overlapping) {
            slimes.add(slime);
            slimeTimers.add(MathUtils.random(0f, 1f));
        }
    }


    @Override
    public void resize(int width, int height) {

        if (width <= 0 || height <= 0) {
            return;
        }

        viewport.update(width, height, true);
    }

    @Override
    public void render() {

        // Update animation time
        float delta = Gdx.graphics.getDeltaTime();
        elapsedTime += delta;

        // Update game
        input(delta);
        logic();
        enemy(delta);

        // Clear screen
        ScreenUtils.clear(0, 0, 0, 1);

        // Camera
        OrthographicCamera orthoCamera =
            (OrthographicCamera) viewport.getCamera();

        orthoCamera.zoom = 0.35f;

        float targetX = dinoX + 4;
        float targetY = dinoY + 4;

        orthoCamera.position.set(targetX, targetY, 0);
        orthoCamera.update();

        spriteBatch.setProjectionMatrix(orthoCamera.combined);

        spriteBatch.begin();

        // Draw background
        spriteBatch.draw(
            backgroundTexture,
            0,
            0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );

        // -----------------------------
        // Draw dinosaur
        // -----------------------------

        TextureRegion currentFrame =
            animation.getKeyFrame(elapsedTime);

        TextureRegion drawFrame =
            new TextureRegion(currentFrame);

        drawFrame.flip(facingLeft, false);

        spriteBatch.draw(
            drawFrame,
            dinoX,
            dinoY,
            SPRITE_SIZE,
            SPRITE_SIZE
        );

        // -----------------------------
        // Draw animated slimes
        // -----------------------------

        for (int i = 0; i < slimes.size; i++) {

            Rectangle slime = slimes.get(i);

            TextureRegion slimeFrame =
                slimeAnimation.getKeyFrame(slimeTimers.get(i));

            spriteBatch.draw(
                slimeFrame,
                slime.x,
                slime.y,
                slime.width,
                slime.height
            );
        }

        spriteBatch.end();
    }

    private void input(float delta) {

        float speed = 100f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dinoX += (speed * delta*(elapsedTime/100))+3;
            facingLeft = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dinoX -= (speed * delta*(elapsedTime/100))+3;
            facingLeft = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dinoY += (speed * delta*(elapsedTime/100))+3;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dinoY -= (speed * delta *(elapsedTime/100))+3;
        }
    }

    private void logic() {

        // Keep dinosaur inside map boundaries
        dinoX = MathUtils.clamp(
            dinoX,
            97,
            594 - SPRITE_SIZE
        );

        dinoY = MathUtils.clamp(
            dinoY,
            118,
            616 - SPRITE_SIZE
        );

        // Update dinosaur collision rectangle
        dinoRectangle.set(
            dinoX,
            dinoY,
            SPRITE_SIZE,
            SPRITE_SIZE
        );

        if(health == 0){
            Gdx.app.log("Lose", "You lose");
            System.exit(0);
        }

    }



    private void enemy(float delta) {

        spawnTimer += delta;

        if (spawnTimer >= spawnInterval
            && slimesSpawned < SLIME_COUNT) {

            spawnOneSlime();

            spawnTimer = 0f;
            slimesSpawned++;
        }
        float slimeSpeed = 30f;
        float separationSpeed = 40f;
        float minimumDistance = 16f;

        // Update slime timers and movement
        for (int i = 0; i < slimes.size; i++) {

            Rectangle slime = slimes.get(i);

            slimeTimers.set(
                i,
                slimeTimers.get(i) + delta
            );

            // Slime center
            float slimeCenterX = slime.x + slime.width / 2f;
            float slimeCenterY = slime.y + slime.height / 2f;

            // Direction toward dinosaur
            float dinoCenterX = dinoX + 8;
            float dinoCenterY = dinoY + 8;

            float dx = dinoCenterX - slimeCenterX;
            float dy = dinoCenterY - slimeCenterY;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            float moveX = 0;
            float moveY = 0;

            // Chase dinosaur
            if (distance > 0.001f) {
                moveX += (dx / distance) * slimeSpeed;
                moveY += (dy / distance) * slimeSpeed;
            }

            // Avoid other slimes
            for (int j = 0; j < slimes.size; j++) {

                if (i == j) {
                    continue;
                }

                Rectangle other = slimes.get(j);

                float otherCenterX = other.x + other.width / 2f;
                float otherCenterY = other.y + other.height / 2f;

                float awayX = slimeCenterX - otherCenterX;
                float awayY = slimeCenterY - otherCenterY;

                float separationDistance = (float) Math.sqrt(
                    awayX * awayX + awayY * awayY
                );

                // Push away when slimes are too close
                if (separationDistance > 0.001f &&
                    separationDistance < minimumDistance) {

                    float strength =
                        (minimumDistance - separationDistance)
                            / minimumDistance;

                    moveX += (awayX / separationDistance)
                        * separationSpeed * strength;

                    moveY += (awayY / separationDistance)
                        * separationSpeed * strength;
                }
            }

            // Normalize movement so diagonal movement isn't faster
            float moveLength = (float) Math.sqrt(
                moveX * moveX + moveY * moveY
            );

            if (moveLength > 0.001f) {
                slime.x += (moveX / moveLength)
                    * slimeSpeed * delta *(elapsedTime/5);

                slime.y += (moveY / moveLength)
                    * slimeSpeed * delta*(elapsedTime/5);
            }

            // Check collision with dinosaur
            if (dinoRectangle.overlaps(slime)) {
                slimes.removeIndex(i);
                slimeTimers.removeIndex(i);
                Gdx.app.log("Collision", "You were hit by a slime.");

                i--;
                health -= 1;
                Gdx.app.log("Hits left", "You have "+health+" health left");
            }
        }
    }



    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {

        backgroundTexture.dispose();
        spriteSheet.dispose();
        slimeSheet.dispose();
        spriteBatch.dispose();
    }
}
