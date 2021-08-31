package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.setting.DownloadSettingVo;
import me.xcyoung.markdown.bean.setting.SettingVo;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.ListContainer;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        ListContainer listContainer = (ListContainer)getFractionAbility().findComponentById(ResourceTable.Id_settingListContainer);
        SettingListProvider settingListProvider = new SettingListProvider(abilitySlice);
        List<SettingVo> list = new ArrayList<>();
        list.add(new DownloadSettingVo());
        settingListProvider.setData(list);
        listContainer.setItemProvider(settingListProvider);
    }
}
