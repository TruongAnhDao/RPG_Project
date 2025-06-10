package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;


public class Inventory {
    private List<Item> items;

    public Inventory() {
        items = new ArrayList<>();
        addSampleItems();
    }

    private void addSampleItems() {
        items.add(new Item("Iron Helmet", "Armor", "A great helmet gives you massive defense stats", 1, 1, "helmet.png"));
        items.add(new Item("Iron Armor", "Armor", "A great armor gives you massive defense stats", 1, 1, "armor.png"));
        items.add(new Item("Black Shield", "Armor", "A great shield gives you massive defense stats", 1, 1, "shield.png"));
        items.add(new Item("Iron Sword", "Weapon", "A mighty sword with divine power", 1, 1, "sword.png"));
        items.add(new Item("Mana Potion", "Consumable", "Restore 30 MP", 3, 5, "mana_potion.png"));
        items.add(new Item("Heal Potion", "Consumable", "Heal 50 HP", 8, 10, "heal_potion.png"));
        items.add(new Item("Iron Pickaxe", "Weapon", "A mighty pickaxe gives you great attack stats", 1, 1, "pickaxe.png"));
        items.add(new Item("Epic Chest", "Chest", "A chest containing rare items", 1, 2, "epic_chest.png"));
        items.add(new Item("Wooden Chest", "Chest", "A chest containing normal items", 1, 2, "wooden_chest.png"));
    }


 // Hiển thị các item từ inventory trên vị trí được chỉ định
    public void render(SpriteBatch batch, int startX, int startY) {
        int slotSize = 128;
        for (int i = 0; i < items.size(); i++) {
            int row = i / 3;
            int col = i % 3;
            int x = startX + col * slotSize;
            int y = startY - row * slotSize;

            // Vẽ item với kích thước 96x96 vào giữa ô 128x128
            batch.draw(items.get(i).getTexture(), x + 16, y + 16, 96, 96);
        }
    }


    public java.util.List<Item> getItems() {
    	return items;	
    }
}
