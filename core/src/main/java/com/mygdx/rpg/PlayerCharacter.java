package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.Gdx;

public class PlayerCharacter extends Character {
    private List<Item> inventory;
    private int gold;
    public float x, y; // các biến vị trí
    private int currentMana;
    private int maxMana;
    private int experience;
    private int level;
    private int experienceToNextLevel;

    public PlayerCharacter(String name) {
        super(name, 1, 100, 10, 5, 5);

        configureStatsForLevel();

        this.currentMana = this.maxMana;

        this.inventory = new ArrayList<>(); // Khởi tạo hành trang rỗng
        this.x = Gdx.graphics.getWidth() / 2f ; // Vị trí ban đầu ví dụ
        this.y = Gdx.graphics.getHeight() / 2f ;

        gold = 0;
    }

    private void configureStatsForLevel() {
        // Ví dụ: công thức tính chỉ số dựa trên level
        this.maxhp = 80 + this.level * 20; // Ví dụ: 100 ở level 1
        this.maxMana = 40 + this.level * 10;   // Ví dụ: 50 ở level 1
        this.experience = 0;
        // Ví dụ: Lượng XP cần để lên cấp tiếp theo
        this.experienceToNextLevel = calculateXpForNextLevel(this.level);
    }

    private int calculateXpForNextLevel(int currentLevel) {
        return 75 + currentLevel * 25; // Ví dụ: 100 XP cho level 1 lên 2, 125 XP cho level 2 lên 3
    }

    // --- Getters/setters cho các chỉ số mới ---
    public int getCurrentMana() { return currentMana; }
    public int getMaxMana() { return maxMana; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public int getExperienceToNextLevel() { return experienceToNextLevel; }

    public void setLevel(int level) { this.level = Math.max(1, level); }
    public void setExperience(int experience) { this.experience = Math.max(0, experience); }
    public void setMaxHealth(int maxHealth) { this.maxhp = Math.max(1, maxHealth); }
    public void setMaxMana(int maxMana) { this.maxMana = Math.max(1, maxMana); }
    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, Math.min(mana, maxMana));
    }

    public void recalculateDependentStats() {
        // Gọi lại hàm cấu hình chỉ số dựa trên level hiện tại
        // Điều này sẽ cập nhật experienceToNextLevel và có thể cả maxHealth/maxMana
        // nếu bạn muốn chúng chỉ được tính từ level.
        // Hoặc, nếu bạn lưu maxHealth/maxMana, thì chỉ cần tính experienceToNextLevel.
        this.experienceToNextLevel = calculateXpForNextLevel(this.level);
        // Đảm bảo currentHealth/Mana không vượt max mới (nếu max thay đổi)
        if (this.hp > this.maxhp) this.hp = this.maxhp;
        if (this.currentMana > this.maxMana) this.currentMana = this.maxMana;
    }

    // --- Phương thức thay đổi chỉ số ---
    public void spendMana(int amount) {
        if (amount > 0) {
            this.currentMana -= amount;
            if (this.currentMana < 0) this.currentMana = 0;
            Gdx.app.log("PlayerCharacter", name + " spent " + amount + " mana. Current Mana: " + currentMana);
        }
    }

    public void restoreMana(int amount) {
        if (amount > 0) {
            this.currentMana += amount;
            if (this.currentMana > this.maxMana) this.currentMana = this.maxMana;
            Gdx.app.log("PlayerCharacter", name + " restored " + amount + " mana. Current Mana: " + currentMana);
        }
    }

    public void addExperience(int amount) {
        if (amount <= 0) return;

        this.experience += amount;
        Gdx.app.log("PlayerCharacter", name + " gained " + amount + " XP. Total XP: " + experience + "/" + experienceToNextLevel);

        while (this.experience >= this.experienceToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.experience -= this.experienceToNextLevel; // Giữ lại XP dư
        Gdx.app.log("PlayerCharacter", name + " leveled up to Level " + this.level + "!");

        // Cập nhật lại chỉ số cho cấp mới
        int oldMaxHealth = this.maxhp;
        int oldMaxMana = this.maxMana;

        configureStatsForLevel(); // Tính lại maxHealth, maxMana, experienceToNextLevel

        // Hồi một phần hoặc toàn bộ HP/Mana khi lên cấp (tùy thiết kế)
        this.hp += (this.maxhp - oldMaxHealth); // Hồi lượng máu tăng thêm
        if (this.hp > this.maxhp) this.hp = this.maxhp;

        this.currentMana += (this.maxMana - oldMaxMana); // Hồi lượng mana tăng thêm
        if (this.currentMana > this.maxMana) this.currentMana = this.maxMana;

        Gdx.app.log("PlayerCharacter", "Stats updated. HP: " + hp + "/" + maxhp + ", Mana: " + currentMana + "/" + maxMana);
    }

    public void addItem(Item newItem) {
        if (newItem == null || newItem.getQuantity() <= 0) return;

        if (newItem.isStackable()) {
            boolean itemStacked = false;
            // Tìm stack hiện có của item cùng loại và còn chỗ
            for (Item existingItem : inventory) {
                if (existingItem.isSameType(newItem) && existingItem.getQuantity() < existingItem.getMaxStackSize()) {
                    int canAdd = existingItem.getMaxStackSize() - existingItem.getQuantity();
                    int amountToAdd = Math.min(canAdd, newItem.getQuantity());

                    existingItem.addQuantity(amountToAdd); // addQuantity đã có giới hạn bởi maxStackSize của existingItem
                    newItem.removeQuantity(amountToAdd); // Giảm số lượng của newItem

                    Gdx.app.log("PlayerCharacter", "Stacked " + amountToAdd + " of " + newItem.getName() + " to existing stack. Remaining in new item: " + newItem.getQuantity());
                    itemStacked = true;

                    if (newItem.getQuantity() <= 0) {
                    break; // Đã cộng dồn hết newItem
                    }
                }
            }   

            // Nếu newItem vẫn còn số lượng (chưa stack hết hoặc không tìm thấy stack phù hợp)
            // thì thêm nó như một stack mới (nếu còn chỗ trong inventory)
            if (newItem.getQuantity() > 0) {
                // (Tùy chọn: Kiểm tra giới hạn số ô trong inventory ở đây nếu có)
                this.inventory.add(newItem); // Thêm phần còn lại (hoặc toàn bộ nếu không stack được) như một item mới
                Gdx.app.log("PlayerCharacter", "Added new stack of " + newItem.getName() + " (x" + newItem.getQuantity() + ")");
            }
        } else {
            // Item không stackable, thêm như bình thường (mỗi item là một ô)
            // (Tùy chọn: Kiểm tra giới hạn số ô trong inventory)
            this.inventory.add(newItem);
            Gdx.app.log("PlayerCharacter", "Added non-stackable item: " + newItem.getName());
        }
    }

    public boolean useItem(Item itemToUse) {
        if (inventory.contains(itemToUse)) {
            boolean consumedOrUsed = false; // Đổi tên biến cho rõ ràng hơn
            String originalItemName = itemToUse.getName(); // Lưu tên gốc phòng trường hợp item bị xóa
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
                        consumedOrUsed = true;
                    } else {
                        Gdx.app.log("PlayerCharacter", "Health is full. Cannot use Potion.");
                        return false; // Không sử dụng nếu máu đầy
                    }
                } else if ("Mana Potion".equalsIgnoreCase(itemToUse.getName())) {
                    int manaRestoreAmount = 30; // Ví dụ
                    if (this.currentMana < this.maxMana) {
                        restoreMana(manaRestoreAmount); // Gọi hàm restoreMana bạn đã tạo
                        Gdx.app.log("PlayerCharacter", "Used Mana Potion. Restored " + manaRestoreAmount + " MP. Current MP: " + this.currentMana);
                        consumedOrUsed = true;
                    } else {
                        Gdx.app.log("PlayerCharacter", "Mana is full. Cannot use Mana Potion.");
                        return false; // Không sử dụng nếu mana đầy
                    }
                }
                // Thêm các loại "Consumable" khác ở đây
            } else {
                Gdx.app.log("PlayerCharacter", itemToUse.getName() + " is not a consumable item.");
                return false; // Không phải là vật phẩm có thể tiêu thụ trực tiếp qua hành động này
            }

            if (consumedOrUsed) {
                itemToUse.removeQuantity(1); // Giảm số lượng đi 1
                Gdx.app.log("PlayerCharacter", "Used one " + originalItemName + ". Remaining: " + itemToUse.getQuantity());

                if (itemToUse.getQuantity() <= 0) {
                    // Nếu số lượng về 0, xóa item khỏi inventory
                    Iterator<Item> iter = inventory.iterator();
                    while (iter.hasNext()) {
                        Item currentItem = iter.next();
                        if (currentItem == itemToUse) { // Hoặc equals() nếu bạn đã override
                            iter.remove();
                            Gdx.app.log("PlayerCharacter", "Stack of " + originalItemName + " depleted and removed from inventory.");
                            break;
                        }
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

        // itemToDrop.removeQuantity(1);
        // if (itemToDrop.getQuantity() <= 0) { /* xóa cả stack khỏi inventory */ }

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

    public void interact(String object) {
        System.out.println(name + " interacts with " + object);
    }
}
