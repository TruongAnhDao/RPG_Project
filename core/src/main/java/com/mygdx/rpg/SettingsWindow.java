package com.mygdx.rpg;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class SettingsWindow extends Window {

    public SettingsWindow(Skin skin) {
        super("Settings", skin); // Tiêu đề cửa sổ

        getTitleTable().getCell(getTitleLabel()).expandX().center();

        getTitleLabel().setFontScale(3.5f);
        padTop(80f);
        padLeft(50f);

        // --- Cấu hình cửa sổ ---
        setModal(true); // Khi cửa sổ hiện ra, không thể click các thứ bên dưới
        setMovable(true);
        setVisible(false); // Ban đầu ẩn đi

        // --- Tạo các thành phần UI ---
        final Slider musicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        musicSlider.setValue(SettingsManager.musicVolume); // Gán giá trị ban đầu

        final Slider sfxSlider = new Slider(0f, 1f, 0.1f, false, skin);
        sfxSlider.setValue(SettingsManager.sfxVolume);

        // Nút 'X' để đóng cửa sổ
        TextButton closeButton = new TextButton("X", skin);
        closeButton.getLabel().setFontScale(4f);

        // --- Thêm Listener cho các thành phần ---
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SettingsManager.musicVolume = musicSlider.getValue();
            }
        });

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SettingsManager.sfxVolume = sfxSlider.getValue();
            }
        });

        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false); // Ẩn cửa sổ đi
            }
        });

        // --- Sắp xếp layout bằng Table ---
        Label musicLabel = new Label("Music Volume", skin);
        musicLabel.setFontScale(3.5f); // CẬP NHẬT: Tăng cỡ chữ label
        
        Label sfxLabel = new Label("Sound Effects", skin);
        sfxLabel.setFontScale(3.5f); // CẬP NHẬT: Tăng cỡ chữ label

        Table contentTable = new Table();

        float horizontalPadding = 20f; // Khoảng cách từ viền trái và phải
        float verticalPadding = 55f;   // Khoảng cách giữa các hàng

        contentTable.add(musicLabel).left().padLeft(horizontalPadding).padRight(40f);
        contentTable.add(musicSlider).width(500).height(40).row(); // CẬP NHẬT: Tăng kích thước thanh trượt
        
        // CẬP NHẬT: Thêm khoảng cách giữa 2 hàng
        contentTable.add(sfxLabel).left().padTop(verticalPadding).padLeft(horizontalPadding).padRight(40f); 
        contentTable.add(sfxSlider).width(500).height(40).padTop(verticalPadding);

        this.add(contentTable).expand().fill();
        this.row();

        this.setSize(1400, 500);

        this.getTitleTable().add(closeButton).size(40, 40).padRight(40f).padTop(15f);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // Nếu cửa sổ được bật lên
        if (visible) {
            // Tự động căn giữa dựa trên Stage chứa nó
            Stage stage = getStage();
            if (stage != null) {
                setPosition(
                    (stage.getWidth() - getWidth()) / 2,
                    (stage.getHeight() - getHeight()) / 2
                );
            }
        }
    }
}