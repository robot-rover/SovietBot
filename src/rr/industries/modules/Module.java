package rr.industries.modules;

public interface Module {
    boolean isEnabled();

    Module enable();

    Module disable();
}
