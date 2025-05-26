package com.mygdx.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter; 
import com.badlogic.gdx.InputMultiplexer; 
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GamePlayScreen implements Screen {

    private final RPGGame game;
    private OrthographicCamera gameCamera; // Camera cho thế giới game
    private Viewport gameViewport;         // Viewport cho thế giới game

    // HUD (Heads-Up Display) elements - sẽ dùng Stage riêng cho HUD
    private Stage hudStage;
    private Viewport hudViewport;
    private Skin skin; // Skin cho các UI elements trên HUD

    // Các đối tượng game
    private PlayerCharacter player; // Đối tượng người chơi
    private Label healthLabel;      // Label để hiển thị máu

    // Ví dụ: Texture cho nền game hoặc nhân vật
    private Texture playerTexture;
    private Texture backgroundTexture; // Tùy chọn

    private InputMultiplexer inputMultiplexer; // Để xử lý nhiều nguồn input

    public GamePlayScreen(final RPGGame game) {
        this.game = game;

        // 1. Thiết lập Camera và Viewport cho thế giới game (nếu bạn vẽ 2D/3D world)
        gameCamera = new OrthographicCamera();
        // Ví dụ: FitViewport để giữ tỷ lệ khung hình của thế giới game
        // Bạn có thể điều chỉnh WORLD_WIDTH, WORLD_HEIGHT theo kích thước thế giới game mong muốn
        float WORLD_WIDTH = 800; // Ví dụ
        float WORLD_HEIGHT = 480; // Ví dụ
        // gameViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, gameCamera);
        gameViewport = new ScreenViewport(gameCamera); // Hoặc dùng ScreenViewport nếu muốn co giãn tự do
        gameCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Cập nhật camera ban đầu
        gameViewport.apply();


        // 2. Thiết lập Stage và Viewport cho HUD (Giao diện trên màn hình)
        hudViewport = new ScreenViewport(); // HUD thường dùng ScreenViewport để co giãn theo màn hình
        hudStage = new Stage(hudViewport, game.batch); // Sử dụng lại batch từ game chính

        // Tải skin (giống như MainMenuScreen)
        try {
            skin = new Skin(Gdx.files.internal("core/assets/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Error loading skin", e);
            skin = new Skin(); // Fallback
        }

        // --- Khởi tạo đối tượng game ---
        player = new PlayerCharacter("Hero"); // Tạo nhân vật
        // player.setHealth(100); // Nếu cần thiết lập máu ban đầu qua setter

        // --- Tải tài nguyên ví dụ ---
        try {
            playerTexture = new Texture(Gdx.files.internal("core/assets/playertexture.png")); 
            backgroundTexture = new Texture(Gdx.files.internal("core/assets/background.png")); 
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Could not load textures", e);
            // playerTexture và backgroundTexture sẽ là null, cần xử lý trong render
        }

        // Tạo các UI elements cho HUD 
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left(); // Căn HUD ở góc trên bên trái

        // Khởi tạo healthLabel ở đây, nhưng sẽ cập nhật text trong render()
        healthLabel = new Label("Health: " + player.getCurrentHealth(), skin, "default"); // Lấy máu ban đầu
        hudTable.add(healthLabel).pad(10).row();
        // Bạn có thể thêm các label khác cho mana, điểm, v.v.
        // Label manaLabel = new Label("Mana: " + player.getMana(), skin, "default");
        // hudTable.add(manaLabel).pad(10);

        hudStage.addActor(hudTable);

        // Khởi tạo các đối tượng game của bạn ở đây
        // Ví dụ: player = new PlayerCharacter("Hero");
        //         loadMap();

        // --- Thiết lập InputMultiplexer ---
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(hudStage); // HUD nhận input trước (thường là vậy)
        inputMultiplexer.addProcessor(new GameInputAdapter()); // Thêm bộ xử lý input cho game world

        Gdx.app.log("GamePlayScreen", "GamePlayScreen created");
    }

    @Override
    public void show() {
        Gdx.app.log("GamePlayScreen", "show called");
        // Quan trọng: Khi màn hình này được hiển thị, bạn cần đặt InputProcessor
        // Nếu bạn có cả input cho game world (nhân vật di chuyển) và HUD (click nút trên HUD)
        // bạn sẽ cần InputMultiplexer. Tạm thời chỉ đặt cho HUD nếu có.
        Gdx.input.setInputProcessor(inputMultiplexer); // Cho phép HUD nhận input
    }

    // Hàm cập nhật logic game (ví dụ)
    private void updateGame(float delta) {
        // Di chuyển nhân vật, cập nhật AI, xử lý va chạm, v.v.
        // Ví dụ: player.update(delta);

        // Tạm thời mô phỏng việc máu thay đổi để test HUD
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.takeDamage(10); // Giả sử có phương thức takeDamage
            if (player.getCurrentHealth() < 0) player.setCurrentHealth(100); // Reset máu ví dụ
        }

        // Cập nhật text của healthLabel
        healthLabel.setText("Health: " + player.getCurrentHealth());
    }

    @Override
    public void render(float delta) {
        // Xóa màn hình
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Cập nhật logic game (di chuyển, AI, va chạm,...)
        updateGame(delta);

        // 2. Vẽ thế giới game
        gameCamera.update(); // Cập nhật camera của game world
        game.batch.setProjectionMatrix(gameCamera.combined); // Quan trọng: Batch dùng camera của game

        game.batch.begin();
        // Vẽ nền (nếu có)
        if (backgroundTexture != null) {
            game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Vẽ nhân vật (ví dụ đơn giản)
        if (playerTexture != null) {
            // Bạn sẽ cần có vị trí x, y cho player
            // ví dụ player.getX(), player.getY()
            game.batch.draw(playerTexture, Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f, Gdx.graphics.getHeight() / 2f - playerTexture.getHeight() / 2f);
        }
        game.batch.end();


        // 3. Cập nhật và Vẽ HUD
        // hudViewport.apply(); // Không cần gọi nếu hudStage tự quản lý batch và viewport
        hudStage.act(Math.min(delta, 1 / 30f));
        hudStage.draw();

        // Gdx.app.log("GamePlayScreen", "Rendering GamePlayScreen");
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GamePlayScreen", "Resizing to: " + width + "x" + height);
        gameViewport.update(width, height, true); // Cập nhật viewport của game, true để căn giữa camera
        hudViewport.update(width, height, true);  // Cập nhật viewport của HUD
        gameCamera.setToOrtho(false, width, height); // Cập nhật camera nếu không dùng FitViewport/ExtendViewport cho game world
                                                     // Nếu dùng FitViewport/ExtendViewport thì gameCamera.update() là đủ
    }

    @Override
    public void pause() {
        Gdx.app.log("GamePlayScreen", "pause called");
    }

    @Override
    public void resume() {
        Gdx.app.log("GamePlayScreen", "resume called");
    }

    @Override
    public void hide() {
        Gdx.app.log("GamePlayScreen", "hide called");
        // Không nhất thiết phải dispose ở đây nếu bạn muốn quay lại màn hình này (ví dụ: từ PauseMenu)
    }

    @Override
    public void dispose() {
        Gdx.app.log("GamePlayScreen", "Disposing GamePlayScreen");
        hudStage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        if (playerTexture != null) {
            playerTexture.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        // Dispose các tài nguyên khác của màn hình game (map, textures nhân vật,...)
    }

    // Lớp nội (inner class) để xử lý input cho thế giới game
    class GameInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                Gdx.app.log("GameInputAdapter", "Escape pressed - Returning to Main Menu");
                game.setScreen(new MainMenuScreen(game)); // Quay lại menu
                dispose(); // Dispose GamePlayScreen
                return true; // Input đã được xử lý
            }
            // Thêm xử lý input cho nhân vật ở đây (di chuyển, tấn công, v.v.)
            // Ví dụ:
            // if (keycode == Input.Keys.W) { player.moveForward(); return true; }
            // if (keycode == Input.Keys.S) { player.moveBackward(); return true; }
            // if (keycode == Input.Keys.A) { player.moveLeft(); return true; }
            // if (keycode == Input.Keys.D) { player.moveRight(); return true; }

            Gdx.app.log("GameInputAdapter", "Key Down: " + Input.Keys.toString(keycode));
            return false; // Cho phép các InputProcessor khác (nếu có) xử lý input này
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Xử lý click chuột/chạm màn hình trong thế giới game
            // Bạn có thể cần unproject tọa độ màn hình sang tọa độ thế giới game
            // Vector3 worldCoordinates = gameCamera.unproject(new Vector3(screenX, screenY, 0));
            // Gdx.app.log("GameInputAdapter", "Touched at world coordinates: " + worldCoordinates.x + ", " + worldCoordinates.y);
            return false;
        }
    }
}