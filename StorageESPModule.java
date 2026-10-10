package com.zenya.module.render;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.module.ModuleManager;
import com.zenya.render.WorldRenderer;
import com.zenya.setting.Setting;
import com.zenya.setting.StorageSelectionSetting;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

public final class StorageESPModule extends Module {
   private static final double STORAGE_LAYER_INSET = 0.08;
   private static final float TRACER_THICKNESS_SCALE = 1.2F;
   private static final String CHEST = "chest";
   private static final String TRAPPED_CHEST = "trapped_chest";
   private static final String ENDER_CHEST = "ender_chest";
   private static final String SPAWNER = "spawner";
   private static final String SHULKER_BOX = "shulker_box";
   private static final String FURNACE = "furnace";
   private static final String BARREL = "barrel";
   private static final String DISPENSER = "dispenser";
   private static final String HOPPER = "hopper";
   private static final String PISTON = "piston";
   private static final String STICKY_PISTON = "sticky_piston";
   private static final String CRAFTER = "crafter";
   private static final String SMOKER = "smoker";
   private static final String BLAST_FURNACE = "blast_furnace";
   private static final Color CHEST_COLOR = rgb(156, 91, 0);
   private static final Color TRAPPED_COLOR = rgb(200, 91, 0);
   private static final Color ENDER_COLOR = rgb(117, 0, 255);
   private static final Color SPAWNER_COLOR = rgb(255, 52, 52);
   private static final Color SHULKER_COLOR = rgb(134, 0, 158);
   private static final Color FURNACE_COLOR = rgb(125, 125, 125);
   private static final Color BARREL_COLOR = rgb(255, 140, 140);
   private static final Color DISPENSER_COLOR = rgb(100, 100, 100);
   private static final Color HOPPER_COLOR = rgb(144, 238, 144);
   private static final Color PISTON_COLOR = rgb(50, 205, 50);
   private static final Color PISTON_STATIONARY_COLOR = rgb(173, 255, 47);
   private static final Color CRAFTER_COLOR = rgb(255, 165, 0);
   private static final Color SMOKER_COLOR = rgb(100, 100, 100);
   private static final Color BLAST_FURNACE_COLOR = rgb(80, 80, 80);
   private static final int SCAN_INTERVAL_TICKS = 10;
   private final Setting<Integer> espRendering = new Setting<>("ESP Rendering", 64, 16, 512);
   private final Setting<Integer> alpha = new Setting<>("Fill Alpha", 100, 0, 255);
   private final Setting<Integer> tracerAlpha = new Setting<>("Tracer Alpha", 255, 0, 255);
   private final Setting<Boolean> tracers = new Setting<>("Tracers", true);
   private final Setting<Boolean> bodyTracers = new Setting<>("Body Tracers", false);
   private final Setting<Double> tracerWidth = new Setting<>("Tracer Weight", 1.25, 0.1, 5.0);
   private final Setting<Boolean> fill = new Setting<>("Fill", true);
   private final StorageSelectionSetting storageBlocks = StorageSelectionSetting.createDefault("Storage Blocks");
   private final ArrayList<StorageESPModule.RenderBox> renderBoxes = new ArrayList<>(256);
   private final ArrayList<StorageESPModule.Tracer> renderTracers = new ArrayList<>(256);
   private final Map<ChunkPos, List<BlockPos>> cachedSpecialBlocks = new ConcurrentHashMap<>();
   private final Set<ChunkPos> scannedChunks = ConcurrentHashMap.newKeySet();
   private final ArrayDeque<ChunkPos> scanQueue = new ArrayDeque<>();
   private BlockESPModule blockEsp;
   private int scanSelectionHash;
   private int scanTick;
   private int currentScanX = 0;
   private int currentScanZ = 0;

   public StorageESPModule() {
      super("Storage ESP", Category.RENDER);
      this.setDescription("Renders storage blocks through walls using their real shapes.");
      LinkedHashSet<String> defaultSelected = new LinkedHashSet<>();
      defaultSelected.add("minecraft:chest");
      defaultSelected.add("minecraft:trapped_chest");
      defaultSelected.add("minecraft:ender_chest");
      defaultSelected.add("minecraft:spawner");
      defaultSelected.add("minecraft:trial_spawner");
      defaultSelected.add("minecraft:shulker_box");
      defaultSelected.add("minecraft:furnace");
      defaultSelected.add("minecraft:barrel");
      defaultSelected.add("minecraft:hopper");
      defaultSelected.add("minecraft:piston");
      defaultSelected.add("minecraft:sticky_piston");
      defaultSelected.add("minecraft:crafter");
      defaultSelected.add("minecraft:smoker");
      defaultSelected.add("minecraft:blast_furnace");
      this.storageBlocks.setValue(defaultSelected);
      this.addSetting(this.espRendering);
      this.addSetting(this.alpha);
      this.addSetting(this.tracerAlpha);
      this.addSetting(this.tracers);
      this.addSetting(this.bodyTracers);
      this.addSetting(this.tracerWidth);
      this.addSetting(this.fill);
      this.addSetting(this.storageBlocks);
   }

   private static Color rgb(int red, int green, int blue) {
      return new Color(red, green, blue, 255);
   }

   private static Color withAlpha(Color base, int alpha) {
      int clamped = Math.max(0, Math.min(255, alpha));
      return new Color(base.getRed(), base.getGreen(), base.getBlue(), clamped);
   }

   public boolean isRendering(Block block) {
      return this.isEnabled() && block != null ? this.shouldHighlight(Registries.BLOCK.getId(block).toString()) : false;
   }

   @Override
   public void onWorldChange() {
      this.renderBoxes.clear();
      this.renderTracers.clear();
      this.cachedSpecialBlocks.clear();
      this.scannedChunks.clear();
      this.scanQueue.clear();
      this.scanSelectionHash = 0;
      this.scanTick = 0;
   }

   @Override
   public void onTick() {
      if (mc.world != null && mc.player != null) {
         if (++this.scanTick >= 10) {
            this.scanTick = 0;
            int currentSelectionHash = this.storageBlocks.getSelected().hashCode();
            if (currentSelectionHash != this.scanSelectionHash) {
               this.scanSelectionHash = currentSelectionHash;
               this.scannedChunks.clear();
               this.cachedSpecialBlocks.clear();
            }

            int viewDistance = this.espRendering.getValue() / 16;
            ChunkPos center = mc.player.getChunkPos();
            int startX = center.x - viewDistance;
            int endX = center.x + viewDistance;
            int startZ = center.z - viewDistance;
            int endZ = center.z + viewDistance;

            for (int i = 0; i < 2; i++) {
               if (this.currentScanX < startX || this.currentScanX > endX) {
                  this.currentScanX = startX;
               }

               if (this.currentScanZ < startZ || this.currentScanZ > endZ) {
                  this.currentScanZ = startZ;
               }

               ChunkPos cp = new ChunkPos(this.currentScanX, this.currentScanZ);
               WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cp.x, cp.z, false);
               if (chunk != null && this.scannedChunks.add(cp)) {
                  this.scanChunkForNormalBlocks(chunk, cp);
               }

               this.currentScanX++;
               if (this.currentScanX > endX) {
                  this.currentScanX = startX;
                  this.currentScanZ++;
                  if (this.currentScanZ > endZ) {
                     this.currentScanZ = startZ;
                  }
               }
            }

            int maxDist = viewDistance + 2;
            this.cachedSpecialBlocks.keySet().removeIf(cpx -> Math.abs(cpx.x - center.x) > maxDist || Math.abs(cpx.z - center.z) > maxDist);
            this.scannedChunks.removeIf(cpx -> Math.abs(cpx.x - center.x) > maxDist || Math.abs(cpx.z - center.z) > maxDist);
         }
      }
   }

   private void scanChunkForNormalBlocks(WorldChunk chunk, ChunkPos cp) {
      Set<Block> selectedTargets = new HashSet<>(12);
      if (this.storageBlocks.isSelected("minecraft:note_block")) {
         selectedTargets.add(Blocks.NOTE_BLOCK);
      }

      if (this.storageBlocks.isSelected("minecraft:diamond_block")) {
         selectedTargets.add(Blocks.DIAMOND_BLOCK);
      }

      if (this.storageBlocks.isSelected("minecraft:beacon")) {
         selectedTargets.add(Blocks.BEACON);
      }

      if (this.storageBlocks.isSelected("minecraft:observer")) {
         selectedTargets.add(Blocks.OBSERVER);
      }

      if (this.storageBlocks.isSelected("minecraft:repeater")) {
         selectedTargets.add(Blocks.REPEATER);
      }

      if (this.storageBlocks.isSelected("minecraft:redstone_wire")) {
         selectedTargets.add(Blocks.REDSTONE_WIRE);
      }

      if (this.storageBlocks.isSelected("minecraft:redstone_block")) {
         selectedTargets.add(Blocks.REDSTONE_BLOCK);
      }

      if (this.storageBlocks.isSelected("minecraft:piston")) {
         selectedTargets.add(Blocks.PISTON);
      }

      if (this.storageBlocks.isSelected("minecraft:sticky_piston")) {
         selectedTargets.add(Blocks.STICKY_PISTON);
      }

      if (selectedTargets.isEmpty()) {
         this.cachedSpecialBlocks.remove(cp);
      } else {
         List<BlockPos> found = new ArrayList<>();
         Mutable m = new Mutable();
         int startX = cp.getStartX();
         int startZ = cp.getStartZ();
         int bottom = mc.world.getBottomY();
         int top = mc.world.getTopYInclusive();

         for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
               for (int y = bottom; y <= top; y++) {
                  m.set(startX + lx, y, startZ + lz);
                  BlockState state = chunk.getBlockState(m);
                  Block b = state.getBlock();
                  if (selectedTargets.contains(b)) {
                     found.add(m.toImmutable());
                  }
               }
            }
         }

         if (found.isEmpty()) {
            this.cachedSpecialBlocks.remove(cp);
         } else {
            this.cachedSpecialBlocks.put(cp, found);
         }
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         if (this.fill.getValue() || this.tracers.getValue()) {
            Camera camera = WorldRenderer.getCamera();
            if (camera != null) {
               Vec3d cameraPos = WorldRenderer.getCameraPos(camera);
               this.renderBoxes.clear();
               this.renderTracers.clear();
               int viewDistance = this.espRendering.getValue() / 16;
               double maxDistance = (double)viewDistance * 16.0;
               double maxDistanceSq = maxDistance * maxDistance;
               ChunkPos center = mc.player.getChunkPos();
               HashSet<Long> seen = new HashSet<>(256);
               ChunkManager chunkManager = mc.world.getChunkManager();

               for (int cx = center.x - viewDistance; cx <= center.x + viewDistance; cx++) {
                  for (int cz = center.z - viewDistance; cz <= center.z + viewDistance; cz++) {
                     ChunkPos cp = new ChunkPos(cx, cz);
                     WorldChunk chunk = chunkManager.getWorldChunk(cx, cz, false);
                     if (chunk != null) {
                        this.collectChunk(chunk, cameraPos, maxDistanceSq, seen);
                     }

                     List<BlockPos> specials = this.cachedSpecialBlocks.get(cp);
                     if (specials != null) {
                        for (BlockPos pos : specials) {
                           long key = pos.asLong();
                           if (seen.add(key)) {
                              double centerX = (double)pos.getX() + 0.5 - cameraPos.x;
                              double centerY = (double)pos.getY() + 0.5 - cameraPos.y;
                              double centerZ = (double)pos.getZ() + 0.5 - cameraPos.z;
                              if (!(centerX * centerX + centerY * centerY + centerZ * centerZ > maxDistanceSq)) {
                                 BlockState state = mc.world.getBlockState(pos);
                                 Block block = state.getBlock();
                                 boolean blockEspOwnsFill = this.isClaimedByBlockEsp(block);
                                 String type = Registries.BLOCK.getId(block).toString();
                                 if (this.shouldHighlight(type)) {
                                    Color base = this.baseColor(type);
                                    if ((type.equals("minecraft:piston") || type.equals("minecraft:sticky_piston"))
                                       && state.contains(Properties.EXTENDED)
                                       && !(Boolean)state.get(Properties.EXTENDED)) {
                                       base = PISTON_STATIONARY_COLOR;
                                    }

                                    Color fillColor = withAlpha(base, this.alpha.getValue());
                                    Color outlineColor = withAlpha(base, 0);
                                    VoxelShape shape = state.getOutlineShape(mc.world, pos);
                                    List<Box> boxes = shape == null ? List.of() : shape.getBoundingBoxes();
                                    if (!blockEspOwnsFill) {
                                       if (boxes.isEmpty()) {
                                          this.addBox(new Box(pos).contract(0.08), cameraPos, fillColor, outlineColor);
                                       } else {
                                          for (Box part : boxes) {
                                             Box b = part.offset(pos).contract(0.08);
                                             if (b.maxX > b.minX && b.maxY > b.minY && b.maxZ > b.minZ) {
                                                this.addBox(b, cameraPos, fillColor, outlineColor);
                                             }
                                          }
                                       }
                                    } else {
                                       this.addSharedFillMarker(pos, cameraPos, fillColor, outlineColor);
                                    }

                                    this.renderTracers
                                       .add(new StorageESPModule.Tracer(new Vec3d(centerX, centerY, centerZ), withAlpha(base, this.tracerAlpha.getValue())));
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (!this.renderBoxes.isEmpty() || !this.renderTracers.isEmpty()) {
                  if (!this.renderBoxes.isEmpty()) {
                     WorldRenderer.WorldBatch batch = WorldRenderer.beginWorldBatch(matrices);

                     for (StorageESPModule.RenderBox box : this.renderBoxes) {
                        if (this.fill.getValue() && box.fillColor.getAlpha() > 0) {
                           double cx = (box.minX + box.maxX) / 2.0;
                           double cy = (box.minY + box.maxY) / 2.0;
                           double cz = (box.minZ + box.maxZ) / 2.0;
                           double dx = Math.max(0.001, (box.maxX - box.minX) / 2.0 - 0.03);
                           double dy = Math.max(0.001, (box.maxY - box.minY) / 2.0 - 0.03);
                           double dz = Math.max(0.001, (box.maxZ - box.minZ) / 2.0 - 0.03);
                           batch.renderFilledBox(cx - dx, cy - dy, cz - dz, cx + dx, cy + dy, cz + dz, box.fillColor);
                        }
                     }

                     batch.flush();
                  }

                  if (this.tracers.getValue() && !this.renderTracers.isEmpty()) {
                     Vec3d cameraForward = WorldRenderer.getCameraForward(camera);
                     Vec3d cameraRight = WorldRenderer.getCameraRight(camera);
                     Vec3d cameraUp = WorldRenderer.getCameraUp(cameraForward, cameraRight);
                     boolean detachedView = !mc.options.getPerspective().isFirstPerson()
                        || FreecamModule.instance != null && FreecamModule.instance.isEnabled();
                     Vec3d tracerStart = this.bodyTracers.getValue() && detachedView
                        ? this.getPlayerHeadPosition(tickDelta).subtract(cameraPos)
                        : cameraForward.multiply(0.1);
                     WorldRenderer.WorldBatch batch = WorldRenderer.beginWorldBatch(matrices);

                     for (StorageESPModule.Tracer tracer : this.renderTracers) {
                        Vec3d cleanEnd = WorldRenderer.getForwardClampedTracerEnd(tracer.target, cameraForward, cameraRight, cameraUp, 16.0);
                        if (cleanEnd != null) {
                           batch.renderBeam(tracer.color, tracerStart, cleanEnd, this.tracerWidth.getValue().floatValue() * 1.2F);
                        }
                     }

                     batch.flush();
                  }
               }
            }
         }
      }
   }

   private void collectChunk(WorldChunk chunk, Vec3d cameraPos, double maxDistanceSq, HashSet<Long> seen) {
      for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
         BlockPos pos = blockEntity.getPos();
         long key = pos.asLong();
         if (seen.add(key)) {
            double centerX = (double)pos.getX() + 0.5 - cameraPos.x;
            double centerY = (double)pos.getY() + 0.5 - cameraPos.y;
            double centerZ = (double)pos.getZ() + 0.5 - cameraPos.z;
            if (!(centerX * centerX + centerY * centerY + centerZ * centerZ > maxDistanceSq)) {
               BlockState state = blockEntity.getCachedState();
               boolean blockEspOwnsFill = this.isClaimedByBlockEsp(state.getBlock());
               String type = this.classify(blockEntity);
               if (type != null && this.shouldHighlight(type)) {
                  Color base = this.baseColor(type);
                  if (type.equals("piston") || type.equals("sticky_piston")) {
                     BlockState liveState = mc.world.getBlockState(pos);
                     if (liveState != null && !(Boolean)liveState.get(Properties.EXTENDED)) {
                        base = PISTON_STATIONARY_COLOR;
                     }
                  }

                  Color fillColor = withAlpha(base, this.alpha.getValue());
                  Color outlineColor = withAlpha(base, 0);
                  VoxelShape shape = state.getOutlineShape(mc.world, pos);
                  List<Box> boxes = shape == null ? List.of() : shape.getBoundingBoxes();
                  if (!blockEspOwnsFill) {
                     if (!boxes.isEmpty() && state.getBlock() != Blocks.HOPPER) {
                        for (Box part : boxes) {
                           Box b = part.offset(pos).contract(0.08);
                           if (b.maxX > b.minX && b.maxY > b.minY && b.maxZ > b.minZ) {
                              this.addBox(b, cameraPos, fillColor, outlineColor);
                           }
                        }
                     } else {
                        this.addBox(new Box(pos).contract(0.08), cameraPos, fillColor, outlineColor);
                     }
                  } else {
                     this.addSharedFillMarker(pos, cameraPos, fillColor, outlineColor);
                  }

                  this.renderTracers.add(new StorageESPModule.Tracer(new Vec3d(centerX, centerY, centerZ), withAlpha(base, this.tracerAlpha.getValue())));
               }
            }
         }
      }
   }

   private boolean isClaimedByBlockEsp(Block block) {
      if (this.blockEsp == null && ModuleManager.INSTANCE.getModuleByName("Block ESP") instanceof BlockESPModule found) {
         this.blockEsp = found;
      }

      return this.blockEsp != null && this.blockEsp.isEnabled() && this.blockEsp.isSelected(block);
   }

   private void addSharedFillMarker(BlockPos pos, Vec3d cameraPos, Color fillColor, Color outlineColor) {
      this.addBox(new Box(pos).contract(0.08), cameraPos, fillColor, outlineColor);
   }

   private void addBox(Box box, Vec3d cameraPos, Color fillColor, Color outlineColor) {
      this.renderBoxes
         .add(
            new StorageESPModule.RenderBox(
               box.minX - cameraPos.x,
               box.minY - cameraPos.y,
               box.minZ - cameraPos.z,
               box.maxX - cameraPos.x,
               box.maxY - cameraPos.y,
               box.maxZ - cameraPos.z,
               fillColor,
               outlineColor
            )
         );
   }

   private String classify(BlockEntity blockEntity) {
      String directBlockId = Registries.BLOCK.getId(blockEntity.getCachedState().getBlock()).toString();
      if (this.storageBlocks.findEntry(directBlockId) != null) {
         return directBlockId;
      } else if (blockEntity instanceof TrappedChestBlockEntity) {
         return "trapped_chest";
      } else if (blockEntity instanceof ChestBlockEntity) {
         return "chest";
      } else if (blockEntity instanceof EnderChestBlockEntity) {
         return "ender_chest";
      } else if (blockEntity instanceof MobSpawnerBlockEntity) {
         return "spawner";
      } else if (blockEntity instanceof ShulkerBoxBlockEntity) {
         BlockPos pos = blockEntity.getPos();
         BlockState state = mc.world.getBlockState(pos);
         return Registries.BLOCK.getId(state.getBlock()).toString();
      } else if (blockEntity instanceof FurnaceBlockEntity) {
         return "furnace";
      } else if (blockEntity instanceof BlastFurnaceBlockEntity) {
         return "blast_furnace";
      } else if (blockEntity instanceof SmokerBlockEntity) {
         return "smoker";
      } else if (blockEntity instanceof BarrelBlockEntity) {
         return "barrel";
      } else if (blockEntity instanceof DispenserBlockEntity) {
         BlockPos pos = blockEntity.getPos();
         BlockState state = mc.world.getBlockState(pos);
         return state.getBlock() == Blocks.DROPPER ? "minecraft:dropper" : "dispenser";
      } else if (blockEntity instanceof DropperBlockEntity) {
         return "minecraft:dropper";
      } else if (blockEntity instanceof HopperBlockEntity) {
         return "hopper";
      } else {
         if (blockEntity instanceof PistonBlockEntity) {
            BlockPos pos = blockEntity.getPos();
            BlockState state = mc.world.getBlockState(pos);
            if (state.getBlock() == Blocks.PISTON) {
               return "piston";
            }

            if (state.getBlock() == Blocks.STICKY_PISTON) {
               return "sticky_piston";
            }
         }

         BlockPos posx = blockEntity.getPos();
         BlockState statex = mc.world.getBlockState(posx);
         return statex.getBlock() == Blocks.CRAFTER ? "crafter" : null;
      }
   }

   private boolean shouldHighlight(String type) {
      if (type.startsWith("minecraft:")) {
         return this.storageBlocks.isSelected(type);
      } else {
         String blockId = this.typeToBlockId(type);
         return blockId != null ? this.storageBlocks.isSelected(blockId) : false;
      }
   }

   private Color baseColor(String type) {
      if (type.startsWith("minecraft:")) {
         Color customColor = this.storageBlocks.getColor(type);
         if (customColor != null) {
            return customColor;
         }

         StorageSelectionSetting.Entry entry = this.storageBlocks.findEntry(type);
         if (entry != null) {
            return entry.defaultColor();
         }
      }

      String blockId = this.typeToBlockId(type);
      if (blockId != null) {
         Color customColorx = this.storageBlocks.getColor(blockId);
         if (customColorx != null) {
            return customColorx;
         }
      }
      return switch (type) {
         case "trapped_chest" -> TRAPPED_COLOR;
         case "ender_chest" -> ENDER_COLOR;
         case "spawner" -> SPAWNER_COLOR;
         case "shulker_box" -> SHULKER_COLOR;
         case "furnace" -> FURNACE_COLOR;
         case "blast_furnace" -> BLAST_FURNACE_COLOR;
         case "smoker" -> SMOKER_COLOR;
         case "barrel" -> BARREL_COLOR;
         case "dispenser" -> DISPENSER_COLOR;
         case "hopper" -> HOPPER_COLOR;
         case "piston" -> PISTON_COLOR;
         case "sticky_piston" -> PISTON_COLOR;
         case "crafter" -> CRAFTER_COLOR;
         default -> CHEST_COLOR;
      };
   }

   private String typeToBlockId(String type) {
      return switch (type) {
         case "chest" -> "minecraft:chest";
         case "trapped_chest" -> "minecraft:trapped_chest";
         case "ender_chest" -> "minecraft:ender_chest";
         case "spawner" -> "minecraft:spawner";
         case "shulker_box" -> "minecraft:shulker_box";
         case "furnace" -> "minecraft:furnace";
         case "blast_furnace" -> "minecraft:blast_furnace";
         case "smoker" -> "minecraft:smoker";
         case "barrel" -> "minecraft:barrel";
         case "dispenser" -> "minecraft:dispenser";
         case "hopper" -> "minecraft:hopper";
         case "piston" -> "minecraft:piston";
         case "sticky_piston" -> "minecraft:sticky_piston";
         case "crafter" -> "minecraft:crafter";
         default -> null;
      };
   }

   private boolean isThirdPersonView() {
      if (mc.options != null) {
         try {
            return !mc.options.getPerspective().isFirstPerson();
         } catch (Throwable var2) {
         }
      }

      return false;
   }

   private Vec3d getPlayerHeadPosition(float tickDelta) {
      if (mc.player == null) {
         return Vec3d.ZERO;
      } else {
         try {
            return mc.player.getCameraPosVec(tickDelta);
         } catch (Throwable var11) {
            double worldX = MathHelper.lerp((double)tickDelta, mc.player.lastRenderX, mc.player.getX());
            double worldY = MathHelper.lerp((double)tickDelta, mc.player.lastRenderY, mc.player.getY());
            double worldZ = MathHelper.lerp((double)tickDelta, mc.player.lastRenderZ, mc.player.getZ());
            double eyeHeight = (double)mc.player.getEyeHeight(mc.player.getPose());
            return new Vec3d(worldX, worldY + eyeHeight, worldZ);
         }
      }
   }

   public StorageSelectionSetting getStorageBlocksSetting() {
      return this.storageBlocks;
   }

   public boolean usesBodyTracers() {
      return this.bodyTracers.getValue();
   }

   public float getTracerPixelWidth() {
      return this.tracerWidth.getValue().floatValue() * 1.2F;
   }

   private static record RenderBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Color fillColor, Color outlineColor) {
   }

   private static record Tracer(Vec3d target, Color color) {
   }
}
