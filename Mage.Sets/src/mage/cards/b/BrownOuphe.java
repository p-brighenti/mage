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
package mage.cards.b;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CounterTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AbilityType;
import mage.constants.CardType;
import mage.constants.Zone;
import mage.filter.FilterStackObject;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.stack.StackAbility;
import mage.target.common.TargetActivatedOrTriggeredAbility;

/**
 *
 * @author TheElk801
 */
public class BrownOuphe extends CardImpl {

    private final static FilterStackObject filter = new FilterStackObject("ability from an artifact source");

    static {
        filter.add(new ArtifactSourcePredicate());
    }

    public BrownOuphe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{G}");

        this.subtype.add("Ouphe");
        this.power = new MageInt(1);
        this.toughness = new MageInt(1);

        // {1}{G}, {tap}: Counter target activated ability from an artifact source.
        Ability ability = new SimpleActivatedAbility(Zone.BATTLEFIELD, new CounterTargetEffect(), new ManaCostsImpl<>("{1}{G}"));
        ability.addCost(new TapSourceCost());
        ability.addTarget(new TargetActivatedOrTriggeredAbility(filter));
        this.addAbility(ability);
    }

    public BrownOuphe(final BrownOuphe card) {
        super(card);
    }

    @Override
    public BrownOuphe copy() {
        return new BrownOuphe(this);
    }
}

class ArtifactSourcePredicate implements Predicate<Ability> {

    public ArtifactSourcePredicate() {
    }

    @Override
    public boolean apply(Ability input, Game game) {
        if (input instanceof StackAbility) {
            return input.getSourceObject(game).isArtifact() && input.getAbilityType() == AbilityType.ACTIVATED;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Source(Artifact)";
    }
}
