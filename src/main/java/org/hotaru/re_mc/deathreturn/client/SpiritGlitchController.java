package org.hotaru.re_mc.deathreturn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.hotaru.re_mc.Re_mc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Re_mc.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpiritGlitchController {
    private static final List<Block> FAKE_BLOCKS = List.of(
            Blocks.STONE,
            Blocks.COBBLESTONE,
            Blocks.DIRT,
            Blocks.OAK_PLANKS,
            Blocks.SAND,
            Blocks.NETHERRACK,
            Blocks.OBSIDIAN,
            Blocks.BRICKS,
            Blocks.CRAFTING_TABLE,
            Blocks.FURNACE,
            Blocks.TNT,
            Blocks.BOOKSHELF,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.COAL_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.GOLD_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.PRISMARINE,
            Blocks.NETHER_BRICKS
    );
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<GlitchEntry> GLITCHES = new ArrayList<>();
    private static float spirit = 100.0F;
    private static float witchScent;
    private static boolean visible;
    private static int tickCounter;
    private static int lastChunkX = Integer.MIN_VALUE;
    private static int lastChunkZ = Integer.MIN_VALUE;

    private SpiritGlitchController() {
    }

    public static void setSpirit(float value, float scent, boolean enabled) {
        spirit = Math.max(0.0F, Math.min(100.0F, value));
        witchScent = Math.max(0.0F, Math.min(100.0F, scent));
        visible = enabled;
        if (!enabled) {
            GLITCHES.clear();
            lastChunkX = Integer.MIN_VALUE;
            lastChunkZ = Integer.MIN_VALUE;
        } else if (spirit >= 100.0F) {
            GLITCHES.removeIf(entry -> !entry.persistent);
        }
    }

    public static void clear() {
        spirit = 100.0F;
        witchScent = 0.0F;
        visible = false;
        GLITCHES.clear();
        lastChunkX = Integer.MIN_VALUE;
        lastChunkZ = Integer.MIN_VALUE;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) {
            clearGlitchAt(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            clearGlitchAt(event.getPos());
        }
    }

    private static void clearGlitchAt(BlockPos pos) {
        GLITCHES.removeIf(entry -> entry.pos.equals(pos));
    }
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tickCounter++;
        Iterator<GlitchEntry> iterator = GLITCHES.iterator();
        while (iterator.hasNext()) {
            GlitchEntry entry = iterator.next();
            if (!entry.persistent && --entry.ticks <= 0) {
                iterator.remove();
            }
        }

        if (!visible || spirit >= 100.0F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen instanceof ReturnTransitionScreen) {
            return;
        }

        int chunkX = SectionPos.blockToSectionCoord(player.getBlockX());
        int chunkZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
            lastChunkX = chunkX;
            lastChunkZ = chunkZ;
            GLITCHES.clear();
        }

        float severity = Math.max(0.0F, Math.min(1.0F, (100.0F - spirit) / 100.0F + witchScent / 180.0F));
        int interval = Math.max(2, Math.round(15.0F - severity * 12.0F));
        if (tickCounter % interval != 0 || RANDOM.nextFloat() > 0.35F + severity * 0.65F) {
            return;
        }
        int count = 1 + RANDOM.nextInt(2 + Math.round(severity * 5.0F));
        for (int i = 0; i < count && GLITCHES.size() < 48; i++) {
            spawnGlitch(level, player, severity);
        }
    }

    private static void spawnGlitch(ClientLevel level, Player player, float severity) {
        int playerChunkX = SectionPos.blockToSectionCoord(player.getBlockX());
        int playerChunkZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        for (int attempt = 0; attempt < 24; attempt++) {
            int chunkX = playerChunkX + RANDOM.nextInt(3) - 1;
            int chunkZ = playerChunkZ + RANDOM.nextInt(3) - 1;
            BlockPos pos = BlockPos.containing(
                    chunkX * 16 + RANDOM.nextInt(16) + 0.5D,
                    player.getY() + RANDOM.nextDouble() * 8.0D - 4.0D,
                    chunkZ * 16 + RANDOM.nextInt(16) + 0.5D
            );
            if (!level.hasChunkAt(pos) || !isExposed(level, pos)) {
                continue;
            }
            BlockState fakeState = FAKE_BLOCKS.get(RANDOM.nextInt(FAKE_BLOCKS.size())).defaultBlockState();
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer()
                    .getBlockModelShaper()
                    .getBlockModel(fakeState)
                    .getParticleIcon();
            int tint = switch (RANDOM.nextInt(4)) {
                case 0 -> 0xFFFFFF;
                case 1 -> 0xC77CFF;
                case 2 -> 0x5FF2FF;
                default -> 0xFF6BB6;
            };
            boolean persistent = RANDOM.nextFloat() < severity * 0.35F;
            GLITCHES.add(new GlitchEntry(pos.immutable(), sprite, tint, persistent ? -1 : 5 + RANDOM.nextInt(12), persistent));
            return;
        }
    }

    private static boolean isExposed(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || GLITCHES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        RenderType renderType = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
        var bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (GlitchEntry entry : GLITCHES) {
            renderGlitchBlock(consumer, poseStack, level, entry);
        }

        poseStack.popPose();
        bufferSource.endBatch(renderType);
    }

    private static void renderGlitchBlock(VertexConsumer consumer, PoseStack poseStack, ClientLevel level, GlitchEntry entry) {
        BlockPos pos = entry.pos;
        TextureAtlasSprite sprite = entry.sprite;
        if (sprite == null || level.getBlockState(pos).isAir()) {
            return;
        }
        int alpha = 215 + RANDOM.nextInt(41);
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                renderFace(consumer, poseStack, pos, direction, sprite, entry.tint, alpha);
            }
        }
    }

    private static void renderFace(VertexConsumer consumer, PoseStack poseStack, BlockPos pos, Direction direction, TextureAtlasSprite sprite, int tint, int alpha) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float e = 0.004F;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        int r = (tint >> 16) & 0xFF;
        int g = (tint >> 8) & 0xFF;
        int b = tint & 0xFF;
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();

        switch (direction) {
            case UP -> {
                vertex(consumer, matrix, normal, x, y + 1.0F + e, z, u0, v1, r, g, b, alpha, 0, 1, 0);
                vertex(consumer, matrix, normal, x, y + 1.0F + e, z + 1.0F, u0, v0, r, g, b, alpha, 0, 1, 0);
                vertex(consumer, matrix, normal, x + 1.0F, y + 1.0F + e, z + 1.0F, u1, v0, r, g, b, alpha, 0, 1, 0);
                vertex(consumer, matrix, normal, x + 1.0F, y + 1.0F + e, z, u1, v1, r, g, b, alpha, 0, 1, 0);
            }
            case DOWN -> {
                vertex(consumer, matrix, normal, x, y - e, z, u0, v0, r, g, b, alpha, 0, -1, 0);
                vertex(consumer, matrix, normal, x + 1.0F, y - e, z, u1, v0, r, g, b, alpha, 0, -1, 0);
                vertex(consumer, matrix, normal, x + 1.0F, y - e, z + 1.0F, u1, v1, r, g, b, alpha, 0, -1, 0);
                vertex(consumer, matrix, normal, x, y - e, z + 1.0F, u0, v1, r, g, b, alpha, 0, -1, 0);
            }
            case NORTH -> {
                vertex(consumer, matrix, normal, x + 1.0F, y, z - e, u1, v1, r, g, b, alpha, 0, 0, -1);
                vertex(consumer, matrix, normal, x + 1.0F, y + 1.0F, z - e, u1, v0, r, g, b, alpha, 0, 0, -1);
                vertex(consumer, matrix, normal, x, y + 1.0F, z - e, u0, v0, r, g, b, alpha, 0, 0, -1);
                vertex(consumer, matrix, normal, x, y, z - e, u0, v1, r, g, b, alpha, 0, 0, -1);
            }
            case SOUTH -> {
                vertex(consumer, matrix, normal, x, y, z + 1.0F + e, u0, v1, r, g, b, alpha, 0, 0, 1);
                vertex(consumer, matrix, normal, x, y + 1.0F, z + 1.0F + e, u0, v0, r, g, b, alpha, 0, 0, 1);
                vertex(consumer, matrix, normal, x + 1.0F, y + 1.0F, z + 1.0F + e, u1, v0, r, g, b, alpha, 0, 0, 1);
                vertex(consumer, matrix, normal, x + 1.0F, y, z + 1.0F + e, u1, v1, r, g, b, alpha, 0, 0, 1);
            }
            case WEST -> {
                vertex(consumer, matrix, normal, x - e, y, z, u0, v1, r, g, b, alpha, -1, 0, 0);
                vertex(consumer, matrix, normal, x - e, y + 1.0F, z, u0, v0, r, g, b, alpha, -1, 0, 0);
                vertex(consumer, matrix, normal, x - e, y + 1.0F, z + 1.0F, u1, v0, r, g, b, alpha, -1, 0, 0);
                vertex(consumer, matrix, normal, x - e, y, z + 1.0F, u1, v1, r, g, b, alpha, -1, 0, 0);
            }
            case EAST -> {
                vertex(consumer, matrix, normal, x + 1.0F + e, y, z + 1.0F, u0, v1, r, g, b, alpha, 1, 0, 0);
                vertex(consumer, matrix, normal, x + 1.0F + e, y + 1.0F, z + 1.0F, u0, v0, r, g, b, alpha, 1, 0, 0);
                vertex(consumer, matrix, normal, x + 1.0F + e, y + 1.0F, z, u1, v0, r, g, b, alpha, 1, 0, 0);
                vertex(consumer, matrix, normal, x + 1.0F + e, y, z, u1, v1, r, g, b, alpha, 1, 0, 0);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, org.joml.Matrix4f matrix, org.joml.Matrix3f normal, float x, float y, float z, float u, float v, int r, int g, int b, int a, float nx, float ny, float nz) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    private static final class GlitchEntry {
        private final BlockPos pos;
        private final TextureAtlasSprite sprite;
        private final int tint;
        private final boolean persistent;
        private int ticks;

        private GlitchEntry(BlockPos pos, TextureAtlasSprite sprite, int tint, int ticks, boolean persistent) {
            this.pos = pos;
            this.sprite = sprite;
            this.tint = tint;
            this.ticks = ticks;
            this.persistent = persistent;
        }
    }
}
