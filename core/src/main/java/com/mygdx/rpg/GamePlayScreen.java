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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Window; 
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane; 
import com.badlogic.gdx.scenes.scene2d.ui.TextButton; 
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener; 
import com.badlogic.gdx.scenes.scene2d.InputEvent;    
import com.badlogic.gdx.maps.tiled.TiledMap;                         
import com.badlogic.gdx.maps.tiled.TmxMapLoader;                      
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils; 
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.audio.Sound;

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
    private ProgressBar healthBar;
    private ProgressBar manaBar;
    private ProgressBar experienceBar;
    private Label healthValueLabel, manaValueLabel, experienceValueLabel; // Các label giá trị

    // Ví dụ: Texture cho nền game hoặc nhân vật
    private Texture playerTexture;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    public static final float DEFAULT_CAMERA_ZOOM = 0.3f;
    private float mapPixelWidth;
    private float mapPixelHeight;
    private TiledMapTileLayer collisionLayer; 
    private int tilePixelWidth;
    private int tilePixelHeight;
    private Array<Enemy> enemies;

    private InputMultiplexer inputMultiplexer; // Để xử lý nhiều nguồn input

    // --- TCác biến cho Inventory UI ---
    private Window inventoryWindow; // Cửa sổ chính của hành trang
    private Table inventoryItemTable; // Bảng để chứa danh sách item
    private ScrollPane inventoryScrollPane; // Để cuộn item nếu nhiều
    private boolean inventoryVisible = false; // Trạng thái hiển thị hành trang
    private Label itemDescriptionLabel; // Label để hiển thị mô tả vật phẩm
    private TextButton dropItemButton; // Nút để vứt bỏ vật phẩm
    private Item currentSelectedItem = null; // Để lưu trữ vật phẩm đang được chọn
    private TextButton useItemButton; // Nút để sử dụng vật phẩm

    // Cờ cho trạng thái di chuyển
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean playerAttackRequested = false; // Cờ cho yêu cầu tấn công

    private ShapeRenderer shapeRenderer;

    private Music backgroundMusic;
    private Sound attackSound;
    private Sound enemyattackSound;
    private Sound playerDeathSound;

    private Label gameOverLabel;
    private TextButton backToMenuButton;
    private SettingsWindow settingsWindow;

    public GamePlayScreen(final RPGGame game) {
        this.game = game;

        // Thiết lập Camera và Viewport cho thế giới game (nếu bạn vẽ 2D/3D world)
        gameCamera = new OrthographicCamera();      
        gameViewport = new ScreenViewport(gameCamera); 
        gameCamera.zoom = DEFAULT_CAMERA_ZOOM;


        // Thiết lập Stage và Viewport cho HUD (Giao diện trên màn hình)
        hudViewport = new ScreenViewport(); // HUD thường dùng ScreenViewport để co giãn theo màn hình
        hudStage = new Stage(hudViewport, game.batch); // Sử dụng lại batch từ game chính

        // Tải skin (giống như MainMenuScreen)
        try {
            skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Error loading skin", e);
            skin = new Skin(); // Fallback
        }

        settingsWindow = new SettingsWindow(skin);
        hudStage.addActor(settingsWindow);

        // --- Khởi tạo đối tượng game ---
        player = new PlayerCharacter("Hero"); // Tạo nhân vật
        player.setHealth(100); // Nếu cần thiết lập máu ban đầu qua setter

        // Thêm item mẫu vào hành trang của người chơi để test
        // Item(String name, String type, String description, int initialQuantity, int maxStackSize)
        player.addItem(new Item("Potion", "Consumable", "Heals 50 HP", 5, 10)); // 5 Potion, stack tối đa 10
        player.addItem(new Item("Potion", "Consumable", "Heals 50 HP", 3, 10)); // Thêm 3 Potion nữa để test stacking
        player.addItem(new Item("Sword of Power", "Weapon", "A mighty fine sword.", 1, 1)); // Kiếm không stack
        player.addItem(new Item("Old Key", "Key Item", "Opens an old door.", 1, 1));
        player.addItem(new Item("Mana Potion", "Consumable", "Restores 30 MP", 3, 5)); // 3 Mana Potion, stack tối đa 5
        player.addItem(new Item("Arrow", "Ammo", "Standard arrow.", 20, 50)); // 20 mũi tên, stack 50
        player.addItem(new Item("Arrow", "Ammo", "Standard arrow.", 15, 50)); // Thêm 15 mũi tên nữa

        // --- Tải tài nguyên ví dụ ---
        try {
            playerTexture = new Texture(Gdx.files.internal("PlayScreen/playertexture.png")); 
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Could not load textures", e);
        }

        Label.LabelStyle gameOverStyle = new Label.LabelStyle(skin.getFont("default-font"), com.badlogic.gdx.graphics.Color.WHITE);
        gameOverLabel = new Label("GAME OVER", gameOverStyle);
        gameOverLabel.setFontScale(15.0f); // Phóng to font lên 3 lần
        gameOverLabel.setVisible(false); // Ban đầu ẩn đi

        backToMenuButton = new TextButton("Back to Main Menu", skin, "default");
        backToMenuButton.getLabel().setFontScale(5.5f);
        backToMenuButton.setVisible(false); // Ban đầu cũng ẩn đi

        backToMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Phát âm thanh click nếu có
                // ...

                // Chuyển về màn hình Main Menu
                game.setScreen(new MainMenuScreen(game));

                // Quan trọng: Hủy màn hình hiện tại để giải phóng bộ nhớ
                dispose();
            }
        });

        // Đặt label vào một table để căn giữa màn hình
        Table table = new Table();
        table.setFillParent(true); // table chiếm toàn bộ stage
        table.add(gameOverLabel).center().padBottom(200);
        table.row();
        table.add(backToMenuButton).size(1000f, 200f);

        hudStage.addActor(table); // Thêm table vào stage của HUD

        // Tạo các UI elements cho HUD 
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left().pad(10); // Thêm padding chung cho hudTable

        // --- Health ---
        healthLabel = new Label("HP:", skin, "default"); // Chỉ còn chữ "HP:"
        healthBar = new ProgressBar(0, player.getMaxHealth(), 1, false, skin, "health-bar-style");
        healthBar.setValue(player.getCurrentHealth());
        this.healthValueLabel = new Label(player.getCurrentHealth() + "/" + player.getMaxHealth(), skin, "default");

        hudTable.add(healthLabel).padRight(5);
        hudTable.add(healthBar).width(200).height(20).padRight(10); // Đặt kích thước cho thanh máu
        hudTable.add(this.healthValueLabel).row();

        // --- Mana ---
        manaLabel = new Label("MP:", skin, "default");
        manaBar = new ProgressBar(0, player.getMaxMana(), 1, false, skin, "mana-bar-style");
        manaBar.setValue(player.getCurrentMana());
        this.manaValueLabel = new Label(player.getCurrentMana() + "/" + player.getMaxMana(), skin, "default");

        hudTable.add(manaLabel).padRight(5).padTop(5);
        hudTable.add(manaBar).width(200).height(20).padRight(10).padTop(5);
        hudTable.add(this.manaValueLabel).padTop(5).row();

        // --- Experience & Level ---
        //levelLabel = new Label("Lv: " + player.getLevel(), skin, "default");
        experienceBar = new ProgressBar(0, player.getExperienceToNextLevel(), 1, false, skin, "xp-bar-style");
        experienceBar.setValue(player.getExperience());
        this.experienceValueLabel = new Label(player.getExperience() + "/" + player.getExperienceToNextLevel(), skin, "default");

        hudTable.add(new Label("XP:", skin, "default")).padRight(5).padTop(5); // Label "XP:"
        hudTable.add(experienceBar).width(200).height(20).padRight(10).padTop(5);
        hudTable.add(this.experienceValueLabel).padTop(5).row();

        hudStage.addActor(hudTable);
        // Các label experienceLabel, healthLabel, manaLabel (nếu chỉ dùng cho tiêu đề) không cần update text nữa
        // Chúng ta sẽ update healthValueLabel, manaValueLabel, experienceValueLabel

        // --- Thiết lập Inventory UI ---
        setupInventoryUI();

        // --- Tải TileMap ---
        try {
            tiledMap = new TmxMapLoader().load("Map/Map.tmx"); 
            // Đơn vị của map renderer, nếu bạn muốn mỗi pixel trong map tương ứng 1 đơn vị thế giới:
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, game.batch);
            // Nếu bạn muốn dùng unit scale (ví dụ 1 tile = 32 đơn vị thế giới, mỗi đơn vị là 1 pixel)
            float unitScale = 3f / 1f; // Ví dụ: 1 pixel trên màn hình = 1/32 đơn vị thế giới
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, unitScale, game.batch);
            // Việc dùng unitScale sẽ ảnh hưởng đến tọa độ và tốc độ của player, chúng ta sẽ giữ đơn giản trước.

            Gdx.app.log("GamePlayScreen", "Tilemap loaded successfully.");
        } catch (Exception e) {
            Gdx.app.error("GamePlayScreen", "Error loading tilemap", e);
            tiledMap = null; // Để tránh lỗi NullPointerException nếu tải thất bại
            tiledMapRenderer = null;
        }

        // --- Thiết lập InputMultiplexer ---
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(hudStage); // HUD nhận input trước (thường là vậy)
        inputMultiplexer.addProcessor(new GameInputAdapter()); // Thêm bộ xử lý input cho game world

        Gdx.app.log("GamePlayScreen", "GamePlayScreen created");

        // --- Tải TileMap và lấy kích thước ---
        if (tiledMap != null) { // Đảm bảo tiledMap đã được tải thành công
            MapProperties prop = tiledMap.getProperties();
            int mapWidthInTiles = prop.get("width", Integer.class);
            int mapHeightInTiles = prop.get("height", Integer.class);
            int tilePixelWidth = prop.get("tilewidth", Integer.class);
            int tilePixelHeight = prop.get("tileheight", Integer.class);

            this.mapPixelWidth = mapWidthInTiles * tilePixelWidth * 3;
            this.mapPixelHeight = mapHeightInTiles * tilePixelHeight * 3;
            Gdx.app.log("GamePlayScreen", "Map dimensions: " + mapPixelWidth + "x" + mapPixelHeight + " pixels");

            this.collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Tile Layer 3");
            if (collisionLayer == null) {
                Gdx.app.error("GamePlayScreen", "Collision layer 'Tile Layer 2' not found!");
            }
            // Lấy kích thước tile để tính toán tọa độ ô
            this.tilePixelWidth = tiledMap.getProperties().get("tilewidth", Integer.class) * 3;
            this.tilePixelHeight = tiledMap.getProperties().get("tileheight", Integer.class) * 3;
        } else {
            // Đặt giá trị mặc định hoặc xử lý nếu map không tải được
            this.mapPixelWidth = Gdx.graphics.getWidth(); // Hoặc một giá trị an toàn khác
            this.mapPixelHeight = Gdx.graphics.getHeight();
        }

        // --- **SPAWN ENEMIES TỪ MAP** ---
        this.enemies = new Array<>();
        spawnEnemiesFromMap();

        shapeRenderer = new ShapeRenderer();

        try {
            // Thay "sounds/attack_swing.wav" bằng đường dẫn file âm thanh của bạn
            attackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/attack_player.mp3"));
            enemyattackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/attack_enemy.wav"));
            playerDeathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Die.MP3"));
        } catch (Exception e) {
            Gdx.app.error("SoundLoader", "Could not load attack sound", e);
        }
    }

    private void spawnEnemiesFromMap() {
        if (tiledMap == null) {
            Gdx.app.error("EnemySpawn", "TiledMap is not loaded. Cannot spawn enemies.");
            return;
        }

        // Lấy layer object có tên là "enemies"
        MapLayer objectLayer = tiledMap.getLayers().get("Enemies");
        if (objectLayer == null) {
            Gdx.app.log("EnemySpawn", "No 'Enemies' object layer found in the map.");
            return;
        }

        MapObjects objects = objectLayer.getObjects();
        if (objects.getCount() == 0) {
             Gdx.app.log("EnemySpawn", "The 'Enemies' object layer contains no objects.");
             return;
        }

        Gdx.app.log("EnemySpawn", "Found " + objects.getCount() + " enemy spawn points.");

        // Lặp qua từng object (spawn point) trong layer
        for (MapObject object : objects) {
            if (object.getProperties().containsKey("x") && object.getProperties().containsKey("y")) {
                float unitScale = 3f / 1f;
                float x = object.getProperties().get("x", Float.class) * unitScale;
                float y = object.getProperties().get("y", Float.class) * unitScale;

                // Tạo một enemy mới tại vị trí này.
                // Bạn có thể tùy chỉnh các chỉ số của enemy ở đây, hoặc đọc chúng từ properties của object trong Tiled
                Enemy newEnemy = new Enemy("Wolf", 1, 50, 15, 2, 120, new java.util.ArrayList<>(), 10);
                newEnemy.setPosition(x, y); // Đặt vị trí cho enemy theo tọa độ thế giới
                enemies.add(newEnemy);

                Gdx.app.log("EnemySpawn", "Spawned an enemy at world coordinates: (" + x + ", " + y + ")");
            }
        }
    }

    private boolean isCellBlocked(float x, float y) {
        if (collisionLayer == null) return false; // Nếu không có layer va chạm thì không có gì cản cả

        // Chuyển đổi tọa độ pixel của thế giới game sang tọa độ ô (cell) của map
        int cellX = (int) (x / tilePixelWidth);
        int cellY = (int) (y / tilePixelHeight);

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(cellX, cellY);

        // Một ô được coi là "cản" nếu nó không rỗng (cell != null) VÀ ô đó chứa một tile (cell.getTile() != null)
        return cell != null && cell.getTile() != null;
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

        // --- Khởi tạo nút Use ---
        useItemButton = new TextButton("Use", skin, "default");
        useItemButton.setVisible(false); // Ban đầu ẩn nút Use

        useItemButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentSelectedItem != null && isItemUsable(currentSelectedItem)) { // Kiểm tra xem item có dùng được không
                    Gdx.app.log("InventoryAction", "Attempting to use: " + currentSelectedItem.getName());
                    if (player.useItem(currentSelectedItem)) {
                        Gdx.app.log("InventoryAction", currentSelectedItem.getName() + " used successfully.");
                        populateInventoryTable(); // Cập nhật lại danh sách hành trang
                        // Sau khi dùng, item có thể đã biến mất hoặc thay đổi
                        // Ẩn mô tả và các nút hành động, reset item đang chọn
                        if (player.getInventory().contains(currentSelectedItem)) {
                            // Nếu item vẫn còn (ví dụ không phải consumable 1 lần dùng, hoặc dùng thất bại nhưng vẫn còn)
                            showItemDescription(currentSelectedItem); // Hiển thị lại thông tin của nó
                        } else {
                            // Item đã bị tiêu thụ hoàn toàn
                            hideItemDescription(); // Ẩn mô tả, ẩn nút Use/Drop
                            currentSelectedItem = null; // Không còn item nào được chọn
                        }
                    } else {
                        Gdx.app.log("InventoryAction", "Could not use " + currentSelectedItem.getName());
                        // Hiển thị thông báo lỗi cho người chơi (ví dụ: máu đầy)
                        itemDescriptionLabel.setText("Could not use " + currentSelectedItem.getName() + ".");
                        // Nút Use/Drop vẫn hiển thị vì item vẫn đang được chọn
                    }
                }
            }
        });

        // Thêm một hàng mới cho nút Drop (hoặc đặt nó ở vị trí khác tùy ý)
        // Ví dụ: đặt nó dưới phần mô tả
        Table actionButtonsTable = new Table(); // Tạo một bảng nhỏ để chứa các nút hành động nếu cần
        // Cập nhật actionButtonsTable để thêm nút Use
        // Table actionButtonsTable = new Table(); // Đã khai báo ở bước trước
        actionButtonsTable.clearChildren(); // Xóa các nút cũ nếu có (đảm bảo thứ tự đúng)
        actionButtonsTable.add(useItemButton).pad(5).uniformX().fillX(); // uniformX và fillX để nút đều và chiếm không gian
        actionButtonsTable.add(dropItemButton).pad(5).uniformX().fillX();

        inventoryWindow.row(); // Xuống hàng mới sau ScrollPane và DescriptionLabel
        inventoryWindow.add(actionButtonsTable).pad(5); // Thêm bảng chứa nút Drop

        // Thêm inventoryWindow vào hudStage để nó được vẽ và nhận input (khi visible)
        // Chúng ta thêm nó vào hudStage để nó được vẽ trên cùng các element khác của HUD
        hudStage.addActor(inventoryWindow);
    }

    private boolean isItemUsable(Item item) {
        if (item == null) return false;
        // Hiện tại, chúng ta dựa vào type "Consumable"
        // Bạn có thể mở rộng logic này sau (ví dụ: item có cờ isUsable, hoặc kiểm tra các type khác)
        return "Consumable".equalsIgnoreCase(item.getType());
    }

    private void populateInventoryTable() {
        inventoryItemTable.clearChildren(); // Xóa các item cũ trước khi cập nhật
        List<Item> items = player.getInventory();

        if (items.isEmpty()) {
            inventoryItemTable.add(new Label("Inventory is empty.", skin)).pad(10);
        } else {
            for (Item item : items) {
                String buttonText = item.getName();
                if (item.isStackable() && item.getQuantity() > 1) { // Hoặc chỉ cần item.getQuantity() > 1 nếu mọi item > 1 đều là stackable
                    buttonText += " (x" + item.getQuantity() + ")";
                }
                buttonText += " (" + item.getType() + ")";

                TextButton itemButton = new TextButton(buttonText, skin, "default");

                itemButton.setUserObject(item); // **Gán đối tượng Item vào UserObject của Button**

                itemButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Item clickedItem = (Item) ((TextButton)event.getListenerActor()).getUserObject();
                        Gdx.app.log("InventoryClick", "Clicked on: " + clickedItem.getName());

                        // Hiển thị mô tả trước
                        showItemDescription(clickedItem);
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

            // Kiểm tra xem item có dùng được không để hiển thị nút Use
            if (isItemUsable(item)) {
                useItemButton.setVisible(true);
            } else {
                useItemButton.setVisible(false);
            }
        } else {
            itemDescriptionLabel.setText("No description available.");
            dropItemButton.setVisible(false); // Ẩn nút Drop nếu không có item hoặc mô tả
            useItemButton.setVisible(false);
            currentSelectedItem = null;
        }
    }

    private void hideItemDescription() {
        itemDescriptionLabel.setText("Select an item to see its description.");
        dropItemButton.setVisible(false); // Luôn ẩn nút Drop khi không có mô tả/item nào được chọn
        useItemButton.setVisible(false);
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

        try {
            // Thay "sounds/gameplay_music.mp3" bằng đường dẫn đến file nhạc 
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/gameplay_music.mp3"));
            backgroundMusic.setLooping(true); // Đặt cho nhạc lặp lại
            backgroundMusic.play();           // Bắt đầu phát nhạc
        } catch (Exception e) {
            Gdx.app.error("MusicLoader", "Could not load background music", e);
        }
    }

    // Hàm cập nhật logic game (ví dụ)
    private void updateGame(float delta) {
        if (settingsWindow.isVisible()) {
            return;
        }

        if (player.isDead()) {    
            // Hiển thị thông báo Game Over
            if (!gameOverLabel.isVisible()) {
                gameOverLabel.setVisible(true);
                backToMenuButton.setVisible(true);

                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }

                if (playerDeathSound != null) {
                    playerDeathSound.play(SettingsManager.sfxVolume); // play(volume)
                }
            }
        } else {
            PlayerCharacter.PlayerState playerStateBeforeUpdate = player.getCurrentState();
            // Di chuyển nhân vật, cập nhật AI, xử lý va chạm, v.v.
            player.update(delta, movingUp, movingDown, movingLeft, movingRight, playerAttackRequested);
            playerAttackRequested = false; // Reset cờ sau khi đã truyền đi, để chỉ tấn công 1 lần mỗi lần nhấn

            PlayerCharacter.PlayerState playerStateAfterUpdate = player.getCurrentState();

            if (playerStateBeforeUpdate != PlayerCharacter.PlayerState.ATTACKING && playerStateAfterUpdate == PlayerCharacter.PlayerState.ATTACKING) {
                if (attackSound != null) {
                    attackSound.play(SettingsManager.sfxVolume); // play(volume)
                }
            }

            handlePlayerMovement(delta); // Gọi hàm xử lý di chuyển

            for (Enemy enemy : enemies) {
                Enemy.EnemyState enemyStateBeforeUpdate = enemy.getCurrentState();
                enemy.update(delta, player); // Hàm này giờ chỉ cập nhật trạng thái
                Enemy.EnemyState enemyStateAfterUpdate = enemy.getCurrentState();

                // Nếu trạng thái vừa chuyển sang ATTACKING, phát âm thanh
                if (enemyStateBeforeUpdate != Enemy.EnemyState.ATTACKING && enemyStateAfterUpdate == Enemy.EnemyState.ATTACKING) {
                    if (enemyattackSound != null) {
                        enemyattackSound.play(SettingsManager.sfxVolume); // Có thể cho âm lượng nhỏ hơn một chút
                    }
                }
            }
            handleEnemyMovement(delta); // Hàm này xử lý di chuyển vật lý và va chạm

            // --- Gọi hàm xử lý combat ---
            handleCombat();

            for (Enemy enemy : enemies) {
                enemy.update(delta, player);
            }
        }

        // --- CẬP NHẬT VỊ TRÍ CAMERA ĐỂ THEO SAU NGƯỜI CHƠI ---
        if (player != null) { // Đảm bảo player đã được khởi tạo
            gameCamera.position.set(player.getX(), player.getY(), 0); // z = 0 cho 2D
        }

        // --- Giới hạn Camera trong phạm vi Map ---
        if (tiledMap != null) { // Chỉ giới hạn nếu có map
            // Tính toán kích thước thực tế của vùng nhìn thấy qua camera (đã tính zoom)
            float cameraHalfWidth = gameCamera.viewportWidth * gameCamera.zoom / 2f;
            float cameraHalfHeight = gameCamera.viewportHeight * gameCamera.zoom / 2f;

            float effectiveViewportWidth = cameraHalfWidth * 2;
            float effectiveViewportHeight = cameraHalfHeight * 2;

            // Tính toán giới hạn cho tâm của camera
            float minCameraX = cameraHalfWidth;
            float maxCameraX = mapPixelWidth - cameraHalfWidth;
            float minCameraY = cameraHalfHeight;
            float maxCameraY = mapPixelHeight - cameraHalfHeight;

            // Giới hạn vị trí X của camera
            // Đảm bảo rằng map luôn lớn hơn hoặc bằng kích thước viewport của camera
            if (mapPixelWidth > effectiveViewportWidth) {
                gameCamera.position.x = MathUtils.clamp(gameCamera.position.x, minCameraX, maxCameraX);
            } else {
                gameCamera.position.x = mapPixelWidth / 2f;
            }

            if (mapPixelHeight > effectiveViewportHeight) {
                gameCamera.position.y = MathUtils.clamp(gameCamera.position.y, minCameraY, maxCameraY);
            } else {
                gameCamera.position.y = mapPixelHeight / 2f;
            }
        }

        // --- Cập nhật HUD ---
        // Health
        healthBar.setRange(0, player.getMaxHealth());
        healthBar.setValue(player.getCurrentHealth());
        healthValueLabel.setText(player.getCurrentHealth() + "/" + player.getMaxHealth());

        // Mana
        manaBar.setRange(0, player.getMaxMana());
        manaBar.setValue(player.getCurrentMana());
        manaValueLabel.setText(player.getCurrentMana() + "/" + player.getMaxMana());

        // Level & Experience
        //levelLabel.setText("Lv: " + player.getLevel());
        experienceBar.setRange(0, player.getExperienceToNextLevel());
        experienceBar.setValue(player.getExperience());
        experienceValueLabel.setText(player.getExperience() + "/" + player.getExperienceToNextLevel());
    }

    private void handlePlayerMovement(float delta) {
        if (inventoryVisible) return; // Không cho di chuyển khi inventory mở

        float currentSpeed = player.getSpeed();
        float moveAmount = currentSpeed * delta; // Lượng di chuyển dựa trên tốc độ và delta time

        // Tạo vector di chuyển để chuẩn hóa nếu di chuyển chéo
        float velocityX = 0;
        float velocityY = 0;

        if (movingUp) {
            velocityY += moveAmount;
        }
        if (movingDown) {
            velocityY -= moveAmount;
        }
        if (movingLeft) {
            velocityX -= moveAmount;
        }
        if (movingRight) {
            velocityX += moveAmount;
        }

        // Xử lý di chuyển chéo: Nếu cả hai hướng đều có vận tốc,
        // nhân vật sẽ di chuyển nhanh hơn theo đường chéo.
        // Để tránh điều này, chúng ta có thể chuẩn hóa vector vận tốc.
        if (velocityX != 0 && velocityY != 0) {
            // (velocity.len() là độ dài vector, nếu khác 0 thì chia cho nó để được vector đơn vị)
            // Sau đó nhân với moveAmount để có tốc độ chuẩn.
            // Cách đơn giản hơn (gần đúng) là giảm tốc độ khi đi chéo:
            float diagonalFactor = 0.7071f; // Khoảng 1/sqrt(2)
            velocityX *= diagonalFactor;
            velocityY *= diagonalFactor;
        }
        
        // --- Kiểm tra va chạm và di chuyển theo trục X ---
        Rectangle pBox = player.getBoundingBox(); // Lấy bounding box của player
        pBox.x += velocityX; // Thử di chuyển bounding box theo X

        // Kiểm tra các điểm trên bounding box sau khi di chuyển theo X
        // Ví dụ: kiểm tra 2 góc của cạnh sẽ di chuyển tới
        boolean collisionX = false;
        if (velocityX > 0) { // Di chuyển sang phải
            collisionX = isCellBlocked(pBox.x + pBox.width, pBox.y) || isCellBlocked(pBox.x + pBox.width, pBox.y + pBox.height);
        } else if (velocityX < 0) { // Di chuyển sang trái
            collisionX = isCellBlocked(pBox.x, pBox.y) || isCellBlocked(pBox.x, pBox.y + pBox.height);
        }

        if (!collisionX) {
            player.x += velocityX;
        }
    
        // --- Kiểm tra va chạm và di chuyển theo trục Y ---
        pBox.x = player.getBoundingBox().x; // Reset pBox.x về vị trí hiện tại (đã được xác thực)
        pBox.y += velocityY; // Thử di chuyển bounding box theo Y

        boolean collisionY = false;
        if (velocityY > 0) { // Di chuyển lên
            collisionY = isCellBlocked(pBox.x, pBox.y + pBox.height) || isCellBlocked(pBox.x + pBox.width, pBox.y + pBox.height);
        } else if (velocityY < 0) { // Di chuyển xuống
            collisionY = isCellBlocked(pBox.x, pBox.y) || isCellBlocked(pBox.x + pBox.width, pBox.y);
        }

        if (!collisionY) {
            player.y += velocityY;
        }

        // Cập nhật lại vị trí bounding box cuối cùng
        player.updateBoundingBox();
    }

    private void handleEnemyMovement(float delta) {
        if (enemies == null || enemies.isEmpty()) return;

        for (Enemy enemy : enemies) {
            // Chỉ xử lý di chuyển nếu enemy đang ở trạng thái WALKING
            if (enemy.getCurrentState() != Enemy.EnemyState.RUN) {
                continue; // Bỏ qua enemy này nếu nó không di chuyển
            }

            // --- Logic di chuyển và va chạm tương tự handlePlayerMovement ---
            float currentSpeed = enemy.getSpeed();
            float moveAmount = currentSpeed * delta;

            // Tính toán vector hướng về phía người chơi
            float velocityX = 0;
            float velocityY = 0;
        
            float angle = (float) Math.atan2(player.getY() - enemy.getY(), player.getX() - enemy.getX());
            velocityX = (float) Math.cos(angle) * moveAmount;
            velocityY = (float) Math.sin(angle) * moveAmount;

            // Lấy bounding box của enemy
            Rectangle eBox = enemy.getBoundingBox();
            eBox.x = enemy.getX() - eBox.width / 2f; // Cập nhật lại vị trí box trước khi kiểm tra
            eBox.y = enemy.getY() - enemy.getBoundingBox().height / 2f;

            // --- Kiểm tra va chạm và di chuyển theo trục X ---
            eBox.x += velocityX;
            boolean collisionX = false;
            if (velocityX > 0) { // Di chuyển sang phải
                collisionX = isCellBlocked(eBox.x + eBox.width, eBox.y) || isCellBlocked(eBox.x + eBox.width, eBox.y + eBox.height);
            } else if (velocityX < 0) { // Di chuyển sang trái
                collisionX = isCellBlocked(eBox.x, eBox.y) || isCellBlocked(eBox.x, eBox.y + eBox.height);
            }

            if (!collisionX) {
                enemy.setPosition(enemy.getX() + velocityX, enemy.getY()); // Cập nhật vị trí X thật
            }

            // --- Kiểm tra va chạm và di chuyển theo trục Y ---
            eBox.x = enemy.getBoundingBox().x; // Reset pBox.x
            eBox.y += velocityY;
            boolean collisionY = false;
            if (velocityY > 0) { // Di chuyển lên
                collisionY = isCellBlocked(eBox.x, eBox.y + eBox.height) || isCellBlocked(eBox.x + eBox.width, eBox.y + eBox.height);
            } else if (velocityY < 0) { // Di chuyển xuống
                collisionY = isCellBlocked(eBox.x, eBox.y) || isCellBlocked(eBox.x + eBox.width, eBox.y);
            }

            if (!collisionY) {
                enemy.setPosition(enemy.getX(), enemy.getY() + velocityY); // Cập nhật vị trí Y thật
            }

            // Cập nhật lại vị trí bounding box cuối cùng
            enemy.updateBoundingBox();
        }
    }

    private void handleCombat() {
        // 1. Người chơi tấn công Enemy
        if (player.isAttackHitboxActive()) {
            for (Enemy enemy : enemies) {
                if (player.getAttackHitbox().overlaps(enemy.getBoundingBox())) {
                    player.attack(enemy); // Phương thức attack đã xử lý việc chỉ đánh 1 lần
                }
            }
        }

        // 2. Enemy tấn công người chơi
        for (Enemy enemy : enemies) {
            if (enemy.isAttackHitboxActive()) {
                if (enemy.getAttackHitbox().overlaps(player.getBoundingBox())) {
                    enemy.attack(player);
                }
            }
        }
    }

    private void handleInput(float delta) {
        // Ví dụ xử lý input chung cho GamePlayScreen, không thuộc GameInputAdapter
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
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

        backgroundMusic.setVolume(SettingsManager.musicVolume);  // Đặt âm lượng (từ 0.0 đến 1.0)

        // 1. Cập nhật logic game (di chuyển, AI, va chạm,...)
        updateGame(delta);
        handleInput(delta);

        // --- CẬP NHẬT VÀ ÁP DỤNG GAME CAMERA ---
        gameCamera.update(); // Rất quan trọng: Cập nhật camera sau khi đã thay đổi vị trí (hoặc các thuộc tính khác)
        game.batch.setProjectionMatrix(gameCamera.combined); // Áp dụng cho SpriteBatch

        // --- Vẽ TileMap ---
        if (tiledMapRenderer != null) {
            tiledMapRenderer.setView(gameCamera); // Đặt "góc nhìn" của map renderer theo gameCamera
            tiledMapRenderer.render();           // Vẽ tất cả các layer của map
        }

        // --- Vẽ các đối tượng game khác (Player, Enemies, Items trên đất, v.v.) ---
        // --- Vẽ thế giới game (sẽ di chuyển theo camera) ---
        game.batch.begin();
        // --- Vẽ Player bằng currentFrame ---
        if (player != null && player.getCurrentFrame() != null) {
            TextureRegion frame = player.getCurrentFrame();
            // Vẽ frame tại vị trí player, căn giữa frame
            game.batch.draw(frame,
                            player.getX() - PlayerCharacter.FRAME_WIDTH / 2f,
                            player.getY() - PlayerCharacter.FRAME_HEIGHT / 2f,
                            PlayerCharacter.FRAME_WIDTH,
                            PlayerCharacter.FRAME_HEIGHT);
        }

        for (Enemy enemy : enemies) {
            if (enemy.getCurrentState() == Enemy.EnemyState.DEAD) {
                // Nếu enemy đã chết, vẽ hình ảnh cái xác
                Texture corpse = enemy.getCorpseTexture();
                if (corpse != null) {
                    game.batch.draw(corpse,
                    enemy.getX() - Enemy.FRAME_WIDTH / 2f,
                    enemy.getY() - Enemy.FRAME_HEIGHT / 2f,
                    Enemy.FRAME_WIDTH - 10,
                    Enemy.FRAME_HEIGHT - 10);
                }
            } else {
                // Nếu enemy còn sống, vẽ animation frame hiện tại
                TextureRegion frame = enemy.getCurrentFrame();
                if (frame != null) {
                    game.batch.draw(frame,
                    enemy.getX() - Enemy.FRAME_WIDTH / 2f,
                    enemy.getY() - Enemy.FRAME_HEIGHT / 2f,
                    Enemy.FRAME_WIDTH,
                    Enemy.FRAME_HEIGHT);
                }
            }
        }
        // Vẽ các entities khác ở đây
        game.batch.end();

        // --- Vẽ các hitbox và hurtbox để debug ---
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    
        // Vẽ hurtbox của player (màu xanh)
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(player.getBoundingBox().x, player.getBoundingBox().y, player.getBoundingBox().width, player.getBoundingBox().height);
    
        // Vẽ hitbox tấn công của player nếu đang active (màu đỏ)
        if (player.isAttackHitboxActive()) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(player.getAttackHitbox().x, player.getAttackHitbox().y, player.getAttackHitbox().width, player.getAttackHitbox().height);
        }
    
        // Vẽ cho các enemies
        for (Enemy enemy : enemies) {
            // Vẽ hurtbox của enemy (màu xanh)
                shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(enemy.getBoundingBox().x, enemy.getBoundingBox().y, enemy.getBoundingBox().width, enemy.getBoundingBox().height);

            // Vẽ hitbox tấn công của enemy nếu đang active (màu đỏ)
            if (enemy.isAttackHitboxActive()) {
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(enemy.getAttackHitbox().x, enemy.getAttackHitbox().y, enemy.getAttackHitbox().width, enemy.getAttackHitbox().height);
            }
        }
    
        shapeRenderer.end();

        // 3. Cập nhật và Vẽ HUD
        // hudViewport.apply(); // Không cần gọi nếu hudStage tự quản lý batch và viewport
        hudStage.act(Math.min(delta, 1 / 30f));
        hudStage.draw();

        // Gdx.app.log("GamePlayScreen", "Rendering GamePlayScreen");
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GamePlayScreen", "Resizing to: " + width + "x" + height);
        gameViewport.update(width, height, false); // Cập nhật viewport của game, true để căn giữa camera
        hudViewport.update(width, height, true);  // Cập nhật viewport của HUD
        //gameCamera.setToOrtho(false, width, height); // Cập nhật camera nếu không dùng FitViewport/ExtendViewport cho game world
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
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("GamePlayScreen", "Disposing GamePlayScreen");
        hudStage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        if (player != null) {
            player.dispose(); // Gọi dispose của player để giải phóng texture animation
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (tiledMapRenderer != null) {
            tiledMapRenderer.dispose(); // Quan trọng!
        }
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        shapeRenderer.dispose();
        // Dispose các tài nguyên khác của màn hình game (map, textures nhân vật,...)
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (attackSound != null) {
            attackSound.dispose();
        }
        if (enemyattackSound != null) {
            enemyattackSound.dispose();
        }
        if (playerDeathSound != null) {
            playerDeathSound.dispose();
        }
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

            if (keycode == Input.Keys.T) {
                settingsWindow.setVisible(!settingsWindow.isVisible()); // Bật/tắt cửa sổ
                return true;
            }

            if (inventoryVisible) return false;
                // Di chuyển nhân vật ví dụ
            boolean keyProcessed = true; // Mặc định là input đã được xử lý
            switch (keycode) {
                case Input.Keys.W:
                    movingUp = true;
                    break;
                case Input.Keys.S:
                    movingDown = true;
                    break;
                case Input.Keys.A:
                    movingLeft = true;
                    break;
                case Input.Keys.D:
                    movingRight = true;
                    break;
                case Input.Keys.ESCAPE:
                    Gdx.app.log("GameInputAdapter", "Escape pressed - Returning to Main Menu");
                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                    break;
                default:
                    keyProcessed = false; // Nếu không phải phím chúng ta quan tâm, đánh dấu là chưa xử lý
                    break;
            }

            if (keycode == Input.Keys.J) {
                if (!inventoryVisible) { // Chỉ tấn công khi không ở trong inventory
                    playerAttackRequested = true;
                    keyProcessed = true; // Đánh dấu phím đã được xử lý
                }
            }

            Gdx.app.log("GameInputAdapter", "Key Down: " + Input.Keys.toString(keycode)); // Có thể giữ lại để debug
            return keyProcessed; // Trả về true nếu là phím di chuyển hoặc ESC/F5
        }

        @Override
        public boolean keyUp(int keycode) {
            if (inventoryVisible && keycode != Input.Keys.I) return false; // Cho phép phím I hoạt động để đóng inventory

            boolean keyProcessed = true;
            switch (keycode) {
                case Input.Keys.W:
                    movingUp = false;
                    break;
                case Input.Keys.S:
                    movingDown = false;
                    break;
                case Input.Keys.A:
                    movingLeft = false;
                    break;
                case Input.Keys.D:
                    movingRight = false;
                    break;
                default:
                    keyProcessed = false;
                    break;
            }
            return keyProcessed;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }
    }
}