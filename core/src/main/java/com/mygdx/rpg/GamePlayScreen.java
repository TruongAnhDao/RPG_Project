package com.mygdx.rpg;

import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter; 
import com.badlogic.gdx.InputMultiplexer; 
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Window; // Cho cửa sổ inventory
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane; // Để cuộn danh sách item
import com.badlogic.gdx.scenes.scene2d.ui.TextButton; // Có thể dùng để tương tác với item sau này
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener; // Cho nút đóng
import com.badlogic.gdx.scenes.scene2d.InputEvent;     // Cho ClickListener

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
    private Label manaLabel;
    private Label levelLabel;
    private Label experienceLabel;

    // Ví dụ: Texture cho nền game hoặc nhân vật
    private Texture playerTexture;
    private Texture backgroundTexture; // Tùy chọn

    private InputMultiplexer inputMultiplexer; // Để xử lý nhiều nguồn input

    // --- TCác biến cho Inventory UI ---
    private Window inventoryWindow; // Cửa sổ chính của hành trang
    private Table inventoryItemTable; // Bảng để chứa danh sách item
    private ScrollPane inventoryScrollPane; // Để cuộn item nếu nhiều
    private boolean inventoryVisible = false; // Trạng thái hiển thị hành trang
    private Label itemDescriptionLabel; // Label để hiển thị mô tả vật phẩm
    private TextButton dropItemButton; // Nút để vứt bỏ vật phẩm
    private Item currentSelectedItem = null; // Để lưu trữ vật phẩm đang được chọn

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
            skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Error loading skin", e);
            skin = new Skin(); // Fallback
        }

        // --- Khởi tạo đối tượng game ---
        player = new PlayerCharacter("Hero"); // Tạo nhân vật
        // player.setHealth(100); // Nếu cần thiết lập máu ban đầu qua setter

        // --- Thêm item mẫu vào hành trang của người chơi để test ---
        player.addItem(new Item("Potion", "Consumable", "Heals 50 HP"));
        player.addItem(new Item("Sword of Power", "Weapon", "A mighty fine sword."));
        player.addItem(new Item("Old Key", "Key Item", "Opens an old door."));
        player.addItem(new Item("Mana Potion", "Consumable", "Restores 30 MP"));
        player.addItem(new Item("Shield", "Armor", "Provides good defense."));

        // --- Tải tài nguyên ví dụ ---
        try {
            playerTexture = new Texture(Gdx.files.internal("PlayScreen/playertexture.png")); 
            backgroundTexture = new Texture(Gdx.files.internal("PlayScreen/background.png")); 
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

        manaLabel = new Label("Mana: " + player.getCurrentMana() + "/" + player.getMaxMana(), skin, "default");
        hudTable.add(manaLabel).padLeft(10).row(); // padLeft để căn với healthLabel nếu ở cùng cột

        levelLabel = new Label("Level: " + player.getLevel(), skin, "default");
        hudTable.add(levelLabel).padLeft(10).row();

        experienceLabel = new Label("XP: " + player.getExperience() + "/" + player.getExperienceToNextLevel(), skin, "default");
        hudTable.add(experienceLabel).padLeft(10).row();
        // hudTable.add(manaLabel).pad(10);

        hudStage.addActor(hudTable);

        // --- Thiết lập Inventory UI ---
        setupInventoryUI();

        // --- Thiết lập InputMultiplexer ---
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(hudStage); // HUD nhận input trước (thường là vậy)
        inputMultiplexer.addProcessor(new GameInputAdapter()); // Thêm bộ xử lý input cho game world

        Gdx.app.log("GamePlayScreen", "GamePlayScreen created");
    }

    private void setupInventoryUI() {
        inventoryWindow = new Window("Inventory", skin); // Tiêu đề cửa sổ
        inventoryWindow.setSize(400, 450); // Kích thước cửa sổ
        inventoryWindow.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 150); // Căn giữa
        inventoryWindow.setMovable(true); // Cho phép di chuyển cửa sổ
        inventoryWindow.setVisible(inventoryVisible); // Ban đầu ẩn

        inventoryItemTable = new Table(skin); // Bảng chứa item
        inventoryScrollPane = new ScrollPane(inventoryItemTable, skin); // Bọc bảng item bằng ScrollPane
        inventoryScrollPane.setFadeScrollBars(false); // Giữ thanh cuộn luôn hiện (tùy chọn)
        inventoryScrollPane.setScrollingDisabled(true, false); // Chỉ cho cuộn dọc

        // Thêm nút đóng vào thanh tiêu đề của Window (hoặc bên trong)
        TextButton closeButton = new TextButton("X", skin, "default"); // Dùng style "default" cho nút
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleInventory(); // Gọi hàm toggle để ẩn inventory
                hideItemDescription(); // Ẩn mô tả khi đóng inventory
            }
        });
        inventoryWindow.getTitleTable().add(closeButton).height(inventoryWindow.getPadTop()); // Thêm vào thanh tiêu đề

        // --- Thêm Label cho mô tả ---
        itemDescriptionLabel = new Label("Select an item to see its description.", skin);
        itemDescriptionLabel.setWrap(true); // Cho phép xuống dòng nếu mô tả dài

        // inventoryWindow.add(inventoryScrollPane).expand().fill().pad(5).colspan(1); // Dòng cũ
        inventoryWindow.add(inventoryScrollPane).expandX().fillX().height(200).pad(5).row(); // Giới hạn chiều cao ScrollPane
        inventoryWindow.add(itemDescriptionLabel).expandX().fillX().height(150).pad(5).top().row(); // Thêm Label mô tả bên dưới

        // --- Khởi tạo và thêm nút Drop ---
        dropItemButton = new TextButton("Drop", skin, "default");
        dropItemButton.setVisible(false); // Ban đầu ẩn nút Drop

        dropItemButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentSelectedItem != null) {
                    Gdx.app.log("InventoryAction", "Attempting to drop: " + currentSelectedItem.getName());
                    if (player.dropItem(currentSelectedItem)) {
                        Gdx.app.log("InventoryAction", currentSelectedItem.getName() + " dropped successfully.");
                        populateInventoryTable(); // Cập nhật lại danh sách hành trang
                        // Sau khi drop, không còn item nào được chọn nữa
                        hideItemDescription(); // Ẩn mô tả và nút Drop
                        currentSelectedItem = null;
                    } else {
                        // Trường hợp này ít khi xảy ra nếu currentSelectedItem được quản lý đúng
                        Gdx.app.log("InventoryAction", "Failed to drop " + currentSelectedItem.getName());
                    }
                }
            }
        });

        // Thêm một hàng mới cho nút Drop (hoặc đặt nó ở vị trí khác tùy ý)
        // Ví dụ: đặt nó dưới phần mô tả
        Table actionButtonsTable = new Table(); // Tạo một bảng nhỏ để chứa các nút hành động nếu cần
        // actionButtonsTable.add(useItemButton).pad(5); // Nếu bạn có nút "Use" riêng
        actionButtonsTable.add(dropItemButton).pad(5);

        inventoryWindow.row(); // Xuống hàng mới sau ScrollPane và DescriptionLabel
        inventoryWindow.add(actionButtonsTable).pad(5); // Thêm bảng chứa nút Drop

        // Thêm inventoryWindow vào hudStage để nó được vẽ và nhận input (khi visible)
        // Chúng ta thêm nó vào hudStage để nó được vẽ trên cùng các element khác của HUD
        hudStage.addActor(inventoryWindow);
    }

    private void populateInventoryTable() {
        inventoryItemTable.clearChildren(); // Xóa các item cũ trước khi cập nhật
        List<Item> items = player.getInventory();

        if (items.isEmpty()) {
            inventoryItemTable.add(new Label("Inventory is empty.", skin)).pad(10);
        } else {
            for (Item item : items) {
                // Tạo TextButton 
                TextButton itemButton = new TextButton(item.getName() + " (" + item.getType() + ")", skin, "default");;
                itemButton.setUserObject(item); // **Gán đối tượng Item vào UserObject của Button**

                itemButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Item clickedItem = (Item) ((TextButton)event.getListenerActor()).getUserObject();
                        Gdx.app.log("InventoryClick", "Clicked on: " + clickedItem.getName());

                        // Hiển thị mô tả trước
                        showItemDescription(clickedItem);

                        // Nếu là vật phẩm có thể sử dụng ngay khi click (ví dụ: Consumable)
                        if ("Consumable".equalsIgnoreCase(clickedItem.getType())) {
                            // Thêm một nút "Use" hoặc xử lý trực tiếp
                            // Ví dụ xử lý trực tiếp:
                            if (player.useItem(clickedItem)) {
                                Gdx.app.log("InventoryAction", clickedItem.getName() + " used successfully.");
                                populateInventoryTable();
                                // Sau khi dùng, item đã biến mất, nên không còn "selected" để drop nữa
                                // Có thể cập nhật mô tả hoặc ẩn đi
                                if (player.getInventory().contains(clickedItem)) { // Kiểm tra xem item còn không (ví dụ item không bị consume 100%)
                                    showItemDescription(clickedItem); // Nếu vẫn còn thì hiển thị lại
                                } else {
                                    hideItemDescription(); // Nếu item đã bị consume hoàn toàn
                                    currentSelectedItem = null; // Đảm bảo không còn item nào được chọn
                                }

                            } else {
                                Gdx.app.log("InventoryAction", "Could not use " + clickedItem.getName());
                                itemDescriptionLabel.setText("Could not use " + clickedItem.getName() + ".");
                                // Nút Drop vẫn sẽ hiển thị dựa trên item được click ban đầu
                            }
                        }
                        // Nếu không phải Consumable, chỉ hiển thị mô tả như trước
                    }
                });

                inventoryItemTable.add(itemButton).left().fillX().pad(2).row(); // fillX để button chiếm hết chiều rộng ô
            }
        }
    }

    private void toggleInventory() {
        inventoryVisible = !inventoryVisible;
        inventoryWindow.setVisible(inventoryVisible);

        if (inventoryVisible) {
            populateInventoryTable(); // Cập nhật danh sách item khi mở
            inputMultiplexer.removeProcessor(1); // Gỡ GameInputAdapter tạm thời
            inputMultiplexer.addProcessor(0, hudStage); // Đảm bảo hudStage (chứa inventoryWindow) xử lý input trước tiên
                                                         // hoặc chỉ cần addProcessor(inventoryWindow.getStage()) nếu inventoryWindow không thuộc hudStage
            Gdx.input.setInputProcessor(inputMultiplexer); // Cập nhật lại input processor
            // Có thể bạn muốn tạm dừng game ở đây
            // Gdx.app.log("Inventory", "Inventory Opened");
        } else {
            hideItemDescription(); // Ẩn mô tả khi inventory đóng
            // Khôi phục input processor như cũ
            inputMultiplexer.removeProcessor(hudStage); // Gỡ hudStage (nếu đã thêm ở vị trí 0)
            inputMultiplexer.addProcessor(hudStage);    // Thêm lại hudStage ở cuối
            inputMultiplexer.addProcessor(1,new GameInputAdapter()); // Thêm lại GameInputAdapter
            Gdx.input.setInputProcessor(inputMultiplexer); // Cập nhật lại
            // Gdx.app.log("Inventory", "Inventory Closed");
        }
    }

    private void showItemDescription(Item item) {
        currentSelectedItem = item; // Lưu vật phẩm đang được chọn
        if (item != null && item.getEffect() != null) {
            itemDescriptionLabel.setText(item.getEffect());
            dropItemButton.setVisible(true); // Hiển thị nút Drop
        } else {
            itemDescriptionLabel.setText("No description available.");
            dropItemButton.setVisible(false); // Ẩn nút Drop nếu không có item hoặc mô tả
            currentSelectedItem = null;
        }
    }

    private void hideItemDescription() {
        itemDescriptionLabel.setText("Select an item to see its description.");
        dropItemButton.setVisible(false); // Luôn ẩn nút Drop khi không có mô tả/item nào được chọn
        currentSelectedItem = null; // Không còn item nào được chọn
}

    @Override
    public void show() {
        Gdx.app.log("GamePlayScreen", "show called");
        // Quan trọng: Khi màn hình này được hiển thị, bạn cần đặt InputProcessor
        // Nếu bạn có cả input cho game world (nhân vật di chuyển) và HUD (click nút trên HUD)
        // bạn sẽ cần InputMultiplexer. Tạm thời chỉ đặt cho HUD nếu có.
        if (!inventoryVisible) { // Trạng thái ban đầu
            inputMultiplexer.clear(); // Xóa hết processor cũ
            inputMultiplexer.addProcessor(hudStage); // HUD (bao gồm cả inventory window nếu nó là con của hudStage)
            inputMultiplexer.addProcessor(new GameInputAdapter()); // Input cho game
        }
        Gdx.input.setInputProcessor(inputMultiplexer); // Cho phép HUD nhận input
    }

    // Hàm cập nhật logic game (ví dụ)
    private void updateGame(float delta) {
        // Di chuyển nhân vật, cập nhật AI, xử lý va chạm, v.v.
        // Ví dụ: player.update(delta);

        // --- Cập nhật HUD Labels ---
        healthLabel.setText("Health: " + player.getCurrentHealth() + "/" + player.getMaxHealth()); // Thêm maxHealth cho rõ ràng
        manaLabel.setText("Mana: " + player.getCurrentMana() + "/" + player.getMaxMana());
        levelLabel.setText("Level: " + player.getLevel());
        experienceLabel.setText("XP: " + player.getExperience() + "/" + player.getExperienceToNextLevel());

        // Tạm thời mô phỏng việc máu thay đổi để test HUD
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.takeDamage(10); // takeDamage
            if (player.getCurrentHealth() < 0) player.setCurrentHealth(100); // Reset máu ví dụ
        }
        // --- Tạm thời: Thêm XP khi nhấn phím E ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !inventoryVisible) {
            player.addExperience(30); // Cộng 30 XP mỗi lần nhấn E
        }

        // Cập nhật text của healthLabel
        healthLabel.setText("Health: " + player.getCurrentHealth()); // Dòng này sẽ tự động cập nhật máu sau khi dùng Potion
    }

    private void handleInput(float delta) {
        // Ví dụ xử lý input chung cho GamePlayScreen, không thuộc GameInputAdapter
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            toggleInventory();
        }

        // Cập nhật máu người chơi (ví dụ khi nhấn SPACE)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !inventoryVisible) { // Chỉ cho thay đổi máu khi inventory đóng
            player.takeDamage(10);
            if (player.getCurrentHealth() < 0) player.setCurrentHealth(100);
        }
    }

    @Override
    public void render(float delta) {
        // Xóa màn hình
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Cập nhật logic game (di chuyển, AI, va chạm,...)
        updateGame(delta);
        handleInput(delta);

        // 2. Vẽ thế giới game
        gameCamera.update(); // Cập nhật camera của game world
        game.batch.setProjectionMatrix(gameCamera.combined); // Quan trọng: Batch dùng camera của game
        game.batch.begin();
        // Vẽ nền (nếu có)
        if (backgroundTexture != null) {
            game.batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Vẽ nhân vật (ví dụ đơn giản)
        float Width = 100;
        float Height = 100;
        if (playerTexture != null) {
            // Bạn sẽ cần có vị trí x, y cho player
            // ví dụ player.getX(), player.getY()
            game.batch.draw(playerTexture, player.getX() - Width / 2f, player.getY() - Height / 2f, Width, Height);
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
            // Nếu inventory đang hiển thị VÀ con trỏ chuột đang nằm trên inventory window
            // thì không xử lý input game. Điều này giúp khi bạn click vào inventory (ví dụ để kéo)
            // thì input game không bị kích hoạt.
            if (inventoryVisible && inventoryWindow.hasKeyboardFocus()) { // Hoặc một kiểm tra khác phù hợp hơn
                Gdx.app.log("GameInputAdapter", "Inventory has focus, not processing game input");
                // return true; // Nếu muốn chặn hoàn toàn
            }
            if (inventoryVisible && Gdx.input.isCursorCatched()) {
                // This is not a perfect check, but can help if mouse is over a UI element on inventory
            }


            if (inventoryVisible) return false; // Cách đơn giản nhất: Nếu inventory mở, không xử lý input game
            // ... (code xử lý input game còn lại) ...

            if (keycode == Input.Keys.ESCAPE) {
                Gdx.app.log("GameInputAdapter", "Escape pressed - Returning to Main Menu");
                game.setScreen(new MainMenuScreen(game)); // Quay lại menu
                dispose(); // Dispose GamePlayScreen
                return true; // Input đã được xử lý
            }

            // Di chuyển nhân vật ví dụ
            float moveAmount = 100.0f * Gdx.graphics.getDeltaTime(); // Tốc độ di chuyển
            if (keycode == Input.Keys.W) { player.y += moveAmount; return true; }
            if (keycode == Input.Keys.S) { player.y -= moveAmount; return true; }
            if (keycode == Input.Keys.A) { player.x -= moveAmount; return true; }
            if (keycode == Input.Keys.D) { player.x += moveAmount; return true; }


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