package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.setting.SettingVo;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;

import java.util.ArrayList;
import java.util.List;

public class SettingListProvider extends BaseItemProvider {
    private final List<SettingVo> data = new ArrayList<>();
    private final AbilitySlice slice;

    public SettingListProvider(AbilitySlice slice) {
        super();
        this.slice = slice;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
        final Component cpt;
        if (component == null) {
            cpt = LayoutScatter.getInstance(slice).parse(ResourceTable.Layout_item_download_setting, null, false);
        } else {
            cpt = component;
        }
        SettingVo vo = data.get(i);
//        Text title = (Text) cpt.findComponentById(ResourceTable.Id_titleText);
//        title.setText(vo.getTitle());
//        if (vo instanceof Dow)
//        text.setText(sampleItem.getName());
        return cpt;
    }

    public void setData(List<SettingVo> data) {
        this.data.clear();
        this.data.addAll(data);
    }
}
