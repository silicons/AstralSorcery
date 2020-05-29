/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2020
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.journal.progression.ScreenJournalProgressionRenderer;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.client.util.draw.TextureHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalProgression
 * Created by HellFirePvP
 * Date: 03.08.2019 / 16:48
 */
public class ScreenJournalProgression extends ScreenJournal {

    private static ScreenJournalProgression currentInstance = null;
    private boolean expectReinit = false;
    private boolean rescaleAndRefresh = true;

    private ScreenTextEntry searchTextEntry = new ScreenTextEntry();

    //Defines how many search results are on the left/right page
    private static final int searchEntriesLeft = 15;
    private static final int searchEntriesRight = 14;
    private static final int searchEntryDrawWidth = 170; //How long search result strings may be at most

    private int searchPageOffset = 0; //* 2 = left page.
    private Rectangle searchPrevRct, searchNextRct; //Frame-draw information on clickable rectangles
    private ResearchNode searchHoverNode = null; //The currently hovered research node
    private List<ResearchNode> searchResult = new ArrayList<>(); //The raw, sorted search result
    private Map<Integer, List<ResearchNode>> searchResultPageIndex = Maps.newHashMap(); //page-indexed sorted result

    private static ScreenJournalProgressionRenderer progressionRenderer;

    private ScreenJournalProgression() {
        super(new TranslationTextComponent("screen.astralsorcery.tome.progression"), 10);

        this.searchTextEntry.setChangeCallback(this::onSearchTextInput);
    }

    public static ScreenJournalProgression getJournalInstance() {
        if (currentInstance != null) {
            return currentInstance;
        }
        return new ScreenJournalProgression();
    }

    public static ScreenJournal getOpenJournalInstance() {
        ScreenJournal gui = ScreenJournalPages.getClearOpenGuiInstance();
        if (gui == null) {
            gui = getJournalInstance();
        }
        return gui;
    }

    public void expectReInit() {
        this.expectReinit = true;
    }

    public void preventRefresh() {
        this.rescaleAndRefresh = false;
    }

    public static void resetJournal() {
        currentInstance = null;
        ScreenJournalPages.getClearOpenGuiInstance();
    }

    @Override
    public void removed() {
        super.removed();
        rescaleAndRefresh = false;
    }

    @Override
    protected void init() {
        super.init();

        if (expectReinit) {
            expectReinit = false;
            return; //We ASSUME, that the state is valid.
        }

        if (currentInstance == null || progressionRenderer == null) {
            currentInstance = this;
            progressionRenderer = new ScreenJournalProgressionRenderer(currentInstance, guiHeight - 10, guiWidth - 10);
            progressionRenderer.centerMouse();
        }

        progressionRenderer.updateOffset(guiLeft + 10, guiTop + 10);
        progressionRenderer.setBox(10, 10, guiWidth - 10, guiHeight - 10);
        //progressionRenderer.resetOverlayText();

        if (rescaleAndRefresh) {
            progressionRenderer.resetZoom();
            progressionRenderer.unfocus();
            progressionRenderer.refreshSize();
            progressionRenderer.updateMouseState();
        } else {
            rescaleAndRefresh = true;
        }
    }

    private boolean inProgressView() {
        return this.searchTextEntry.getText().length() < 3;
    }

    @Override
    public void render(int mouseX, int mouseY, float pTicks) {
        GlStateManager.enableBlend();
        super.render(mouseX, mouseY, pTicks);

        this.searchPrevRct = null;
        this.searchNextRct = null;
        this.searchHoverNode = null;

        if (this.inProgressView()) {
            this.searchPageOffset = 0; //Reset page offset

            this.renderProgressView(mouseX, mouseY, pTicks);
        } else {
            this.renderSearchView(mouseX, mouseY, pTicks);
        }
    }

    private void renderSearchView(int mouseX, int mouseY, float pTicks) {
        this.drawDefault(TexturesAS.TEX_GUI_BOOK_BLANK, mouseX, mouseY);

        this.drawSearchResults(mouseX, mouseY, pTicks);
        this.drawSearchBox();

        this.blitOffset += 150;
        this.drawSearchPageNavArrows(mouseX, mouseY, pTicks);
        this.blitOffset -= 150;
    }

    private void renderProgressView(int mouseX, int mouseY, float pTicks) {
        double guiFactor = Minecraft.getInstance().mainWindow.getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(MathHelper.floor((guiLeft + 27) * guiFactor), MathHelper.floor((guiTop + 27) * guiFactor),
                MathHelper.floor((guiWidth - 54) * guiFactor), MathHelper.floor((guiHeight - 54) * guiFactor));
        progressionRenderer.drawProgressionPart(this.blitOffset, mouseX, mouseY);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableDepthTest();
        drawDefault(TexturesAS.TEX_GUI_BOOK_FRAME_FULL, mouseX, mouseY);
        GlStateManager.enableDepthTest();

        this.drawSearchBox();

        this.blitOffset += 150;
        drawMouseHighlight(this.blitOffset, mouseX, mouseY);
        this.blitOffset -= 150;
    }

    private void drawSearchResults(int mouseX, int mouseY, float pTicks) {
        GlStateManager.color4f(0.875F, 0.875F, 0.875F, 1F);
        GlStateManager.disableDepthTest();
        Blending.DEFAULT.applyStateManager();
        TextureHelper.bindBlockAtlas();

        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        int lineHeight = 12;
        int offsetX = this.getGuiLeft() + 35;
        int offsetY = this.getGuiTop() + 26;

        double effectPart = (Math.sin(Math.toRadians(((ClientScheduler.getClientTick()) * 5D) % 360D)) + 1D) / 2D;
        int alpha = Math.round((0.45F + 0.1F * ((float) effectPart)) * 255F);
        int grayScale = Math.round((0.7F + 0.2F * ((float) effectPart)) * 255F);
        Color boxColor = new Color(grayScale, grayScale, grayScale, alpha);

        List<ResearchNode> entries = this.searchResultPageIndex.getOrDefault(this.searchPageOffset, new ArrayList<>());
        for (ResearchNode node : entries) {
            int startOffsetY = offsetY;

            List<String> nodeTitleLines = fr.listFormattedStringToWidth(node.getName().getFormattedText(), searchEntryDrawWidth);
            int maxLength = 0;

            for (String line : nodeTitleLines) {
                int length = fr.drawString(line, offsetX, offsetY, 0x00D0D0D0) - offsetX;
                if (length > maxLength) {
                    maxLength = length;
                }
                offsetY += lineHeight;
            }

            if (this.searchHoverNode == null) {
                Rectangle rctDrawn = new Rectangle(offsetX - 2, startOffsetY - 2, maxLength + 4, offsetY - startOffsetY);
                if (rctDrawn.contains(mouseX, mouseY)) {
                    fill(rctDrawn.x, rctDrawn.y, rctDrawn.x + rctDrawn.width, rctDrawn.y + rctDrawn.height, boxColor.getRGB());
                    GlStateManager.enableBlend();
                    this.searchHoverNode = node;
                }
            }
        }

        offsetX = this.getGuiLeft() + 225;
        offsetY = this.getGuiTop() + 39;
        entries = this.searchResultPageIndex.getOrDefault(this.searchPageOffset + 1, new ArrayList<>());
        for (ResearchNode node : entries) {
            int startOffsetY = offsetY;

            List<String> nodeTitleLines = fr.listFormattedStringToWidth(node.getName().getFormattedText(), searchEntryDrawWidth);
            int maxLength = 0;

            for (String line : nodeTitleLines) {
                int length = fr.drawString(line, offsetX, offsetY, 0x00D0D0D0) - offsetX;
                if (length > maxLength) {
                    maxLength = length;
                }
                offsetY += lineHeight;
            }

            if (this.searchHoverNode == null) {
                Rectangle rctDrawn = new Rectangle(offsetX - 2, startOffsetY - 2, maxLength + 4, offsetY - startOffsetY);
                if (rctDrawn.contains(mouseX, mouseY)) {
                    fill(rctDrawn.x, rctDrawn.y, rctDrawn.x + rctDrawn.width, rctDrawn.y + rctDrawn.height, boxColor.getRGB());
                    GlStateManager.enableBlend();
                    this.searchHoverNode = node;
                }
            }
        }

        GlStateManager.enableDepthTest();
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        TextureHelper.refreshTextureBind();
    }

    private void drawMouseHighlight(float zLevel, int mouseX, int mouseY) {
        progressionRenderer.drawMouseHighlight(zLevel, mouseX, mouseY);
    }

    private void drawSearchBox() {
        GlStateManager.disableAlphaTest();
        GlStateManager.disableDepthTest();
        GlStateManager.pushMatrix();
        GlStateManager.translated(guiLeft + 300, guiTop + 16, 0);
        TexturesAS.TEX_GUI_TEXT_FIELD.bindTexture();
        RenderingGuiUtils.drawTexturedRectAtCurrentPos(88.5F, 15, this.blitOffset);

        String text = this.searchTextEntry.getText();

        int length = font.getStringWidth(text);
        boolean addDots = length > 75;
        while (length > 75) {
            text = text.substring(1);
            length = font.getStringWidth("..." + text);
        }
        if (addDots) {
            text = "..." + text;
        }

        if ((ClientScheduler.getClientTick() % 20) > 10) {
            text += "_";
        }

        GlStateManager.translated(4, 4, 0);
        RenderingDrawUtils.renderStringAtCurrentPos(font, text, 0xCCCCCC);

        GlStateManager.popMatrix();
        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
    }

    private void drawSearchPageNavArrows(int mouseX, int mouseY, float pTicks) {
        GlStateManager.disableDepthTest();
        GlStateManager.color4f(1F, 1F, 1F, 0.8F);

        if (this.searchPageOffset > 0) {
            int width = 30;
            int height = 15;
            this.searchPrevRct = new Rectangle(guiLeft + 25, guiTop + 220, width, height);
            GlStateManager.pushMatrix();
            GlStateManager.translated(this.searchPrevRct.getX() + (width / 2), this.searchPrevRct.getY() + (height / 2), 0);
            float uFrom = 0F, vFrom = 0.5F;
            if (this.searchPrevRct.contains(mouseX, mouseY)) {
                uFrom = 0.5F;
                GlStateManager.scaled(1.1, 1.1, 1.1);
            } else {
                double t = ClientScheduler.getClientTick() + pTicks;
                float sin = ((float) Math.sin(t / 4F)) / 32F + 1F;
                GlStateManager.scaled(sin, sin, sin);
            }
            GlStateManager.color4f(1F, 1F, 1F, 0.8F);
            GlStateManager.translated(-(width / 2), -(height / 2), 0);
            TexturesAS.TEX_GUI_BOOK_ARROWS.bindTexture();
            RenderingGuiUtils.drawTexturedRectAtCurrentPos(width, height, this.blitOffset, uFrom, vFrom, 0.5F, 0.5F);
            GlStateManager.popMatrix();
        }
        int nextDoublePageIndex = (this.searchPageOffset * 2) + 2;
        if (this.searchResultPageIndex.size() >= nextDoublePageIndex + 1) {
            int width = 30;
            int height = 15;
            this.searchNextRct = new Rectangle(guiLeft + 367, guiTop + 220, width, height);
            GlStateManager.pushMatrix();
            GlStateManager.translated(this.searchNextRct.getX() + (width / 2), this.searchNextRct.getY() + (height / 2), 0);
            float uFrom = 0F, vFrom = 0F;
            if (this.searchNextRct.contains(mouseX, mouseY)) {
                uFrom = 0.5F;
                GlStateManager.scaled(1.1, 1.1, 1.1);
            } else {
                double t = ClientScheduler.getClientTick() + pTicks;
                float sin = ((float) Math.sin(t / 4F)) / 32F + 1F;
                GlStateManager.scaled(sin, sin, sin);
            }
            GlStateManager.translated(-(width / 2), -(height / 2), 0);
            TexturesAS.TEX_GUI_BOOK_ARROWS.bindTexture();
            RenderingGuiUtils.drawTexturedRectAtCurrentPos(width, height, this.blitOffset, uFrom, vFrom, 0.5F, 0.5F);
            GlStateManager.popMatrix();
        }
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        GlStateManager.enableDepthTest();
    }

    private void onSearchTextInput() {
        if (!this.inProgressView() && this.isCurrentlyDragging()) {
            this.stopDragging(-1, -1);

            progressionRenderer.applyMovedMouseOffset();
        }
        PlayerProgress prog = ResearchHelper.getClientProgress();

        this.searchResult.clear();
        this.searchResultPageIndex.clear();
        String searchText = this.searchTextEntry.getText().toLowerCase();
        for (ResearchProgression research : ResearchProgression.values()) {
            if (!prog.getResearchProgression().contains(research)) {
                continue;
            }

            for (ResearchNode node : research.getResearchNodes()) {
                String nodeName = node.getName().getFormattedText().toLowerCase();
                if (nodeName.contains(searchText) && !this.searchResult.contains(node)) {
                    this.searchResult.add(node);
                }
            }
        }

        this.searchResult.sort(Comparator.comparing(node -> node.getName().getFormattedText()));

        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        int addedPages = 0;
        int pageIndex = 0;
        while (addedPages < this.searchResult.size()) {
            List<ResearchNode> page = this.searchResultPageIndex.computeIfAbsent(pageIndex, index -> new ArrayList<>());
            int remainingLines = (pageIndex % 2 == 0 ? searchEntriesLeft : searchEntriesRight) - page.size();

            ResearchNode toAddNode = this.searchResult.get(addedPages);
            int nodeLines = fr.listFormattedStringToWidth(toAddNode.getName().getFormattedText(), searchEntryDrawWidth).size();

            if (remainingLines < nodeLines) {
                pageIndex++; //Add this node to the next page.
                continue;
            }

            page.add(toAddNode);
            addedPages++;
        }

        //Shift the pages further down in case the result gets narrower
        while (this.searchPageOffset > 0 && this.searchPageOffset >= this.searchResultPageIndex.size()) {
            this.searchPageOffset--;
        }
    }

    @Override
    protected void mouseDragTick(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY, double mouseOffsetX, double mouseOffsetY) {
        super.mouseDragTick(mouseX, mouseY, mouseDiffX, mouseDiffY, mouseOffsetX, mouseOffsetY);

        if (this.inProgressView()) {
            progressionRenderer.moveMouse(mouseDiffX, mouseDiffY);
        }
    }

    @Override
    protected void mouseDragStop(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY) {
        super.mouseDragStop(mouseX, mouseY, mouseDiffX, mouseDiffY);

        if (this.inProgressView()) {
            progressionRenderer.applyMovedMouseOffset();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (this.inProgressView()) {
            if (scroll < 0) {
                progressionRenderer.handleZoomOut();
                return true;
            }
            if (scroll > 0)  {
                progressionRenderer.handleZoomIn(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (mouseButton != 0) {
            return false;
        }
        if (handleBookmarkClick(mouseX, mouseY)) {
            return true;
        }
        if (this.inProgressView()) {
            return progressionRenderer.propagateClick(mouseX, mouseY);
        } else {
            if (this.searchPrevRct != null && this.searchPrevRct.contains(mouseX, mouseY)) {
                this.searchPageOffset -= 1;
                SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
                return true;
            }
            if (this.searchNextRct != null && this.searchNextRct.contains(mouseX, mouseY)) {
                this.searchPageOffset += 1;
                SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
                return true;
            }
            if (this.searchHoverNode != null) {
                this.searchTextEntry.setText("");
                Minecraft.getInstance().displayGuiScreen(new ScreenJournalPages(this, this.searchHoverNode));
                SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.searchTextEntry.keyTyped(key)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (this.searchTextEntry.charTyped(charCode)) {
            return true;
        }
        return super.charTyped(charCode, keyModifiers);
    }
}
