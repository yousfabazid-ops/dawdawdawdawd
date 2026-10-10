package com.zenya.module.smps;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.setting.Setting;
import java.awt.Color;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.ChunkPos;

public final class PlayerChunkFinderModule extends Module {
   private final Setting<Boolean> detectUnnaturalBlocks;
   private final Setting<Integer> renderRadius;
   private final Setting<Double> renderY;
   private final Setting<Boolean> filledEsp;
   private final Setting<Color> sideColor;
   private final Setting<Color> lineColor;
   private final Map<ChunkPos, String> modifiedChunks;

   public PlayerChunkFinderModule() {
      super("Player Chunk Finder", Category.SMPS);
      Setting<Boolean> setting1 = new com.zenya.setting.Setting<>("Detect Unnatural Blocks", java.lang.Boolean.valueOf(true));
      this.detectUnnaturalBlocks = setting1;
      Setting<Integer> setting2 = new com.zenya.setting.Setting<>("Grid Radius", java.lang.Integer.valueOf(8), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(32));
      this.renderRadius = setting2;
      Setting<Double> setting3 = new com.zenya.setting.Setting<>("Render Y", java.lang.Double.valueOf(-60.0), java.lang.Double.valueOf(-64.0), java.lang.Double.valueOf(320.0));
      this.renderY = setting3;
      Setting<Boolean> setting4 = new com.zenya.setting.Setting<>("Filled ESP", java.lang.Boolean.valueOf(true));
      this.filledEsp = setting4;
      var color1 = new java.awt.Color(255, 82, 82, 80);
      Setting<Color> setting5 = new com.zenya.setting.Setting<>("Side Color", color1);
      this.sideColor = setting5;
      var color2 = new java.awt.Color(255, 82, 82, 255);
      Setting<Color> setting6 = new com.zenya.setting.Setting<>("Line Color", color2);
      this.lineColor = setting6;
      Map<ChunkPos, String> linkedHashMap1 = new java.util.LinkedHashMap<>();
      this.modifiedChunks = linkedHashMap1;
      this.setDescription("Detects underground player-modified chunks.");
      this.addSetting(this.detectUnnaturalBlocks);
      this.addSetting(this.renderRadius);
      this.addSetting(this.renderY);
      this.addSetting(this.filledEsp);
      this.addSetting(this.sideColor);
      this.addSetting(this.lineColor);
   }

   private static boolean isUnnaturalUndergroundBlock(Block block) {
      var field1 = net.minecraft.block.Blocks.COBBLESTONE;
      if ((block == field1)) {
         return true;
      }
      var field2 = net.minecraft.block.Blocks.COBBLED_DEEPSLATE;
      if ((block == field2)) {
         return true;
      }
      var field3 = net.minecraft.block.Blocks.OAK_PLANKS;
      if ((block == field3)) {
         return true;
      }
      var field4 = net.minecraft.block.Blocks.SPRUCE_PLANKS;
      if ((block == field4)) {
         return true;
      }
      var field5 = net.minecraft.block.Blocks.BIRCH_PLANKS;
      if ((block == field5)) {
         return true;
      }
      var field6 = net.minecraft.block.Blocks.JUNGLE_PLANKS;
      if ((block == field6)) {
         return true;
      }
      var field7 = net.minecraft.block.Blocks.ACACIA_PLANKS;
      if ((block == field7)) {
         return true;
      }
      var field8 = net.minecraft.block.Blocks.DARK_OAK_PLANKS;
      if ((block == field8)) {
         return true;
      }
      var field9 = net.minecraft.block.Blocks.MANGROVE_PLANKS;
      if ((block == field9)) {
         return true;
      }
      var field10 = net.minecraft.block.Blocks.CHERRY_PLANKS;
      if ((block == field10)) {
         return true;
      }
      var field11 = net.minecraft.block.Blocks.BAMBOO_PLANKS;
      if ((block == field11)) {
         return true;
      }
      var field12 = net.minecraft.block.Blocks.CRIMSON_PLANKS;
      if ((block == field12)) {
         return true;
      }
      var field13 = net.minecraft.block.Blocks.WARPED_PLANKS;
      if ((block == field13)) {
         return true;
      }
      var field14 = net.minecraft.block.Blocks.TORCH;
      if ((block == field14)) {
         return true;
      }
      var field15 = net.minecraft.block.Blocks.WALL_TORCH;
      if ((block == field15)) {
         return true;
      }
      var field16 = net.minecraft.block.Blocks.LADDER;
      if ((block == field16)) {
         return true;
      }
      var field17 = net.minecraft.block.Blocks.RAIL;
      if ((block == field17)) {
         return true;
      }
      var field18 = net.minecraft.block.Blocks.CRAFTING_TABLE;
      if ((block == field18)) {
         return true;
      }
      var field19 = net.minecraft.block.Blocks.FURNACE;
      if ((block == field19)) {
         return true;
      }
      var field20 = net.minecraft.block.Blocks.BLAST_FURNACE;
      if ((block == field20)) {
         return true;
      }
      var field21 = net.minecraft.block.Blocks.SMOKER;
      if ((block == field21)) {
         return true;
      }
      var field22 = net.minecraft.block.Blocks.CHEST;
      if ((block == field22)) {
         return true;
      }
      var field23 = net.minecraft.block.Blocks.TRAPPED_CHEST;
      if ((block == field23)) {
         return true;
      }
      var field24 = net.minecraft.block.Blocks.BARREL;
      if ((block == field24)) {
         return true;
      }
      var field25 = net.minecraft.block.Blocks.GLASS;
      return (block == field25);
   }

   @Override
   public void onEnable() {
      this.modifiedChunks.clear();
   }

   @Override
   public void onDisable() {
      this.modifiedChunks.clear();
   }

   @Override
   public void onPacketReceive(Packet<?> packet) {
      if ((packet != null)) {
         if ((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket)) {
            if (!((packet == null || packet instanceof net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket))) {
               throw new java.lang.ClassCastException("");
            }
            var field1 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
            if ((field1 != null)) {
               var field2 = field1.world;
               if ((field2 == null)) {
                  return;
               }
               var field3 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
               if ((field3 != null)) {
                  var field4 = field3.player;
                  if ((field4 == null)) {
                     return;
                  }
                  var result1 = ((net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket) packet).getChunkX();
                  var result2 = ((net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket) packet).getChunkZ();
                  var chunkPos1 = new net.minecraft.util.math.ChunkPos(result1, result2);
                  var field5 = this.modifiedChunks;
                  if ((field5 != null)) {
                     var result3 = ((java.util.Map) field5).containsKey(chunkPos1);
                     if (result3) {
                        return;
                     }
                     var field6 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                     if ((field6 != null)) {
                        var field7 = field6.world;
                        if ((field7 != null)) {
                           var result4 = field7.getChunkManager();
                           var field8 = chunkPos1.x;
                           var field9 = chunkPos1.z;
                           if ((result4 != null)) {
                              var result5 = result4.getWorldChunk((int) (Integer.toUnsignedLong(field8)), field9, false);
                              if ((result5 == null)) {
                                 return;
                              }
                              var result6 = this.scanChunk(chunkPos1);
                              if ((result6 == null)) {
                                 return;
                              }
                              var field10 = this.modifiedChunks;
                              if ((field10 != null)) {
                                 var result7 = ((java.util.Map) field10).put(chunkPos1, result6);
                                 var field11 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                                 if ((field11 != null)) {
                                    var field12 = field11.player;
                                    var stringBuilder1 = new java.lang.StringBuilder();
                                    var result8 = stringBuilder1.append("\u00a7b[Player Chunk Finder]\u00a7r Modified chunk at ");
                                    var field13 = chunkPos1.x;
                                    if ((result8 != null)) {
                                       var result9 = result8.append((int) (Integer.toUnsignedLong(field13)));
                                       if ((result9 != null)) {
                                          var result10 = result9.append(", ");
                                          var field14 = chunkPos1.z;
                                          if ((result10 != null)) {
                                             var result11 = result10.append((int) (Integer.toUnsignedLong(field14)));
                                             if ((result11 != null)) {
                                                var result12 = result11.append(" \u00a77(");
                                                if ((result12 != null)) {
                                                   var result13 = result12.append(result6);
                                                   if ((result13 != null)) {
                                                      var result14 = result13.append(")");
                                                      if ((result14 != null)) {
                                                         var result15 = result14.toString();
                                                         var result16 = net.minecraft.text.Text.literal(result15);
                                                         if ((field12 != null)) {
                                                            field12.sendMessage(((net.minecraft.text.Text) result16), false);
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
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                }
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
                                       throw new java.lang.NullPointerException("object reference is null");
                                    }
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
                           throw new java.lang.NullPointerException("object reference is null");
                        }
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
               throw new java.lang.NullPointerException("object reference is null");
            }
         } else {
            return;
         }
      } else {
         return;
      }
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      var field1 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.world;
         if ((field2 == null)) {
            return;
         } else {
            var field3 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var field4 = field3.player;
               if ((field4 == null)) {
                  return;
               } else {
                  var field5 = this.modifiedChunks;
                  if ((field5 == null)) {
                     throw new java.lang.NullPointerException("object reference is null");
                  } else {
                     var result1 = field5.isEmpty();
                     if (result1) {
                        return;
                     } else {
                        var result2 = com.zenya.render.WorldRenderer.getCamera();
                        if ((result2 == null)) {
                           return;
                        } else {
                           var result3 = com.zenya.render.WorldRenderer.getCameraPos(result2);
                           var field6 = this.renderRadius;
                           if ((field6 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result4 = field6.getValue();
                              if ((result4 != null)) {
                                 if (!((result4 == null || result4 instanceof java.lang.Integer))) {
                                    throw new java.lang.ClassCastException("");
                                 } else {
                                    var result5 = ((java.lang.Integer) result4).intValue();
                                    var field7 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                                    if ((field7 != null)) {
                                       var field8 = field7.player;
                                       if ((field8 != null)) {
                                          var result6 = field8.getChunkPos();
                                          if ((result6 != null)) {
                                             var field9 = result6.x;
                                             var field10 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                                             if ((field10 != null)) {
                                                var field11 = field10.player;
                                                if ((field11 != null)) {
                                                   var result7 = field11.getChunkPos();
                                                   if ((result7 != null)) {
                                                      var field12 = result7.z;
                                                      var field13 = this.renderY;
                                                      if ((field13 != null)) {
                                                         var result8 = field13.getValue();
                                                         if ((result8 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            if (!((result8 == null || result8 instanceof java.lang.Double))) {
                                                               throw new java.lang.ClassCastException("");
                                                            } else {
                                                               var result9 = ((java.lang.Double) result8).doubleValue();
                                                               if ((result3 != null)) {
                                                                  var field14 = result3.y;
                                                                  var result10 = com.zenya.render.WorldRenderer.beginWorldBatch(matrices);
                                                                  var field15 = this.modifiedChunks;
                                                                  if ((field15 != null)) {
                                                                     var result11 = field15.keySet();
                                                                     if ((result11 != null)) {
                                                                        var result12 = result11.iterator();
                                                                        if ((result12 != null)) {
                                                                           repeat1: while (true) {
                                                                              var result13 = result12.hasNext();
                                                                              if (!(result13)) {
                                                                                 return;
                                                                              } else {
                                                                                 var result14 = result12.next();
                                                                                 if ((result14 != null)) {
                                                                                    if (!((result14 == null || result14 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                                       throw new java.lang.ClassCastException("");
                                                                                    } else {
                                                                                       if ((result14 == null)) {
                                                                                          throw new java.lang.NullPointerException("object reference is null");
                                                                                       } else {
                                                                                          var field16 = ((net.minecraft.util.math.ChunkPos) result14).x;
                                                                                          var selected1 = ((field9 < field16) ? (field16 - field9) : (field9 - field16));
                                                                                          if ((selected1 <= result5)) {
                                                                                             var field17 = ((net.minecraft.util.math.ChunkPos) result14).z;
                                                                                             var selected2 = ((field12 < field17) ? (field17 - field12) : (field12 - field17));
                                                                                             if (Integer.compareUnsigned(selected2, result5) <= 0) {
                                                                                                var result15 = ((net.minecraft.util.math.ChunkPos) result14).getStartX();
                                                                                                var field18 = result3.x;
                                                                                                var result16 = ((net.minecraft.util.math.ChunkPos) result14).getStartZ();
                                                                                                var field19 = result3.z;
                                                                                                var field20 = this.filledEsp;
                                                                                                if ((field20 == null)) {
                                                                                                   throw new java.lang.NullPointerException("object reference is null");
                                                                                                } else {
                                                                                                   var result17 = field20.getValue();
                                                                                                   if ((result17 == null)) {
                                                                                                      throw new java.lang.NullPointerException("object reference is null");
                                                                                                   } else {
                                                                                                      if (!((result17 == null || result17 instanceof java.lang.Boolean))) {
                                                                                                         throw new java.lang.ClassCastException("");
                                                                                                      } else {
                                                                                                         var result18 = ((java.lang.Boolean) result17).booleanValue();
                                                                                                         if (result18) {
                                                                                                            var field21 = this.sideColor;
                                                                                                            if ((field21 == null)) {
                                                                                                               throw new java.lang.NullPointerException("object reference is null");
                                                                                                            } else {
                                                                                                               var result19 = field21.getValue();
                                                                                                               if ((result19 != null)) {
                                                                                                                  if (!((result19 == null || result19 instanceof java.awt.Color))) {
                                                                                                                     throw new java.lang.ClassCastException("");
                                                                                                                  } else {
                                                                                                                     if ((result10 == null)) {
                                                                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                                                                     } else {
                                                                                                                        result10.renderFilledBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                        var field22 = this.lineColor;
                                                                                                                        if ((field22 == null)) {
                                                                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                                                                        } else {
                                                                                                                           var result20 = field22.getValue();
                                                                                                                           if ((result20 != null)) {
                                                                                                                              if (!((result20 == null || result20 instanceof java.awt.Color))) {
                                                                                                                                 throw new java.lang.ClassCastException("");
                                                                                                                              } else {
                                                                                                                                 result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                                 continue repeat1;
                                                                                                                              }
                                                                                                                           } else {
                                                                                                                              result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                              continue repeat1;
                                                                                                                           }
                                                                                                                        }
                                                                                                                     }
                                                                                                                  }
                                                                                                               } else {
                                                                                                                  if ((result10 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     result10.renderFilledBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                     var field22 = this.lineColor;
                                                                                                                     if ((field22 == null)) {
                                                                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                                                                     } else {
                                                                                                                        var result20 = field22.getValue();
                                                                                                                        if ((result20 != null)) {
                                                                                                                           if (!((result20 == null || result20 instanceof java.awt.Color))) {
                                                                                                                              throw new java.lang.ClassCastException("");
                                                                                                                           } else {
                                                                                                                              result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                              continue repeat1;
                                                                                                                           }
                                                                                                                        } else {
                                                                                                                           result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                           continue repeat1;
                                                                                                                        }
                                                                                                                     }
                                                                                                                  }
                                                                                                               }
                                                                                                            }
                                                                                                         } else {
                                                                                                            var field21 = this.lineColor;
                                                                                                            if ((field21 == null)) {
                                                                                                               throw new java.lang.NullPointerException("object reference is null");
                                                                                                            } else {
                                                                                                               var result19 = field21.getValue();
                                                                                                               if ((result19 != null)) {
                                                                                                                  if (!((result19 == null || result19 instanceof java.awt.Color))) {
                                                                                                                     throw new java.lang.ClassCastException("");
                                                                                                                  } else {
                                                                                                                     if ((result10 == null)) {
                                                                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                                                                     } else {
                                                                                                                        result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                        continue repeat1;
                                                                                                                     }
                                                                                                                  }
                                                                                                               } else {
                                                                                                                  if ((result10 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                     continue repeat1;
                                                                                                                  }
                                                                                                               }
                                                                                                            }
                                                                                                         }
                                                                                                      }
                                                                                                   }
                                                                                                }
                                                                                             } else {
                                                                                                continue repeat1;
                                                                                             }
                                                                                          } else {
                                                                                             continue repeat1;
                                                                                          }
                                                                                       }
                                                                                    }
                                                                                 } else {
                                                                                    if ((result14 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var field16 = ((net.minecraft.util.math.ChunkPos) result14).x;
                                                                                       var selected1 = ((field9 < field16) ? (field16 - field9) : (field9 - field16));
                                                                                       if ((selected1 <= result5)) {
                                                                                          var field17 = ((net.minecraft.util.math.ChunkPos) result14).z;
                                                                                          var selected2 = ((field12 < field17) ? (field17 - field12) : (field12 - field17));
                                                                                          if (Integer.compareUnsigned(selected2, result5) <= 0) {
                                                                                             var result15 = ((net.minecraft.util.math.ChunkPos) result14).getStartX();
                                                                                             var field18 = result3.x;
                                                                                             var result16 = ((net.minecraft.util.math.ChunkPos) result14).getStartZ();
                                                                                             var field19 = result3.z;
                                                                                             var field20 = this.filledEsp;
                                                                                             if ((field20 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                var result17 = field20.getValue();
                                                                                                if ((result17 == null)) {
                                                                                                   throw new java.lang.NullPointerException("object reference is null");
                                                                                                } else {
                                                                                                   if (!((result17 == null || result17 instanceof java.lang.Boolean))) {
                                                                                                      throw new java.lang.ClassCastException("");
                                                                                                   } else {
                                                                                                      var result18 = ((java.lang.Boolean) result17).booleanValue();
                                                                                                      if (result18) {
                                                                                                         var field21 = this.sideColor;
                                                                                                         if ((field21 == null)) {
                                                                                                            throw new java.lang.NullPointerException("object reference is null");
                                                                                                         } else {
                                                                                                            var result19 = field21.getValue();
                                                                                                            if ((result19 != null)) {
                                                                                                               if (!((result19 == null || result19 instanceof java.awt.Color))) {
                                                                                                                  throw new java.lang.ClassCastException("");
                                                                                                               } else {
                                                                                                                  if ((result10 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     result10.renderFilledBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                     var field22 = this.lineColor;
                                                                                                                     if ((field22 == null)) {
                                                                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                                                                     } else {
                                                                                                                        var result20 = field22.getValue();
                                                                                                                        if ((result20 != null)) {
                                                                                                                           if (!((result20 == null || result20 instanceof java.awt.Color))) {
                                                                                                                              throw new java.lang.ClassCastException("");
                                                                                                                           } else {
                                                                                                                              result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                              continue repeat1;
                                                                                                                           }
                                                                                                                        } else {
                                                                                                                           result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                           continue repeat1;
                                                                                                                        }
                                                                                                                     }
                                                                                                                  }
                                                                                                               }
                                                                                                            } else {
                                                                                                               if ((result10 == null)) {
                                                                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                                                                               } else {
                                                                                                                  result10.renderFilledBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                  var field22 = this.lineColor;
                                                                                                                  if ((field22 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     var result20 = field22.getValue();
                                                                                                                     if ((result20 != null)) {
                                                                                                                        if (!((result20 == null || result20 instanceof java.awt.Color))) {
                                                                                                                           throw new java.lang.ClassCastException("");
                                                                                                                        } else {
                                                                                                                           result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                           continue repeat1;
                                                                                                                        }
                                                                                                                     } else {
                                                                                                                        result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result20));
                                                                                                                        continue repeat1;
                                                                                                                     }
                                                                                                                  }
                                                                                                               }
                                                                                                            }
                                                                                                         }
                                                                                                      } else {
                                                                                                         var field21 = this.lineColor;
                                                                                                         if ((field21 == null)) {
                                                                                                            throw new java.lang.NullPointerException("object reference is null");
                                                                                                         } else {
                                                                                                            var result19 = field21.getValue();
                                                                                                            if ((result19 != null)) {
                                                                                                               if (!((result19 == null || result19 instanceof java.awt.Color))) {
                                                                                                                  throw new java.lang.ClassCastException("");
                                                                                                               } else {
                                                                                                                  if ((result10 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                     continue repeat1;
                                                                                                                  }
                                                                                                               }
                                                                                                            } else {
                                                                                                               if ((result10 == null)) {
                                                                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                                                                               } else {
                                                                                                                  result10.renderOutlineBox(((double) (result15) - field18), (result9 - field14), ((double) (result16) - field19), (((double) (result15) - field18) + 16.0), (0.12 + (result9 - field14)), (((double) (result16) - field19) + 16.0), ((java.awt.Color) result19));
                                                                                                                  continue repeat1;
                                                                                                               }
                                                                                                            }
                                                                                                         }
                                                                                                      }
                                                                                                   }
                                                                                                }
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
                                                                           }
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
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               }
                                                            }
                                                         }
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
                                                throw new java.lang.NullPointerException("object reference is null");
                                             }
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
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private String scanChunk(ChunkPos cp) {
      if ((cp != null)) {
         var result1 = cp.getStartX();
         var result2 = cp.getStartZ();
         var field1 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
         if ((field1 != null)) {
            var field2 = field1.world;
            if ((field2 != null)) {
               var result3 = field2.getBottomY();
               var selected1 = ((-(64) < result3) ? result3 : -64);
               var field3 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
               if ((field3 != null)) {
                  var field4 = field3.world;
                  if ((field4 != null)) {
                     var result4 = field4.getBottomY();
                     var field5 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                     if ((field5 != null)) {
                        var field6 = field5.world;
                        if ((field6 != null)) {
                           var result5 = field6.getHeight();
                           var selected2 = (((result5 + result4) < 1) ? (result5 + result4) : 1);
                           int iteration1_1 = result2;
                           int iteration1_2 = result1;
                           repeat1: while (true) {
                              var mutable1 = new net.minecraft.util.math.BlockPos.Mutable(iteration1_2, selected1, iteration1_1);
                              if ((selected1 < (selected2 - 1))) {
                                 int iteration2_1 = selected1;
                                 repeat2: while (true) {
                                    var result6 = mutable1.setY((int) (Integer.toUnsignedLong(iteration2_1)));
                                    var field7 = com.zenya.module.smps.PlayerChunkFinderModule.mc;
                                    if ((field7 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var field8 = field7.world;
                                       if ((field8 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var result7 = field8.getBlockState(((net.minecraft.util.math.BlockPos) mutable1));
                                          var result8 = com.zenya.module.ModuleUtils.isRotatedDeepslate(result7);
                                          if (result8) {
                                             return "MODIFIED_DEEPSLATE";
                                          } else {
                                             var field9 = this.detectUnnaturalBlocks;
                                             if ((field9 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var result9 = field9.getValue();
                                                if ((result9 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   if (!((result9 == null || result9 instanceof java.lang.Boolean))) {
                                                      throw new java.lang.ClassCastException("");
                                                   } else {
                                                      var result10 = ((java.lang.Boolean) result9).booleanValue();
                                                      if (result10) {
                                                         if ((result7 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var result11 = result7.getBlock();
                                                            var result12 = com.zenya.module.smps.PlayerChunkFinderModule.isUnnaturalUndergroundBlock(result11);
                                                            if (result12) {
                                                               return "UNNATURAL";
                                                            } else {
                                                               iteration2_1 = (iteration2_1 + 1);
                                                               if (((selected2 - 1) != iteration2_1)) {
                                                                  continue repeat2;
                                                               } else {
                                                                  var saved1 = ((result2 + 15) <= iteration1_1);
                                                                  iteration1_1 = (iteration1_1 + 1);
                                                                  if (saved1) {
                                                                     var saved2 = ((result1 + 15) <= iteration1_2);
                                                                     iteration1_1 = result2;
                                                                     iteration1_2 = (iteration1_2 + 1);
                                                                     if (saved2) {
                                                                        return null;
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
                                                         iteration2_1 = (iteration2_1 + 1);
                                                         if (((selected2 - 1) != iteration2_1)) {
                                                            continue repeat2;
                                                         } else {
                                                            var saved1 = ((result2 + 15) <= iteration1_1);
                                                            iteration1_1 = (iteration1_1 + 1);
                                                            if (saved1) {
                                                               var saved2 = ((result1 + 15) <= iteration1_2);
                                                               iteration1_1 = result2;
                                                               iteration1_2 = (iteration1_2 + 1);
                                                               if (saved2) {
                                                                  return null;
                                                               } else {
                                                                  continue repeat1;
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
                              } else {
                                 var saved1 = ((result2 + 15) <= iteration1_1);
                                 iteration1_1 = (iteration1_1 + 1);
                                 if (saved1) {
                                    var saved2 = ((result1 + 15) <= iteration1_2);
                                    iteration1_1 = result2;
                                    iteration1_2 = (iteration1_2 + 1);
                                    if (saved2) {
                                       return null;
                                    } else {
                                       continue repeat1;
                                    }
                                 } else {
                                    continue repeat1;
                                 }
                              }
                           }
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
                  throw new java.lang.NullPointerException("object reference is null");
               }
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
