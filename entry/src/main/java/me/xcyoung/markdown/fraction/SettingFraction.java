package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;

public class SettingFraction extends Fraction {
    AbilitySlice abilitySlice;

    public SettingFraction(AbilitySlice abilitySlice) {
        super();
        this.abilitySlice = abilitySlice;
    }

    @Override
    protected Component onComponentAttached(LayoutScatter scatter, ComponentContainer container, Intent intent) {
        return scatter.parse(ResourceTable.Layout_fraction_setting, container, false);
    }
}
