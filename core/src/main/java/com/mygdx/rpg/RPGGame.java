package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.List;

import com.mygdx.rpg.*;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RPGGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture(Gdx.files.internal("core/assets/libgdx.png"));
        System.out.println("Game started!");

        // Test tạo nhân vật
        PlayerCharacter player = new PlayerCharacter("Hero");
    
        // Tạo item tạm để test
        Item potion = new Item("Potion", "Heal", "Heals 20 HP");
        player.addItem(potion);
    
        // Tạo quái
        List<Item> drops = new ArrayList<>();
        drops.add(new Item("Gold Coin", "Misc", "Some gold"));
        Enemy slime = new Enemy("Slime", 1, 30, 10, 5, 2, 40, drops, 50);
    
        // Giao tranh
        player.attack(slime);
        slime.attackPlayer(player);

        // Diệt slime
        slime.takeDamage(999);
        slime.dropLoot(player);

        // Nếu không lỗi thì in ra hoàn tất
        System.out.println("Test thành công!");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
