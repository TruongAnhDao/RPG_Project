package com.mygdx.rpg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */

public class RPGGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        System.out.println("Game started!");

        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // Khi sử dụng Game và Screen, phương thức render() của Game
        // sẽ tự động gọi render() của Screen hiện tại.
        // Vì vậy, chúng ta cần gọi super.render().
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        
         if (screen != null) {
            screen.dispose();
        }
        System.out.println("Game instance disposed!");
    }
}
