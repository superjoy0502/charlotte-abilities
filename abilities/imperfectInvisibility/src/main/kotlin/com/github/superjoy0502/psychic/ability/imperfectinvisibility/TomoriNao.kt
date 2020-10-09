package com.github.superjoy0502.psychic.ability.imperfectinvisibility

import com.github.noonmaru.psychics.AbilityConcept
import com.github.noonmaru.psychics.AbilityType
import com.github.noonmaru.psychics.ActiveAbility
import com.github.noonmaru.psychics.Esper
import com.github.noonmaru.psychics.item.isPsychicbound
import com.github.noonmaru.psychics.util.TargetFilter
import com.github.noonmaru.psychics.util.Tick
import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.config.Name
import com.github.noonmaru.tap.event.EntityProvider.EntityDamageByEntity
import org.bukkit.ChatColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.math.max


@Name("imperfectInvisibility")
class TomoriNaoConcept : AbilityConcept() {

    @Config
    var invisibleDurationTick: Long = 1200L

    @Config
    var invisibleCooldownTick: Long = 200L

    init {
        type = AbilityType.TOGGLE
        displayName = "불완전 투명화"
        cost = 40.0 // TODO 마나 소모
        castingTicks = 20
        supplyItems = listOf(
            ItemStack(Material.IRON_INGOT).apply {
                val meta = itemMeta
                meta.setDisplayName("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}투명화")
                meta.isUnbreakable = true
                meta.isPsychicbound = true
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                itemMeta = meta
            }
        )
        interruptible = true
        wand = ItemStack(Material.IRON_INGOT)
        description = listOf(
            "지정된 대상에게서 모습을 감춥니다.",
            "웅크려서 대상을 지정할 수 있으며",
            "유리창 클릭 시 \${common.casting-ticks / 20}초 후 \${imperfectInvisibility.invisible-duration-tick / 20}초 동안 모습을 감춥니다.",
            "투명화 중 공격을 하면 투명화가 풀리며",
            "\${imperfectInvisibility.invisible-cooldown-tick / 20}초의 쿨타임을 가집니다."
        )
    }

}

class TomoriNao : ActiveAbility<TomoriNaoConcept>(), Runnable, Listener {

    // 능력: 지정된 대상에게서 모습을 감춤

    private var target: Player? = null
    private var player: Player? = null

    var isHiding: Boolean = false
    var inCooldown: Boolean = false

    var invisibilityTick = Tick.currentTicks;
    var cooldownTick = Tick.currentTicks;

    override fun onAttach() {
        player = esper.player
    }

    override fun onEnable() {
        psychic.registerEvents(TomoriNao())
        psychic.runTaskTimer(this, 0L, 1L)
    }

    // 대상 지정

    private fun processRayTrace(): Entity? {
        val from = esper.player.eyeLocation
        val direction = from.direction
        from.world.rayTrace(
            from,
            direction,
            64.0,
            FluidCollisionMode.NEVER,
            true,
            1.0,
            TargetFilter(esper.player)
        )?.let { rayTraceResult ->
            rayTraceResult.hitEntity?.let { hit ->
                if (hit is LivingEntity) {
                    return hit
                }
            }

            return null
        }

        return null
    }

    override fun onCast(event: PlayerEvent, action: WandAction, target: Any?) {
        when (action) {
            WandAction.LEFT_CLICK -> {
                val hit: Entity? = processRayTrace()
                player = esper.player

                when {
                    hit == null -> {
                        return
                    }
                    hit.type == EntityType.PLAYER -> {
                        this.target = hit as Player
                        player!!.sendActionBar("${ChatColor.AQUA}${ChatColor.BOLD}대상 설정: ${this.target!!.name}")
                    }
                    hit.type != EntityType.PLAYER -> {
                        player!!.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}해당 엔티티는 플레이어가 아닙니다.")
                    }
                }
            }
            WandAction.RIGHT_CLICK -> {
                when (isHiding) {
                    true -> {
                        showMe()
                    }
                    false -> {
                        hideMe()
                    }
                }
            }
        }
    }

    // 대상에게서 모습 감추기
    private fun hideMe() {
        if (player != null && target != null && !inCooldown){
            invisibilityTick = Tick.currentTicks + concept.invisibleDurationTick
            player?.let { target?.hidePlayer(psychic.plugin, it) }
            isHiding = true
            inCooldown = false
        }
    }

    val invisibilityRemainingTicks
        get() = max(0, invisibilityTick - Tick.currentTicks)

    // 지속시간 초과 시 투명화 해제 및 쿨타임 초기화
    private fun showMe(){
        if (player != null && target != null){
            cooldownTick = Tick.currentTicks + concept.invisibleCooldownTick
            player?.let { target?.showPlayer(psychic.plugin, it) }
            isHiding = false
            inCooldown = true
        }
    }

    val cooldownRemainingTicks
        get() = max(0, cooldownTick - Tick.currentTicks)

    override fun run() {
        if (isHiding) {
            player?.sendActionBar("${ChatColor.AQUA}${ChatColor.BOLD}${(invisibilityRemainingTicks / 20.0).toInt()}  | 대상: ${target?.name}")
            if (invisibilityRemainingTicks <= 0){
                showMe()
            }
        }
        if (inCooldown) {
            player?.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}쿨타임: ${(cooldownRemainingTicks / 20.0).toInt()}")
            if (cooldownRemainingTicks <= 0){
                inCooldown = false
            }
        }
    }


    // TODO 공격 시 투명화 해제 및 쿨타임 초기화

    @EventHandler
    fun onPlayerAttack(event: EntityDamageByEntityEvent) {
        println("${event.damager} hit ${event.entity}.")
        if (event.damager.type == EntityType.PLAYER) {
            println("${(event.damager as Player).name} hit")
            var damager: Player = event.damager as Player

            if (damager == this.player && isHiding){
                showMe()
            }
        }
    }

}
