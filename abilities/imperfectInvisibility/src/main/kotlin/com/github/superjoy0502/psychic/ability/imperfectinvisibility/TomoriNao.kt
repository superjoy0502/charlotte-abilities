package com.github.superjoy0502.psychic.ability.imperfectinvisibility

import com.github.noonmaru.psychics.AbilityConcept
import com.github.noonmaru.psychics.AbilityType
import com.github.noonmaru.psychics.ActiveAbility
import com.github.noonmaru.psychics.item.isPsychicbound
import com.github.noonmaru.psychics.util.TargetFilter
import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.config.Name
import org.bukkit.ChatColor
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

@Name("imperfectInvisibility")
class TomoriNaoConcept : AbilityConcept() {

    @Config
    var invisibleDurationTick: Long = 1200L

    @Config
    var invisibleCooldownTick: Long = 200L

    init {
        type = AbilityType.TOGGLE
        displayName = "불완전 투명화"
        cost = 40.0
        castingTicks = 20
        supplyItems = listOf(
                ItemStack(Material.GLASS_PANE).apply {
                    val meta = itemMeta
                    meta.setDisplayName("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}투명화")
                    meta.isUnbreakable = true
                    meta.isPsychicbound = true
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                    itemMeta = meta
                }
        )
        interruptible = true
        wand = ItemStack(Material.GLASS_PANE)
        description = listOf(
                "지정된 대상에게서 모습을 감춥니다.",
                "웅크려서 대상을 지정할 수 있으며",
                "유리창 클릭 시 \${common.casting-ticks / 20}초 후 \${imperfectInvisibility.invisible-duration / 20}초 동안 모습을 감춥니다.",
                "투명화 중 공격을 하면 투명화가 풀리며",
                "\${imperfectInvisibility.invisible-cooldown / 20}초의 쿨타임을 가집니다."
        )
    }

}

class TomoriNao : ActiveAbility<TomoriNaoConcept>(), Listener {

    // 능력: 지정된 대상에게서 모습을 감춤

    private var target: Player? = null
    private var player: Player? = null

    override fun onEnable() {
        psychic.registerEvents(this)
    }

    // 대상 지정
    @EventHandler
    fun onPlayerSneak(event: PlayerToggleSneakEvent) {
        val hit: Entity? = processRayTrace()
        player = esper.player

        when {
            hit == null -> {
                return
            }
            hit.type == EntityType.PLAYER -> {
                target = hit as Player
                player!!.sendActionBar("${ChatColor.AQUA}${ChatColor.BOLD}Target: ${target!!.name}")
            }
            hit.type != EntityType.PLAYER -> {
                player!!.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}That entity is not a Player!")
            }
        }
    }

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

    override fun onCast(target: Any?) {
        hideMe()
    }

    // 대상에게서 모습 감추기
    private fun hideMe() {
        if (player != null && target != null){
            player?.let { target?.hidePlayer(psychic.plugin, it) }
        }
    }

    // TODO 지속시간 초과 시 투명화 해제 및 쿨타임 초기화
    // TODO 공격 시 투명화 해제 및 쿨타임 초기화

}
