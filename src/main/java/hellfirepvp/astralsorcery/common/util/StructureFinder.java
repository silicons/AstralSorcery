/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.data.world.StructureGenerationBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.structure.StructureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureFinder
 * Created by HellFirePvP
 * Date: 02.06.2019 / 11:07
 */
public class StructureFinder {

    public static final String STRUCT_VILLAGE = "village";
    public static final String STRUCT_STRONGHOLD = "stronghold";
    public static final String STRUCT_MASNION = "mansion";
    public static final String STRUCT_MONUMENT = "monument";
    public static final String STRUCT_MINESHAFT = "mineshaft";
    public static final String STRUCT_DESERT_PYRAMID = "desert_pyramid";
    public static final String STRUCT_JUNGLE_TEMPLE = "jungle_temple";
    public static final String STRUCT_ENDCITY = "endCity";
    public static final String STRUCT_FORTRESS = "fortress";

    private StructureFinder() {}

    @Nullable
    public static BlockPos tryFindClosestAstralSorceryStructure(ServerWorld world, BlockPos playerPos, StructureType searchKey, int searchRadius) {
        return DataAS.DOMAIN_AS.getData(world, DataAS.KEY_STRUCTURE_GENERATION)
                .getClosest(searchKey, playerPos, searchRadius);
    }

    @Nullable
    public static BlockPos tryFindClosestVanillaStructure(ServerWorld world, BlockPos playerPos, Structure<?> structure, int searchRadius) {
        ChunkGenerator<?> gen = world.getChunkProvider().getChunkGenerator();
        try {
            return structure.findNearest(world, gen, playerPos, searchRadius, true);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static BlockPos tryFindClosestBiomeType(ServerWorld world, BlockPos playerPos, BiomeDictionary.Type biomeType, int searchRadius) {
        List<Biome> fitting = Lists.newArrayList(BiomeDictionary.getBiomes(biomeType));
        if(fitting.isEmpty()) {
            return null;
        }
        BiomeProvider gen = world.getChunkProvider().getChunkGenerator().getBiomeProvider();
        for (int reach = 64; reach < searchRadius; reach += 128) {
            BlockPos closest = gen.findBiomePosition(playerPos.getX(), playerPos.getZ(), reach, fitting, new Random(world.getSeed()));
            if(closest != null) {
                return closest;
            }
        }
        return null;
    }

}
