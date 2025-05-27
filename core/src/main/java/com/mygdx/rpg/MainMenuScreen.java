package com.mygdx.rpg; // Hoặc package của bạn

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
// TextButton không còn cần thiết nếu Start Game cũng là ImageButton
// import com.badlogic.gdx.scenes.scene2d.ui.TextButton; 
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton; 
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable; 
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    private final RPGGame game;
    private Stage stage;
    private Viewport viewport;
    private Skin skin;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private Texture settingsIconTexture; 
    private Texture exitIconTexture;
    private Texture startIconTexture; // Khai báo Texture cho icon Start Game

    public MainMenuScreen(final RPGGame game) {
        this.game = game;
        viewport = new ScreenViewport();
        stage = new Stage(viewport, game.batch);

        // Load background texture
        try {
            backgroundTexture = new Texture(Gdx.files.internal("MainMenuScreen/mainmenuscreen.png"));
            backgroundImage = new Image(backgroundTexture);
            backgroundImage.setPosition(0, 0);
            stage.addActor(backgroundImage);
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading background image", e);
        }

        // Load UI skin
        try {
            skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading skin", e);
            skin = new Skin();
        }

        // Load icons textures
        try {
            settingsIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/setting_icon.png"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading setting_icon.png", e);
        }
        try {
<<<<<<< Updated upstream
            exitIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/exit_game.png")); 
=======
            exitIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/exit_game.png")); // Tải icon exit
>>>>>>> Stashed changes
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading exit_game.png", e);
        }
        try {
            startIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/play_button.png")); // Tải icon Start Game
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading play_button.png", e);
        }


        Gdx.input.setInputProcessor(stage);

        // --- THIẾT LẬP UI MỚI ---

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        // mainTable.setDebug(true);
        stage.addActor(mainTable);

        ImageButton startImageButton = null; // Nút Start giờ là ImageButton
        ImageButton optionsButton = null;
        ImageButton exitImageButton = null;

        // Tạo ImageButton cho Start Game
        if (startIconTexture != null) {
            ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
            startStyle.imageUp = new TextureRegionDrawable(startIconTexture);
            startImageButton = new ImageButton(startStyle);
            startImageButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("MainMenuScreen", "Start Game ImageButton clicked");
                    game.setScreen(new GamePlayScreen(game)); 
                    dispose();
                }
            });
        } else {
            Gdx.app.log("MainMenuScreen", "Start icon texture not loaded, Start button not created as ImageButton.");
            // Fallback: có thể tạo TextButton nếu không có ảnh
        }


        // Tạo ImageButton cho Options
        if (settingsIconTexture != null) {
            ImageButton.ImageButtonStyle optionsStyle = new ImageButton.ImageButtonStyle();
            optionsStyle.imageUp = new TextureRegionDrawable(settingsIconTexture);
            optionsButton = new ImageButton(optionsStyle);
            optionsButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("MainMenuScreen", "Options ImageButton clicked");
                    // TODO: Implement Options Screen logic here
                }
            });
        } else {
            Gdx.app.log("MainMenuScreen", "Settings icon texture not loaded, Options button not created as ImageButton.");
        }

        // Tạo ImageButton cho Exit Game
        if (exitIconTexture != null) {
            ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
            exitStyle.imageUp = new TextureRegionDrawable(exitIconTexture);
            exitImageButton = new ImageButton(exitStyle);
            exitImageButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log("MainMenuScreen", "Exit ImageButton clicked");
                    Gdx.app.exit(); 
                }
            });
        } else {
            Gdx.app.log("MainMenuScreen", "Exit icon texture not loaded, Exit button not created as ImageButton.");
        }


        // 1. Tạo bảng con cho các nút Options và Exit ở góc trên phải
        Table topRightButtonsTable = new Table();
        // topRightButtonsTable.setDebug(true);

        float iconButtonSize = 80f; 
        float paddingBetweenSmallButtons = 20f;

        if (optionsButton != null) {
            topRightButtonsTable.add(optionsButton).size(iconButtonSize).padRight(paddingBetweenSmallButtons);
        }
        if (exitImageButton != null) { 
            topRightButtonsTable.add(exitImageButton).size(iconButtonSize);
        }


        // 2. Kích thước nút Start Game (ImageButton)
        float startButtonWidth = 700f;
        float startButtonHeight = 180f;

        // --- Bố cục trong mainTable ---
        mainTable.add().expandX();
        mainTable.add(topRightButtonsTable).top().right().padTop(30f).padRight(30f);
        mainTable.row();

        mainTable.add().colspan(2).expandY();
        mainTable.row();

        float screenHeight = 1700f;
        float padding_start_button_from_bottom = screenHeight * 0.20f;

        if (startImageButton != null) { // Thêm startImageButton vào table
            mainTable.add(startImageButton)
                     .width(startButtonWidth)
                     .height(startButtonHeight)
                     .colspan(2)
                     .bottom()
                     .padBottom(padding_start_button_from_bottom);
        } else {
            // Nếu startImageButton không được tạo, có thể thêm một thông báo hoặc để trống
            mainTable.add().colspan(2).height(startButtonHeight); // Giữ chỗ trống
        }

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.enableBlending(); // Đảm bảo bật alpha blending

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (backgroundImage != null) {
            backgroundImage.setSize(width, height);
            backgroundImage.setPosition(0,0);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        Gdx.app.log("MainMenuScreen", "Disposing MainMenuScreen");
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (settingsIconTexture != null) settingsIconTexture.dispose();
        if (exitIconTexture != null) exitIconTexture.dispose();
        if (startIconTexture != null) startIconTexture.dispose(); // Giải phóng texture của icon Start
    }
}