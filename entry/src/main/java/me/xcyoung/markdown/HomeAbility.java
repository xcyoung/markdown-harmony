package me.xcyoung.markdown;

import me.xcyoung.markdown.slice.HomeAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.window.service.WindowManager;
import ohos.global.resource.NotExistException;
import ohos.global.resource.WrongTypeException;

import java.io.IOException;

public class HomeAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        try {
            WindowManager.getInstance().getTopWindow().get().setStatusBarColor(getResourceManager()
                    .getElement(ResourceTable.Color_markdown_background).getColor());
            WindowManager.getInstance().getTopWindow().get().setStatusBarVisibility(Component.VISIBLE);
        } catch (IOException | NotExistException | WrongTypeException e) {
            e.printStackTrace();
        }

        super.setMainRoute(HomeAbilitySlice.class.getName());

        requestPermissionsFromUser(new String[]{
                "ohos.permission.INTERNET", "ohos.permission.READ_MEDIA", "ohos.permission.WRITE_MEDIA"
        }, 100);
    }
}
