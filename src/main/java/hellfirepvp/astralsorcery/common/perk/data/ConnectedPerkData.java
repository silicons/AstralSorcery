package hellfirepvp.astralsorcery.common.perk.data;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConnectedPerkData
 * Created by HellFirePvP
 * Date: 13.08.2020 / 17:17
 */
public class ConnectedPerkData {

    private final AbstractPerk perk;
    private final Set<ResourceLocation> connections = new HashSet<>();

    public ConnectedPerkData(AbstractPerk perk) {
        this.perk = perk;
    }

    void addConnection(ResourceLocation to) {
        this.connections.add(to);
    }

    public Set<ResourceLocation> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    public AbstractPerk getPerk() {
        return perk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectedPerkData that = (ConnectedPerkData) o;
        return Objects.equals(perk.getRegistryName(), that.perk.getRegistryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(perk.getRegistryName());
    }
}
