package com.mygdx.rpg;

import java.util.List;

public class Enemy extends Character {
    private List<Item> dropItems;
    private int expReward;

    public Enemy(String name, int level, int hp, int mp, int attack, int defense, int speed, List<Item> dropItems, int expReward) {
        super(name, level, hp, mp, attack, defense, speed);
        this.dropItems = dropItems;
        this.expReward = expReward;
    }

    public void attackPlayer(PlayerCharacter player) {
        attack(player);
    }

    public void dropLoot(PlayerCharacter player) {
        System.out.println(name + " dropped loot!");
        for (Item item : dropItems) {
            player.addItem(item);
        }
        player.gainExp(expReward);
    }
}
