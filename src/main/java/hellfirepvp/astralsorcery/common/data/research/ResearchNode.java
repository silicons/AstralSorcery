/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.client.resource.query.TextureQuery;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResearchNode
 * Created by HellFirePvP
 * Date: 12.08.2016 / 20:51
 */
public class ResearchNode {

    private static int counter = 0;

    private final int id;
    private final RenderType renderType;
    public final int renderPosX, renderPosZ;
    private String unlocName;

    private ItemStack[] renderItemStacks;
    private SpriteQuery renderSpriteQuery;
    private TextureQuery backgroundTextureQuery = new TextureQuery(AssetLoader.TextureLocation.GUI, "research_frame_wood");

    private Color textureColorHint = new Color(0xFFFFFFFF, true);

    private List<ResearchNode> connectionsTo = new LinkedList<>();
    private List<JournalPage> pages = new LinkedList<>();

    private ResearchNode(RenderType type, String unlocName, int rPosX, int rPosZ) {
        this.id = counter;
        counter++;
        this.renderType = type;
        this.renderPosX = rPosX;
        this.renderPosZ = rPosZ;
        this.unlocName = unlocName;
    }

    public ResearchNode(ItemStack itemStack, String unlocName, int renderPosX, int renderPosZ) {
        this(RenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = new ItemStack[] { itemStack };
    }

    public ResearchNode(ItemStack[] stacks, String unlocName, int renderPosX, int renderPosZ) {
        this(RenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = stacks;
    }

    public ResearchNode(SpriteQuery query, String unlocName, int renderPosX, int renderPosZ) {
        this(RenderType.TEXTURE_SPRITE, unlocName, renderPosX, renderPosZ);
        this.renderSpriteQuery = query;
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode node) {
        this.connectionsTo.add(node);
        return this;
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode... node) {
        return addSourceConnectionsFrom(Arrays.asList(node));
    }

    public ResearchNode addSourceConnectionsFrom(Collection<ResearchNode> node) {
        this.connectionsTo.addAll(node);
        return this;
    }

    public List<ResearchNode> getConnectionsTo() {
        return connectionsTo;
    }

    public ResearchNode addPage(JournalPage page) {
        pages.add(page);
        return this;
    }

    public boolean canSee(@Nullable PlayerProgress progress) {
        return true;
    }

    public ResearchNode setTextureColorHintWithAlpha(Color textureColorHint) {
        this.textureColorHint = textureColorHint;
        return this;
    }

    public Color getTextureColorHint() {
        return textureColorHint;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public ItemStack getRenderItemStack() {
        return getRenderItemStack(0);
    }

    public ItemStack getRenderItemStack(long tick) {
        return renderItemStacks[((int) ((tick / 40) % renderItemStacks.length))];
    }

    public TextureQuery getBackgroundTexture() {
        return backgroundTextureQuery;
    }

    public SpriteQuery getSpriteTexture() {
        return renderSpriteQuery;
    }

    public List<JournalPage> getPages() {
        return pages;
    }

    public String getUnLocalizedName() {
        return String.format("research.%s.name", unlocName);
    }

    public String getSimpleName() {
        return this.unlocName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResearchNode that = (ResearchNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static enum RenderType {

        ITEMSTACK, TEXTURE_SPRITE

    }

}
