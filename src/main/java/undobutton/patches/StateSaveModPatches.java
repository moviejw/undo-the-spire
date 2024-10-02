package undobutton.patches;


import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import savestate.CardState;
import savestate.SaveStateMod;
import savestate.monsters.MonsterState;
import savestate.relics.RelicState;
import undobutton.GameState;
import undobutton.UndoButtonMod;


public class StateSaveModPatches {
    // Patch SaveStateMod's CardState to add extra card data (e.g. name and description) to GameState.
    @SpirePatch(requiredModId = "SaveStateMod", clz = CardState.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class})
    public static class CardStateConstructorPatch {
        @SpirePostfixPatch
        public static void addExtraCardData(CardState __instance, AbstractCard card) {
            GameState.extraCardData.put(card.uuid, new GameState.CardData(card));
        }
    }

    @SpirePatch(requiredModId = "SaveStateMod", clz = CardState.class, method = "loadCard")
    public static class LoadCardPatch {
        @SpirePostfixPatch
        public static AbstractCard loadCard(AbstractCard __result, CardState __instance) {
            GameState.CardData data = GameState.extraCardData.get(__result.uuid);
            if (data != null) {
                data.apply(__result);
            } else {
                UndoButtonMod.logger.error("Extra card data not found for card with UUID {}", __result.uuid);
            }
            return __result;
        }
    }

    // There is a bug in MonsterState where a method tried to set the block colour to a float (instead of Color).
    // This patch fixes that.
    @SpirePatch(requiredModId = "SaveStateMod", clz = MonsterState.class, method = "populateSharedFields")
    public static class MonsterPopulateSharedFieldsPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyPopulateSharedFields() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setPrivate")) {
                        m.replace("if (!$3.equals(\"blockTextColor\")) { $proceed($$); }");
                    }
                }
            };
        }
    }

    // Patch SaveStateMod to not add a config panel.
    @SpirePatch(requiredModId = "SaveStateMod", clz = SaveStateMod.class, method = "receivePostInitialize")
    public static class receivePostInitializePatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyRegisterModBadge() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("registerModBadge")) {
                        m.replace("$proceed($1, $2, $3, $4, null);");
                    }
                }
            };
        }
    }

    // Patch SaveStateMod's CardState to ignore the (arbitrary) limit of 100 on free cards.
    @SpirePatch(requiredModId = "SaveStateMod", clz = CardState.class, method = "freeCard")
    public static class freeCardPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyFreeCard() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("size")) {
                        m.replace("$_ = 0;");
                    }
                }
            };
        }
    }

    // Patch SaveStateMod's RelicState to ignore the (arbitrary) limit of 100 on free relics.
    @SpirePatch(requiredModId = "SaveStateMod", clz = RelicState.class, method = "freeRelic")
    public static class freeRelicPatch {
        @SpireInstrumentPatch
        public static ExprEditor modifyFreeCard() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("size")) {
                        m.replace("$_ = 0;");
                    }
                }
            };
        }
    }
}
