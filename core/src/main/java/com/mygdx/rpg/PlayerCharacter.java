package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.List;
//import com.mygdx.rpg.Character;

public class PlayerCharacter extends Character {
    private List<Item> inventory;
    private int exp;
    private int gold;

    public PlayerCharacter(String name) {
        super(name, 1, 100, 50, 10, 5, 5);
        inventory = new ArrayList<>();
        exp = 0;
        gold = 0;
    }

    public void gainExp(int amount) {
        exp += amount;
        System.out.println(name + " gained " + amount + " EXP. Total: " + exp);
        if (exp >= level * 100) {
            exp -= level * 100;
            levelUp();
        }
    }

    public void equipItem(Item item) {
        System.out.println(name + " equipped " + item.getName());
        // Tùy loại item, có thể tăng thuộc tính
    }

    public void interact(String object) {
        System.out.println(name + " interacts with " + object);
    }

    public void addItem(Item item) {
        inventory.add(item);
        System.out.println(item.getName() + " was added to inventory.");
    }

    public void addGold(int amount) {
        gold += amount;
        System.out.println(name + " received " + amount + " gold. Total gold: " + gold);
    }
}
