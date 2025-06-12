package com.mygdx.rpg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;

public class PlayerCharacter extends Character {
    private List<Item> inventory;
    private int gold;
    private int currentMana;
    private int maxMana;
    private int experience;
    private int level;
    private int experienceToNextLevel;
    private Rectangle boundingBox; // Hộp va chạm của người chơi

    // --- Thêm các biến cho Animation ---
    public enum PlayerState { IDLE, WALKING, ATTACKING, DEAD }
    public enum Facing { LEFT, RIGHT }

    private PlayerState currentState;
    private Facing currentFacing;
    private float stateTime; // Thời gian đã trôi qua trong trạng thái hiện tại, dùng cho animation

    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> attackAnimation; 

    private TextureRegion currentFrame; // Khung hình hiện tại để vẽ

    // Kích thước của một frame (quan trọng cho việc vẽ và cắt sprite sheet)
    public static final int FRAME_WIDTH = 80;
    public static final int FRAME_HEIGHT = 80;

    private Texture runSheet;
    private Texture idleSheet;
    private Texture attackSheet;

    public PlayerCharacter(String name) {
        super(name, 1, 100, 10, 10, 80);

        configureStatsForLevel();

        this.currentMana = this.maxMana;
        this.speed = 180f;

        this.inventory = new ArrayList<>(); // Khởi tạo hành trang rỗng
        this.x = 700f;
        this.y = 530f;

        gold = 0;

        this.currentState = PlayerState.IDLE;
        this.currentFacing = Facing.RIGHT; // Mặc định quay phải
        this.stateTime = 0f;
        loadAnimations();
        // Đặt currentFrame ban đầu
        if (idleAnimation != null) {
            this.currentFrame = idleAnimation.getKeyFrame(0f);
            // Nếu mặc định là Facing.LEFT, thì flip ngay từ đầu
            // if (currentFacing == Facing.LEFT && currentFrame != null && !currentFrame.isFlipX()) {
            //    currentFrame.flip(true, false);
            // }
        }

        // Khởi tạo bounding box
        // Ví dụ: làm cho nó nhỏ hơn kích thước frame một chút và ở dưới chân nhân vật
        float boxWidth = FRAME_WIDTH * 0.5f;   // Rộng bằng 50% ảnh
        float boxHeight = FRAME_HEIGHT * 0.56f; 
        this.boundingBox = new Rectangle();
        this.boundingBox.width = boxWidth;
        this.boundingBox.height = boxHeight;
        // Vị trí của bounding box sẽ được cập nhật liên tục theo vị trí của player
        updateBoundingBox();
    }

    public void updateBoundingBox() {
        // Căn giữa bounding box theo chiều ngang với tâm của player
        // và đặt nó ở dưới chân player
        float boxX = x - boundingBox.width / 2f;
        float boxY = y - FRAME_HEIGHT / 2f + 10; // Đặt bounding box ở dưới cùng của sprite
        boundingBox.setPosition(boxX, boxY);
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public PlayerState getCurrentState() {
    return currentState;
    }

    private void loadAnimations() {
        float frameDuration = 0.15f;
        float attackFrameDuration = 0.1f; 
        int numberOfFrames = 4;

        try {
            // Tải các sprite sheet cho hướng mặc định (ví dụ: hướng phải)
            runSheet = new Texture(Gdx.files.internal("character/Wukong_anim_run.png")); 
            idleSheet = new Texture(Gdx.files.internal("character/Wukong_anim_idle.png"));   
            attackSheet = new Texture(Gdx.files.internal("character/Wukong_anim_attack.png"));
        } catch (Exception e) {
            Gdx.app.error("PlayerCharacter", "Error loading animation sheets", e);
            return;
        }

        // Tạo Animation Chạy 
        if (runSheet != null) {
            TextureRegion[][] tmpFramesRun = TextureRegion.split(runSheet, FRAME_WIDTH, FRAME_HEIGHT);
            Array<TextureRegion> runFrames = new Array<>(numberOfFrames);
            for (int i = 0; i < numberOfFrames; i++) {
                runFrames.add(tmpFramesRun[0][i]);
            }
            runAnimation = new Animation<>(frameDuration, runFrames, Animation.PlayMode.LOOP);
        }

        // Tạo Animation Đứng Yên 
        if (idleSheet != null) {
            TextureRegion[][] tmpFramesIdle = TextureRegion.split(idleSheet, FRAME_WIDTH, FRAME_HEIGHT);
            Array<TextureRegion> idleFrames = new Array<>(numberOfFrames);
            for (int i = 0; i < numberOfFrames; i++) {
                idleFrames.add(tmpFramesIdle[0][i]);
            }
            idleAnimation = new Animation<>(frameDuration, idleFrames, Animation.PlayMode.LOOP);
        }

        // Tạo Animation Tấn Công 
        if (attackSheet != null) {
            TextureRegion[][] tmpFramesAttack = TextureRegion.split(attackSheet, FRAME_WIDTH, FRAME_HEIGHT);
            Array<TextureRegion> attackFrames = new Array<>(numberOfFrames);
            for (int i = 0; i < numberOfFrames; i++) {
                attackFrames.add(tmpFramesAttack[0][i]);
            }
            // Animation.PlayMode.NORMAL nghĩa là animation sẽ không lặp lại
            attackAnimation = new Animation<>(attackFrameDuration, attackFrames, Animation.PlayMode.NORMAL);
        }
    }

    public void update(float delta, boolean movingUp, boolean movingDown, boolean movingLeft, boolean movingRight, boolean attackJustPressed) {
        PlayerState previousState = currentState;
        Facing previousFacing = currentFacing;

        // Xử lý input tấn công trước tiên
        if (attackJustPressed && currentState != PlayerState.ATTACKING) {
            currentState = PlayerState.ATTACKING;
            stateTime = 0f; // Reset thời gian animation
            hitTargets.clear(); // --- MỚI: Xóa danh sách mục tiêu cũ khi bắt đầu một đòn tấn công mới
        }
    
        // Nếu đang tấn công, kiểm tra xem animation đã kết thúc chưa
        if (currentState == PlayerState.ATTACKING) {
            stateTime += delta;

            // --- MỚI: Kích hoạt Hitbox tại một thời điểm cụ thể của animation ---
            if (stateTime >= 0.1f && stateTime <= 0.35f) {
                isAttackHitboxActive = true;
                float hitboxX = this.x;
                float hitboxY = this.y - 20; // Hơi thấp xuống một chút
                float hitboxWidth = 30;
                float hitboxHeight = 33;
            
                // Điều chỉnh vị trí hitbox dựa trên hướng mặt
                if (currentFacing == Facing.RIGHT) {
                    hitboxX += 10; // Dịch sang phải
                } else {
                    hitboxX -= (10 + hitboxWidth); // Dịch sang trái
                }
                attackHitbox.set(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
            } else {
                isAttackHitboxActive = false; // Vô hiệu hóa hitbox ngoài khoảng thời gian trên
            }
        
            if (attackAnimation != null && attackAnimation.isAnimationFinished(stateTime)) {
                currentState = PlayerState.IDLE;
                isAttackHitboxActive = false; // Đảm bảo hitbox tắt khi kết thúc
            }
        } else { 
            stateTime += delta; // Tăng stateTime cho idle/walk
            if (movingLeft) {
                currentState = PlayerState.WALKING;
                currentFacing = Facing.LEFT;
            } else if (movingRight) {
                currentState = PlayerState.WALKING;
                currentFacing = Facing.RIGHT;
            } else if (movingUp || movingDown) {
                currentState = PlayerState.WALKING;
                // currentFacing không đổi
            } else {
                currentState = PlayerState.IDLE;
            }
        }

        // Chọn animation gốc (luôn là animation hướng phải hoặc hướng mặc định)
        Animation<TextureRegion> baseAnimation = null;
        boolean loopAnimation = true;

        switch (currentState) {
            case IDLE:
                baseAnimation = idleAnimation;
                loopAnimation = true;
                break;
            case WALKING:
                baseAnimation = runAnimation;
                loopAnimation = true;
                break;
            case ATTACKING:
                baseAnimation = attackAnimation;
                loopAnimation = false; // Animation tấn công không lặp lại
                break;
        }

        if (baseAnimation != null) {
            currentFrame = baseAnimation.getKeyFrame(stateTime, loopAnimation);

            // Lật frame nếu cần (logic này vẫn giữ nguyên)
            if (currentFacing == Facing.LEFT) {
                if (currentFrame != null && !currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                }
            } else { // currentFacing == Facing.RIGHT
                if (currentFrame != null && currentFrame.isFlipX()) {
                    currentFrame.flip(true, false);
                }
            }
        } else if (currentFrame == null && idleAnimation != null) { // Fallback an toàn
             currentFrame = idleAnimation.getKeyFrame(0f);
             if (currentFacing == Facing.LEFT && currentFrame != null && !currentFrame.isFlipX()) {
                 currentFrame.flip(true, false);
             }
        }
    }

    public TextureRegion getCurrentFrame() {
        // Xử lý fallback nếu currentFrame vẫn là null
        if (currentFrame == null) {
            if (idleAnimation != null) {
                TextureRegion defaultFrame = idleAnimation.getKeyFrame(0f);
                if (currentFacing == Facing.LEFT && !defaultFrame.isFlipX()) {
                    defaultFrame.flip(true, false); // Cẩn thận: thay đổi frame trong animation
                } else if (currentFacing == Facing.RIGHT && defaultFrame.isFlipX()){
                    defaultFrame.flip(true, false);
                }
                return defaultFrame;
            }
            return null; // Hoặc một texture mặc định
        }
        return currentFrame;
    }

    public void dispose() {
        Gdx.app.log("PlayerCharacter", "Disposing player resources");
        if (runSheet != null) runSheet.dispose();
        if (idleSheet != null) idleSheet.dispose();
        if (attackSheet != null) attackSheet.dispose();
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

    public void setSpeed(float speed) {
        this.speed = speed;
    }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    public void setExperience(int experience) { this.experience = Math.max(0, experience); }
    public void setMaxHealth(int maxHealth) { this.maxhp = Math.max(1, maxHealth); }
    public void setMaxMana(int maxMana) { this.maxMana = Math.max(1, maxMana); }
    public void setCurrentMana(int mana) {
        this.currentMana = Math.max(0, Math.min(mana, maxMana));
    }
    public void setHealth(int Health) { this.hp = Math.max(1, Health); }

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
            boolean consumedOrUsed = false;
            String originalItemName = itemToUse.getName();
            
            if ("Consumable".equalsIgnoreCase(itemToUse.getType())) {
                if ("Heal Potion".equalsIgnoreCase(itemToUse.getName())) {
                    int healAmount = 50;
                    if (this.hp < this.maxhp) {
                        this.hp += healAmount;
                        if (this.hp > this.maxhp) {
                            this.hp = this.maxhp;
                        }
                        Gdx.app.log("PlayerCharacter", "Used Heal Potion. Healed " + healAmount + " HP. Current HP: " + this.hp);
                        consumedOrUsed = true;
                    } else {
                        Gdx.app.log("PlayerCharacter", "Health is full. Cannot use Heal Potion.");
                        return false;
                    }
                } else if ("Mana Potion".equalsIgnoreCase(itemToUse.getName())) {
                    int manaAmount = 30;
                    if (this.currentMana < this.maxMana) {
                        this.currentMana += manaAmount;
                        if (this.currentMana > this.maxMana) {
                            this.currentMana = this.maxMana;
                        }
                        Gdx.app.log("PlayerCharacter", "Used Mana Potion. Restored " + manaAmount + " MP. Current MP: " + this.currentMana);
                        consumedOrUsed = true;
                    } else {
                        Gdx.app.log("PlayerCharacter", "Mana is full. Cannot use Mana Potion.");
                        return false;
                    }
                }
            }

            if (consumedOrUsed) {
                // Chỉ giảm số lượng đi 1
                itemToUse.removeQuantity(1);
                Gdx.app.log("PlayerCharacter", "Used one " + originalItemName + ". Remaining: " + itemToUse.getQuantity());

                // Nếu số lượng về 0 thì xóa item khỏi inventory
                if (itemToUse.getQuantity() <= 0) {
                    Iterator<Item> iter = inventory.iterator();
                    while (iter.hasNext()) {
                        Item currentItem = iter.next();
                        if (currentItem == itemToUse) {
                            iter.remove();
                            Gdx.app.log("PlayerCharacter", "Stack of " + originalItemName + " depleted and removed from inventory.");
                            break;
                        }
                    }
                }
                return true;
            }
        }
        Gdx.app.log("PlayerCharacter", "Item " + itemToUse.getName() + " not found in inventory.");
        return false;
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

    // Chức năng khác ( sửa sau )
    public void addGold(int amount) {
        gold += amount;
        System.out.println(name + " received " + amount + " gold. Total gold: " + gold);
    }

    public void interact(String object) {
        System.out.println(name + " interacts with " + object);
    }
}
