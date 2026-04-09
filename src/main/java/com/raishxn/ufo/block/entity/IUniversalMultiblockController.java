package com.raishxn.ufo.block.entity;

import java.util.List;

public interface IUniversalMultiblockController {
    boolean isGuiAssembled();

    boolean isGuiRunning();

    int getGuiProgress();

    int getGuiMaxProgress();

    int getGuiTemperature();

    int getGuiMaxTemperature();

    int getGuiMachineTier();

    long getGuiStoredEnergy();

    long getGuiMaxEnergy();

    boolean isGuiSafeMode();

    boolean isGuiOverclocked();

    void toggleSafeMode();

    void toggleOverclock();

    List<UniversalDisplayedRecipe> getDisplayedRecipes();
}
