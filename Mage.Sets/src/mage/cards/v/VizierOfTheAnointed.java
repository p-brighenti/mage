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
package mage.cards.v;

import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Abilities;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.SearchEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.keyword.EmbalmAbility;
import mage.abilities.keyword.EternalizeAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Zone;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicate;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.StackAbility;
import mage.players.Player;
import mage.target.common.TargetCardInLibrary;

/**
 *
 * @author spjspj
 */
public class VizierOfTheAnointed extends CardImpl {

    private static final FilterCard filter = new FilterCard("a creature card with eternalize or embalm");

    static {
        filter.add(new CardTypePredicate(CardType.CREATURE));
        filter.add(new VizierOfTheAnointedAbilityPredicate());
    }

    public VizierOfTheAnointed(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{U}");

        this.subtype.add("Human");
        this.subtype.add("Cleric");
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // When Vizier of the Anointed enters the battlefield, you may search your library for a creature card with eternalize or embalm, put that card into your graveyard, then shuffle your library.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new SearchLibraryPutInGraveyard(filter), true));

        // Whenever you activate an eternalize or embalm ability, draw a card.
        this.addAbility(new VizierOfTheAnointedTriggeredAbility());

    }

    public VizierOfTheAnointed(final VizierOfTheAnointed card) {
        super(card);
    }

    @Override
    public VizierOfTheAnointed copy() {
        return new VizierOfTheAnointed(this);
    }
}

class VizierOfTheAnointedAbilityPredicate implements Predicate<MageObject> {

    public VizierOfTheAnointedAbilityPredicate() {
    }

    @Override
    public boolean apply(MageObject input, Game game) {
        Abilities<Ability> abilities = input.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            if (abilities.get(i) instanceof EmbalmAbility) {
                return true;
            }
            if (abilities.get(i) instanceof EternalizeAbility) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Creature card with Eternalize or Embalm";
    }
}

class VizierOfTheAnointedTriggeredAbility extends TriggeredAbilityImpl {

    VizierOfTheAnointedTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DrawCardSourceControllerEffect(1), false);
    }

    VizierOfTheAnointedTriggeredAbility(final VizierOfTheAnointedTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public VizierOfTheAnointedTriggeredAbility copy() {
        return new VizierOfTheAnointedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.ACTIVATED_ABILITY;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getPlayerId().equals(getControllerId())) {
            StackAbility stackAbility = (StackAbility) game.getStack().getStackObject(event.getSourceId());
            if (stackAbility.getStackAbility() instanceof EternalizeAbility
                    || stackAbility.getStackAbility() instanceof EmbalmAbility) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever you activate an eternalize or embalm ability, draw a card.";
    }
}

class SearchLibraryPutInGraveyard extends SearchEffect {

    public SearchLibraryPutInGraveyard(FilterCard filter) {
        super(new TargetCardInLibrary(filter), Outcome.Neutral);
        staticText = "search for a creature card with eternalize or embalm, put that card into your graveyard, then shuffle your library.";
    }

    public SearchLibraryPutInGraveyard(final SearchLibraryPutInGraveyard effect) {
        super(effect);
    }

    @Override
    public SearchLibraryPutInGraveyard copy() {
        return new SearchLibraryPutInGraveyard(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller != null) {
            if (controller.searchLibrary(target, game)) {
                if (!target.getTargets().isEmpty()) {
                    Card card = controller.getLibrary().getCard(target.getFirstTarget(), game);
                    if (card != null) {
                        controller.moveCards(card, Zone.GRAVEYARD, source, game);
                    }
                }
            }
            controller.shuffleLibrary(source, game);
            return true;
        }
        return false;
    }
}
