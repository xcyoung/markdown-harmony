package me.xcyoung.markdown.slice;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import me.xcyoung.markdown.ResourceTable;
import me.xcyoung.markdown.fraction.RepositoryFraction;
import me.xcyoung.markdown.fraction.SettingFraction;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.fraction.Fraction;
import ohos.aafwk.ability.fraction.FractionAbility;
import ohos.aafwk.ability.fraction.FractionManager;
import ohos.aafwk.ability.fraction.FractionScheduler;
import ohos.aafwk.content.Intent;

import java.util.HashMap;

public class HomeAbilitySlice extends AbilitySlice {
    private BottomNavigationBar bottomNavigationBar;
    private HashMap<Integer, Fraction> fractionHashMap = new HashMap<>();
    private int currentTabIndex = -1;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        setUIContent(ResourceTable.Layout_ability_slice_home);

        bottomNavigationBar = (BottomNavigationBar) findComponentById(ResourceTable.Id_bottomNavigationBar);
        bottomNavigationBar.clearAll();
        bottomNavigationBar.setBarMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(ResourceTable.Media_icon, "Repository", getContext()).setActiveColor(0xffF57C00))
                .addItem(new BottomNavigationItem(ResourceTable.Media_icon, "Setting", getContext()).setActiveColor(0xff2196F3))
                .setFirstSelectedPosition(0)
                .setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(int i) {
                        changeFraction(i);
                    }

                    @Override
                    public void onTabUnselected(int i) {

                    }

                    @Override
                    public void onTabReselected(int i) {

                    }
                })
                .initialise();
        changeFraction(0);


    }

    private boolean changeFraction(int index) {
        if (currentTabIndex == index) {
            return true;
        }

        String name;
        if (index == 0) {
            name = RepositoryFraction.class.getName();
        } else if (index == 1) {
            name = SettingFraction.class.getName();
        } else {
            return false;
        }

        FractionManager fractionManager = ((FractionAbility) getAbility()).getFractionManager();
        String tag = name + "-index" + index;
        Fraction cacheFraction = fractionManager.getFractionByTag(tag).orElse(null);
        Fraction currentFraction = fractionHashMap.get(currentTabIndex);

        FractionScheduler scheduler = fractionManager.startFractionScheduler();
        if (currentFraction != null) {
            scheduler.hide(currentFraction);
        }

        if (cacheFraction == null) {
            Fraction fraction;
            if (index == 0) {
                fraction = new RepositoryFraction(this);
            } else if (index == 1) {
                fraction = new SettingFraction(this);
            } else {
                return false;
            }
            scheduler.add(ResourceTable.Id_container, fraction, tag).show(fraction);
            fractionHashMap.put(index, fraction);
        } else {
            scheduler.show(cacheFraction);
        }

        scheduler.submit();
        currentTabIndex = index;
        return true;
    }
}
