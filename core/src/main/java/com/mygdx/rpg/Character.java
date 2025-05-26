package com.mygdx.rpg;

//import com.mygdx.rpg.Item;

public class Character {
    protected String name;
    protected int level;
    protected int hp;
    protected int mp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected int maxhp;

    public Character(String name, int level, int hp, int mp, int attack, int defense, int speed) {
        this.name = name;
        this.level = level;
        this.hp = this.maxhp;
        this.mp = mp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.maxhp = 100;
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

    public void attack(Character target) {
        int damage = Math.max(0, this.attack - target.defense);
        target.takeDamage(damage);
        System.out.println(name + " attacks " + target.name + " for " + damage + " damage!");
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
        System.out.println(name + " takes " + damage + " damage. Remaining HP: " + hp);
    }

    public void setCurrentHealth(int health) { // Cần thiết nếu muốn reset máu
        this.hp = Math.max(0, Math.min(health, maxhp));
    }

    public void useItem(Item item) {
        item.use(this);
    }

    public void levelUp() {
        level++;
        hp += 10;
        mp += 5;
        attack += 2;
        defense += 2;
        speed += 1;
        System.out.println(name + " leveled up to " + level + "!");
    }

    // Getter & Setter có thể thêm nếu cần
}
