/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.base;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
* This class is part of the Astral Sorcery Mod
* The complete source code for this mod can be found on github.
* Class: MoonPhase
* Created by HellFirePvP
* Date: 17.11.2016 / 02:45
*/
public enum MoonPhase {

    FULL, WANING_3_4, WANING_1_2, WANING_1_4,
    NEW, WAXING_1_4, WAXING_1_2, WAXING_3_4;

    public static MoonPhase fromWorld(IWorld world) {
        return values()[MathHelper.clamp(world.getMoonPhase(), 0, values().length)];
    }

    @OnlyIn(Dist.CLIENT)
    public AbstractRenderableTexture getTexture() {
        return AssetLibrary.loadTexture(AssetLoader.TextureLocation.MISC, "moon_" + this.name().toLowerCase());
    }

}