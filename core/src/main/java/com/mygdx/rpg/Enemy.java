package com.mygdx.rpg;

import com.badlogic.gdx.Gdx; // Cho deltaTime và logging
import com.badlogic.gdx.math.Vector2; // Để lưu trữ tọa độ điểm tuần tra
import java.util.List;
import java.util.ArrayList; // Nếu muốn khởi tạo patrolPoints rỗng

public class Enemy extends Character {
    private List<Item> dropItems;
    private int expReward;

    // Thuộc tính cho AI
    private List<Vector2> patrolPoints;         // Danh sách các điểm tuần tra
    private int currentPatrolPointIndex;        // Chỉ số của điểm tuần tra hiện tại đang hướng tới
    private float patrolReachedThreshold = 10f; // Khoảng cách đủ gần để coi là đã đến điểm (pixels)

    private float detectionRadius;              // Bán kính phát hiện người chơi
    private float attackRange;                  // Tầm tấn công của Enemy
    private PlayerCharacter target;             // Người chơi mục tiêu
    
    private enum AIState {                      // Trạng thái AI của Enemy
        PATROLLING,
        CHASING,
        ATTACKING
    }
    private AIState currentState;

    private float attackCooldown;               // Thời gian chờ giữa các đòn đánh (giây)
    private float currentAttackCooldown;        // Thời gian chờ hiện tại

    public Enemy(String name, int level, int maxhp, int attack, int defense, float speed,
                 float initialX, float initialY, // Thêm vị trí khởi tạo cho Character
                 List<Item> dropItems, int expReward,
                 List<Vector2> patrolPoints,
                 float detectionRadius, float attackRange, float attackCooldown) {
        super(name, level, maxhp, attack, defense, speed, initialX, initialY); // Gọi constructor của Character đã cập nhật
        this.dropItems = dropItems != null ? dropItems : new ArrayList<Item>(); // Tránh NullPointerException
        this.expReward = expReward;
        
        this.patrolPoints = patrolPoints;
        this.currentPatrolPointIndex = 0;
        if (this.patrolPoints == null || this.patrolPoints.isEmpty()) {
            Gdx.app.log(this.name, "No patrol points defined. Enemy will be idle or use other AI logic.");
        }

        this.detectionRadius = detectionRadius;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.currentAttackCooldown = 0; // Sẵn sàng tấn công ngay
        this.target = null;
        this.currentState = AIState.PATROLLING; // Bắt đầu bằng trạng thái tuần tra
    }

    /**
     * Phương thức update chính cho AI của Enemy, được gọi mỗi frame.
     * @param player Đối tượng người chơi để Enemy tương tác.
     * @param deltaTime Thời gian trôi qua giữa frame trước và frame hiện tại.
     */
    public void update(PlayerCharacter player, float deltaTime) {
        if (!isAlive()) {
            return; // Nếu Enemy đã chết, không làm gì cả
        }

        // Giảm thời gian hồi chiêu tấn công
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= deltaTime;
        }

        // Logic quyết định trạng thái (Phát hiện người chơi)
        if (player != null && player.isAlive()) {
            float distanceToPlayer = calculateDistance(this.x, this.y, player.getX(), player.getY());

            if (distanceToPlayer <= detectionRadius) {
                this.target = player; // Phát hiện người chơi, đặt làm mục tiêu
                if (distanceToPlayer <= attackRange && currentAttackCooldown <= 0) {
                    currentState = AIState.ATTACKING;
                } else {
                    currentState = AIState.CHASING;
                }
            } else {
                // Nếu người chơi ra khỏi tầm phát hiện VÀ đang là mục tiêu -> mất dấu
                if (this.target == player) { 
                    Gdx.app.log(name, "Lost sight of " + player.getName());
                    this.target = null;
                }
                currentState = AIState.PATROLLING; // Quay lại tuần tra
            }
        } else {
            // Không có người chơi hoặc người chơi đã chết
            this.target = null;
            currentState = AIState.PATROLLING;
        }

        // Thực hiện hành động dựa trên trạng thái
        switch (currentState) {
            case PATROLLING:
                patrolBehavior(deltaTime);
                break;
            case CHASING:
                if (this.target != null) {
                    chaseBehavior(this.target, deltaTime);
                } else { // Bất ngờ target bị null dù đang chasing (ví dụ player disconnect)
                    currentState = AIState.PATROLLING;
                }
                break;
            case ATTACKING:
                if (this.target != null) {
                    attackBehavior(this.target);
                } else { // Target biến mất khi chuẩn bị attack
                     currentState = AIState.PATROLLING;
                }
                break;
        }
    }

    private void patrolBehavior(float deltaTime) {
        if (patrolPoints == null || patrolPoints.isEmpty()) {
            // Đứng yên hoặc có hành vi mặc định khác nếu không có điểm tuần tra
            // Gdx.app.log(name, "Is idle (no patrol points).");
            return;
        }

        Vector2 targetPoint = patrolPoints.get(currentPatrolPointIndex);
        float distanceToTarget = calculateDistance(this.x, this.y, targetPoint.x, targetPoint.y);

        if (distanceToTarget <= patrolReachedThreshold) {
            currentPatrolPointIndex = (currentPatrolPointIndex + 1) % patrolPoints.size();
            // Gdx.app.log(name, "Reached patrol point. New target index: " + currentPatrolPointIndex);
        } else {
            moveTo(targetPoint.x, targetPoint.y, deltaTime);
        }
    }

    private void chaseBehavior(Character target, float deltaTime) {
        // Gdx.app.log(name, "Chasing " + target.getName());
        moveTo(target.getX(), target.getY(), deltaTime);
    }

    private void attackBehavior(PlayerCharacter targetPlayer) {
        Gdx.app.log(name, "Attacking " + targetPlayer.getName());
        attackPlayer(targetPlayer); // Gọi phương thức tấn công
        currentAttackCooldown = attackCooldown; // Đặt lại thời gian hồi chiêu
        currentState = AIState.CHASING; // Sau khi tấn công, có thể quay lại trạng thái Chasing hoặc chờ
    }

    /**
     * Di chuyển Enemy về phía (targetX, targetY)
     */
    private void moveTo(float targetX, float targetY, float deltaTime) {
        float moveAmount = this.speed * deltaTime;
        float directionX = targetX - this.x;
        float directionY = targetY - this.y;
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);

        if (length > 0) { // Chỉ di chuyển nếu chưa ở đúng vị trí
            if (length <= moveAmount) { // Nếu khoảng cách còn lại nhỏ hơn hoặc bằng quãng đường di chuyển
                this.x = targetX;       // Di chuyển thẳng đến đích
                this.y = targetY;
            } else { // Nếu còn xa hơn một bước di chuyển
                directionX /= length;   // Chuẩn hóa vector hướng
                directionY /= length;
                this.x += directionX * moveAmount;
                this.y += directionY * moveAmount;
            }
        }
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // Phương thức attackPlayer của bạn (giữ nguyên, chỉ cần đảm bảo Character có attack(Character))
    public void attackPlayer(PlayerCharacter player) {
        if (player != null && player.isAlive()) {
            super.attack(player); // Gọi phương thức attack của lớp Character cha
        }
    }

    // Phương thức dropLoot của bạn (giữ nguyên)
    public void dropLoot(PlayerCharacter player) {
        if (player == null) return;
        // Chỉ rơi đồ nếu Enemy đã chết (thường sẽ gọi hàm này từ bên ngoài sau khi isAlive() = false)
        Gdx.app.log(name, "dropped loot!");
        for (Item item : dropItems) {
            player.addItem(item); // Giả sử PlayerCharacter có phương thức addItem
        }
        if (player.isAlive()) {
            player.addExperience(expReward); // Giả sử PlayerCharacter có phương thức addExperience
        }
    }
    
    // Ghi đè phương thức die từ lớp Character nếu muốn thêm hành vi cụ thể
    @Override
    protected void die() {
        super.die(); // Gọi die() của lớp cha (ví dụ: in ra "Name has been defeated.")
        Gdx.app.log(name, "The enemy has fallen!");
        // Logic khác khi enemy chết, ví dụ:
        // - Bắt đầu animation chết
        // - Đánh dấu để xóa khỏi danh sách enemy đang hoạt động trong GamePlayScreen
        // - Việc gọi dropLoot() thường được thực hiện bởi GamePlayScreen sau khi phát hiện enemy die()
    }

    // Getters cho các thuộc tính AI (nếu cần truy cập từ bên ngoài)
    public PlayerCharacter getTarget() {
        return target;
    }

    public AIState getCurrentState() {
        return currentState;
    }
}