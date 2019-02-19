package rr.industries.commands;

import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;

import java.util.List;
import java.util.function.Predicate;

//yes I know this is horrible code and I don't care :-P
@CommandInfo(
        commandName = "test",
        helpText = "Temporary command for testing new features",
        permLevel = Permissions.BOTOPERATOR
)
public class Test implements Command {

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> true;
    }
}
