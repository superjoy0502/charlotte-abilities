package com.github.superjoy0502.psychic.ability.newability

import com.github.noonmaru.psychics.Ability
import com.github.noonmaru.psychics.AbilityConcept

class AbilitySample : Ability<AbilityConcept>(){
    override fun onAttach() {
        super.onAttach()
        this.esper.player.sendMessage("ABILITYSAMPLE ATTACHED.")
    }
}