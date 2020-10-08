package com.github.superjoy0502.psychic.ability.imperfectinvisibility

import com.github.noonmaru.psychics.Ability
import com.github.noonmaru.psychics.AbilityConcept
import com.github.noonmaru.psychics.Esper
import com.github.noonmaru.psychics.Psychic
import com.github.noonmaru.psychics.plugin.PsychicPlugin
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin


class TomoriNao : Ability<AbilityConcept>(), Listener{

    // 능력: 지정된 대상에게서 모습을 감춤

    private lateinit var target: Esper
    private lateinit var player: Esper

    override fun onEnable() {
        psychic.registerEvents(this)
    }

    // 대상 지정
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        player = event.player as Esper
        if (event.action == Action.LEFT_CLICK_AIR && player.player.inventory.itemInMainHand.type == Material.IRON_BARS) {
            player.player.sendMessage("능력 사용자가 철괴를 들고 왼손을 휘둘렀다.")
            for (entity in player.player.world.entities) {
                if (entity.location === player.player.eyeLocation && entity.type == EntityType.PLAYER) {
                    target = entity as Esper
                    player.player.sendMessage("You're looking at " + target.player.name + "!")
                }
            }
        }
        else if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) && player.player.inventory.itemInMainHand.type == Material.IRON_BARS){
            hideMe()
        }
    }

    // 대상에게서 모습 감추기
    fun hideMe(){
        target.player.hidePlayer(psychic.plugin, player.player)
    }

}
