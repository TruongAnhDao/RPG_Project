package com.mygdx.rpg;

//import com.mygdx.rpg.Character;


public class Item {
    private String name;
    private String type;     // e.g., "Potion", "Weapon", "Armor"
    private String effect;   // Mô tả hiệu ứng, ví dụ: "Heals 50 HP"
    private int quantity;
    private int maxStackSize; 

    public Item(String name, String type, String effect, int initialQuantity, int maxStackSize) {
        this.name = name;
        this.type = type;
        this.effect = effect;
        this.maxStackSize = Math.max(1, maxStackSize); // Đảm bảo maxStackSize ít nhất là 1
        this.quantity = Math.max(1, Math.min(initialQuantity, this.maxStackSize)); // Đảm bảo quantity hợp lệ
    }

    // Constructor cũ hơn cho item không stack hoặc mặc định quantity = 1, maxStack = 1
    public Item(String name, String type, String description) {
        this(name, type, description, 1, 1); // Mặc định không stack, số lượng là 1
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
    public int getQuantity() { return quantity; }
    public int getMaxStackSize() { return maxStackSize; }

    // Setters and Modifiers
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, this.maxStackSize)); // Đảm bảo số lượng không âm và không vượt maxStack
    }

    public void addQuantity(int amount) {
        if (amount <= 0) return;
        this.quantity += amount;
        if (this.quantity > this.maxStackSize) {
            // Xử lý trường hợp vượt quá maxStack (ví dụ: tạo item mới hoặc giới hạn)
            // Hiện tại, chúng ta sẽ chỉ giới hạn ở maxStackSize trong một stack cụ thể này
            // Logic cộng dồn khi nhặt item sẽ xử lý việc tạo stack mới nếu cần
            this.quantity = this.maxStackSize;
        }
    }

    public boolean removeQuantity(int amount) {
        if (amount <= 0) return false;
        if (this.quantity >= amount) {
            this.quantity -= amount;
            return true; // Xóa thành công
        }
        return false; // Không đủ số lượng để xóa
    }

    public boolean isStackable() {
        return this.maxStackSize > 1;
    }

    // Quan trọng: Để xác định item có "giống nhau" để cộng dồn không
    // Chúng ta có thể dựa vào name và type (hoặc một ID duy nhất nếu có)
    public boolean isSameType(Item otherItem) {
        if (otherItem == null) return false;
        return this.name.equals(otherItem.name) && this.type.equals(otherItem.type);
        // Nếu có ID, nên so sánh ID: return this.id == otherItem.id;
    }

    @Override
    public String toString() {
        return name + " (" + type + "): " + effect;
    }
}
