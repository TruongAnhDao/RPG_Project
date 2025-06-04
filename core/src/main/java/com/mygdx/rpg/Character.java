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
    protected float x;
    protected float y;

    public Character(String name, int level, int maxhp, int attack, int defense, float speed, float initialX, float initialY) {
        this.name = name;
        this.level = level;
        this.hp = this.maxhp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.x = initialX;
        this.y = initialY;
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
    public float getX() { return x; }
    public float getY() { return y; }

    // Setters
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }   

    // Phương thức di chuyển (sẽ được enemy sử dụng để cập nhật x, y)
    // Phương thức move() hiện tại của bạn chỉ in ra, không thực sự thay đổi x,y.
    // Logic di chuyển thực tế sẽ nằm trong Enemy (moveToPoint) hoặc PlayerCharacter.
    // Có thể để trống hoặc làm abstract nếu muốn các lớp con tự định nghĩa.
    public void move(float deltaX, float deltaY) {
        // Ví dụ cơ bản, hoặc để lớp con tự xử lý chi tiết hơn
        this.x += deltaX;
        this.y += deltaY;
        // System.out.println(name + " moved to (" + x + ", " + y + ")");
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
