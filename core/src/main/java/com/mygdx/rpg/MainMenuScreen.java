package com.mygdx.rpg; // Hoặc package của bạn

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label; 
import com.badlogic.gdx.scenes.scene2d.ui.Slider; 
import com.badlogic.gdx.scenes.scene2d.ui.Dialog; 
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener; 
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Cell; // Import Cell

public class MainMenuScreen implements Screen {

    private final RPGGame game;
    private Stage stage;
    private Viewport viewport;
    private Skin skin;
    private Texture backgroundTexture;
    private Image backgroundImage;
    private Texture settingsIconTexture;
    private Texture exitIconTexture;
    private Texture startIconTexture;
    private Texture gameNameTexture; 
    private Image gameNameImage;     

    private Dialog optionsDialog; 
    private Table mainTable; // <<< ĐẢM BẢO KHAI BÁO mainTable LÀ BIẾN THÀNH VIÊN

    // <<< KHAI BÁO CÁC NÚT LÀ BIẾN THÀNH VIÊN NẾU CẦN TRUY CẬP TỪ NHIỀU PHƯƠNG THỨC >>>
    private ImageButton startImageButton;
    private ImageButton optionsButton;
    private ImageButton exitImageButton;


    public MainMenuScreen(final RPGGame game) {
        this.game = game;
        viewport = new ScreenViewport();
        stage = new Stage(viewport, game.batch);

        // Load background texture
        try {
            backgroundTexture = new Texture(Gdx.files.internal("MainMenuScreen/mainmenuscreen.png"));
            backgroundImage = new Image(backgroundTexture);
            // Kích thước và vị trí sẽ được đặt trong resize()
            stage.addActor(backgroundImage); 
        } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading background image", e); }

        // Load Game Name Texture
        try {
            gameNameTexture = new Texture(Gdx.files.internal("MainMenuScreen/name_game.png"));
            gameNameImage = new Image(gameNameTexture);
            stage.addActor(gameNameImage); 
        } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading name_game.png", e); }


        // Load UI skin
        try { skin = new Skin(Gdx.files.internal("uiskin/uiskin.json")); } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading skin", e); skin = new Skin(); }

        // Load icons textures
        try { settingsIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/setting_icon.png")); } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading setting_icon.png", e); }
        try { exitIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/exit_game.png")); } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading exit_game.png", e); }
        try { startIconTexture = new Texture(Gdx.files.internal("MainMenuScreen/play_button.png")); } catch (Exception e) { Gdx.app.error("MainMenuScreen", "Error loading play_button.png", e); }

        Gdx.input.setInputProcessor(stage);

        // --- TẠO OPTIONS DIALOG --- 
        optionsDialog = new Dialog("Settings", skin, "dialog") { 
            @Override 
            protected void result(Object object) { Gdx.app.log("OptionsDialog", "Dialog closed with result: " + object); } 
        };
        optionsDialog.setModal(true); 
        optionsDialog.setMovable(true); 

        Table dialogContentTable = optionsDialog.getContentTable(); 
        Label volumeLabel = new Label("Sound:", skin); 
        final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin); 
        volumeSlider.setValue(0.75f); 

        volumeSlider.addListener(new ChangeListener() { 
            @Override 
            public void changed(ChangeEvent event, Actor actor) { Gdx.app.log("VolumeSlider", "New Value: " + volumeSlider.getValue()); } 
        }); 

        dialogContentTable.add(volumeLabel).padRight(15f); 
        dialogContentTable.add(volumeSlider).width(Gdx.graphics.getWidth() * 0.2f).height(50f); 

        TextButton closeButtonDialog = new TextButton("Close", skin); // Đổi tên biến để tránh trùng
        optionsDialog.button(closeButtonDialog).padBottom(20f); 
        optionsDialog.getButtonTable().padTop(20f); 
        
        // Kích thước dialog sẽ được đặt trong resize hoặc khi show lần đầu
        // optionsDialog.setSize(2000f, 1200f); // Bỏ dòng này, để resize() xử lý


        // --- THIẾT LẬP UI CHÍNH (MAIN MENU BUTTONS) --- 
        mainTable = new Table(); // Khởi tạo biến thành viên mainTable
        mainTable.setFillParent(true); 
        // mainTable.setDebug(true); 
        stage.addActor(mainTable); 

        // Khởi tạo các biến thành viên ImageButton
        // ImageButton startImageButton = null; // Xóa khai báo cục bộ
        // ImageButton optionsButton = null;  // Xóa khai báo cục bộ
        // ImageButton exitImageButton = null; // Xóa khai báo cục bộ

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
        } else { Gdx.app.log("MainMenuScreen", "Start icon texture not loaded."); } 

        // Tạo ImageButton cho Options 
        if (settingsIconTexture != null) { 
            ImageButton.ImageButtonStyle optionsStyle = new ImageButton.ImageButtonStyle(); 
            optionsStyle.imageUp = new TextureRegionDrawable(settingsIconTexture); 
            optionsButton = new ImageButton(optionsStyle); 
            optionsButton.addListener(new ClickListener() { 
                @Override 
                public void clicked(InputEvent event, float x, float y) { 
                    Gdx.app.log("MainMenuScreen", "Options ImageButton clicked"); 
                    if (optionsDialog != null) { 
                        // Kích thước và vị trí dialog sẽ được đặt trong resize(), được gọi từ show()
                        optionsDialog.show(stage); 
                        // Căn giữa lại sau khi show để đảm bảo nó có kích thước đúng
                        optionsDialog.setPosition( 
                                Math.round((stage.getWidth() - optionsDialog.getWidth()) / 2), 
                                Math.round((stage.getHeight() - optionsDialog.getHeight()) / 2) 
                        ); 
                    } 
                } 
            }); 
        } else { Gdx.app.log("MainMenuScreen", "Settings icon texture not loaded."); } 

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
        } else { Gdx.app.log("MainMenuScreen", "Exit icon texture not loaded."); } 

        // 1. Tạo bảng con cho các nút Options và Exit ở góc trên phải 
        Table topRightButtonsTable = new Table(); 
        // topRightButtonsTable.setDebug(true); 

        // Kích thước sẽ được đặt trong resize
        // float iconButtonSize = Gdx.graphics.getHeight() * 0.08f; 
        // float paddingBetweenSmallButtons = iconButtonSize * 0.2f; 

        if (optionsButton != null) { 
            topRightButtonsTable.add(optionsButton); //.size(iconButtonSize).padRight(paddingBetweenSmallButtons); 
        } 
        if (exitImageButton != null) { 
            topRightButtonsTable.add(exitImageButton); //.size(iconButtonSize); 
        } 

        // 2. Kích thước nút Start Game (ImageButton) 
        // Sẽ được đặt trong resize
        // float startButtonWidth = Gdx.graphics.getWidth() * 0.35f; 
        // float startButtonHeight = startButtonWidth * (180f / 700f); 

        // --- Bố cục trong mainTable --- 
        mainTable.add().expandX(); 
        mainTable.add(topRightButtonsTable).top().right(); // Padding sẽ đặt trong resize
        mainTable.row(); 

        mainTable.add().colspan(2).expandY(); 
        mainTable.row(); 

        // float currentScreenHeight = Gdx.graphics.getHeight(); // Sẽ lấy trong resize
        // float padding_start_button_from_bottom = currentScreenHeight * 0.15f; 

        if (startImageButton != null) { 
            mainTable.add(startImageButton) 
                    .colspan(2) 
                    .center() 
                    .bottom(); // Padding sẽ đặt trong resize
        } else { 
            // mainTable.add().colspan(2).height(startButtonHeight).padBottom(Gdx.graphics.getHeight() * 0.15f); 
            mainTable.add().colspan(2); // Giữ chỗ nếu không có nút
        } 
    } 

    @Override 
    public void show() { 
        Gdx.input.setInputProcessor(stage); 
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); 
    } 

    @Override 
    public void render(float delta) { 
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); 
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); 

        if (game.batch != null) { game.batch.enableBlending(); } 

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); 
        stage.draw(); 
    } 

    @Override 
    public void resize(int width, int height) { 
        stage.getViewport().update(width, height, true); 

        if (backgroundImage != null) { 
            backgroundImage.setSize(width, height); 
            backgroundImage.setPosition(0, 0); 
        } 
        
        if (gameNameImage != null && gameNameTexture != null && gameNameTexture.isManaged()) {
            float originalImageWidth = gameNameTexture.getWidth();
            float originalImageHeight = gameNameTexture.getHeight();
            if (originalImageHeight == 0 || originalImageWidth == 0) { /* skip */ }
            else {
                float targetImageHeight = height * (2f/3f); 
                float scaleRatio = targetImageHeight / originalImageHeight;
                float targetImageWidth = originalImageWidth * scaleRatio;
                if (targetImageWidth > width * 0.8f) {
                    targetImageWidth = width * 0.8f;
                    scaleRatio = targetImageWidth / originalImageWidth;
                    targetImageHeight = originalImageHeight * scaleRatio;
                }
                gameNameImage.setSize(targetImageWidth, targetImageHeight);
                float imageX = (width - targetImageWidth) / 2;
                float centerYpoint = height * (2.0f / 3.0f);
                float imageY = centerYpoint - targetImageHeight / 2.0f;
                gameNameImage.setPosition(imageX, imageY);
            }
        }

        if (mainTable != null) {
            // Cập nhật topRightButtonsTable (cell 1 của mainTable)
            Cell<?> topRightButtonsCell = mainTable.getCells().get(1); // Ô thứ hai (index 1) của hàng đầu tiên
            if (topRightButtonsCell != null && topRightButtonsCell.getActor() instanceof Table) {
                Table topRightButtonsTable = (Table) topRightButtonsCell.getActor();
                float iconButtonSize = height * 0.07f; // Kích thước icon nhỏ hơn một chút
                float paddingBetweenSmallButtons = iconButtonSize * 0.25f;
                
                Cell<?> optCell = null;
                if(optionsButton != null) optCell = topRightButtonsTable.getCell(optionsButton);
                Cell<?> exitCell = null;
                if(exitImageButton != null) exitCell = topRightButtonsTable.getCell(exitImageButton);

                if (optCell != null) {
                    optCell.size(iconButtonSize).padRight(paddingBetweenSmallButtons);
                }
                if (exitCell != null) {
                    exitCell.size(iconButtonSize);
                }
                topRightButtonsCell.padTop(height * 0.03f).padRight(width * 0.03f); // Padding từ mép màn hình
            }

            // Cập nhật nút Start Game (cell cuối của mainTable)
            if (startImageButton != null) {
                Cell<?> startButtonCell = mainTable.getCell(startImageButton);
                if (startButtonCell != null) {
                    float startButtonWidth = width * 0.35f;
                    float startButtonHeight = startButtonWidth * (180f / 700f); // Giữ tỷ lệ
                    float padding_start_button_from_bottom = height * 0.15f;

                    startButtonCell.width(startButtonWidth).height(startButtonHeight)
                                   .padBottom(padding_start_button_from_bottom);
                }
            }
            mainTable.invalidateHierarchy();
        }

        if (optionsDialog != null ) { 
            float newDialogWidth = width * 0.50f; // Dialog rộng 50%
            float newDialogHeight = height * 0.40f; // Dialog cao 40%
            optionsDialog.setSize(newDialogWidth, newDialogHeight); 

            Table dialogContentTable = optionsDialog.getContentTable();
            if(dialogContentTable.getChildren().size > 1) {
                Actor sliderActor = null;
                for(Actor actor : dialogContentTable.getChildren()){
                    if(actor instanceof Slider){
                        sliderActor = actor;
                        break;
                    }
                }
                if(sliderActor != null){
                    Cell<Actor> sliderCell = dialogContentTable.getCell(sliderActor);
                    if(sliderCell != null) sliderCell.width(newDialogWidth * 0.60f).height(70f);
                }
            }

            Table dialogButtonTable = optionsDialog.getButtonTable();
            if(dialogButtonTable.getCells().size > 0){
                Cell<?> closeButtonCell = dialogButtonTable.getCells().first();
                if(closeButtonCell != null) closeButtonCell.width(newDialogWidth * 0.35f).height(newDialogHeight * 0.18f);
            }
            
            optionsDialog.invalidateHierarchy();
            if(optionsDialog.getStage() != null) { // Chỉ đặt vị trí nếu dialog đang được show (hoặc đã từng)
                 optionsDialog.setPosition( 
                    Math.round((stage.getWidth() - optionsDialog.getWidth()) / 2), 
                    Math.round((stage.getHeight() - optionsDialog.getHeight()) / 2) 
                ); 
            }
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
        if (startIconTexture != null) startIconTexture.dispose(); 
        if (gameNameTexture != null) gameNameTexture.dispose(); 
    } 
}