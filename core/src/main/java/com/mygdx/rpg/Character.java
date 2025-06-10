package com.mygdx.rpg;

//import com.mygdx.rpg.Item;

public class Character {
    protected String name;
    protected int level;
    protected int hp;
    protected int attack;
    protected int defense;
    protected float speed;
    protected int maxhp;
    protected float x, y;

    public Character(String name, int level, int maxhp, int attack, int defense, float speed) {
        this.name = name;
        this.level = level;
        this.hp = this.maxhp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.x = 0; 
        this.y = 0;
    }

    public String getName() {
        return name;
    }

    public void move() {
        System.out.println(name + " is moving...");
    }

    public int getCurrentHealth() {
        return hp;
    }

    public int getMaxHealth() {
        return maxhp;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void attack(Character target) {
        int damage = Math.max(0, this.attack - target.defense);
        target.takeDamage(damage);
        System.out.println(name + " attacks " + target.name + " for " + damage + " damage!");
    }

    public void takeDamage(int damage) {
        hp -= damage;
        //if (hp < 0) hp = 0;
        System.out.println(name + " takes " + damage + " damage. Remaining HP: " + hp);
    }

    public void setCurrentHealth(int health) { // Cần thiết nếu muốn reset máu
        this.hp = Math.max(0, Math.min(health, maxhp));
    }
    // Getter & Setter có thể thêm nếu cần
}
