package tectech.thing.metaTileEntity.hatch;

import static gregtech.api.enums.GTValues.V;
import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;

import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import tectech.mechanics.pipe.IConnectsToEnergyTunnel;
import tectech.thing.metaTileEntity.Textures;
import tectech.util.CommonValues;

/**
 * Created by danie_000 on 16.12.2016.
 */
public class MTEHatchEnergyTunnel extends MTEHatchEnergyMulti implements IConnectsToEnergyTunnel {

    public MTEHatchEnergyTunnel(int aID, String aName, String aNameRegional, int aTier, int aAmp) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            0,
            new String[] { CommonValues.TEC_MARK_GENERAL,
                translateToLocal("gt.blockmachines.hatch.energytunnel.desc.0"),
                translateToLocal("gt.blockmachines.hatch.screwdrivertooltip"),
                translateToLocal("gt.blockmachines.hatch.energytunnel.desc.1") + ": "
                    + EnumChatFormatting.YELLOW
                    + GTUtility.formatNumbers(aAmp * V[aTier])
                    + EnumChatFormatting.RESET
                    + " EU/t", },
            aAmp); // Energy injecting terminal for Multiblocks
    }

    public MTEHatchEnergyTunnel(String aName, int aTier, int aAmp, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aAmp, aDescription, aTextures);
    }

    @Override
    public int getHatchType() {
        return 2;
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, Textures.OVERLAYS_ENERGY_IN_LASER_TT[mTier] };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, Textures.OVERLAYS_ENERGY_IN_LASER_TT[mTier] };
    }

    @Override
    public long getMinimumStoredEU() {
        return V[mTier];
    }

    @Override
    public long maxEUStore() {
        return V[mTier] * 24L * Amperes;
    }

    @Override
    public boolean isEnetInput() {
        return false;
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.LASER;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEHatchEnergyTunnel(mName, mTier, Amperes, mDescriptionArray, mTextures);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (Amperes != maxAmperes) {
            aNBT.setInteger("amperes", Amperes);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        int savedAmperes = aNBT.getInteger("amperes");
        if (savedAmperes != 0) {
            Amperes = savedAmperes;
        }
    }

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {
        openGui(aPlayer);
        super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, aTool);
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        final int x = getGUIWidth() / 2 - 37;
        final int y = getGUIHeight() / 5 - 7;
        builder.widget(
            TextWidget.localised("GT5U.machines.laser_hatch.amperage")
                .setPos(x, y)
                .setSize(74, 14))
            .widget(
                new NumericWidget().setSetter(val -> Amperes = (int) val)
                    .setGetter(() -> Amperes)
                    .setBounds(1, maxAmperes)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(x, y + 16)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));
    }

    @Override
    public boolean canConnect(ForgeDirection side) {
        return isInputFacing(side);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide()) {
            byte Tick = (byte) (aTick % 20);
            if (CommonValues.TRANSFER_AT == Tick) {
                if (aBaseMetaTileEntity.getStoredEU() > 0) {
                    setEUVar(aBaseMetaTileEntity.getStoredEU() - Amperes);
                    if (aBaseMetaTileEntity.getStoredEU() < 0) {
                        setEUVar(0);
                    }
                }
            }
        }
    }
}
