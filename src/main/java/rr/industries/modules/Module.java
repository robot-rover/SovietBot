package rr.industries.modules;

import rr.industries.util.BotActions;

public interface Module {
    boolean isEnabled();

    Module enable(BotActions actions);

    Module disable();
}
