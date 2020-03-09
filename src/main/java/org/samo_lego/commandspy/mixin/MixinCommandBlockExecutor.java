package org.samo_lego.commandspy.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.samo_lego.commandspy.CommandSpy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Mixin(CommandBlockExecutor.class)
public abstract class MixinCommandBlockExecutor {

    @Shadow public abstract ServerCommandSource getSource();
    @Shadow public abstract String getCommand();

    // Injection for command block executing commands
    @Inject(method = "execute(Lnet/minecraft/world/World;)Z", at = @At(value = "RETURN"))
    private void execute(World world, CallbackInfoReturnable<Boolean> cir) {
        // Checking if mixin should be enabled todo
        boolean enabled = CommandSpy.config.main.logCommandBlockCommands;
        String command = this.getCommand();

        if(enabled && CommandSpy.canSend(command)) {
            // Getting message style from config
            String message = CommandSpy.config.main.commandBlockMessageStyle;

            // Getting other info
            String dimension = Objects.requireNonNull(DimensionType.byRawId(world.getDimension().getType().getRawId())).toString();
            int x = (int) this.getSource().getPosition().getX();
            int y = (int) this.getSource().getPosition().getY();
            int z = (int) this.getSource().getPosition().getZ();

            // Saving those to hashmap for fancy printing with logger
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("dimension", String.valueOf(dimension));
            valuesMap.put("command", command);
            valuesMap.put("x", String.valueOf(x));
            valuesMap.put("y", String.valueOf(y));
            valuesMap.put("z", String.valueOf(z));
            StrSubstitutor sub = new StrSubstitutor(valuesMap);

            // Logging to console
            CommandSpy.LOGGER.info(sub.replace(message));

            // Clearing the left-over map
            valuesMap.clear();
        }
    }
}