package com.mygdx.rpg;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Character {
    protected String name;
    protected int level;
    protected int hp;
    protected int attack;
    protected int defense;
    protected float speed;
    protected int maxhp;
    protected float x, y;
    // --- MỚI: Các biến cho Hitbox Tấn công ---
    protected Rectangle attackHitbox;
    protected boolean isAttackHitboxActive;
    // Danh sách để theo dõi các mục tiêu đã bị đánh trúng trong 1 lần tấn công
    protected Array<Character> hitTargets;

    public Character(String name, int level, int maxhp, int attack, int defense, float speed) {
        this.name = name;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.x = 0; 
        this.y = 0;
        this.maxhp = maxhp;
        this.hp = maxhp;
        this.attackHitbox = new Rectangle();
        this.isAttackHitboxActive = false;
        this.hitTargets = new Array<>();
    }

    public boolean isDead() {
        return this.hp <= 0;
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

    public float getSpeed(){
        return speed;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // --- MỚI: Thêm các getter cần thiết ---
    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    public boolean isAttackHitboxActive() {
        return isAttackHitboxActive;
    }
    
    public Array<Character> getHitTargets() {
        return hitTargets;
    }

    public void attack(Character target) {
        if (hitTargets.contains(target, true)) {
            return; // Đã đánh trúng mục tiêu này rồi, không tấn công nữa
        }

        int damage = Math.max(0, this.attack - target.defense);
        target.takeDamage(damage);
        System.out.println(name + " attacks " + target.name + " for " + damage + " damage!");
        hitTargets.add(target); // Thêm mục tiêu vào danh sách đã bị đánh trúng
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
        System.out.println(name + " takes " + damage + " damage. Remaining HP: " + hp);
    }

    public void setCurrentHealth(int health) { // Cần thiết nếu muốn reset máu
        this.hp = Math.max(0, Math.min(health, maxhp));
    }
    // Getter & Setter có thể thêm nếu cần
}
