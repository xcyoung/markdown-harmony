package me.xcyoung.markdown.slice;

import me.xcyoung.markdown.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;

public class HomeAbilitySlice extends AbilitySlice {

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_slice_home);
        Button addMarkdownBtn = (Button) findComponentById(ResourceTable.Id_addBtn);
        addMarkdownBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {

            }
        });
    }
}
