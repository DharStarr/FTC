package net.forthecrown.utils;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import net.forthecrown.user.enums.Branch;
import org.jetbrains.annotations.Nullable;

public class BranchFlag extends Flag<Branch> {

    public BranchFlag(String name) {
        super(name);
    }

    @Override
    public Branch parseInput(FlagContext context) throws InvalidFlagFormat {
        if(context.getUserInput().equalsIgnoreCase("none")) return null;
        try {
            return Branch.valueOf(context.getUserInput().toUpperCase());
        } catch (Exception e){
            throw new InvalidFlagFormat(context.getUserInput() + " is not a valid branch value");
        }
    }

    @Override
    public Branch unmarshal(@Nullable Object o) {
        try {
            return Branch.valueOf(o.toString().toUpperCase());
        } catch (Exception e){
            return null;
        }
    }

    @Override
    public Object marshal(Branch o) {
        try {
            return o.toString().toLowerCase();
        } catch (Exception e){
            return "none";
        }
    }
}