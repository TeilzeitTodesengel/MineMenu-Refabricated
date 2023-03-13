package com.github.clockwerkkaiser.minemenufabric.client.util;

import com.github.clockwerkkaiser.minemenufabric.client.screen.MineMenuSettingsScreen;
import com.mojang.authlib.GameProfile;
import me.shedaniel.math.Color;
import com.github.clockwerkkaiser.minemenufabric.client.MineMenuFabricClient;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.HashMap;
import java.util.Map;

public class RandomUtil {

    public static Color getColor(String inp) {
        long colorLong = Long.decode(inp);
        float f = (float) (colorLong >> 24 & 0xff) / 255F;
        float f1 = (float) (colorLong >> 16 & 0xff) / 255F;
        float f2 = (float) (colorLong >> 8 & 0xff) / 255F;
        float f3 = (float) (colorLong & 0xff) / 255F;
        return Color.ofRGBA(f, f1, f2, f3);
    }

    @SuppressWarnings("ConstantConditions")
    public static ItemStack iconify(String iconItem, boolean enchanted, String skullowner, int customModelData) {
        ItemStack out;
        try {
            out = itemStackFromString(iconItem);
            NbtCompound customModelTag = out.getOrCreateNbt();
            customModelTag.putInt("CustomModelData", customModelData);
            try {
                if (enchanted) {
                    Map<Enchantment, Integer> e = new HashMap<>();
                    e.put(Enchantment.byRawId(1), 1);
                    EnchantmentHelper.set(e, out);
                }

                if (!skullowner.isEmpty() && isSkullItem(out)) {
                    ItemStack finalOut = out;
                    Thread nbTater = new Thread(() -> {
                        NbtCompound skullTag = finalOut.getOrCreateNbt();
                        GameProfile gameProfile = new GameProfile(null, skullowner);
                        SkullBlockEntity.loadProperties(gameProfile, RandomUtil::setGameProfile);
                        skullTag.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfile));
                        MineMenuFabricClient.playerHeadCache.putIfAbsent(skullowner, finalOut);
                    });
                    nbTater.start();
                    out = null;

                } else out.removeSubNbt("SkullOwner");

            } catch (Exception e) {e.printStackTrace();}
        } catch (InvalidIdentifierException e) {
            out = new ItemStack(Items.AIR);
        }
        return out;
    }

    public static ItemStack itemStackFromString(String itemStack) {
        return Registries.ITEM.get(new Identifier(itemStack)).getDefaultStack();
    }

    public static boolean isSkullItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((BlockItem)
                stack.getItem()).getBlock() instanceof AbstractSkullBlock;
    }


    public static void openConfigScreen(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            client.setScreenAndRender(new MineMenuSettingsScreen(parent, false));
        } catch (NullPointerException e) {
            e.printStackTrace();
            client.setScreenAndRender(null);
            assert client.player != null;
            client.player.sendMessage(Text.translatable("minemenu.error.config"), false);
        }
    }

    private static void setGameProfile(GameProfile gameProfile) {
    }
}
