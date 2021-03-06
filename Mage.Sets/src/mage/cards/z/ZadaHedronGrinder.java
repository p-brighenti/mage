/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.z;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.players.Player;
import mage.target.Target;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author LevelX2
 */
public class ZadaHedronGrinder extends CardImpl {

    public ZadaHedronGrinder(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");
        addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.GOBLIN, SubType.ALLY);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Whenever you cast an instant or sorcery spell that targets only Zada, Hedron Grinder, copy that spell for each other creature you control that the spell could target. Each copy targets a different one of those creatures.
        this.addAbility(new ZadaHedronGrinderTriggeredAbility());

    }

    public ZadaHedronGrinder(final ZadaHedronGrinder card) {
        super(card);
    }

    @Override
    public ZadaHedronGrinder copy() {
        return new ZadaHedronGrinder(this);
    }
}

class ZadaHedronGrinderTriggeredAbility extends TriggeredAbilityImpl {

    ZadaHedronGrinderTriggeredAbility() {
        super(Zone.BATTLEFIELD, new ZadaHedronGrinderEffect(), false);
    }

    ZadaHedronGrinderTriggeredAbility(final ZadaHedronGrinderTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public ZadaHedronGrinderTriggeredAbility copy() {
        return new ZadaHedronGrinderTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.SPELL_CAST;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getPlayerId().equals(this.getControllerId())) {
            Spell spell = game.getStack().getSpell(event.getTargetId());
            if (isControlledInstantOrSorcery(spell)) {
                boolean targetsSource = false;
                for (Ability ability : spell.getSpellAbilities()) {
                    for (UUID modeId : ability.getModes().getSelectedModes()) {
                        Mode mode = ability.getModes().get(modeId);
                        for (Target target : mode.getTargets()) {
                            if (!target.isNotTarget()) {
                                for (UUID targetId : target.getTargets()) {
                                    if (targetId.equals(getSourceId())) {
                                        targetsSource = true;
                                    } else {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                if (targetsSource) {
                    this.getEffects().get(0).setTargetPointer(new FixedTarget(spell.getId()));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isControlledInstantOrSorcery(Spell spell) {
        return spell != null
                && (spell.getControllerId().equals(this.getControllerId()))
                && (spell.isInstant() || spell.isSorcery());
    }

    @Override
    public String getRule() {
        return "Whenever you cast an instant or sorcery spell that targets only {this}, copy that spell for each other creature you control that the spell could target. Each copy targets a different one of those creatures.";
    }
}

class ZadaHedronGrinderEffect extends OneShotEffect {

    public ZadaHedronGrinderEffect() {
        super(Outcome.Detriment);
        this.staticText = "copy that spell for each other creature you control that the spell could target. Each copy targets a different one of those creatures";
    }

    public ZadaHedronGrinderEffect(final ZadaHedronGrinderEffect effect) {
        super(effect);
    }

    @Override
    public ZadaHedronGrinderEffect copy() {
        return new ZadaHedronGrinderEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Spell spell = game.getStack().getSpell(targetPointer.getFirst(game, source));
        if (spell == null) {
            spell = (Spell) game.getLastKnownInformation(targetPointer.getFirst(game, source), Zone.STACK);
        }
        Player controller = game.getPlayer(source.getControllerId());
        if (spell != null && controller != null) {
            // search the target that targets source
            Target usedTarget = null;
            setUsedTarget:
            for (Ability ability : spell.getSpellAbilities()) {
                for (UUID modeId : ability.getModes().getSelectedModes()) {
                    Mode mode = ability.getModes().get(modeId);
                    for (Target target : mode.getTargets()) {
                        if (!target.isNotTarget() && target.getFirstTarget().equals(source.getSourceId())) {
                            usedTarget = target.copy();
                            usedTarget.clearChosen();
                            break setUsedTarget;
                        }
                    }
                }
            }
            if (usedTarget == null) {
                return false;
            }
            for (Permanent creature : game.getState().getBattlefield().getAllActivePermanents(StaticFilters.FILTER_PERMANENT_CREATURE, source.getControllerId(), game)) {
                if (!creature.getId().equals(source.getSourceId()) && usedTarget.canTarget(source.getControllerId(), creature.getId(), source, game)) {
                    Spell copy = spell.copySpell(source.getControllerId());
                    game.getStack().push(copy);
                    setTarget:
                    for (UUID modeId : copy.getSpellAbility().getModes().getSelectedModes()) {
                        Mode mode = copy.getSpellAbility().getModes().get(modeId);
                        for (Target target : mode.getTargets()) {
                            if (target.getClass().equals(usedTarget.getClass())) {
                                target.clearChosen(); // For targets with Max > 1 we need to clear before the text is comapred
                                if (target.getMessage().equals(usedTarget.getMessage())) {
                                    target.addTarget(creature.getId(), copy.getSpellAbility(), game, false);
                                    break setTarget;
                                }
                            }
                        }
                    }
                    game.fireEvent(new GameEvent(GameEvent.EventType.COPIED_STACKOBJECT, copy.getId(), spell.getId(), source.getControllerId()));
                    String activateMessage = copy.getActivatedMessage(game);
                    if (activateMessage.startsWith(" casts ")) {
                        activateMessage = activateMessage.substring(6);
                    }
                    if (!game.isSimulation()) {
                        game.informPlayers(controller.getLogName() + activateMessage);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
