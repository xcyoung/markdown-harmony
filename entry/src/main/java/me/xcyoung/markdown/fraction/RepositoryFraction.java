package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.slice.MarkdownAbilitySlice;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;

public class RepositoryFraction extends Fraction {
    private final AbilitySlice abilitySlice;

    public RepositoryFraction(AbilitySlice abilitySlice) {
        this.abilitySlice = abilitySlice;
    }

    @Override
    protected Component onComponentAttached(LayoutScatter scatter, ComponentContainer container, Intent intent) {
        return scatter.parse(ResourceTable.Layout_fraction_repository, container, false);
    }

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        getFractionAbility().findComponentById(ResourceTable.Id_btn).setClickedListener(component -> {
            abilitySlice.present(new MarkdownAbilitySlice(), intent);
        });
    }
}
