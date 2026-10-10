package com.zenya.module.render;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.setting.Setting;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public final class SpawnerTagsModule extends Module {
   private static final int SCAN_INTERVAL_TICKS = 10;
   private static final double Y_OFFSET = 0.85;
   private static SpawnerTagsModule INSTANCE;
   private final Setting<Color> textColor;
   private final Setting<Color> backgroundColor;
   private final Setting<Double> scale;
   private final Setting<Integer> scanRadius;
   private final Map<Long, SpawnerTagsModule.SpawnerTag> spawners;
   private final List<SpawnerTagsModule.SpawnerTag> renderList;
   private int tickCounter;
   private int spawnerCount;

   public SpawnerTagsModule() {
      super("SpawnerTags", Category.RENDER);
      Setting<Color> setting1 = new com.zenya.setting.Setting<>("Text Color", java.awt.Color.WHITE);
      this.textColor = setting1;
      var color1 = new java.awt.Color(0, 0, 0, 120);
      Setting<Color> setting2 = new com.zenya.setting.Setting<>("Background", color1);
      this.backgroundColor = setting2;
      Setting<Double> setting3 = new com.zenya.setting.Setting<>("Scale", java.lang.Double.valueOf(1.15), java.lang.Double.valueOf(0.35), java.lang.Double.valueOf(4.0));
      this.scale = setting3;
      Setting<Integer> setting4 = new com.zenya.setting.Setting<>("Scan Radius", java.lang.Integer.valueOf(32), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(32));
      this.scanRadius = setting4;
      Map<Long, SpawnerTagsModule.SpawnerTag> concurrentHashMap1 = new java.util.concurrent.ConcurrentHashMap<>();
      this.spawners = concurrentHashMap1;
      List<SpawnerTagsModule.SpawnerTag> arrayList1 = new java.util.ArrayList<>();
      this.renderList = arrayList1;
      com.zenya.module.render.SpawnerTagsModule.INSTANCE = this;
      this.setDescription("Shows mob type nametags above nearby loaded spawners.");
      this.addSetting(this.textColor);
      this.addSetting(this.backgroundColor);
      this.addSetting(this.scale);
      this.addSetting(this.scanRadius);
   }

   public static void renderHud(DrawContext context, float tickDelta) {
      var field1 = com.zenya.module.render.SpawnerTagsModule.INSTANCE;
      if ((field1 != null)) {
         var result1 = field1.isEnabled();
         if (result1) {
            field1.renderHudInternal(context);
            return;
         }
         return;
      } else {
         return;
      }
   }

   @Override
   public void onEnable() {
      this.spawners.clear();
      this.renderList.clear();
      this.tickCounter = 0;
      this.spawnerCount = 0;
      this.scanAroundPlayer();
   }

   @Override
   public void onDisable() {
      this.spawners.clear();
      this.renderList.clear();
      this.spawnerCount = 0;
   }

   @Override
   public void onTick() {
      var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
      if ((field1 != null)) {
         var field2 = field1.player;
         if ((field2 != null)) {
            var field3 = com.zenya.module.render.SpawnerTagsModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            }
            var field4 = field3.world;
            if ((field4 != null)) {
               var field5 = this.tickCounter;
               this.tickCounter = (field5 + 1);
               if ((field5 < 9)) {
                  return;
               }
               this.tickCounter = 0;
               this.scanAroundPlayer();
               return;
            } else {
               var field5 = this.spawners;
               if ((field5 == null)) {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               field5.clear();
               var field6 = this.renderList;
               if ((field6 == null)) {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               field6.clear();
               this.spawnerCount = 0;
               return;
            }
         } else {
            var field3 = this.spawners;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            }
            field3.clear();
            var field4 = this.renderList;
            if ((field4 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            }
            field4.clear();
            this.spawnerCount = 0;
            return;
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var field2 = field1.world;
      if ((field2 == null)) {
         return;
      }
      if ((packet == null)) {
         return;
      }
      if (!((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket))) {
         if (!((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket))) {
            if (!((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket))) {
               return;
            }
            if ((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket)) {
               var result1 = ((net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket) packet).getPos();
               var result2 = ((net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket) packet).getState();
               var field3 = net.minecraft.block.Blocks.SPAWNER;
               if ((result2 != null)) {
                  var result3 = result2.isOf(field3);
                  if (!(result3)) {
                     var field4 = this.spawners;
                     if ((result1 != null)) {
                        var result4 = result1.asLong();
                        if ((field4 != null)) {
                           var result5 = ((java.util.Map) field4).remove(java.lang.Long.valueOf(result4));
                           var field5 = this.spawners;
                           if ((field5 != null)) {
                              this.spawnerCount = field5.size();
                              return;
                           }
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           throw new java.lang.NullPointerException("object reference is null");
                        }
                     } else {
                        throw new java.lang.NullPointerException("object reference is null");
                     }
                  } else {
                     var field4 = com.zenya.module.render.SpawnerTagsModule.mc;
                     if ((field4 != null)) {
                        var field5 = field4.world;
                        if ((field5 != null)) {
                           var result4 = field5.getChunkManager();
                           var chunkPos1 = new net.minecraft.util.math.ChunkPos(result1);
                           var field6 = chunkPos1.x;
                           var chunkPos2 = new net.minecraft.util.math.ChunkPos(result1);
                           var field7 = chunkPos2.z;
                           if ((result4 != null)) {
                              var result5 = result4.getWorldChunk((int) (Integer.toUnsignedLong(field6)), field7, false);
                              if ((result5 == null)) {
                                 return;
                              }
                              this.scanChunk(result5);
                              return;
                           } else {
                              throw new java.lang.NullPointerException("object reference is null");
                           }
                        } else {
                           throw new java.lang.NullPointerException("object reference is null");
                        }
                     } else {
                        throw new java.lang.NullPointerException("object reference is null");
                     }
                  }
               } else {
                  throw new java.lang.NullPointerException("object reference is null");
               }
            } else {
               throw new java.lang.ClassCastException("");
            }
         } else {
            if ((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket)) {
               var array1 = new java.lang.Object[1];
               array1[0] = this;
               var captured1 = this;
               var callback1 = new java.util.function.BiConsumer() { public void accept(java.lang.Object parameter0, java.lang.Object parameter1) { (captured1).lambda$onPacketReceive$0(((net.minecraft.util.math.BlockPos) parameter0), ((net.minecraft.block.BlockState) parameter1)); } };
               ((net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket) packet).visitUpdates(callback1);
               return;
            }
            throw new java.lang.ClassCastException("");
         }
      } else {
         if ((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket)) {
            var field3 = com.zenya.module.render.SpawnerTagsModule.mc;
            if ((field3 != null)) {
               var field4 = field3.world;
               if ((field4 != null)) {
                  var result1 = field4.getChunkManager();
                  var result2 = ((net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket) packet).getChunkX();
                  var result3 = ((net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket) packet).getChunkZ();
                  if ((result1 != null)) {
                     var result4 = result1.getWorldChunk((int) (Integer.toUnsignedLong(result2)), result3, false);
                     if ((result4 == null)) {
                        return;
                     }
                     this.scanChunk(result4);
                     return;
                  } else {
                     throw new java.lang.NullPointerException("object reference is null");
                  }
               } else {
                  throw new java.lang.NullPointerException("object reference is null");
               }
            } else {
               throw new java.lang.NullPointerException("object reference is null");
            }
         } else {
            throw new java.lang.ClassCastException("");
         }
      }
   }

   private void renderHudInternal(DrawContext context) {
      net.minecraft.client.MinecraftClient saved1 = null;
      net.minecraft.client.MinecraftClient saved2 = null;
      java.util.Map saved3 = null;
      java.util.List saved4 = null;
      double saved5 = 0.0;
      net.minecraft.client.MinecraftClient saved6 = null;
      net.minecraft.client.network.ClientPlayerEntity saved7 = null;
      double saved8 = 0.0;
      double saved9 = 0.0;
      double saved10 = 0.0;
      java.util.Collection saved11 = null;
      java.util.Iterator saved12 = null;
      java.lang.Object saved13 = null;
      java.util.Map saved14 = null;
      java.util.Comparator saved15 = null;
      net.minecraft.client.world.ClientWorld saved16 = null;
      net.minecraft.util.math.BlockPos saved17 = null;
      net.minecraft.client.font.TextRenderer saved18 = null;
      com.zenya.setting.Setting saved19 = null;
      net.minecraft.block.Block saved20 = null;
      net.minecraft.block.BlockState saved21 = null;
      java.lang.Long saved22 = null;
      java.util.Map saved23 = null;
      double saved24 = 0.0;
      float saved25 = 0.0F;
      com.zenya.setting.Setting saved26 = null;
      java.util.List saved27 = null;
      java.lang.Object saved28 = null;
      int saved29 = 0;
      int saved30 = 0;
      java.util.List saved31 = null;
      java.util.Iterator saved32 = null;
      net.minecraft.util.math.BlockPos saved33 = null;
      int saved34 = 0;
      int saved35 = 0;
      net.minecraft.util.math.BlockPos saved36 = null;
      net.minecraft.util.Pair saved37 = null;
      java.lang.Object saved38 = null;
      java.lang.Object saved39 = null;
      int step = 0;
      dispatch: while (true) {
         switch (step) {
            case 0 -> {
               var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field1 == null)) {
                  step = 1;
                  continue dispatch;
               } else {
                  net.minecraft.client.MinecraftClient nextValue0 = field1;
                  saved1 = nextValue0;
                  step = 2;
                  continue dispatch;
               }
            }
            case 1 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 2 -> {
               var field2 = saved1.player;
               if ((field2 == null)) {
                  step = 3;
                  continue dispatch;
               } else {
                  step = 4;
                  continue dispatch;
               }
            }
            case 3 -> {
               return;
            }
            case 4 -> {
               var field3 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field3 == null)) {
                  step = 5;
                  continue dispatch;
               } else {
                  net.minecraft.client.MinecraftClient nextValue0 = field3;
                  saved2 = nextValue0;
                  step = 6;
                  continue dispatch;
               }
            }
            case 5 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 6 -> {
               var field4 = saved2.world;
               if ((field4 == null)) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 8;
                  continue dispatch;
               }
            }
            case 7 -> {
               return;
            }
            case 8 -> {
               var field5 = this.spawners;
               if ((field5 == null)) {
                  step = 9;
                  continue dispatch;
               } else {
                  java.util.Map nextValue0 = field5;
                  saved3 = nextValue0;
                  step = 10;
                  continue dispatch;
               }
            }
            case 9 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 10 -> {
               var result1 = saved3.isEmpty();
               if (result1) {
                  step = 11;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 11 -> {
               return;
            }
            case 12 -> {
               var field6 = this.renderList;
               if ((field6 == null)) {
                  step = 13;
                  continue dispatch;
               } else {
                  java.util.List nextValue0 = field6;
                  saved4 = nextValue0;
                  step = 14;
                  continue dispatch;
               }
            }
            case 13 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 14 -> {
               saved4.clear();
               var result2 = this.maxRenderDistanceSq();
               var field7 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field7 == null)) {
                  step = 15;
                  continue dispatch;
               } else {
                  double nextValue0 = result2;
                  net.minecraft.client.MinecraftClient nextValue1 = field7;
                  saved5 = nextValue0;
                  saved6 = nextValue1;
                  step = 16;
                  continue dispatch;
               }
            }
            case 15 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 16 -> {
               var field8 = saved6.player;
               if ((field8 == null)) {
                  step = 17;
                  continue dispatch;
               } else {
                  net.minecraft.client.network.ClientPlayerEntity nextValue0 = field8;
                  saved7 = nextValue0;
                  step = 18;
                  continue dispatch;
               }
            }
            case 17 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 18 -> {
               var result3 = saved7.getX();
               var field9 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field9 == null)) {
                  step = 19;
                  continue dispatch;
               } else {
                  double nextValue0 = result3;
                  net.minecraft.client.MinecraftClient nextValue1 = field9;
                  saved8 = nextValue0;
                  saved6 = nextValue1;
                  step = 20;
                  continue dispatch;
               }
            }
            case 19 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 20 -> {
               var field10 = saved6.player;
               if ((field10 == null)) {
                  step = 21;
                  continue dispatch;
               } else {
                  net.minecraft.client.network.ClientPlayerEntity nextValue0 = field10;
                  saved7 = nextValue0;
                  step = 22;
                  continue dispatch;
               }
            }
            case 21 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 22 -> {
               var result4 = saved7.getY();
               var field11 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field11 == null)) {
                  step = 23;
                  continue dispatch;
               } else {
                  double nextValue0 = result4;
                  net.minecraft.client.MinecraftClient nextValue1 = field11;
                  saved9 = nextValue0;
                  saved6 = nextValue1;
                  step = 24;
                  continue dispatch;
               }
            }
            case 23 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 24 -> {
               var field12 = saved6.player;
               if ((field12 == null)) {
                  step = 25;
                  continue dispatch;
               } else {
                  net.minecraft.client.network.ClientPlayerEntity nextValue0 = field12;
                  saved7 = nextValue0;
                  step = 26;
                  continue dispatch;
               }
            }
            case 25 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 26 -> {
               var result5 = saved7.getZ();
               var field13 = this.spawners;
               if ((field13 == null)) {
                  step = 27;
                  continue dispatch;
               } else {
                  double nextValue0 = result5;
                  java.util.Map nextValue1 = field13;
                  saved10 = nextValue0;
                  saved3 = nextValue1;
                  step = 28;
                  continue dispatch;
               }
            }
            case 27 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 28 -> {
               var result6 = saved3.values();
               if ((result6 == null)) {
                  step = 29;
                  continue dispatch;
               } else {
                  java.util.Collection nextValue0 = result6;
                  saved11 = nextValue0;
                  step = 30;
                  continue dispatch;
               }
            }
            case 29 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 30 -> {
               var result7 = saved11.iterator();
               if ((result7 == null)) {
                  step = 31;
                  continue dispatch;
               } else {
                  java.util.Iterator nextValue0 = result7;
                  saved12 = nextValue0;
                  step = 32;
                  continue dispatch;
               }
            }
            case 31 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 32 -> {
               var result8 = saved12.hasNext();
               if (!(result8)) {
                  step = 33;
                  continue dispatch;
               } else {
                  step = 34;
                  continue dispatch;
               }
            }
            case 33 -> {
               var field14 = this.renderList;
               if ((field14 == null)) {
                  step = 35;
                  continue dispatch;
               } else {
                  java.util.List nextValue0 = field14;
                  saved4 = nextValue0;
                  step = 36;
                  continue dispatch;
               }
            }
            case 34 -> {
               var result9 = saved12.next();
               if ((result9 != null)) {
                  java.lang.Object nextValue0 = result9;
                  saved13 = nextValue0;
                  step = 37;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result9;
                  saved13 = nextValue0;
                  step = 38;
                  continue dispatch;
               }
            }
            case 35 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 36 -> {
               var result9 = saved4.isEmpty();
               if (result9) {
                  step = 39;
                  continue dispatch;
               } else {
                  step = 40;
                  continue dispatch;
               }
            }
            case 37 -> {
               if (!((saved13 == null || saved13 instanceof com.zenya.module.render.SpawnerTagsModule.SpawnerTag))) {
                  step = 41;
                  continue dispatch;
               } else {
                  step = 38;
                  continue dispatch;
               }
            }
            case 38 -> {
               var field14 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field14 == null)) {
                  step = 42;
                  continue dispatch;
               } else {
                  net.minecraft.client.MinecraftClient nextValue0 = field14;
                  saved1 = nextValue0;
                  step = 43;
                  continue dispatch;
               }
            }
            case 39 -> {
               var field15 = this.spawners;
               if ((field15 != null)) {
                  java.util.Map nextValue0 = field15;
                  saved14 = nextValue0;
                  step = 44;
                  continue dispatch;
               } else {
                  step = 45;
                  continue dispatch;
               }
            }
            case 40 -> {
               var field15 = this.renderList;
               var array1 = new java.lang.Object[0];
               var callback1 = new java.util.function.ToDoubleFunction() { public double applyAsDouble(java.lang.Object parameter0) { return com.zenya.module.render.SpawnerTagsModule.lambda$renderHudInternal$1(((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) parameter0)); } };
               var result10 = java.util.Comparator.comparingDouble(callback1);
               if ((field15 != null)) {
                  java.util.List nextValue0 = field15;
                  java.util.Comparator nextValue1 = result10;
                  saved4 = nextValue0;
                  saved15 = nextValue1;
                  step = 46;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 41 -> {
               throw new java.lang.ClassCastException("");
            }
            case 42 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 43 -> {
               var field15 = saved1.world;
               if ((saved13 == null)) {
                  step = 48;
                  continue dispatch;
               } else {
                  net.minecraft.client.world.ClientWorld nextValue0 = field15;
                  saved16 = nextValue0;
                  step = 49;
                  continue dispatch;
               }
            }
            case 44 -> {
               this.spawnerCount = saved14.size();
               return;
            }
            case 45 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 46 -> {
               saved4.sort(saved15);
               var field16 = com.zenya.module.render.SpawnerTagsModule.mc;
               if ((field16 != null)) {
                  net.minecraft.client.MinecraftClient nextValue0 = field16;
                  saved6 = nextValue0;
                  step = 50;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 47 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 48 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 49 -> {
               var field16 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved13).pos;
               if ((saved16 == null)) {
                  step = 51;
                  continue dispatch;
               } else {
                  net.minecraft.util.math.BlockPos nextValue0 = field16;
                  saved17 = nextValue0;
                  step = 52;
                  continue dispatch;
               }
            }
            case 50 -> {
               var field17 = saved6.textRenderer;
               var field18 = this.scale;
               if ((field18 != null)) {
                  net.minecraft.client.font.TextRenderer nextValue0 = field17;
                  com.zenya.setting.Setting nextValue1 = field18;
                  saved18 = nextValue0;
                  saved19 = nextValue1;
                  step = 53;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 51 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 52 -> {
               var result10 = saved16.getBlockState(saved17);
               var field17 = net.minecraft.block.Blocks.SPAWNER;
               if ((result10 == null)) {
                  step = 54;
                  continue dispatch;
               } else {
                  net.minecraft.block.Block nextValue0 = field17;
                  net.minecraft.block.BlockState nextValue1 = result10;
                  saved20 = nextValue0;
                  saved21 = nextValue1;
                  step = 55;
                  continue dispatch;
               }
            }
            case 53 -> {
               var result11 = saved19.getValue();
               if ((result11 == null)) {
                  step = 56;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result11;
                  saved13 = nextValue0;
                  step = 57;
                  continue dispatch;
               }
            }
            case 54 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 55 -> {
               var result11 = saved21.isOf(saved20);
               if (!(result11)) {
                  step = 58;
                  continue dispatch;
               } else {
                  step = 59;
                  continue dispatch;
               }
            }
            case 56 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 57 -> {
               if (!((saved13 == null || saved13 instanceof java.lang.Double))) {
                  step = 60;
                  continue dispatch;
               } else {
                  step = 61;
                  continue dispatch;
               }
            }
            case 58 -> {
               var field18 = this.spawners;
               var field19 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved13).key;
               if ((field18 == null)) {
                  step = 62;
                  continue dispatch;
               } else {
                  java.lang.Long nextValue0 = java.lang.Long.valueOf(field19);
                  java.util.Map nextValue1 = field18;
                  saved22 = nextValue0;
                  saved23 = nextValue1;
                  step = 63;
                  continue dispatch;
               }
            }
            case 59 -> {
               var field18 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved13).pos;
               var result12 = this.squaredDistance(field18, saved8, saved9, saved10);
               if ((result12 <= saved5)) {
                  double nextValue0 = result12;
                  saved24 = nextValue0;
                  step = 64;
                  continue dispatch;
               } else {
                  step = 65;
                  continue dispatch;
               }
            }
            case 60 -> {
               throw new java.lang.ClassCastException("");
            }
            case 61 -> {
               var result12 = ((java.lang.Double) saved13).floatValue();
               var field19 = this.textColor;
               if ((field19 != null)) {
                  float nextValue0 = result12;
                  com.zenya.setting.Setting nextValue1 = field19;
                  saved25 = nextValue0;
                  saved26 = nextValue1;
                  step = 66;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 62 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 63 -> {
               var result12 = ((java.util.Map) saved23).remove(saved22);
               var result13 = saved12.hasNext();
               if (!(result13)) {
                  step = 33;
                  continue dispatch;
               } else {
                  step = 34;
                  continue dispatch;
               }
            }
            case 64 -> {
               ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved13).distanceSq = saved24;
               var field19 = this.renderList;
               if ((field19 != null)) {
                  java.util.List nextValue0 = field19;
                  saved27 = nextValue0;
                  step = 67;
                  continue dispatch;
               } else {
                  step = 68;
                  continue dispatch;
               }
            }
            case 65 -> {
               var result13 = saved12.hasNext();
               if (!(result13)) {
                  step = 33;
                  continue dispatch;
               } else {
                  step = 34;
                  continue dispatch;
               }
            }
            case 66 -> {
               var result13 = saved26.getValue();
               if ((result13 != null)) {
                  java.lang.Object nextValue0 = result13;
                  saved28 = nextValue0;
                  step = 69;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result13;
                  saved28 = nextValue0;
                  step = 70;
                  continue dispatch;
               }
            }
            case 67 -> {
               var result13 = ((java.util.List) saved27).add(saved13);
               var result14 = saved12.hasNext();
               if (!(result14)) {
                  step = 33;
                  continue dispatch;
               } else {
                  step = 34;
                  continue dispatch;
               }
            }
            case 68 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 69 -> {
               if (!((saved28 == null || saved28 instanceof java.awt.Color))) {
                  step = 71;
                  continue dispatch;
               } else {
                  step = 70;
                  continue dispatch;
               }
            }
            case 70 -> {
               var result14 = this.rgb(((java.awt.Color) saved28));
               var field20 = this.backgroundColor;
               if ((field20 != null)) {
                  int nextValue0 = result14;
                  com.zenya.setting.Setting nextValue1 = field20;
                  saved29 = nextValue0;
                  saved26 = nextValue1;
                  step = 72;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 71 -> {
               throw new java.lang.ClassCastException("");
            }
            case 72 -> {
               var result15 = saved26.getValue();
               if ((result15 != null)) {
                  java.lang.Object nextValue0 = result15;
                  saved28 = nextValue0;
                  step = 73;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result15;
                  saved28 = nextValue0;
                  step = 74;
                  continue dispatch;
               }
            }
            case 73 -> {
               if (!((saved28 == null || saved28 instanceof java.awt.Color))) {
                  step = 75;
                  continue dispatch;
               } else {
                  step = 74;
                  continue dispatch;
               }
            }
            case 74 -> {
               var result16 = this.argb(((java.awt.Color) saved28));
               var field21 = this.renderList;
               if ((field21 != null)) {
                  int nextValue0 = result16;
                  java.util.List nextValue1 = field21;
                  saved30 = nextValue0;
                  saved31 = nextValue1;
                  step = 76;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 75 -> {
               throw new java.lang.ClassCastException("");
            }
            case 76 -> {
               var result17 = saved31.iterator();
               if ((result17 != null)) {
                  java.util.Iterator nextValue0 = result17;
                  saved32 = nextValue0;
                  step = 77;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 77 -> {
               var result18 = saved32.hasNext();
               if (!(result18)) {
                  step = 78;
                  continue dispatch;
               } else {
                  double nextValue0 = 0.5;
                  double nextValue1 = 0.85;
                  double nextValue2 = -1.0;
                  double nextValue3 = 1.0;
                  saved5 = nextValue0;
                  saved8 = nextValue1;
                  saved9 = nextValue2;
                  saved10 = nextValue3;
                  step = 79;
                  continue dispatch;
               }
            }
            case 78 -> {
               var field22 = this.spawners;
               if ((field22 != null)) {
                  java.util.Map nextValue0 = field22;
                  saved14 = nextValue0;
                  step = 80;
                  continue dispatch;
               } else {
                  step = 47;
                  continue dispatch;
               }
            }
            case 79 -> {
               var result19 = saved32.next();
               if ((result19 != null)) {
                  java.lang.Object nextValue0 = result19;
                  saved28 = nextValue0;
                  step = 81;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result19;
                  saved28 = nextValue0;
                  step = 82;
                  continue dispatch;
               }
            }
            case 80 -> {
               this.spawnerCount = saved14.size();
               return;
            }
            case 81 -> {
               if (!((saved28 == null || saved28 instanceof com.zenya.module.render.SpawnerTagsModule.SpawnerTag))) {
                  step = 83;
                  continue dispatch;
               } else {
                  step = 82;
                  continue dispatch;
               }
            }
            case 82 -> {
               if ((saved28 == null)) {
                  step = 84;
                  continue dispatch;
               } else {
                  step = 85;
                  continue dispatch;
               }
            }
            case 83 -> {
               throw new java.lang.ClassCastException("");
            }
            case 84 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 85 -> {
               var field22 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved28).pos;
               if ((field22 == null)) {
                  step = 86;
                  continue dispatch;
               } else {
                  net.minecraft.util.math.BlockPos nextValue0 = field22;
                  saved33 = nextValue0;
                  step = 87;
                  continue dispatch;
               }
            }
            case 86 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 87 -> {
               var result20 = saved33.getX();
               var field23 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved28).pos;
               if ((field23 == null)) {
                  step = 88;
                  continue dispatch;
               } else {
                  int nextValue0 = result20;
                  net.minecraft.util.math.BlockPos nextValue1 = field23;
                  saved34 = nextValue0;
                  saved33 = nextValue1;
                  step = 89;
                  continue dispatch;
               }
            }
            case 88 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 89 -> {
               var result21 = saved33.getY();
               var field24 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved28).pos;
               if ((field24 == null)) {
                  step = 90;
                  continue dispatch;
               } else {
                  int nextValue0 = result21;
                  net.minecraft.util.math.BlockPos nextValue1 = field24;
                  saved35 = nextValue0;
                  saved36 = nextValue1;
                  step = 91;
                  continue dispatch;
               }
            }
            case 90 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 91 -> {
               var result22 = saved36.getZ();
               var vec3d1 = new net.minecraft.util.math.Vec3d(((double) (saved34) + saved5), ((double) (saved35) + saved8), ((double) (result22) + saved5));
               var field25 = com.zenya.render.WorldProjection.modelViewMatrix;
               var field26 = com.zenya.render.WorldProjection.projectionMatrix;
               var result23 = com.zenya.render.WorldProjection.project(field25, field26, vec3d1);
               if ((result23 != null)) {
                  net.minecraft.util.Pair nextValue0 = result23;
                  saved37 = nextValue0;
                  step = 92;
                  continue dispatch;
               } else {
                  step = 93;
                  continue dispatch;
               }
            }
            case 92 -> {
               var result24 = saved37.getRight();
               if ((result24 == null)) {
                  step = 94;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result24;
                  saved38 = nextValue0;
                  step = 95;
                  continue dispatch;
               }
            }
            case 93 -> {
               var result24 = saved32.hasNext();
               if (!(result24)) {
                  step = 78;
                  continue dispatch;
               } else {
                  step = 79;
                  continue dispatch;
               }
            }
            case 94 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 95 -> {
               if (!((saved38 == null || saved38 instanceof java.lang.Boolean))) {
                  step = 96;
                  continue dispatch;
               } else {
                  step = 97;
                  continue dispatch;
               }
            }
            case 96 -> {
               throw new java.lang.ClassCastException("");
            }
            case 97 -> {
               var result25 = ((java.lang.Boolean) saved38).booleanValue();
               if (result25) {
                  step = 98;
                  continue dispatch;
               } else {
                  step = 93;
                  continue dispatch;
               }
            }
            case 98 -> {
               var result26 = saved37.getLeft();
               if ((result26 != null)) {
                  java.lang.Object nextValue0 = result26;
                  saved39 = nextValue0;
                  step = 99;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result26;
                  saved39 = nextValue0;
                  step = 100;
                  continue dispatch;
               }
            }
            case 99 -> {
               if (!((saved39 == null || saved39 instanceof net.minecraft.util.math.Vec3d))) {
                  step = 101;
                  continue dispatch;
               } else {
                  step = 100;
                  continue dispatch;
               }
            }
            case 100 -> {
               if ((saved39 == null)) {
                  step = 102;
                  continue dispatch;
               } else {
                  step = 103;
                  continue dispatch;
               }
            }
            case 101 -> {
               throw new java.lang.ClassCastException("");
            }
            case 102 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 103 -> {
               var field27 = ((net.minecraft.util.math.Vec3d) saved39).z;
               if ((saved9 <= field27)) {
                  step = 104;
                  continue dispatch;
               } else {
                  step = 93;
                  continue dispatch;
               }
            }
            case 104 -> {
               var field28 = ((net.minecraft.util.math.Vec3d) saved39).z;
               if ((field28 <= saved10)) {
                  step = 105;
                  continue dispatch;
               } else {
                  step = 93;
                  continue dispatch;
               }
            }
            case 105 -> {
               var field29 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) saved28).label;
               var field30 = ((net.minecraft.util.math.Vec3d) saved39).x;
               var field31 = ((net.minecraft.util.math.Vec3d) saved39).y;
               this.renderTag(context, saved18, field29, (float) ((double) ((float) (field30))), (float) ((double) ((float) (field31))), (float) ((double) (saved25)), saved29, saved30);
               var result27 = saved32.hasNext();
               if (!(result27)) {
                  step = 78;
                  continue dispatch;
               } else {
                  step = 79;
                  continue dispatch;
               }
            }
            default -> throw new IllegalStateException("Invalid control-flow state");
         }
      }
   }

   private void scanAroundPlayer() {
      var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.player;
         if ((field2 == null)) {
            return;
         } else {
            var field3 = com.zenya.module.render.SpawnerTagsModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var field4 = field3.world;
               if ((field4 == null)) {
                  return;
               } else {
                  var field5 = com.zenya.module.render.SpawnerTagsModule.mc;
                  if ((field5 == null)) {
                     throw new java.lang.NullPointerException("object reference is null");
                  } else {
                     var field6 = field5.player;
                     if ((field6 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     } else {
                        var result1 = field6.getChunkPos();
                        var field7 = this.scanRadius;
                        if ((field7 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           var result2 = field7.getValue();
                           if ((result2 != null)) {
                              if (!((result2 == null || result2 instanceof java.lang.Integer))) {
                                 throw new java.lang.ClassCastException("");
                              } else {
                                 var result3 = ((java.lang.Integer) result2).intValue();
                                 var concurrentHashMap1 = new java.util.concurrent.ConcurrentHashMap();
                                 if ((-(1) < result3)) {
                                    int iteration1_1 = ((result3 * 2) + 1);
                                    int iteration1_2 = -(result3);
                                    int iteration1_3 = -(result3);
                                    repeat1: while (true) {
                                       if ((result1 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var field8 = result1.x;
                                          var field9 = result1.z;
                                          var field10 = com.zenya.module.render.SpawnerTagsModule.mc;
                                          if ((field10 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field11 = field10.world;
                                             if ((field11 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var result4 = field11.getChunkManager();
                                                if ((result4 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var saved1 = (field9 + iteration1_2);
                                                   var result5 = result4.getWorldChunk((field8 + iteration1_3), saved1, false);
                                                   if ((result5 != null)) {
                                                      this.collectChunkSpawners(result5, ((java.util.Map) concurrentHashMap1));
                                                      iteration1_1 = (iteration1_1 + -(1));
                                                      iteration1_2 = (iteration1_2 + 1);
                                                      if ((iteration1_1 != 0)) {
                                                         continue repeat1;
                                                      } else {
                                                         var saved2 = (iteration1_3 != result3);
                                                         iteration1_1 = ((result3 * 2) + 1);
                                                         iteration1_2 = -(result3);
                                                         iteration1_3 = (iteration1_3 + 1);
                                                         if (saved2) {
                                                            continue repeat1;
                                                         } else {
                                                            var field12 = this.spawners;
                                                            if ((field12 != null)) {
                                                               field12.clear();
                                                               var field13 = this.spawners;
                                                               if ((field13 != null)) {
                                                                  field13.putAll(((java.util.Map) concurrentHashMap1));
                                                                  var field14 = this.spawners;
                                                                  if ((field14 != null)) {
                                                                     this.spawnerCount = field14.size();
                                                                     return;
                                                                  } else {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  }
                                                               } else {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               }
                                                            } else {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            }
                                                         }
                                                      }
                                                   } else {
                                                      iteration1_1 = (iteration1_1 + -(1));
                                                      iteration1_2 = (iteration1_2 + 1);
                                                      if ((iteration1_1 != 0)) {
                                                         continue repeat1;
                                                      } else {
                                                         var saved2 = (iteration1_3 != result3);
                                                         iteration1_1 = ((result3 * 2) + 1);
                                                         iteration1_2 = -(result3);
                                                         iteration1_3 = (iteration1_3 + 1);
                                                         if (saved2) {
                                                            continue repeat1;
                                                         } else {
                                                            var field12 = this.spawners;
                                                            if ((field12 != null)) {
                                                               field12.clear();
                                                               var field13 = this.spawners;
                                                               if ((field13 != null)) {
                                                                  field13.putAll(((java.util.Map) concurrentHashMap1));
                                                                  var field14 = this.spawners;
                                                                  if ((field14 != null)) {
                                                                     this.spawnerCount = field14.size();
                                                                     return;
                                                                  } else {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  }
                                                               } else {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               }
                                                            } else {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            }
                                                         }
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 } else {
                                    var field8 = this.spawners;
                                    if ((field8 != null)) {
                                       field8.clear();
                                       var field9 = this.spawners;
                                       if ((field9 != null)) {
                                          field9.putAll(((java.util.Map) concurrentHashMap1));
                                          var field10 = this.spawners;
                                          if ((field10 != null)) {
                                             this.spawnerCount = field10.size();
                                             return;
                                          } else {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          }
                                       } else {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       }
                                    } else {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    }
                                 }
                              }
                           } else {
                              throw new java.lang.NullPointerException("object reference is null");
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void scanChunk(WorldChunk chunk) {
      if ((chunk != null)) {
         var result1 = chunk.getPos();
         if ((result1 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result2 = result1.toLong();
         var field1 = this.spawners;
         if ((field1 != null)) {
            var result3 = field1.entrySet();
            var array1 = new java.lang.Object[1];
            array1[0] = java.lang.Long.valueOf(result2);
            var captured1 = ((java.lang.Long) java.lang.Long.valueOf(result2)).longValue();
            var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.render.SpawnerTagsModule.lambda$scanChunk$2(captured1, ((java.util.Map.Entry) parameter0)); } };
            if ((result3 != null)) {
               var result4 = result3.removeIf(callback1);
               var field2 = this.spawners;
               this.collectChunkSpawners(chunk, field2);
               var field3 = this.spawners;
               if ((field3 != null)) {
                  this.spawnerCount = field3.size();
                  return;
               }
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               throw new java.lang.NullPointerException("object reference is null");
            }
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         return;
      }
   }

   private void collectChunkSpawners(WorldChunk chunk, Map<Long, SpawnerTagsModule.SpawnerTag> output) {
      if ((chunk == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var result1 = chunk.getBlockEntities();
         if ((result1 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         } else {
            var result2 = result1.values();
            if ((result2 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var result3 = result2.iterator();
               if ((result3 == null)) {
                  throw new java.lang.NullPointerException("object reference is null");
               } else {
                  repeat1: while (true) {
                     var result4 = result3.hasNext();
                     if (!(result4)) {
                        return;
                     } else {
                        var result5 = result3.next();
                        if ((result5 == null)) {
                           continue repeat1;
                        } else {
                           if (!((result5 == null || result5 instanceof net.minecraft.block.entity.BlockEntity))) {
                              throw new java.lang.ClassCastException("");
                           } else {
                              if ((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity)) {
                                 if (!((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity))) {
                                    throw new java.lang.ClassCastException("");
                                 } else {
                                    var result6 = ((net.minecraft.block.entity.BlockEntity) result5).getPos();
                                    var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
                                    if ((field1 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var field2 = field1.world;
                                       if ((field2 != null)) {
                                          var field3 = com.zenya.module.render.SpawnerTagsModule.mc;
                                          if ((field3 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field4 = field3.world;
                                             if ((field4 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var result7 = field4.getBlockState(result6);
                                                var field5 = net.minecraft.block.Blocks.SPAWNER;
                                                if ((result7 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result8 = result7.isOf(field5);
                                                   if (result8) {
                                                      var result9 = this.resolveSpawnerLabel(((net.minecraft.block.entity.MobSpawnerBlockEntity) result5), result6);
                                                      if ((result9 != null)) {
                                                         var result10 = result9.isBlank();
                                                         if (!(result10)) {
                                                            if ((result6 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result11 = result6.asLong();
                                                               var spawnerTag1 = new com.zenya.module.render.SpawnerTagsModule.SpawnerTag(result11, result6, result9);
                                                               if ((output == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result12 = ((java.util.Map) output).put(java.lang.Long.valueOf(result11), spawnerTag1);
                                                                  continue repeat1;
                                                               }
                                                            }
                                                         } else {
                                                            continue repeat1;
                                                         }
                                                      } else {
                                                         continue repeat1;
                                                      }
                                                   } else {
                                                      continue repeat1;
                                                   }
                                                }
                                             }
                                          }
                                       } else {
                                          continue repeat1;
                                       }
                                    }
                                 }
                              } else {
                                 continue repeat1;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private String resolveSpawnerLabel(MobSpawnerBlockEntity spawner, BlockPos pos) {
      var result1 = this.readSpawnerEntityType(spawner, pos);
      if ((result1 != null)) {
         return this.formatMobName(result1);
      }
      return null;
   }

   private EntityType<?> readSpawnerEntityType(MobSpawnerBlockEntity spawner, BlockPos pos) {
      Throwable pendingFailure = null;
      net.minecraft.client.MinecraftClient saved1 = null;
      boolean saved2 = false;
      net.minecraft.block.spawner.MobSpawnerLogic saved3 = null;
      net.minecraft.client.MinecraftClient saved4 = null;
      net.minecraft.client.world.ClientWorld saved5 = null;
      net.minecraft.entity.Entity saved6 = null;
      net.minecraft.entity.EntityType saved7 = null;
      int step = 0;
      dispatch: while (true) {
         switch (step) {
            case 0 -> {
               net.minecraft.client.MinecraftClient field1 = null;
               try {
                  field1 = com.zenya.module.render.SpawnerTagsModule.mc;
               } catch (Throwable capturedFailure1) {
                  pendingFailure = capturedFailure1;
               }
               var failed1 = pendingFailure != null;
               if (failed1) {
                  step = 1;
                  continue dispatch;
               } else {
                  net.minecraft.client.MinecraftClient nextValue0 = field1;
                  saved1 = nextValue0;
                  step = 2;
                  continue dispatch;
               }
            }
            case 1 -> {
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 2 -> {
               if ((saved1 == null)) {
                  step = 3;
                  continue dispatch;
               } else {
                  step = 4;
                  continue dispatch;
               }
            }
            case 3 -> {
               pendingFailure = new java.lang.NullPointerException("object reference is null");
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 4 -> {
               net.minecraft.client.world.ClientWorld field2 = null;
               try {
                  field2 = saved1.world;
               } catch (Throwable capturedFailure2) {
                  pendingFailure = capturedFailure2;
               }
               var failed2 = pendingFailure != null;
               if ((field2 == null)) {
                  step = 5;
                  continue dispatch;
               } else {
                  boolean nextValue0 = failed2;
                  saved2 = nextValue0;
                  step = 6;
                  continue dispatch;
               }
            }
            case 5 -> {
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 6 -> {
               if (saved2) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 8;
                  continue dispatch;
               }
            }
            case 7 -> {
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 8 -> {
               if ((spawner == null)) {
                  step = 9;
                  continue dispatch;
               } else {
                  step = 10;
                  continue dispatch;
               }
            }
            case 9 -> {
               pendingFailure = new java.lang.NullPointerException("object reference is null");
               var failure1 = pendingFailure;
               pendingFailure = null;
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 10 -> {
               net.minecraft.block.spawner.MobSpawnerLogic result1 = null;
               try {
                  result1 = spawner.getLogic();
               } catch (Throwable capturedFailure3) {
                  pendingFailure = capturedFailure3;
               }
               var failed3 = pendingFailure != null;
               if (!(failed3)) {
                  net.minecraft.block.spawner.MobSpawnerLogic nextValue0 = result1;
                  saved3 = nextValue0;
                  step = 11;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 11 -> {
               net.minecraft.client.MinecraftClient field3 = null;
               try {
                  field3 = com.zenya.module.render.SpawnerTagsModule.mc;
               } catch (Throwable capturedFailure4) {
                  pendingFailure = capturedFailure4;
               }
               var failed4 = pendingFailure != null;
               if (!(failed4)) {
                  net.minecraft.client.MinecraftClient nextValue0 = field3;
                  saved4 = nextValue0;
                  step = 13;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 12 -> {
               var failure1 = pendingFailure;
               pendingFailure = null;
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 13 -> {
               if ((saved4 == null)) {
                  step = 14;
                  continue dispatch;
               } else {
                  step = 15;
                  continue dispatch;
               }
            }
            case 14 -> {
               pendingFailure = new java.lang.NullPointerException("object reference is null");
               var failure1 = pendingFailure;
               pendingFailure = null;
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 15 -> {
               net.minecraft.client.world.ClientWorld field4 = null;
               try {
                  field4 = saved4.world;
               } catch (Throwable capturedFailure5) {
                  pendingFailure = capturedFailure5;
               }
               var failed5 = pendingFailure != null;
               if (!(failed5)) {
                  net.minecraft.client.world.ClientWorld nextValue0 = field4;
                  saved5 = nextValue0;
                  step = 16;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 16 -> {
               if ((saved3 == null)) {
                  step = 17;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 17 -> {
               pendingFailure = new java.lang.NullPointerException("object reference is null");
               var failure1 = pendingFailure;
               pendingFailure = null;
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 18 -> {
               net.minecraft.entity.Entity result2 = null;
               try {
                  result2 = saved3.getRenderedEntity(((net.minecraft.world.World) saved5), pos);
               } catch (Throwable capturedFailure6) {
                  pendingFailure = capturedFailure6;
               }
               var failed6 = pendingFailure != null;
               if (!(failed6)) {
                  net.minecraft.entity.Entity nextValue0 = result2;
                  saved6 = nextValue0;
                  step = 19;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 19 -> {
               if ((saved6 == null)) {
                  step = 20;
                  continue dispatch;
               } else {
                  step = 21;
                  continue dispatch;
               }
            }
            case 20 -> {
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return null;
            }
            case 21 -> {
               net.minecraft.entity.EntityType result3 = null;
               try {
                  result3 = saved6.getType();
               } catch (Throwable capturedFailure7) {
                  pendingFailure = capturedFailure7;
               }
               var failed7 = pendingFailure != null;
               if (!(failed7)) {
                  net.minecraft.entity.EntityType nextValue0 = result3;
                  saved7 = nextValue0;
                  step = 22;
                  continue dispatch;
               } else {
                  step = 12;
                  continue dispatch;
               }
            }
            case 22 -> {
               if (pendingFailure != null) throw com.zenya.util.NativeExceptions.propagate(pendingFailure);
               return saved7;
            }
            default -> throw new IllegalStateException("Invalid control-flow state");
         }
      }
   }

   private String formatMobName(EntityType<?> type) {
      var field1 = net.minecraft.entity.EntityType.ZOMBIFIED_PIGLIN;
      if ((type == field1)) {
         return "Zombie Piglin";
      }
      var field2 = net.minecraft.entity.EntityType.PIGLIN_BRUTE;
      if ((type == field2)) {
         return "Piglin Brute";
      }
      var field3 = net.minecraft.entity.EntityType.IRON_GOLEM;
      if ((type == field3)) {
         return "Iron Golem";
      }
      var field4 = net.minecraft.entity.EntityType.SKELETON_HORSE;
      if ((type == field4)) {
         return "Skeleton Horse";
      }
      var field5 = net.minecraft.entity.EntityType.ZOMBIE_HORSE;
      if ((type == field5)) {
         return "Zombie Horse";
      }
      var field6 = net.minecraft.entity.EntityType.CAVE_SPIDER;
      if ((type == field6)) {
         return "Cave Spider";
      }
      var field7 = net.minecraft.entity.EntityType.MAGMA_CUBE;
      if ((type == field7)) {
         return "Magma Cube";
      }
      var field8 = net.minecraft.entity.EntityType.WITHER_SKELETON;
      if ((type == field8)) {
         return "Wither Skeleton";
      }
      var field9 = net.minecraft.entity.EntityType.BLAZE;
      if ((type == field9)) {
         return "Blaze";
      }
      var field10 = net.minecraft.entity.EntityType.SILVERFISH;
      if ((type == field10)) {
         return "Silverfish";
      }
      if ((type == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = type.getName();
      if ((result1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result2 = result1.getString();
      if ((result2 != null)) {
         var result3 = result2.isBlank();
         if (!(result3)) {
            return this.capitalizeWords(result2);
         }
         return type.toString();
      } else {
         return type.toString();
      }
   }

   private void renderTag(DrawContext context, TextRenderer renderer, String label, float x, float y, float tagScale, int textColor, int backgroundColor) {
      if ((renderer != null)) {
         var result1 = renderer.getWidth(label);
         var rounded1 = com.zenya.util.ClientMath.roundToInt(((float) ((-(8) - result1)) * 0.5F));
         if ((context == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result2 = context.getMatrices();
         if ((result2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result3 = result2.pushMatrix();
         var result4 = context.getMatrices();
         if ((result4 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result5 = result4.translate((float) ((double) (x)), (float) ((double) (y)));
         var result6 = context.getMatrices();
         if ((result6 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result7 = result6.scale((float) ((double) (tagScale)), (float) ((double) (tagScale)));
         context.fill((int) (Integer.toUnsignedLong(rounded1)), -13, ((result1 + 8) + rounded1), 0, backgroundColor);
         context.drawText(renderer, label, (rounded1 + 4), -11, textColor, true);
         var result8 = context.getMatrices();
         if ((result8 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result9 = result8.popMatrix();
         return;
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private double maxRenderDistanceSq() {
      var field1 = com.zenya.module.render.SpawnerTagsModule.mc;
      if ((field1 != null)) {
         var field2 = field1.options;
         if ((field2 != null)) {
            var result1 = field2.getClampedViewDistance();
            return (((double) ((result1 + 1)) * 16.0) * ((double) ((result1 + 1)) * 16.0));
         }
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private double squaredDistance(BlockPos pos, double playerX, double playerY, double playerZ) {
      if ((pos == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = pos.getX();
      var result2 = pos.getY();
      var result3 = pos.getZ();
      return ((((((double) (result3) + 0.5) - playerZ) * (((double) (result3) + 0.5) - playerZ)) + ((((double) (result2) + 0.5) - playerY) * (((double) (result2) + 0.5) - playerY))) + ((((double) (result1) + 0.5) - playerX) * (((double) (result1) + 0.5) - playerX)));
   }

   private int rgb(Color color) {
      if ((color == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = color.getRed();
      var result2 = color.getGreen();
      var result3 = color.getBlue();
      return ((((result2 << 8) | (result1 << 16)) | result3) | -16777216);
   }

   private int argb(Color color) {
      if ((color == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = color.getAlpha();
      var result2 = color.getRed();
      var result3 = color.getGreen();
      var result4 = color.getBlue();
      return ((((result3 << 8) | result4) | (result2 << 16)) | (result1 << 24));
   }

   private String capitalizeWords(String value) {
      var field1 = java.util.Locale.ROOT;
      if ((value != null)) {
         var result1 = value.toLowerCase(field1);
         if ((result1 != null)) {
            var length1 = (result1).length();
            var stringBuilder1 = new java.lang.StringBuilder(length1);
            boolean iteration1_1 = true;
            long iteration1_2 = 0L;
            long iteration1_3 = 0L;
            repeat1: while (true) {
               var length2 = (result1).length();
               if (((long) (length2) <= iteration1_3)) {
                  var result2 = stringBuilder1.toString();
                  java.lang.String joined1;
                  if ((result2 != null)) {
                     var result3 = result2.replace((char) (95), (char) (32));
                     if ((result3 != null)) {
                        return result3.replace((char) (45), (char) (32));
                     }
                     joined1 = result3;
                  } else {
                     joined1 = result2;
                  }
                  throw new java.lang.NullPointerException("object reference is null");
               } else {
                  var character1 = (result1).charAt((int) (iteration1_3));
                  if (iteration1_1) {
                     var result2 = java.lang.Character.toUpperCase((char) ((long) (character1)));
                     var result3 = stringBuilder1.append((char) ((long) (result2)));
                     if (Integer.compareUnsigned(63, (character1 - 32)) < 0) {
                        iteration1_1 = false;
                        iteration1_3 = (iteration1_3 + 1);
                        iteration1_2 = (iteration1_2 + 2);
                        continue repeat1;
                     } else {
                        iteration1_1 = true;
                        if ((((-9223372036854767615L >> (Integer.toUnsignedLong((character1 - 32)) & 63)) & 1) == 0)) {
                           iteration1_1 = false;
                           iteration1_3 = (iteration1_3 + 1);
                           iteration1_2 = (iteration1_2 + 2);
                           continue repeat1;
                        } else {
                           iteration1_3 = (iteration1_3 + 1);
                           iteration1_2 = (iteration1_2 + 2);
                           continue repeat1;
                        }
                     }
                  } else {
                     var result2 = stringBuilder1.append((char) (Integer.toUnsignedLong((int) (character1))));
                     if (Integer.compareUnsigned(63, (character1 - 32)) < 0) {
                        iteration1_1 = false;
                        iteration1_3 = (iteration1_3 + 1);
                        iteration1_2 = (iteration1_2 + 2);
                        continue repeat1;
                     } else {
                        iteration1_1 = true;
                        if ((((-9223372036854767615L >> (Integer.toUnsignedLong((character1 - 32)) & 63)) & 1) == 0)) {
                           iteration1_1 = false;
                           iteration1_3 = (iteration1_3 + 1);
                           iteration1_2 = (iteration1_2 + 2);
                           continue repeat1;
                        } else {
                           iteration1_3 = (iteration1_3 + 1);
                           iteration1_2 = (iteration1_2 + 2);
                           continue repeat1;
                        }
                     }
                  }
               }
            }
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static boolean lambda$scanChunk$2(long chunkKey, Entry entry) {
      if ((entry != null)) {
         var result1 = entry.getValue();
         if ((result1 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         if (!((result1 == null || result1 instanceof com.zenya.module.render.SpawnerTagsModule.SpawnerTag))) {
            throw new java.lang.ClassCastException("");
         }
         var field1 = ((com.zenya.module.render.SpawnerTagsModule.SpawnerTag) result1).pos;
         var chunkPos1 = new net.minecraft.util.math.ChunkPos(field1);
         var result2 = chunkPos1.toLong();
         return (result2 == chunkKey);
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static double lambda$renderHudInternal$1(SpawnerTagsModule.SpawnerTag tag) {
      int saved1 = 0;
      int saved2 = 0;
      int step = 0;
      dispatch: while (true) {
         switch (step) {
            case 0 -> {
               if ((tag == null)) {
                  int nextValue0 = 0;
                  int nextValue1 = 0;
                  saved1 = nextValue0;
                  saved2 = nextValue1;
                  step = 1;
                  continue dispatch;
               } else {
                  int nextValue0 = 0;
                  int nextValue1 = 0;
                  saved1 = nextValue0;
                  saved2 = nextValue1;
                  step = 2;
                  continue dispatch;
               }
            }
            case 1 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 2 -> {
               var field1 = tag.distanceSq;
               return Double.longBitsToDouble((((long) (int) (((int) ((Double.doubleToRawLongBits(field1) >>> 32)) ^ -2147483648)) << 32) | Integer.toUnsignedLong((int) (((int) Double.doubleToRawLongBits(field1) ^ 0)))));
            }
            default -> throw new IllegalStateException("Invalid control-flow state");
         }
      }
   }

   private void lambda$onPacketReceive$0(BlockPos pos, BlockState state) {
      var field1 = net.minecraft.block.Blocks.SPAWNER;
      if ((state == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = state.isOf(field1);
      if (!(result1)) {
         var field2 = this.spawners;
         if ((pos == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result2 = pos.asLong();
         if ((field2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result3 = ((java.util.Map) field2).remove(java.lang.Long.valueOf(result2));
         var field3 = this.spawners;
         if ((field3 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         this.spawnerCount = field3.size();
         return;
      } else {
         var field2 = com.zenya.module.render.SpawnerTagsModule.mc;
         if ((field2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var field3 = field2.world;
         if ((field3 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result2 = field3.getChunkManager();
         var chunkPos1 = new net.minecraft.util.math.ChunkPos(pos);
         var field4 = chunkPos1.x;
         var chunkPos2 = new net.minecraft.util.math.ChunkPos(pos);
         var field5 = chunkPos2.z;
         if ((result2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result3 = result2.getWorldChunk(field4, field5, false);
         if ((result3 != null)) {
            this.scanChunk(result3);
            return;
         }
         return;
      }
   }

   private static final class SpawnerTag {
      private final long key;
      private final BlockPos pos;
      private final String label;
      private double distanceSq;

      private SpawnerTag(long key, BlockPos pos, String label) {
         this.key = key;
         this.pos = pos.toImmutable();
         this.label = label;
      }
   }
}
