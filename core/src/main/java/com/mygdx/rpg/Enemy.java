package com.mygdx.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Rectangle;
import java.util.List;

public class Enemy extends Character {
    private List<Item> dropItems;
    private int expReward;

    private Rectangle boundingBox;

    // --- CÁC BIẾN ANIMATION ---
    public enum EnemyState { IDLE, RUN, ATTACKING, DEAD }
    private EnemyState currentState;
    private float stateTime;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> attackAnimation;

    private transient Texture idleSheet; // Dùng `transient` để Gson (nếu có) bỏ qua
    private transient Texture runSheet;
    private transient Texture attackSheet;
    private transient TextureRegion currentFrame;

    private static final float SIGHT_RANGE = 180f; // Khoảng cách nhìn thấy người chơi
    private static final float ATTACK_RANGE = 50f; // Khoảng cách để tấn công

    public static final int FRAME_WIDTH = 80; // Chiều rộng frame của enemy
    public static final int FRAME_HEIGHT = 80; // Chiều cao frame của enemy

    public Enemy(String name, int level, int hp, int attack, int defense, int speed, List<Item> dropItems, int expReward) {
        super(name, level, hp, attack, defense, speed);
        this.dropItems = dropItems;
        this.expReward = expReward;

        // --- KHỞI TẠO TRẠNG THÁI BAN ĐẦU ---
        this.currentState = EnemyState.IDLE;
        this.stateTime = 0f;
        loadAnimations();

        // --- Khởi tạo bounding box ---
        this.boundingBox = new Rectangle();
        float boxWidth = FRAME_WIDTH * 0.5f;
        float boxHeight = FRAME_HEIGHT * 0.56f;
        this.boundingBox.width = boxWidth;
        this.boundingBox.height = boxHeight;
        updateBoundingBox(); // Cập nhật vị trí ban đầu
    }

    public void updateBoundingBox() {
        float boxX = x - boundingBox.width / 2f;
        float boxY = y - FRAME_HEIGHT / 2f + 10; // Đặt bounding box ở dưới chân enemy
        boundingBox.setPosition(boxX, boxY);
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    private void loadAnimations() {
        try {
            // Idle Animation
            idleSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_idle.png"));
            TextureRegion[][] tmpFramesIdle = TextureRegion.split(idleSheet, FRAME_WIDTH, FRAME_HEIGHT);
            idleAnimation = new Animation<>(0.25f, new Array<>(tmpFramesIdle[0]), Animation.PlayMode.LOOP);

            runSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_run.png"));
            TextureRegion[][] tmpFramesWalk = TextureRegion.split(runSheet, FRAME_WIDTH, FRAME_HEIGHT);
            runAnimation = new Animation<>(0.15f, new Array<>(tmpFramesWalk[0]), Animation.PlayMode.LOOP);

            attackSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_attack.png"));
            TextureRegion[][] tmpFramesAttack = TextureRegion.split(attackSheet, FRAME_WIDTH, FRAME_HEIGHT);
            attackAnimation = new Animation<>(0.35f, new Array<>(tmpFramesAttack[0]), Animation.PlayMode.NORMAL); // NORMAL để không lặp lại

        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading enemy animations", e);
        }
    }

    public void update(float delta, PlayerCharacter player) {
        stateTime += delta;

        // --- LOGIC AI ĐỂ THAY ĐỔI TRẠNG THÁI ---
        float distanceToPlayer = Vector2.dst(this.x, this.y, player.getX(), player.getY());

        // Ưu tiên trạng thái tấn công
        if (currentState == EnemyState.ATTACKING) {
            // --- MỚI: Kích hoạt hitbox cho Enemy ---
            if (stateTime >= 0.4f && stateTime <= 1f) { // Căn chỉnh thời gian cho phù hợp với animation của enemy
                isAttackHitboxActive = true;
                float hitboxX = this.x;
                float hitboxY = this.y - 24;
                float hitboxWidth = 30;
                float hitboxHeight = 60;

                if (player.getX() > this.x) { // Nếu player ở bên phải
                    hitboxX += 10;
                } else { // Nếu player ở bên trái
                    hitboxX -= (10 + hitboxWidth);
                }
                attackHitbox.set(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
            } else {
                isAttackHitboxActive = false;
            }

            if (attackAnimation.isAnimationFinished(stateTime)) {
                currentState = EnemyState.IDLE;
                isAttackHitboxActive = false;
            }
        } else {
            isAttackHitboxActive = false; // Đảm bảo hitbox luôn tắt khi không tấn công
            if (distanceToPlayer <= ATTACK_RANGE) {
                currentState = EnemyState.ATTACKING;
                stateTime = 0;
                hitTargets.clear(); // --- MỚI: Xóa danh sách mục tiêu cũ
            } else if (distanceToPlayer <= SIGHT_RANGE) {
                // Nếu thấy người chơi nhưng chưa đủ gần, đi bộ về phía họ
                currentState = EnemyState.RUN;
            } else {
                // Nếu không thấy người chơi, đứng yên
                currentState = EnemyState.IDLE;
            }
        }
        
        // --- CHỌN KHUNG HÌNH DỰA TRÊN TRẠNG THÁI ---
        switch (currentState) {
            case RUN:
                if (runAnimation != null) {
                    currentFrame = runAnimation.getKeyFrame(stateTime, true);
                }
                break;
            case ATTACKING:
                if (attackAnimation != null) {
                    currentFrame = attackAnimation.getKeyFrame(stateTime, false);
                }
                break;
            case IDLE:
            default:
                if (idleAnimation != null) {
                    currentFrame = idleAnimation.getKeyFrame(stateTime, true);
                }
                break;
        }
        
        // (Tùy chọn) Logic lật hình ảnh của Enemy để đối mặt với người chơi
        if (player.getX() < this.x && currentFrame != null && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (player.getX() > this.x && currentFrame != null && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    public void attackPlayer(PlayerCharacter player) {
        attack(player);
    }

    public void dropLoot(PlayerCharacter player) {
        System.out.println(name + " dropped loot!");
        for (Item item : dropItems) {
            player.addItem(item);
        }
        player.addExperience(expReward);
    }

    public void dispose() {
        if (idleSheet != null) idleSheet.dispose();
        if (runSheet != null) runSheet.dispose();     
        if (attackSheet != null) attackSheet.dispose();
    }

    public EnemyState getCurrentState(){
        return currentState;
    }


}
