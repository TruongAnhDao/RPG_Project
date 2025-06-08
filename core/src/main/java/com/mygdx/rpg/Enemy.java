package com.mygdx.rpg;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Character {
    private List<Item> dropItems;
    private int expReward;
    private Texture texture;      // sprite của enemy
    private Vector2 position;     // vị trí tĩnh

    public Enemy(String name, int level, int hp, int attack, int defense, int speed, List<Item> dropItems, int expReward, float x, float y) {
        super(name, level, hp, attack, defense, speed);
        this.dropItems = dropItems;
        this.expReward = expReward;
        texture = new Texture(Gdx.files.internal("PlayScreen/enemy.png"));
        position = new Vector2(x, y);
    }

    public void attackPlayer(PlayerCharacter player) {
        attack(player);
    }

    public void dropLoot(PlayerCharacter player) {
        System.out.println(name + " dropped loot!");
        for (Item item : dropItems) {
            player.addItem(item);
        }
        player.addExperience(expReward);
    }

        /** Vẽ enemy lên màn hình (static). */
    public void render(SpriteBatch batch) {
        Gdx.app.log("Enemy", "Render at " + position.x + ", " + position.y);
        batch.draw(texture, position.x, position.y);
    }

    /** Giải phóng texture khi không dùng nữa. */
    public void dispose() {
        texture.dispose();
    }


}
