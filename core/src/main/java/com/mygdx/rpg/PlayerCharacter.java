package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.List;
//import com.mygdx.rpg.Character;

import com.badlogic.gdx.Gdx;

public class PlayerCharacter extends Character {
    private List<Item> inventory;
    private int exp;
    private int gold;
    public float x, y; // các biến vị trí

    public PlayerCharacter(String name) {
        super(name, 1, 100, 50, 10, 5, 5);
        this.inventory = new ArrayList<>(); // Khởi tạo hành trang rỗng
        exp = 0;
        gold = 0;
        this.x = Gdx.graphics.getWidth() / 2f ; // Vị trí ban đầu ví dụ
        this.y = Gdx.graphics.getHeight() / 2f ;
    }

    public void addItem(Item item) {
        this.inventory.add(item);
        Gdx.app.log("PlayerCharacter", "Added item: " + item.getName());
    }

    public void equipItem(Item item) {
        System.out.println(name + " equipped " + item.getName());
        // Tùy loại item, có thể tăng thuộc tính
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Chức năng khác ( sửa sau )
    public void addGold(int amount) {
        gold += amount;
        System.out.println(name + " received " + amount + " gold. Total gold: " + gold);
    }

    public void gainExp(int amount) {
        exp += amount;
        System.out.println(name + " gained " + amount + " EXP. Total: " + exp);
        if (exp >= level * 100) {
            exp -= level * 100;
            levelUp();
        }
    }

    public void interact(String object) {
        System.out.println(name + " interacts with " + object);
    }
}
