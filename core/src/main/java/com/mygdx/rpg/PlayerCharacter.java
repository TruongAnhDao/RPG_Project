package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public boolean useItem(Item itemToUse) {
        if (inventory.contains(itemToUse)) {
            boolean consumed = false;
            // Xử lý hiệu ứng dựa trên loại và tên vật phẩm
            if ("Consumable".equalsIgnoreCase(itemToUse.getType())) {
                if ("Potion".equalsIgnoreCase(itemToUse.getName())) {
                    int healAmount = 50; // Ví dụ: Potion hồi 50 HP
                    if (this.hp < this.maxhp) {
                        this.hp += healAmount;
                        if (this.hp > this.maxhp) {
                            this.hp = this.maxhp;
                        }
                        Gdx.app.log("PlayerCharacter", "Used Potion. Healed " + healAmount + " HP. Current HP: " + this.hp);
                        consumed = true;
                    } else {
                        Gdx.app.log("PlayerCharacter", "Health is full. Cannot use Potion.");
                        return false; // Không sử dụng nếu máu đầy
                    }
                } else if ("Mana Potion".equalsIgnoreCase(itemToUse.getName())) {
                    // Tương tự, bạn có thể thêm logic cho Mana Potion
                    Gdx.app.log("PlayerCharacter", "Used Mana Potion (logic not implemented yet).");
                    consumed = true; // Giả sử tiêu thụ
                }
                // Thêm các loại "Consumable" khác ở đây
            } else {
                Gdx.app.log("PlayerCharacter", itemToUse.getName() + " is not a consumable item.");
                return false; // Không phải là vật phẩm có thể tiêu thụ trực tiếp qua hành động này
            }

            if (consumed) {
                // Xóa vật phẩm khỏi hành trang một cách an toàn
                // Sử dụng Iterator để tránh ConcurrentModificationException
                Iterator<Item> iter = inventory.iterator();
                while (iter.hasNext()) {
                    Item currentItem = iter.next();
                    // So sánh đối tượng trực tiếp hoặc dựa trên ID duy nhất nếu có
                    if (currentItem == itemToUse) { // Hoặc currentItem.equals(itemToUse) nếu bạn override equals()
                        iter.remove();
                        Gdx.app.log("PlayerCharacter", "Removed " + itemToUse.getName() + " from inventory.");
                        break; // Giả sử mỗi lần chỉ dùng 1 item cùng loại
                    }
                }
                return true; // Vật phẩm đã được sử dụng và tiêu thụ
            }
        }
        Gdx.app.log("PlayerCharacter", "Item " + itemToUse.getName() + " not found in inventory.");
        return false; // Vật phẩm không có trong hành trang hoặc không được tiêu thụ
    }

    public boolean dropItem(Item itemToDrop) {
        if (itemToDrop == null || !inventory.contains(itemToDrop)) {
            Gdx.app.log("PlayerCharacter", "Attempted to drop null or non-existent item.");
            return false;
        }

        // Sử dụng Iterator để xóa an toàn
        Iterator<Item> iter = inventory.iterator();
        while (iter.hasNext()) {
            Item currentItem = iter.next();
            if (currentItem == itemToDrop) { // So sánh đối tượng trực tiếp
                iter.remove();
                Gdx.app.log("PlayerCharacter", "Dropped item: " + itemToDrop.getName());
                // TODO (Tùy chọn): Tạo một đối tượng ItemEntity trong thế giới game tại vị trí người chơi
                return true;
            }
        }
        // Dòng này không nên đạt được nếu itemToDrop có trong inventory.contains(itemToDrop) là true
        return false;
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
