package com.mygdx.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainMenuScreen implements Screen {

    private final RPGGame game; // Tham chiếu đến lớp Game chính để có thể chuyển màn hình
    private Stage stage;
    private Viewport viewport;
    private Skin skin; // Skin để tạo kiểu cho UI elements
    private OrthographicCamera camera; // Không bắt buộc cho UI đơn giản với ScreenViewport, nhưng hữu ích

    public MainMenuScreen(final RPGGame game) {
        this.game = game;
        // camera = new OrthographicCamera(); // Nếu dùng FitViewport hoặc ExtendViewport thì cần camera
        // viewport = new FitViewport(800, 480, camera); // Ví dụ với FitViewport
        viewport = new ScreenViewport(); // Đơn giản hơn cho UI co giãn theo màn hình
        stage = new Stage(viewport, game.batch); // Truyền SpriteBatch từ RPGGame vào

        // Tải skin
        // Giả sử các file skin nằm trực tiếp trong 'assets'
        try {
            skin = new Skin(Gdx.files.internal("core/assets/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error loading skin", e);
            // Tạo một skin rỗng để tránh NullPointerException nếu tải thất bại
            // Hoặc bạn có thể tạo một skin cơ bản bằng code ở đây làm fallback
            skin = new Skin();
        }


        Gdx.input.setInputProcessor(stage); // Rất quan trọng để Stage nhận input

        // Tạo Table để sắp xếp các elements
        Table table = new Table();
        table.setFillParent(true); // Table chiếm toàn bộ Stage
        // table.setDebug(true); // Bật debug để xem đường viền của table và cells

        stage.addActor(table);

        // Tạo các UI elements
        Label titleLabel = new Label("MY RPG ADVENTURE", skin, "default"); // Sử dụng style "default" từ uiskin.json
                                                                        // Nếu skin của bạn có style tên khác, ví dụ "title-font", thì dùng tên đó
        TextButton startButton = new TextButton("Start Game", skin, "default");
        TextButton optionsButton = new TextButton("Options", skin, "default");
        TextButton exitButton = new TextButton("Exit Game", skin, "default");

        // Thêm elements vào Table
        table.add(titleLabel).padBottom(50).colspan(1).center(); // colspan nếu có nhiều cột
        table.row(); // Xuống hàng mới
        table.add(startButton).width(300).height(50).padBottom(20);
        table.row();
        table.add(optionsButton).width(300).height(50).padBottom(20);
        table.row();
        table.add(exitButton).width(300).height(50);

        // Thêm listeners cho các button
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Start Game button clicked");
                // Chuyển sang GamePlayScreen
                game.setScreen(new GamePlayScreen(game)); // Tạo và đặt màn hình mới
                dispose(); // Giải phóng tài nguyên của MainMenuScreen sau khi chuyển
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Options button clicked");
                // TODO: Chuyển sang màn hình Options
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenuScreen", "Exit Game button clicked");
                Gdx.app.exit(); // Thoát game
            }
        });
    }

    @Override
    public void show() {
        // Được gọi khi màn hình này được hiển thị
        Gdx.input.setInputProcessor(stage); // Đảm bảo stage nhận input
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); // Màu nền xám
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Cập nhật Stage
        stage.draw(); // Vẽ Stage
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Cập nhật viewport khi cửa sổ thay đổi kích thước
    }

    @Override
    public void pause() {
        // Được gọi khi game bị tạm dừng (ví dụ: cuộc gọi đến trên Android)
    }

    @Override
    public void resume() {
        // Được gọi khi game tiếp tục sau khi bị tạm dừng
    }

    @Override
    public void hide() {
        // Được gọi khi màn hình này không còn là màn hình hiện tại nữa
        // Bạn có thể không cần dispose ở đây nếu muốn quay lại màn hình này sau
    }

    @Override
    public void dispose() {
        // Giải phóng tài nguyên khi màn hình này không còn dùng nữa
        Gdx.app.log("MainMenuScreen", "Disposing MainMenuScreen");
        stage.dispose();
        if (skin != null) { // Kiểm tra skin có null không trước khi dispose
             skin.dispose();
        }
    }
}
