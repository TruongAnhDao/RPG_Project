package com.mygdx.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.List;

public class Enemy extends Character {
    private List<Item> dropItems;
    private int expReward;

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

    private static final float SIGHT_RANGE = 300f; // Khoảng cách nhìn thấy người chơi
    private static final float ATTACK_RANGE = 60f; // Khoảng cách để tấn công

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
            attackAnimation = new Animation<>(0.1f, new Array<>(tmpFramesAttack[0]), Animation.PlayMode.NORMAL); // NORMAL để không lặp lại

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
            // Nếu animation tấn công đã kết thúc, quay về trạng thái IDLE
            if (attackAnimation.isAnimationFinished(stateTime)) {
                currentState = EnemyState.IDLE;
                stateTime = 0; // Reset stateTime để animation mới bắt đầu từ đầu
            }
        } else { // Nếu không đang tấn công, quyết định hành động tiếp theo
            if (distanceToPlayer <= ATTACK_RANGE) {
                // Nếu đủ gần, chuyển sang tấn công
                currentState = EnemyState.ATTACKING;
                stateTime = 0; // Reset stateTime cho animation tấn công
            } else if (distanceToPlayer <= SIGHT_RANGE) {
                // Nếu thấy người chơi nhưng chưa đủ gần, đi bộ về phía họ
                currentState = EnemyState.RUN;
                // Di chuyển enemy về phía player
                float angle = (float) Math.atan2(player.getY() - this.y, player.getX() - this.x);
                this.x += (float) Math.cos(angle) * this.speed * delta;
                this.y += (float) Math.sin(angle) * this.speed * delta;
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
}
