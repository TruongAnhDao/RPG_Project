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

    public enum EnemyState { IDLE, CHASING, ATTACKING, DEAD, PATROLLING, RETURNING }
    private EnemyState currentState;
    private float stateTime;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> attackAnimation;
    
    // --- BIẾN ĐÃ ĐƯỢC THÊM VÀO ---
    private transient Texture corpseTexture;
    
    private transient Texture idleSheet;
    private transient Texture runSheet;
    private transient Texture attackSheet;
    private transient TextureRegion currentFrame;

    private static final float SIGHT_RANGE = 180f;
    private static final float ATTACK_RANGE = 50f;

    public static final int FRAME_WIDTH = 80;
    public static final int FRAME_HEIGHT = 80;

    private float spawnX, spawnY;
    private float patrolTargetX, patrolTargetY;
    private boolean hasSpawnPointBeenSet = false;
    private static final float PATROL_DISTANCE_X = 120f;
    private static final float PATROL_DISTANCE_Y = 60f;
    private int currentPatrolWaypoint = 0;

    public Enemy(String name, int level, int hp, int attack, int defense, int speed, List<Item> dropItems, int expReward) {
        super(name, level, hp, attack, defense, speed);
        this.dropItems = dropItems;
        this.expReward = expReward;
        this.stateTime = 0f;
        loadAnimations();

        this.boundingBox = new Rectangle();
        this.boundingBox.width = FRAME_WIDTH * 0.5f;
        this.boundingBox.height = FRAME_HEIGHT * 0.56f;
        updateBoundingBox();
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        if (!hasSpawnPointBeenSet) {
            this.spawnX = x;
            this.spawnY = y;
            this.patrolTargetX = this.spawnX + PATROL_DISTANCE_X;
            this.patrolTargetY = this.spawnY;
            this.hasSpawnPointBeenSet = true;
            this.currentState = EnemyState.PATROLLING;
        }
    }

    private void loadAnimations() {
        try {
            idleSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_idle.png"));
            TextureRegion[][] tmpFramesIdle = TextureRegion.split(idleSheet, FRAME_WIDTH, FRAME_HEIGHT);
            idleAnimation = new Animation<>(0.25f, new Array<>(tmpFramesIdle[0]), Animation.PlayMode.LOOP);

            runSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_run.png"));
            TextureRegion[][] tmpFramesWalk = TextureRegion.split(runSheet, FRAME_WIDTH, FRAME_HEIGHT);
            runAnimation = new Animation<>(0.15f, new Array<>(tmpFramesWalk[0]), Animation.PlayMode.LOOP);

            attackSheet = new Texture(Gdx.files.internal("enemy/Wolf_anim_attack.png"));
            TextureRegion[][] tmpFramesAttack = TextureRegion.split(attackSheet, FRAME_WIDTH, FRAME_HEIGHT);
            attackAnimation = new Animation<>(0.1f, new Array<>(tmpFramesAttack[0]), Animation.PlayMode.NORMAL);

            // --- TẢI TEXTURE XÁC CHẾT ---
            // LƯU Ý: "enemy/wolf_corpse.png" là tên file ví dụ, bạn hãy thay bằng tên file thật của bạn
            corpseTexture = new Texture(Gdx.files.internal("enemy/Wolf_anim_death.png"));

        } catch (Exception e) {
            Gdx.app.error("Enemy", "Error loading enemy animations", e);
        }
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    public void update(float delta, PlayerCharacter player) {
        if (isDead()) {
            if (currentState != EnemyState.DEAD) {
                currentState = EnemyState.DEAD;
                stateTime = 0;
            }
            return;
        }

        stateTime += delta;
        float distanceToPlayer = Vector2.dst(this.x, this.y, player.getX(), player.getY());
        EnemyState previousState = this.currentState;

        if (distanceToPlayer <= SIGHT_RANGE) {
            if (distanceToPlayer <= ATTACK_RANGE && currentState != EnemyState.ATTACKING) {
                currentState = EnemyState.ATTACKING;
                stateTime = 0;
                hitTargets.clear();
            } else if (distanceToPlayer > ATTACK_RANGE && currentState != EnemyState.ATTACKING) {
                currentState = EnemyState.CHASING;
            }
        } else {
            if (previousState == EnemyState.CHASING || previousState == EnemyState.ATTACKING) {
                currentState = EnemyState.RETURNING;
            }
        }

        switch (currentState) {
            case ATTACKING:
                if(attackAnimation != null && attackAnimation.isAnimationFinished(stateTime)) {
                    currentState = EnemyState.IDLE;
                }
                break;
            case RETURNING:
                if (Vector2.dst(this.x, this.y, this.spawnX, this.spawnY) < 5f) {
                    currentState = EnemyState.IDLE;
                }
                break;
            case PATROLLING:
                if (Vector2.dst(this.x, this.y, this.patrolTargetX, this.patrolTargetY) < 5f) {
                    currentPatrolWaypoint = (currentPatrolWaypoint + 1) % 4;
                    switch (currentPatrolWaypoint) {
                        case 0: this.patrolTargetX = spawnX + PATROL_DISTANCE_X; this.patrolTargetY = spawnY; break;
                        case 1: this.patrolTargetX = spawnX + PATROL_DISTANCE_X; this.patrolTargetY = spawnY - PATROL_DISTANCE_Y; break;
                        case 2: this.patrolTargetX = spawnX; this.patrolTargetY = spawnY - PATROL_DISTANCE_Y; break;
                        case 3: this.patrolTargetX = spawnX; this.patrolTargetY = spawnY; break;
                    }
                }
                break;
            // THAY THẾ BẰNG ĐOẠN NÀY
case IDLE:
    // IDLE là trạng thái tạm thời để quyết định hành động tiếp theo.
    // Kiểm tra lại khoảng cách tới người chơi ngay tại đây.
    float distToPlayer = Vector2.dst(this.x, this.y, player.getX(), player.getY());
    
    if (distToPlayer <= SIGHT_RANGE) {
        // Nếu người chơi vẫn còn trong tầm nhìn, tiếp tục đuổi theo.
        currentState = EnemyState.CHASING;
    } else {
        // Người chơi đã ở ngoài tầm nhìn. Kiểm tra xem mình có đang ở nhà không.
        float distToSpawn = Vector2.dst(this.x, this.y, this.spawnX, this.spawnY);
        
        if (distToSpawn > 5f) {
            // Nếu đang ở xa nhà, phải quay về.
            currentState = EnemyState.RETURNING;
        } else {
            // Nếu đã ở nhà, bắt đầu tuần tra.
            currentState = EnemyState.PATROLLING;
        }
    }
    break;
            case CHASING: case DEAD: break;
        }

        switch (currentState) {
            case CHASING: case PATROLLING: case RETURNING:
                if (runAnimation != null) currentFrame = runAnimation.getKeyFrame(stateTime, true); break;
            case ATTACKING:
                if (attackAnimation != null) currentFrame = attackAnimation.getKeyFrame(stateTime, false); break;
            case DEAD:
                if (corpseTexture != null) currentFrame = new TextureRegion(corpseTexture); break;
            case IDLE: default:
                if (idleAnimation != null) currentFrame = idleAnimation.getKeyFrame(stateTime, true); break;
        }
        
        // Cập nhật logic lật hình ảnh để chỉ lật khi đang di chuyển
        if(currentState == EnemyState.CHASING || currentState == EnemyState.PATROLLING || currentState == EnemyState.RETURNING) {
            float moveDirectionX = 0;
            if(currentState == EnemyState.CHASING) moveDirectionX = player.getX() - this.x;
            else if(currentState == EnemyState.PATROLLING) moveDirectionX = this.patrolTargetX - this.x;
            else moveDirectionX = this.spawnX - this.x;
    
            if (moveDirectionX < 0 && currentFrame != null && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (moveDirectionX > 0 && currentFrame != null && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
        }
    }

    public void updateBoundingBox() {
        this.boundingBox.setPosition(x - boundingBox.width / 2f, y - FRAME_HEIGHT / 2f + 10);
    }

    public Rectangle getBoundingBox() { return boundingBox; }
    public TextureRegion getCurrentFrame() { return currentFrame; }
    public void attackPlayer(PlayerCharacter player) { attack(player); }

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
        // --- THÊM DÒNG NÀY ĐỂ GIẢI PHÓNG BỘ NHỚ ---
        if (corpseTexture != null) corpseTexture.dispose();
    }

    public EnemyState getCurrentState(){ return currentState; }
    public float getSpawnX() { return spawnX; }
    public float getSpawnY() { return spawnY; }
    public float getPatrolTargetX() { return patrolTargetX; }
    public float getPatrolTargetY() { return patrolTargetY; }

    // --- HÀM MÀ BẠN CẦN ĐÂY RỒI ---
    public Texture getCorpseTexture() {
        return this.corpseTexture;
    }
}