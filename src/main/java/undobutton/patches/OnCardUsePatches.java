package undobutton.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import undobutton.GameState;
import undobutton.UndoButtonMod;

import java.util.ArrayList;

public class OnCardUsePatches {
    @SpirePatch(clz = GameActionManager.class, method = "getNextAction")
    public static class GetNextActionPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void CreateNewState(GameActionManager __instance) {
            if (!((CardQueueItem) __instance.cardQueue.get(0)).autoplayCard) {
                UndoButtonMod.controller.addState(new GameState.Action(GameState.ActionType.CARD_PLAYED, ((CardQueueItem) __instance.cardQueue.get(0)).card));
                UndoButtonMod.logger.info("Added new state before playing card {}", ((CardQueueItem) __instance.cardQueue.get(0)).card.name);
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "dontTriggerOnUseCard");
                ArrayList<Matcher> prerequisites = new ArrayList<>();
                prerequisites.add(new Matcher.MethodCallMatcher(AbstractCard.class, "freeToPlay"));
                return LineFinder.findInOrder(ctMethodToPatch, prerequisites, finalMatcher);
            }
        }
    }
}