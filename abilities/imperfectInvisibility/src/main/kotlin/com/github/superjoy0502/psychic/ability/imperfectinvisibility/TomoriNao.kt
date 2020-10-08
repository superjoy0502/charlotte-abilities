package com.github.superjoy0502.psychic.ability.imperfectinvisibility

import com.github.noonmaru.psychics.*
import com.github.noonmaru.psychics.item.isPsychicbound
import com.github.noonmaru.psychics.plugin.PsychicPlugin
import com.github.noonmaru.tap.config.Config
import com.github.noonmaru.tap.config.Name
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@Name("imperfectInvisibility")
class TomoriNaoConcept : AbilityConcept(){
    init {
        type = AbilityType.TOGGLE
        displayName = "불완전 투명화"
        levelRequirement = 0
        cooldownTicks = 200
        cost = 40.0
        castingTicks = 20
        durationTicks = 1200
        supplyItems = listOf(
            ItemStack(Material.IRON_BARS).apply {
                val meta = itemMeta
                meta.setDisplayName("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}투명화")
                meta.isUnbreakable = true
                meta.isPsychicbound = true
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                itemMeta = meta
            }
        )
        description = listOf(
            "지정된 대상에게서 모습을 감춥니다.",
            "철괴를 좌클릭하여 대상을 지정할 수 있으며",
            "우클릭 시 \${imperfectInvisibility.castingTicks / 20.0}초 뒤 \${imperfectInvisibility.durationTicks / 20.0}초 동안 모습을 감춥니다."
        )
    }
}

class TomoriNao : Ability<TomoriNaoConcept>(), Listener{

    // 능력: 지정된 대상에게서 모습을 감춤

    private lateinit var target: Player
    private lateinit var player: Player

    override fun onEnable() {
        psychic.registerEvents(this)
    }

    // 대상 지정
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        player = event.player
        if (event.action == Action.LEFT_CLICK_AIR && player.inventory.itemInMainHand.type == Material.IRON_BARS) {
            player.sendMessage("능력 사용자가 철괴를 들고 왼손을 휘둘렀다.") // Debug
            for (entity in player.world.entities) {
                if (entity.location === player.eyeLocation && entity.type == EntityType.PLAYER) {
                    target = entity as Player
                    player.sendMessage("You're looking at " + target.name + "!") // Debug
                }
            }
        }
        else if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) && player.inventory.itemInMainHand.type == Material.IRON_BARS){
            hideMe()
        }
    }

    // 대상에게서 모습 감추기
    private fun hideMe(){
        target.hidePlayer(psychic.plugin, player)
    }

    // TODO 지속시간 초과 시 투명화 해제
    // TODO 피격 시 투명화 해제 및 쿨타임 초기화

}
