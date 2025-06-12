package com.mygdx.rpg;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

//import com.mygdx.rpg.Character;


public class Item {
    private String name;
    private String type;     // e.g., "Potion", "Weapon", "Armor"
    private String effect;   // Mô tả hiệu ứng, ví dụ: "Heals 50 HP"
    private int quantity;
    private int maxStackSize; 
    private String texturePath; // Đường dẫn đến texture của item
    private Texture texture;    // Texture của item
    private TextureRegion textureRegion; // TextureRegion để vẽ item

    public Item(String name, String type, String effect, int initialQuantity, int maxStackSize, String texturePath) {
        this.name = name;
        this.type = type;
        this.effect = effect;
        this.maxStackSize = Math.max(1, maxStackSize);
        this.quantity = Math.max(1, Math.min(initialQuantity, this.maxStackSize));
        this.texturePath = texturePath;
        loadTexture();
    }

    private void loadTexture() {
        try {
            texture = new Texture(texturePath);
            textureRegion = new TextureRegion(texture);
        } catch (Exception e) {
            System.err.println("Error loading texture for item: " + name);
            e.printStackTrace();
        }
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

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    @Override
    public String toString() {
        return name + " (" + type + "): " + effect;
    }
}
