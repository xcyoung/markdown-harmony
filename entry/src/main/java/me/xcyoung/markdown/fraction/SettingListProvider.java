package me.xcyoung.markdown.fraction;

import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.bean.setting.DownloadSettingVo;
import me.xcyoung.markdown.bean.setting.SettingVo;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.*;

import java.util.ArrayList;
import java.util.List;

public class SettingListProvider extends BaseItemProvider {
    private final List<SettingVo> data = new ArrayList<>();
    private final AbilitySlice slice;
    private OnEventListener onEventListener;

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
        SettingVo vo = data.get(i);
        if (component == null) {
            if (vo instanceof DownloadSettingVo) {
                cpt = LayoutScatter.getInstance(slice).parse(ResourceTable.Layout_item_download_setting, null,
                        false);
                Button downloadBtn = (Button) cpt.findComponentById(ResourceTable.Id_downloadBtn);
                TextField textField = (TextField) cpt.findComponentById(ResourceTable.Id_addressTextField);
                downloadBtn.setClickedListener(c -> {
                    if (onEventListener != null) onEventListener.onDownloadClick(textField.getText());
                });
            } else {
                cpt = null;
            }
        } else {
            cpt = component;
        }
        return cpt;
    }

    public void setData(List<SettingVo> data) {
        this.data.clear();
        this.data.addAll(data);
    }

    public void setOnEventListener(OnEventListener listener) {
        this.onEventListener = listener;
    }

    interface OnEventListener {
        void onDownloadClick(String address);
    }
}