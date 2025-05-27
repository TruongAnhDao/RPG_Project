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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    private final RPGGame game; 
    private Stage stage;
    private Viewport viewport;
    private Skin skin;
    private Texture backgroundTexture;
    private Image backgroundImage;

    public MainMenuScreen(final RPGGame game) {
        this.game = game;
        viewport = new ScreenViewport();
        stage = new Stage(viewport, game.batch);

        // Load background texture
        try {
            backgroundTexture = new Texture(Gdx.files.internal("mainmenuscreen.png"));
            backgroundImage = new Image(backgroundTexture);
            backgroundImage.setPosition(0, 0);
            stage.addActor(backgroundImage); 
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading background image", e);
        }

        // Load UI skin
        try {
            skin = new Skin(Gdx.files.internal("uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading skin", e);
            skin = new Skin(); 
        }

        Gdx.input.setInputProcessor(stage);

        // --- THIẾT LẬP UI MỚI ---

        // Bảng chính, chiếm toàn bộ màn hình
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        // mainTable.setDebug(true); // Bật để xem đường viền của mainTable và các cell
        stage.addActor(mainTable);

        // Tạo các nút bấm
        TextButton startButton = new TextButton("Start Game", skin); 
        TextButton optionsButton = new TextButton("Options", skin);
        TextButton exitButton = new TextButton("Exit Game", skin);

        // 1. Tạo bảng con cho các nút Options và Exit ở góc trên phải
        Table topRightButtonsTable = new Table();
        // topRightButtonsTable.setDebug(true); // Bật để xem đường viền của bảng con này

        float smallButtonWidth = 220f; // Đủ rộng cho chữ "Options"
        float smallButtonHeight = 80f;  // Chiều cao vừa phải
        float paddingBetweenSmallButtons = 20f;

        topRightButtonsTable.add(optionsButton).width(smallButtonWidth).height(smallButtonHeight).padRight(paddingBetweenSmallButtons);
        topRightButtonsTable.add(exitButton).width(smallButtonWidth).height(smallButtonHeight);

        // 2. Bố cục trong mainTable
        // Dòng 1: Một cell trống co giãn để đẩy topRightButtonsTable sang phải, và topRightButtonsTable
        mainTable.add().expandX(); // Cell trống co giãn theo chiều ngang
        mainTable.add(topRightButtonsTable).top().right().padTop(30f).padRight(30f); // Đặt bảng con vào góc trên phải của mainTable
        mainTable.row(); // Xuống hàng mới

        // Dòng 2: Nút Start Game, chiếm trọn bề ngang của hàng mới, co giãn theo chiều dọc và căn giữa
        float startButtonWidth = 700f;  // Kích thước nút Start như cũ hoặc tùy chỉnh
        float startButtonHeight = 180f;
        mainTable.add(startButton).width(startButtonWidth).height(startButtonHeight).colspan(2).expandY().center();
        // colspan(2) để nút Start chiếm cả 2 cột đã được định nghĩa ở dòng trên (cell trống và cell chứa topRightButtonsTable)
        // expandY() để cell này chiếm hết không gian dọc còn lại
        // center() để nút Start nằm giữa cell đó
        
        mainTable.row(); // Xuống hàng mới (nếu có thêm nội dung bên dưới Start Game)
        // mainTable.add().expandY(); // Thêm cell trống co giãn ở dưới nếu muốn đẩy Start Game lên trên một chút nữa

        // Button listeners
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Start Game button clicked");
                game.setScreen(new GamePlayScreen(game)); 
                dispose();
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Options button clicked");
                // TODO: Implement Options Screen
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Exit Game button clicked");
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
    }
}