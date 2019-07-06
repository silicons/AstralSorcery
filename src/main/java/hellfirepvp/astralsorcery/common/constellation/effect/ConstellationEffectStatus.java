package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationEffectStatus
 * Created by HellFirePvP
 * Date: 11.06.2019 / 20:46
 */
public interface ConstellationEffectStatus {

    public abstract boolean runEffect(World world, BlockPos pos, int mirrorAmount, ConstellationEffectProperties modified, @Nullable IMinorConstellation possibleTraitEffect);

}