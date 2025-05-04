package com.mygdx.rpg;

//import com.mygdx.rpg.Character;


public class Item {
    private String name;
    private String type;     // e.g., "Potion", "Weapon", "Armor"
    private String effect;   // Mô tả hiệu ứng, ví dụ: "Heals 50 HP"

    public Item(String name, String type, String effect) {
        this.name = name;
        this.type = type;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getEffect() {
        return effect;
    }

    public void use(Character target) {
        // Tùy loại item sẽ xử lý khác nhau. Tạm thời in ra để test.
        System.out.println(name + " is used on " + target.getName() + ". Effect: " + effect);
    }

    @Override
    public String toString() {
        return name + " (" + type + "): " + effect;
    }
}
