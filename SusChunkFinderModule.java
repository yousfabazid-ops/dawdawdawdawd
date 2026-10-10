package com.zenya.module.donut;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.setting.Setting;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class SusChunkFinderModule extends Module {
   private static final String FIXED_WORLD_SEED = "6608149111735331168";
   private static final int CHUNKS_PER_TICK = 512;
   private static final int RESCAN_TICKS = 100;
   private static final double OVERLAY_Y = 63.0;
   private static final double AMETHYST_RENDER_DISTANCE_BLOCKS = 300.0;
   private static final int AMETHYST_RENDER_DISTANCE_CHUNKS;
   private final Setting<Integer> amethystThreshold;
   private final Setting<Integer> scanRadius;
   private final Setting<Integer> simulationDistance;
   private final Setting<Integer> overlapSensitivity;
   private final Setting<Color> overlayColor;
   private final Setting<Integer> alpha;
   private final Map<ChunkPos, SusChunkFinderModule.HitType> hits;
   private final Set<ChunkPos> scanned;
   private final Set<ChunkPos> queued;
   private final ArrayDeque<ChunkPos> scanQueue;
   private int rescanTimer;

   public SusChunkFinderModule() {
      super("Sus Chunk Finder", Category.DONUT);
      Setting<Integer> setting1 = new com.zenya.setting.Setting<>("Amethyst Threshold", java.lang.Integer.valueOf(25), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(200));
      this.amethystThreshold = setting1;
      Setting<Integer> setting2 = new com.zenya.setting.Setting<>("Scan Radius", java.lang.Integer.valueOf(com.zenya.module.donut.SusChunkFinderModule.AMETHYST_RENDER_DISTANCE_CHUNKS), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(25));
      this.scanRadius = setting2;
      Setting<Integer> setting3 = new com.zenya.setting.Setting<>("Simulation Distance", java.lang.Integer.valueOf(4), java.lang.Integer.valueOf(2), java.lang.Integer.valueOf(12));
      this.simulationDistance = setting3;
      Setting<Integer> setting4 = new com.zenya.setting.Setting<>("Overlap Sensitivity", java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(20));
      this.overlapSensitivity = setting4;
      var color1 = new java.awt.Color(255, 0, 0, 120);
      Setting<Color> setting5 = new com.zenya.setting.Setting<>("Color", color1);
      this.overlayColor = setting5;
      Setting<Integer> setting6 = new com.zenya.setting.Setting<>("Alpha", java.lang.Integer.valueOf(120), java.lang.Integer.valueOf(0), java.lang.Integer.valueOf(255));
      this.alpha = setting6;
      Map<ChunkPos, SusChunkFinderModule.HitType> concurrentHashMap1 = new java.util.concurrent.ConcurrentHashMap<>();
      this.hits = concurrentHashMap1;
      Set<ChunkPos> result1 = java.util.concurrent.ConcurrentHashMap.newKeySet();
      this.scanned = result1;
      Set<ChunkPos> result2 = java.util.concurrent.ConcurrentHashMap.newKeySet();
      this.queued = result2;
      ArrayDeque<ChunkPos> arrayDeque1 = new java.util.ArrayDeque<>();
      this.scanQueue = arrayDeque1;
      this.setDescription("Marks suspicious chunks using amethyst clusters and calculated geode overlaps.");
      this.addSetting(this.amethystThreshold);
      this.addSetting(this.scanRadius);
      this.addSetting(this.simulationDistance);
      this.addSetting(this.overlapSensitivity);
      this.addSetting(this.overlayColor);
      this.addSetting(this.alpha);
   }

   private static boolean isAmethyst(Block block) {
      var field1 = net.minecraft.block.Blocks.AMETHYST_BLOCK;
      if ((block == field1)) {
         return true;
      }
      var field2 = net.minecraft.block.Blocks.BUDDING_AMETHYST;
      if ((block == field2)) {
         return true;
      }
      var field3 = net.minecraft.block.Blocks.AMETHYST_CLUSTER;
      if ((block == field3)) {
         return true;
      }
      var field4 = net.minecraft.block.Blocks.LARGE_AMETHYST_BUD;
      if ((block == field4)) {
         return true;
      }
      var field5 = net.minecraft.block.Blocks.MEDIUM_AMETHYST_BUD;
      if ((block == field5)) {
         return true;
      }
      var field6 = net.minecraft.block.Blocks.SMALL_AMETHYST_BUD;
      return (block == field6);
   }

   @Override
   public void onEnable() {
      this.clear();
   }

   @Override
   public void onDisable() {
      this.clear();
   }

   @Override
   public void onWorldChange() {
      this.clear();
   }

   @Override
   public void onTick() {
      var field1 = com.zenya.module.donut.SusChunkFinderModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.world;
         if ((field2 != null)) {
            var field3 = com.zenya.module.donut.SusChunkFinderModule.mc;
            if ((field3 != null)) {
               var field4 = field3.player;
               if ((field4 == null)) {
                  return;
               } else {
                  var field5 = this.rescanTimer;
                  this.rescanTimer = (field5 + 1);
                  if ((98 < field5)) {
                     var field6 = this.scanned;
                     if ((field6 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     } else {
                        field6.clear();
                        this.cleanUpFarHits();
                        this.rescanTimer = 0;
                        this.enqueueChunks();
                        int iteration1_1 = 0;
                        repeat1: while (true) {
                           var field7 = this.scanQueue;
                           if ((field7 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result1 = field7.poll();
                              if ((result1 == null)) {
                                 return;
                              } else {
                                 if (!((result1 == null || result1 instanceof net.minecraft.util.math.ChunkPos))) {
                                    throw new java.lang.ClassCastException("");
                                 } else {
                                    var field8 = this.queued;
                                    if ((field8 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var result2 = ((java.util.Set) field8).remove(result1);
                                       var field9 = this.scanned;
                                       if ((field9 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var result3 = ((java.util.Set) field9).contains(result1);
                                          if (!(result3)) {
                                             var field10 = com.zenya.module.donut.SusChunkFinderModule.mc;
                                             if ((field10 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field11 = field10.world;
                                                if ((field11 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result4 = field11.getChunkManager();
                                                   var field12 = ((net.minecraft.util.math.ChunkPos) result1).x;
                                                   var field13 = ((net.minecraft.util.math.ChunkPos) result1).z;
                                                   if ((result4 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var result5 = result4.getWorldChunk(field12, field13, false);
                                                      if ((result5 == null)) {
                                                         if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                                            return;
                                                         } else {
                                                            iteration1_1 = (iteration1_1 + 1);
                                                            continue repeat1;
                                                         }
                                                      } else {
                                                         var field14 = this.scanned;
                                                         if ((field14 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var result6 = ((java.util.Set) field14).add(result1);
                                                            this.scanChunk(result5, ((net.minecraft.util.math.ChunkPos) result1));
                                                            if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                                               return;
                                                            } else {
                                                               iteration1_1 = (iteration1_1 + 1);
                                                               continue repeat1;
                                                            }
                                                         }
                                                      }
                                                   }
                                                }
                                             }
                                          } else {
                                             if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                                return;
                                             } else {
                                                iteration1_1 = (iteration1_1 + 1);
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
                  } else {
                     this.enqueueChunks();
                     int iteration1_1 = 0;
                     repeat1: while (true) {
                        var field6 = this.scanQueue;
                        if ((field6 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           var result1 = field6.poll();
                           if ((result1 == null)) {
                              return;
                           } else {
                              if (!((result1 == null || result1 instanceof net.minecraft.util.math.ChunkPos))) {
                                 throw new java.lang.ClassCastException("");
                              } else {
                                 var field7 = this.queued;
                                 if ((field7 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    var result2 = ((java.util.Set) field7).remove(result1);
                                    var field8 = this.scanned;
                                    if ((field8 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var result3 = ((java.util.Set) field8).contains(result1);
                                       if (!(result3)) {
                                          var field9 = com.zenya.module.donut.SusChunkFinderModule.mc;
                                          if ((field9 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field10 = field9.world;
                                             if ((field10 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var result4 = field10.getChunkManager();
                                                var field11 = ((net.minecraft.util.math.ChunkPos) result1).x;
                                                var field12 = ((net.minecraft.util.math.ChunkPos) result1).z;
                                                if ((result4 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result5 = result4.getWorldChunk(field11, field12, false);
                                                   if ((result5 == null)) {
                                                      if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                                         return;
                                                      } else {
                                                         iteration1_1 = (iteration1_1 + 1);
                                                         continue repeat1;
                                                      }
                                                   } else {
                                                      var field13 = this.scanned;
                                                      if ((field13 == null)) {
                                                         throw new java.lang.NullPointerException("object reference is null");
                                                      } else {
                                                         var result6 = ((java.util.Set) field13).add(result1);
                                                         this.scanChunk(result5, ((net.minecraft.util.math.ChunkPos) result1));
                                                         if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                                            return;
                                                         } else {
                                                            iteration1_1 = (iteration1_1 + 1);
                                                            continue repeat1;
                                                         }
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       } else {
                                          if (Integer.compareUnsigned(510, iteration1_1) < 0) {
                                             return;
                                          } else {
                                             iteration1_1 = (iteration1_1 + 1);
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
               throw new java.lang.NullPointerException("object reference is null");
            }
         } else {
            return;
         }
      }
   }

   private void enqueueChunks() {
      var field1 = com.zenya.module.donut.SusChunkFinderModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.player;
         if ((field2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         } else {
            var result1 = field2.getChunkPos();
            var field3 = this.scanRadius;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var result2 = field3.getValue();
               if ((result2 != null)) {
                  if (!((result2 == null || result2 instanceof java.lang.Integer))) {
                     throw new java.lang.ClassCastException("");
                  } else {
                     var result3 = ((java.lang.Integer) result2).intValue();
                     var field4 = com.zenya.module.donut.SusChunkFinderModule.AMETHYST_RENDER_DISTANCE_CHUNKS;
                     var selected1 = ((result3 < field4) ? result3 : field4);
                     var arrayList1 = new java.util.ArrayList();
                     if ((-(1) < selected1)) {
                        int iteration1_1 = ((selected1 * 2) + 1);
                        int iteration1_2 = -(selected1);
                        int iteration1_3 = -(selected1);
                        repeat1: while (true) {
                           var saved1 = iteration1_2;
                           var saved2 = iteration1_3;
                           var result4 = com.zenya.module.donut.SusChunkFinderModule.isWithinAmethystRenderDistance(result1, saved2, saved1);
                           if (result4) {
                              if ((result1 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 var field5 = result1.x;
                                 var field6 = result1.z;
                                 var saved3 = (field6 + iteration1_2);
                                 var saved4 = (field5 + iteration1_3);
                                 var chunkPos1 = new net.minecraft.util.math.ChunkPos(saved4, saved3);
                                 var field7 = this.scanned;
                                 if ((field7 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    var result5 = ((java.util.Set) field7).contains(chunkPos1);
                                    if (!(result5)) {
                                       var field8 = this.queued;
                                       if ((field8 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var result6 = ((java.util.Set) field8).contains(chunkPos1);
                                          if (!(result6)) {
                                             var field9 = com.zenya.module.donut.SusChunkFinderModule.mc;
                                             if ((field9 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field10 = field9.world;
                                                if ((field10 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result7 = field10.getChunkManager();
                                                   var field11 = chunkPos1.x;
                                                   var field12 = chunkPos1.z;
                                                   if ((result7 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var result8 = result7.getWorldChunk((int) (Integer.toUnsignedLong(field11)), field12, false);
                                                      if ((result8 != null)) {
                                                         var result9 = ((java.util.List) arrayList1).add(chunkPos1);
                                                         iteration1_1 = (iteration1_1 + -(1));
                                                         iteration1_2 = (iteration1_2 + 1);
                                                         if ((iteration1_1 != 0)) {
                                                            continue repeat1;
                                                         } else {
                                                            var saved5 = (iteration1_3 != selected1);
                                                            iteration1_1 = ((selected1 * 2) + 1);
                                                            iteration1_2 = -(selected1);
                                                            iteration1_3 = (iteration1_3 + 1);
                                                            if (saved5) {
                                                               continue repeat1;
                                                            } else {
                                                               var array1 = new java.lang.Object[1];
                                                               array1[0] = result1;
                                                               var captured1 = result1;
                                                               var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                                               var result10 = java.util.Comparator.comparingLong(callback1);
                                                               ((java.util.List) arrayList1).sort(result10);
                                                               var result11 = ((java.util.List) arrayList1).iterator();
                                                               if ((result11 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  repeat2: while (true) {
                                                                     var result12 = result11.hasNext();
                                                                     if (!(result12)) {
                                                                        return;
                                                                     } else {
                                                                        var result13 = result11.next();
                                                                        if ((result13 != null)) {
                                                                           if (!((result13 == null || result13 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                              throw new java.lang.ClassCastException("");
                                                                           } else {
                                                                              var field13 = this.queued;
                                                                              if ((field13 == null)) {
                                                                                 throw new java.lang.NullPointerException("object reference is null");
                                                                              } else {
                                                                                 var result14 = ((java.util.Set) field13).add(result13);
                                                                                 if (result14) {
                                                                                    var field14 = this.scanQueue;
                                                                                    if ((field14 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var result15 = ((java.util.ArrayDeque) field14).offer(result13);
                                                                                       continue repeat2;
                                                                                    }
                                                                                 } else {
                                                                                    continue repeat2;
                                                                                 }
                                                                              }
                                                                           }
                                                                        } else {
                                                                           var field13 = this.queued;
                                                                           if ((field13 == null)) {
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           } else {
                                                                              var result14 = ((java.util.Set) field13).add(result13);
                                                                              if (result14) {
                                                                                 var field14 = this.scanQueue;
                                                                                 if ((field14 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    var result15 = ((java.util.ArrayDeque) field14).offer(result13);
                                                                                    continue repeat2;
                                                                                 }
                                                                              } else {
                                                                                 continue repeat2;
                                                                              }
                                                                           }
                                                                        }
                                                                     }
                                                                  }
                                                               }
                                                            }
                                                         }
                                                      } else {
                                                         iteration1_1 = (iteration1_1 + -(1));
                                                         iteration1_2 = (iteration1_2 + 1);
                                                         if ((iteration1_1 != 0)) {
                                                            continue repeat1;
                                                         } else {
                                                            var saved5 = (iteration1_3 != selected1);
                                                            iteration1_1 = ((selected1 * 2) + 1);
                                                            iteration1_2 = -(selected1);
                                                            iteration1_3 = (iteration1_3 + 1);
                                                            if (saved5) {
                                                               continue repeat1;
                                                            } else {
                                                               var array1 = new java.lang.Object[1];
                                                               array1[0] = result1;
                                                               var captured1 = result1;
                                                               var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                                               var result9 = java.util.Comparator.comparingLong(callback1);
                                                               ((java.util.List) arrayList1).sort(result9);
                                                               var result10 = ((java.util.List) arrayList1).iterator();
                                                               if ((result10 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  repeat2: while (true) {
                                                                     var result11 = result10.hasNext();
                                                                     if (!(result11)) {
                                                                        return;
                                                                     } else {
                                                                        var result12 = result10.next();
                                                                        if ((result12 != null)) {
                                                                           if (!((result12 == null || result12 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                              throw new java.lang.ClassCastException("");
                                                                           } else {
                                                                              var field13 = this.queued;
                                                                              if ((field13 == null)) {
                                                                                 throw new java.lang.NullPointerException("object reference is null");
                                                                              } else {
                                                                                 var result13 = ((java.util.Set) field13).add(result12);
                                                                                 if (result13) {
                                                                                    var field14 = this.scanQueue;
                                                                                    if ((field14 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var result14 = ((java.util.ArrayDeque) field14).offer(result12);
                                                                                       continue repeat2;
                                                                                    }
                                                                                 } else {
                                                                                    continue repeat2;
                                                                                 }
                                                                              }
                                                                           }
                                                                        } else {
                                                                           var field13 = this.queued;
                                                                           if ((field13 == null)) {
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           } else {
                                                                              var result13 = ((java.util.Set) field13).add(result12);
                                                                              if (result13) {
                                                                                 var field14 = this.scanQueue;
                                                                                 if ((field14 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    var result14 = ((java.util.ArrayDeque) field14).offer(result12);
                                                                                    continue repeat2;
                                                                                 }
                                                                              } else {
                                                                                 continue repeat2;
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
                                                }
                                             }
                                          } else {
                                             iteration1_1 = (iteration1_1 + -(1));
                                             iteration1_2 = (iteration1_2 + 1);
                                             if ((iteration1_1 != 0)) {
                                                continue repeat1;
                                             } else {
                                                var saved5 = (iteration1_3 != selected1);
                                                iteration1_1 = ((selected1 * 2) + 1);
                                                iteration1_2 = -(selected1);
                                                iteration1_3 = (iteration1_3 + 1);
                                                if (saved5) {
                                                   continue repeat1;
                                                } else {
                                                   var array1 = new java.lang.Object[1];
                                                   array1[0] = result1;
                                                   var captured1 = result1;
                                                   var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                                   var result7 = java.util.Comparator.comparingLong(callback1);
                                                   ((java.util.List) arrayList1).sort(result7);
                                                   var result8 = ((java.util.List) arrayList1).iterator();
                                                   if ((result8 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      repeat2: while (true) {
                                                         var result9 = result8.hasNext();
                                                         if (!(result9)) {
                                                            return;
                                                         } else {
                                                            var result10 = result8.next();
                                                            if ((result10 != null)) {
                                                               if (!((result10 == null || result10 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                  throw new java.lang.ClassCastException("");
                                                               } else {
                                                                  var field9 = this.queued;
                                                                  if ((field9 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result11 = ((java.util.Set) field9).add(result10);
                                                                     if (result11) {
                                                                        var field10 = this.scanQueue;
                                                                        if ((field10 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result12 = ((java.util.ArrayDeque) field10).offer(result10);
                                                                           continue repeat2;
                                                                        }
                                                                     } else {
                                                                        continue repeat2;
                                                                     }
                                                                  }
                                                               }
                                                            } else {
                                                               var field9 = this.queued;
                                                               if ((field9 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result11 = ((java.util.Set) field9).add(result10);
                                                                  if (result11) {
                                                                     var field10 = this.scanQueue;
                                                                     if ((field10 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result12 = ((java.util.ArrayDeque) field10).offer(result10);
                                                                        continue repeat2;
                                                                     }
                                                                  } else {
                                                                     continue repeat2;
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
                                       iteration1_1 = (iteration1_1 + -(1));
                                       iteration1_2 = (iteration1_2 + 1);
                                       if ((iteration1_1 != 0)) {
                                          continue repeat1;
                                       } else {
                                          var saved5 = (iteration1_3 != selected1);
                                          iteration1_1 = ((selected1 * 2) + 1);
                                          iteration1_2 = -(selected1);
                                          iteration1_3 = (iteration1_3 + 1);
                                          if (saved5) {
                                             continue repeat1;
                                          } else {
                                             var array1 = new java.lang.Object[1];
                                             array1[0] = result1;
                                             var captured1 = result1;
                                             var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                             var result6 = java.util.Comparator.comparingLong(callback1);
                                             ((java.util.List) arrayList1).sort(result6);
                                             var result7 = ((java.util.List) arrayList1).iterator();
                                             if ((result7 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                repeat2: while (true) {
                                                   var result8 = result7.hasNext();
                                                   if (!(result8)) {
                                                      return;
                                                   } else {
                                                      var result9 = result7.next();
                                                      if ((result9 != null)) {
                                                         if (!((result9 == null || result9 instanceof net.minecraft.util.math.ChunkPos))) {
                                                            throw new java.lang.ClassCastException("");
                                                         } else {
                                                            var field8 = this.queued;
                                                            if ((field8 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result10 = ((java.util.Set) field8).add(result9);
                                                               if (result10) {
                                                                  var field9 = this.scanQueue;
                                                                  if ((field9 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result11 = ((java.util.ArrayDeque) field9).offer(result9);
                                                                     continue repeat2;
                                                                  }
                                                               } else {
                                                                  continue repeat2;
                                                               }
                                                            }
                                                         }
                                                      } else {
                                                         var field8 = this.queued;
                                                         if ((field8 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var result10 = ((java.util.Set) field8).add(result9);
                                                            if (result10) {
                                                               var field9 = this.scanQueue;
                                                               if ((field9 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result11 = ((java.util.ArrayDeque) field9).offer(result9);
                                                                  continue repeat2;
                                                               }
                                                            } else {
                                                               continue repeat2;
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
                              }
                           } else {
                              iteration1_1 = (iteration1_1 + -(1));
                              iteration1_2 = (iteration1_2 + 1);
                              if ((iteration1_1 != 0)) {
                                 continue repeat1;
                              } else {
                                 var saved3 = (iteration1_3 != selected1);
                                 iteration1_1 = ((selected1 * 2) + 1);
                                 iteration1_2 = -(selected1);
                                 iteration1_3 = (iteration1_3 + 1);
                                 if (saved3) {
                                    continue repeat1;
                                 } else {
                                    var array1 = new java.lang.Object[1];
                                    array1[0] = result1;
                                    var captured1 = result1;
                                    var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                    var result5 = java.util.Comparator.comparingLong(callback1);
                                    ((java.util.List) arrayList1).sort(result5);
                                    var result6 = ((java.util.List) arrayList1).iterator();
                                    if ((result6 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       repeat2: while (true) {
                                          var result7 = result6.hasNext();
                                          if (!(result7)) {
                                             return;
                                          } else {
                                             var result8 = result6.next();
                                             if ((result8 != null)) {
                                                if (!((result8 == null || result8 instanceof net.minecraft.util.math.ChunkPos))) {
                                                   throw new java.lang.ClassCastException("");
                                                } else {
                                                   var field5 = this.queued;
                                                   if ((field5 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var result9 = ((java.util.Set) field5).add(result8);
                                                      if (result9) {
                                                         var field6 = this.scanQueue;
                                                         if ((field6 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var result10 = ((java.util.ArrayDeque) field6).offer(result8);
                                                            continue repeat2;
                                                         }
                                                      } else {
                                                         continue repeat2;
                                                      }
                                                   }
                                                }
                                             } else {
                                                var field5 = this.queued;
                                                if ((field5 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result9 = ((java.util.Set) field5).add(result8);
                                                   if (result9) {
                                                      var field6 = this.scanQueue;
                                                      if ((field6 == null)) {
                                                         throw new java.lang.NullPointerException("object reference is null");
                                                      } else {
                                                         var result10 = ((java.util.ArrayDeque) field6).offer(result8);
                                                         continue repeat2;
                                                      }
                                                   } else {
                                                      continue repeat2;
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
                        var array1 = new java.lang.Object[1];
                        array1[0] = result1;
                        var captured1 = result1;
                        var callback1 = new java.util.function.ToLongFunction() { public long applyAsLong(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$enqueueChunks$0(captured1, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                        var result4 = java.util.Comparator.comparingLong(callback1);
                        ((java.util.List) arrayList1).sort(result4);
                        var result5 = ((java.util.List) arrayList1).iterator();
                        if ((result5 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           repeat1: while (true) {
                              var result6 = result5.hasNext();
                              if (!(result6)) {
                                 return;
                              } else {
                                 var result7 = result5.next();
                                 if ((result7 != null)) {
                                    if (!((result7 == null || result7 instanceof net.minecraft.util.math.ChunkPos))) {
                                       throw new java.lang.ClassCastException("");
                                    } else {
                                       var field5 = this.queued;
                                       if ((field5 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var result8 = ((java.util.Set) field5).add(result7);
                                          if (result8) {
                                             var field6 = this.scanQueue;
                                             if ((field6 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var result9 = ((java.util.ArrayDeque) field6).offer(result7);
                                                continue repeat1;
                                             }
                                          } else {
                                             continue repeat1;
                                          }
                                       }
                                    }
                                 } else {
                                    var field5 = this.queued;
                                    if ((field5 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var result8 = ((java.util.Set) field5).add(result7);
                                       if (result8) {
                                          var field6 = this.scanQueue;
                                          if ((field6 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var result9 = ((java.util.ArrayDeque) field6).offer(result7);
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
               } else {
                  throw new java.lang.NullPointerException("object reference is null");
               }
            }
         }
      }
   }

   private void scanChunk(WorldChunk chunk, ChunkPos chunkPos) {
      class Execution {
         net.minecraft.client.MinecraftClient saved1 = null;
         net.minecraft.client.world.ClientWorld saved2 = null;
         int saved3 = 0;
         int saved4 = 0;
         int saved5 = 0;
         net.minecraft.world.chunk.ChunkSection[] saved6 = null;
         int saved7 = 0;
         java.util.Map saved8 = null;
         int saved9 = 0;
         net.minecraft.world.chunk.ChunkSection saved10 = null;
         net.minecraft.block.BlockState saved11 = null;
         int saved12 = 0;
         com.zenya.setting.Setting saved13 = null;
         int saved14 = 0;
         java.lang.Object saved15 = null;
         int saved16 = 0;
         int saved17 = 0;
         int saved18 = 0;
         com.zenya.module.donut.SusChunkFinderModule.HitType saved19 = null;
         int step = 0;
         boolean finished;
         void advance0() {
            switch (step) {
               case 0 -> {
                  var field1 = com.zenya.module.donut.SusChunkFinderModule.mc;
                  if ((field1 != null)) {
                     net.minecraft.client.MinecraftClient nextValue0 = field1;
                     saved1 = nextValue0;
                     step = 1;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 1 -> {
                  var field2 = saved1.world;
                  if ((field2 != null)) {
                     net.minecraft.client.world.ClientWorld nextValue0 = field2;
                     saved2 = nextValue0;
                     step = 3;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 2 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 3 -> {
                  var result1 = saved2.getBottomY();
                  var field3 = com.zenya.module.donut.SusChunkFinderModule.mc;
                  if ((field3 != null)) {
                     int nextValue0 = result1;
                     net.minecraft.client.MinecraftClient nextValue1 = field3;
                     saved3 = nextValue0;
                     saved1 = nextValue1;
                     step = 4;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 4 -> {
                  var field4 = saved1.world;
                  if ((field4 != null)) {
                     net.minecraft.client.world.ClientWorld nextValue0 = field4;
                     saved2 = nextValue0;
                     step = 5;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 5 -> {
                  var result2 = saved2.getTopYInclusive();
                  if ((result2 < 32)) {
                     int nextValue0 = result2;
                     saved4 = nextValue0;
                     step = 6;
                     return;
                  } else {
                     step = 7;
                     return;
                  }
               }
               case 6 -> {
                  if ((chunk != null)) {
                     int nextValue0 = saved4;
                     saved5 = nextValue0;
                     step = 8;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 7 -> {
                  if ((chunk != null)) {
                     step = 9;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 8 -> {
                  var result3 = chunk.getSectionArray();
                  if ((result3 != null)) {
                     net.minecraft.world.chunk.ChunkSection[] nextValue0 = result3;
                     saved6 = nextValue0;
                     step = 10;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 9 -> {
                  var result3 = chunk.getSectionArray();
                  if ((result3 != null)) {
                     net.minecraft.world.chunk.ChunkSection[] nextValue0 = result3;
                     saved6 = nextValue0;
                     step = 11;
                     return;
                  } else {
                     step = 2;
                     return;
                  }
               }
               case 10 -> {
                  var length1 = saved6.length;
                  if ((0 < length1)) {
                     step = 12;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 11 -> {
                  var length1 = saved6.length;
                  if ((0 < length1)) {
                     step = 14;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 12 -> {
                  var result4 = chunk.sectionIndexToCoord((int) (0L));
                  if (((result4 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result4 * -(16)));
                     int nextValue1 = result4;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 15;
                     return;
                  } else {
                     step = 16;
                     return;
                  }
               }
               case 13 -> {
                  var field5 = SusChunkFinderModule.this.hits;
                  if ((field5 != null)) {
                     java.util.Map nextValue0 = field5;
                     saved8 = nextValue0;
                     step = 17;
                     return;
                  } else {
                     step = 18;
                     return;
                  }
               }
               case 14 -> {
                  var result4 = chunk.sectionIndexToCoord((int) (0L));
                  if (((result4 * 16) <= 32)) {
                     int nextValue0 = (32 + (result4 * -(16)));
                     int nextValue1 = result4;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 19;
                     return;
                  } else {
                     step = 20;
                     return;
                  }
               }
               case 15 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 21;
                     return;
                  } else {
                     step = 16;
                     return;
                  }
               }
               case 16 -> {
                  var length2 = saved6.length;
                  if (((0 + 1) < length2)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 22;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 17 -> {
                  var result4 = ((java.util.Map) saved8).remove(chunkPos);
                  finished = true; return;
               }
               case 18 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 19 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 23;
                     return;
                  } else {
                     step = 20;
                     return;
                  }
               }
               case 20 -> {
                  var length2 = saved6.length;
                  if (((0 + 1) < length2)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 24;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 21 -> {
                  var length2 = saved6.length;
                  if ((length2 <= 0)) {
                     step = 25;
                     return;
                  } else {
                     step = 26;
                     return;
                  }
               }
               case 22 -> {
                  var result5 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result5 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result5 * -(16)));
                     int nextValue1 = result5;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 27;
                     return;
                  } else {
                     step = 28;
                     return;
                  }
               }
               case 23 -> {
                  var length2 = saved6.length;
                  if ((length2 <= 0)) {
                     step = 25;
                     return;
                  } else {
                     step = 29;
                     return;
                  }
               }
               case 24 -> {
                  var result5 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result5 * 16) <= 32)) {
                     int nextValue0 = (32 + (result5 * -(16)));
                     int nextValue1 = result5;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 30;
                     return;
                  } else {
                     step = 31;
                     return;
                  }
               }
               case 25 -> {
                  throw new java.lang.ArrayIndexOutOfBoundsException("array index out of bounds");
               }
               case 26 -> {
                  var element1 = saved6[0];
                  if ((element1 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element1;
                     saved10 = nextValue0;
                     step = 32;
                     return;
                  } else {
                     step = 16;
                     return;
                  }
               }
               case 27 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 33;
                     return;
                  } else {
                     step = 28;
                     return;
                  }
               }
               case 28 -> {
                  var length3 = saved6.length;
                  if (((saved9 + 1) < length3)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 22;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 29 -> {
                  var element1 = saved6[0];
                  if ((element1 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element1;
                     saved10 = nextValue0;
                     step = 34;
                     return;
                  } else {
                     step = 20;
                     return;
                  }
               }
               case 30 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 35;
                     return;
                  } else {
                     step = 31;
                     return;
                  }
               }
               case 31 -> {
                  var length3 = saved6.length;
                  if (((saved9 + 1) < length3)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 24;
                     return;
                  } else {
                     step = 13;
                     return;
                  }
               }
               case 32 -> {
                  var result5 = saved10.isEmpty();
                  if (!(result5)) {
                     step = 36;
                     return;
                  } else {
                     step = 16;
                     return;
                  }
               }
               case 33 -> {
                  var length3 = saved6.length;
                  if ((length3 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 37;
                     return;
                  }
               }
               case 34 -> {
                  var result5 = saved10.isEmpty();
                  if (!(result5)) {
                     step = 38;
                     return;
                  } else {
                     step = 20;
                     return;
                  }
               }
               case 35 -> {
                  var length3 = saved6.length;
                  if ((length3 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 39;
                     return;
                  }
               }
               case 36 -> {
                  var array1 = new java.lang.Object[0];
                  var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result6 = saved10.hasAny(callback1);
                  if (result6) {
                     step = 40;
                     return;
                  } else {
                     step = 41;
                     return;
                  }
               }
               case 37 -> {
                  var element1 = saved6[saved9];
                  if ((element1 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element1;
                     saved10 = nextValue0;
                     step = 42;
                     return;
                  } else {
                     step = 28;
                     return;
                  }
               }
               case 38 -> {
                  var array1 = new java.lang.Object[0];
                  var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result6 = saved10.hasAny(callback1);
                  if (result6) {
                     step = 43;
                     return;
                  } else {
                     step = 44;
                     return;
                  }
               }
               case 39 -> {
                  var element1 = saved6[saved9];
                  if ((element1 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element1;
                     saved10 = nextValue0;
                     step = 45;
                     return;
                  } else {
                     step = 31;
                     return;
                  }
               }
               case 40 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 46;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 47;
                     return;
                  }
               }
               case 41 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 48;
                     return;
                  } else {
                     step = 49;
                     return;
                  }
               }
               case 42 -> {
                  var result6 = saved10.isEmpty();
                  if (!(result6)) {
                     step = 50;
                     return;
                  } else {
                     step = 28;
                     return;
                  }
               }
               case 43 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 51;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 52;
                     return;
                  }
               }
               case 44 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 53;
                     return;
                  } else {
                     step = 49;
                     return;
                  }
               }
               case 45 -> {
                  var result6 = saved10.isEmpty();
                  if (!(result6)) {
                     step = 54;
                     return;
                  } else {
                     step = 31;
                     return;
                  }
               }
               case 46 -> {
                  if ((14 < saved7)) {
                     step = 55;
                     return;
                  } else {
                     step = 56;
                     return;
                  }
               }
               case 47 -> {
                  if ((14 < saved7)) {
                     step = 57;
                     return;
                  } else {
                     step = 58;
                     return;
                  }
               }
               case 48 -> {
                  var result7 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result7 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result7 * -(16)));
                     int nextValue1 = result7;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 59;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 49 -> {
                  var field5 = SusChunkFinderModule.this.hits;
                  if ((field5 != null)) {
                     java.util.Map nextValue0 = field5;
                     saved8 = nextValue0;
                     step = 61;
                     return;
                  } else {
                     step = 18;
                     return;
                  }
               }
               case 50 -> {
                  var array1 = new java.lang.Object[0];
                  var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result7 = saved10.hasAny(callback1);
                  if (result7) {
                     step = 62;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 51 -> {
                  if ((14 < saved7)) {
                     step = 63;
                     return;
                  } else {
                     step = 64;
                     return;
                  }
               }
               case 52 -> {
                  if ((14 < saved7)) {
                     step = 65;
                     return;
                  } else {
                     step = 66;
                     return;
                  }
               }
               case 53 -> {
                  var result7 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result7 * 16) <= 32)) {
                     int nextValue0 = (32 + (result7 * -(16)));
                     int nextValue1 = result7;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 67;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 54 -> {
                  var array1 = new java.lang.Object[0];
                  var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result7 = saved10.hasAny(callback1);
                  if (result7) {
                     step = 69;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 55 -> {
                  var result7 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result7;
                     saved11 = nextValue0;
                     step = 71;
                     return;
                  }
               }
               case 56 -> {
                  if ((0 <= saved7)) {
                     step = 72;
                     return;
                  } else {
                     step = 41;
                     return;
                  }
               }
               case 57 -> {
                  if ((saved4 <= 15)) {
                     step = 73;
                     return;
                  } else {
                     step = 41;
                     return;
                  }
               }
               case 58 -> {
                  if ((saved4 <= saved7)) {
                     step = 74;
                     return;
                  } else {
                     step = 41;
                     return;
                  }
               }
               case 59 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 75;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 60 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 48;
                     return;
                  } else {
                     step = 49;
                     return;
                  }
               }
               case 61 -> {
                  var result7 = ((java.util.Map) saved8).remove(chunkPos);
                  finished = true; return;
               }
               case 62 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 76;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 77;
                     return;
                  }
               }
               case 63 -> {
                  var result7 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result7;
                     saved11 = nextValue0;
                     step = 78;
                     return;
                  }
               }
               case 64 -> {
                  if ((0 <= saved7)) {
                     step = 79;
                     return;
                  } else {
                     step = 44;
                     return;
                  }
               }
               case 65 -> {
                  if ((saved4 <= 15)) {
                     step = 80;
                     return;
                  } else {
                     step = 44;
                     return;
                  }
               }
               case 66 -> {
                  if ((saved4 <= saved7)) {
                     step = 81;
                     return;
                  } else {
                     step = 44;
                     return;
                  }
               }
               case 67 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 82;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 68 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 53;
                     return;
                  } else {
                     step = 49;
                     return;
                  }
               }
               case 69 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 83;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 84;
                     return;
                  }
               }
               case 70 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 71 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 85;
                     return;
                  } else {
                     step = 86;
                     return;
                  }
               }
               case 72 -> {
                  var result7 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result7;
                     saved11 = nextValue0;
                     step = 87;
                     return;
                  }
               }
               case 73 -> {
                  var result7 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result7;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 88;
                     return;
                  }
               }
               case 74 -> {
                  var result7 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result7;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 89;
                     return;
                  }
               }
               case 75 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 90;
                     return;
                  }
               }
               case 76 -> {
                  if ((14 < saved7)) {
                     step = 91;
                     return;
                  } else {
                     step = 92;
                     return;
                  }
               }
               case 77 -> {
                  if ((14 < saved7)) {
                     step = 93;
                     return;
                  } else {
                     step = 94;
                     return;
                  }
               }
               case 78 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 95;
                     return;
                  } else {
                     step = 96;
                     return;
                  }
               }
               case 79 -> {
                  var result7 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result7;
                     saved11 = nextValue0;
                     step = 97;
                     return;
                  }
               }
               case 80 -> {
                  var result7 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result7;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 98;
                     return;
                  }
               }
               case 81 -> {
                  var result7 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result7 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result7;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 99;
                     return;
                  }
               }
               case 82 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 100;
                     return;
                  }
               }
               case 83 -> {
                  if ((14 < saved7)) {
                     step = 101;
                     return;
                  } else {
                     step = 102;
                     return;
                  }
               }
               case 84 -> {
                  if ((14 < saved7)) {
                     step = 103;
                     return;
                  } else {
                     step = 104;
                     return;
                  }
               }
               case 85 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 106;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance1() {
            switch (step) {
               case 86 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 107;
                     return;
                  } else {
                     step = 108;
                     return;
                  }
               }
               case 87 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 109;
                     return;
                  } else {
                     step = 110;
                     return;
                  }
               }
               case 88 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 111;
                     return;
                  } else {
                     step = 112;
                     return;
                  }
               }
               case 89 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 113;
                     return;
                  } else {
                     step = 114;
                     return;
                  }
               }
               case 90 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 115;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 91 -> {
                  var result8 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result8;
                     saved11 = nextValue0;
                     step = 116;
                     return;
                  }
               }
               case 92 -> {
                  if ((0 <= saved7)) {
                     step = 117;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 93 -> {
                  if ((saved4 <= 15)) {
                     step = 118;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 94 -> {
                  if ((saved4 <= saved7)) {
                     step = 119;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 95 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 120;
                     return;
                  }
               }
               case 96 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 121;
                     return;
                  } else {
                     step = 122;
                     return;
                  }
               }
               case 97 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 123;
                     return;
                  } else {
                     step = 124;
                     return;
                  }
               }
               case 98 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 125;
                     return;
                  } else {
                     step = 126;
                     return;
                  }
               }
               case 99 -> {
                  var result8 = saved11.getBlock();
                  var result9 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result8);
                  if (result9) {
                     step = 127;
                     return;
                  } else {
                     step = 128;
                     return;
                  }
               }
               case 100 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 129;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 101 -> {
                  var result8 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result8;
                     saved11 = nextValue0;
                     step = 130;
                     return;
                  }
               }
               case 102 -> {
                  if ((0 <= saved7)) {
                     step = 131;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 103 -> {
                  if ((saved4 <= 15)) {
                     step = 132;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 104 -> {
                  if ((saved4 <= saved7)) {
                     step = 133;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 105 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 106 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 135;
                     return;
                  }
               }
               case 107 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result10;
                     saved11 = nextValue0;
                     step = 136;
                     return;
                  }
               }
               case 108 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 137;
                     return;
                  } else {
                     step = 138;
                     return;
                  }
               }
               case 109 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 139;
                     return;
                  }
               }
               case 110 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 140;
                     return;
                  } else {
                     step = 141;
                     return;
                  }
               }
               case 111 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 142;
                     return;
                  }
               }
               case 112 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 143;
                     return;
                  } else {
                     step = 144;
                     return;
                  }
               }
               case 113 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 145;
                     return;
                  }
               }
               case 114 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 146;
                     return;
                  } else {
                     step = 147;
                     return;
                  }
               }
               case 115 -> {
                  var result8 = saved10.isEmpty();
                  if (!(result8)) {
                     step = 148;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 116 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 149;
                     return;
                  } else {
                     step = 150;
                     return;
                  }
               }
               case 117 -> {
                  var result8 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result8;
                     saved11 = nextValue0;
                     step = 151;
                     return;
                  }
               }
               case 118 -> {
                  var result8 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result8;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 152;
                     return;
                  }
               }
               case 119 -> {
                  var result8 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result8;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 153;
                     return;
                  }
               }
               case 120 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 154;
                     return;
                  }
               }
               case 121 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result10;
                     saved11 = nextValue0;
                     step = 155;
                     return;
                  }
               }
               case 122 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 156;
                     return;
                  } else {
                     step = 157;
                     return;
                  }
               }
               case 123 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 158;
                     return;
                  }
               }
               case 124 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 159;
                     return;
                  } else {
                     step = 160;
                     return;
                  }
               }
               case 125 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 161;
                     return;
                  }
               }
               case 126 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 162;
                     return;
                  } else {
                     step = 163;
                     return;
                  }
               }
               case 127 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 164;
                     return;
                  }
               }
               case 128 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 165;
                     return;
                  } else {
                     step = 166;
                     return;
                  }
               }
               case 129 -> {
                  var result8 = saved10.isEmpty();
                  if (!(result8)) {
                     step = 167;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 130 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 168;
                     return;
                  } else {
                     step = 169;
                     return;
                  }
               }
               case 131 -> {
                  var result8 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result8;
                     saved11 = nextValue0;
                     step = 170;
                     return;
                  }
               }
               case 132 -> {
                  var result8 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result8;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 171;
                     return;
                  }
               }
               case 133 -> {
                  var result8 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result8 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result8;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 172;
                     return;
                  }
               }
               case 134 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 135 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 174;
                     return;
                  }
               }
               case 136 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 175;
                     return;
                  } else {
                     step = 176;
                     return;
                  }
               }
               case 137 -> {
                  var result10 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 177;
                     return;
                  }
               }
               case 138 -> {
                  var result10 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 88;
                     return;
                  }
               }
               case 139 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 178;
                     return;
                  }
               }
               case 140 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result10;
                     saved11 = nextValue0;
                     step = 179;
                     return;
                  }
               }
               case 141 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 180;
                     return;
                  } else {
                     step = 181;
                     return;
                  }
               }
               case 142 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 182;
                     return;
                  }
               }
               case 143 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 183;
                     return;
                  }
               }
               case 144 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 184;
                     return;
                  } else {
                     step = 185;
                     return;
                  }
               }
               case 145 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 186;
                     return;
                  }
               }
               case 146 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 187;
                     return;
                  }
               }
               case 147 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 188;
                     return;
                  } else {
                     step = 189;
                     return;
                  }
               }
               case 148 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result9 = saved10.hasAny(callback2);
                  if (result9) {
                     step = 62;
                     return;
                  } else {
                     step = 60;
                     return;
                  }
               }
               case 149 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 190;
                     return;
                  }
               }
               case 150 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 191;
                     return;
                  } else {
                     step = 192;
                     return;
                  }
               }
               case 151 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 193;
                     return;
                  } else {
                     step = 194;
                     return;
                  }
               }
               case 152 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 195;
                     return;
                  } else {
                     step = 196;
                     return;
                  }
               }
               case 153 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 197;
                     return;
                  } else {
                     step = 198;
                     return;
                  }
               }
               case 154 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 199;
                     return;
                  }
               }
               case 155 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 200;
                     return;
                  } else {
                     step = 201;
                     return;
                  }
               }
               case 156 -> {
                  var result10 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 202;
                     return;
                  }
               }
               case 157 -> {
                  var result10 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 98;
                     return;
                  }
               }
               case 158 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 203;
                     return;
                  }
               }
               case 159 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result10;
                     saved11 = nextValue0;
                     step = 204;
                     return;
                  }
               }
               case 160 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 205;
                     return;
                  } else {
                     step = 206;
                     return;
                  }
               }
               case 161 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 207;
                     return;
                  }
               }
               case 162 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 208;
                     return;
                  }
               }
               case 163 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 209;
                     return;
                  } else {
                     step = 210;
                     return;
                  }
               }
               case 164 -> {
                  var result10 = saved13.getValue();
                  if ((result10 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result10;
                     saved15 = nextValue0;
                     step = 211;
                     return;
                  }
               }
               case 165 -> {
                  var result10 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 212;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance2() {
            switch (step) {
               case 166 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 213;
                     return;
                  } else {
                     step = 214;
                     return;
                  }
               }
               case 167 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result9 = saved10.hasAny(callback2);
                  if (result9) {
                     step = 69;
                     return;
                  } else {
                     step = 68;
                     return;
                  }
               }
               case 168 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 215;
                     return;
                  }
               }
               case 169 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 216;
                     return;
                  } else {
                     step = 217;
                     return;
                  }
               }
               case 170 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 218;
                     return;
                  } else {
                     step = 219;
                     return;
                  }
               }
               case 171 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 220;
                     return;
                  } else {
                     step = 221;
                     return;
                  }
               }
               case 172 -> {
                  var result9 = saved11.getBlock();
                  var result10 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result9);
                  if (result10) {
                     step = 222;
                     return;
                  } else {
                     step = 223;
                     return;
                  }
               }
               case 173 -> {
                  throw new java.lang.ClassCastException("");
               }
               case 174 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 225;
                     return;
                  }
               }
               case 175 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 226;
                     return;
                  }
               }
               case 176 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 107;
                     return;
                  } else {
                     step = 108;
                     return;
                  }
               }
               case 177 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 227;
                     return;
                  } else {
                     step = 228;
                     return;
                  }
               }
               case 178 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 229;
                     return;
                  }
               }
               case 179 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 230;
                     return;
                  } else {
                     step = 231;
                     return;
                  }
               }
               case 180 -> {
                  var result10 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 232;
                     return;
                  }
               }
               case 181 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 233;
                     return;
                  } else {
                     step = 234;
                     return;
                  }
               }
               case 182 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 235;
                     return;
                  }
               }
               case 183 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 236;
                     return;
                  } else {
                     step = 237;
                     return;
                  }
               }
               case 184 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 238;
                     return;
                  }
               }
               case 185 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 239;
                     return;
                  } else {
                     step = 240;
                     return;
                  }
               }
               case 186 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 241;
                     return;
                  }
               }
               case 187 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 242;
                     return;
                  } else {
                     step = 243;
                     return;
                  }
               }
               case 188 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 244;
                     return;
                  }
               }
               case 189 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 245;
                     return;
                  } else {
                     step = 240;
                     return;
                  }
               }
               case 190 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 246;
                     return;
                  }
               }
               case 191 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result11;
                     saved11 = nextValue0;
                     step = 247;
                     return;
                  }
               }
               case 192 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 248;
                     return;
                  } else {
                     step = 249;
                     return;
                  }
               }
               case 193 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 250;
                     return;
                  }
               }
               case 194 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 251;
                     return;
                  } else {
                     step = 252;
                     return;
                  }
               }
               case 195 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 253;
                     return;
                  }
               }
               case 196 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 254;
                     return;
                  } else {
                     step = 255;
                     return;
                  }
               }
               case 197 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 256;
                     return;
                  }
               }
               case 198 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 257;
                     return;
                  } else {
                     step = 258;
                     return;
                  }
               }
               case 199 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 259;
                     return;
                  }
               }
               case 200 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 260;
                     return;
                  }
               }
               case 201 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 121;
                     return;
                  } else {
                     step = 122;
                     return;
                  }
               }
               case 202 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 261;
                     return;
                  } else {
                     step = 262;
                     return;
                  }
               }
               case 203 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 263;
                     return;
                  }
               }
               case 204 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 264;
                     return;
                  } else {
                     step = 265;
                     return;
                  }
               }
               case 205 -> {
                  var result10 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 266;
                     return;
                  }
               }
               case 206 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 267;
                     return;
                  } else {
                     step = 268;
                     return;
                  }
               }
               case 207 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 269;
                     return;
                  }
               }
               case 208 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 270;
                     return;
                  } else {
                     step = 271;
                     return;
                  }
               }
               case 209 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 272;
                     return;
                  }
               }
               case 210 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 273;
                     return;
                  } else {
                     step = 274;
                     return;
                  }
               }
               case 211 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 275;
                     return;
                  }
               }
               case 212 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 276;
                     return;
                  } else {
                     step = 277;
                     return;
                  }
               }
               case 213 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result10;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 278;
                     return;
                  }
               }
               case 214 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 279;
                     return;
                  } else {
                     step = 274;
                     return;
                  }
               }
               case 215 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 280;
                     return;
                  }
               }
               case 216 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result11;
                     saved11 = nextValue0;
                     step = 281;
                     return;
                  }
               }
               case 217 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 282;
                     return;
                  } else {
                     step = 283;
                     return;
                  }
               }
               case 218 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 284;
                     return;
                  }
               }
               case 219 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 285;
                     return;
                  } else {
                     step = 286;
                     return;
                  }
               }
               case 220 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 287;
                     return;
                  }
               }
               case 221 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 288;
                     return;
                  } else {
                     step = 289;
                     return;
                  }
               }
               case 222 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 290;
                     return;
                  }
               }
               case 223 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 291;
                     return;
                  } else {
                     step = 292;
                     return;
                  }
               }
               case 224 -> {
                  var field6 = SusChunkFinderModule.this.hits;
                  var field7 = com.zenya.module.donut.SusChunkFinderModule.HitType.AMETHYST;
                  if ((field6 == null)) {
                     step = 293;
                     return;
                  } else {
                     com.zenya.module.donut.SusChunkFinderModule.HitType nextValue0 = field7;
                     java.util.Map nextValue1 = field6;
                     saved19 = nextValue0;
                     saved8 = nextValue1;
                     step = 294;
                     return;
                  }
               }
               case 225 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 295;
                     return;
                  } else {
                     step = 296;
                     return;
                  }
               }
               case 226 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 297;
                     return;
                  }
               }
               case 227 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 298;
                     return;
                  }
               }
               case 228 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 299;
                     return;
                  } else {
                     step = 300;
                     return;
                  }
               }
               case 229 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 301;
                     return;
                  }
               }
               case 230 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 302;
                     return;
                  }
               }
               case 231 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 140;
                     return;
                  } else {
                     step = 141;
                     return;
                  }
               }
               case 232 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 303;
                     return;
                  } else {
                     step = 304;
                     return;
                  }
               }
               case 233 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 89;
                     return;
                  }
               }
               case 234 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 305;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 235 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 307;
                     return;
                  }
               }
               case 236 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 308;
                     return;
                  }
               }
               case 237 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 143;
                     return;
                  } else {
                     step = 144;
                     return;
                  }
               }
               case 238 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 309;
                     return;
                  } else {
                     step = 310;
                     return;
                  }
               }
               case 239 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 88;
                     return;
                  }
               }
               case 240 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 311;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 241 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 313;
                     return;
                  }
               }
               case 242 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 314;
                     return;
                  }
               }
               case 243 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 146;
                     return;
                  } else {
                     step = 147;
                     return;
                  }
               }
               case 244 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 315;
                     return;
                  } else {
                     step = 316;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance3() {
            switch (step) {
               case 245 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 89;
                     return;
                  }
               }
               case 246 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 317;
                     return;
                  }
               }
               case 247 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 318;
                     return;
                  } else {
                     step = 319;
                     return;
                  }
               }
               case 248 -> {
                  var result11 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 320;
                     return;
                  }
               }
               case 249 -> {
                  var result11 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 152;
                     return;
                  }
               }
               case 250 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 321;
                     return;
                  }
               }
               case 251 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result11;
                     saved11 = nextValue0;
                     step = 322;
                     return;
                  }
               }
               case 252 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 323;
                     return;
                  } else {
                     step = 324;
                     return;
                  }
               }
               case 253 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 325;
                     return;
                  }
               }
               case 254 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 326;
                     return;
                  }
               }
               case 255 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 327;
                     return;
                  } else {
                     step = 328;
                     return;
                  }
               }
               case 256 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 329;
                     return;
                  }
               }
               case 257 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 330;
                     return;
                  }
               }
               case 258 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 331;
                     return;
                  } else {
                     step = 332;
                     return;
                  }
               }
               case 259 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 333;
                     return;
                  } else {
                     step = 334;
                     return;
                  }
               }
               case 260 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 335;
                     return;
                  }
               }
               case 261 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 336;
                     return;
                  }
               }
               case 262 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 337;
                     return;
                  } else {
                     step = 338;
                     return;
                  }
               }
               case 263 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 339;
                     return;
                  }
               }
               case 264 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 340;
                     return;
                  }
               }
               case 265 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 159;
                     return;
                  } else {
                     step = 160;
                     return;
                  }
               }
               case 266 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 341;
                     return;
                  } else {
                     step = 342;
                     return;
                  }
               }
               case 267 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 99;
                     return;
                  }
               }
               case 268 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 343;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 269 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 344;
                     return;
                  }
               }
               case 270 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 345;
                     return;
                  }
               }
               case 271 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 162;
                     return;
                  } else {
                     step = 163;
                     return;
                  }
               }
               case 272 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 346;
                     return;
                  } else {
                     step = 347;
                     return;
                  }
               }
               case 273 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 98;
                     return;
                  }
               }
               case 274 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 348;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 275 -> {
                  var result11 = ((java.lang.Integer) saved15).intValue();
                  if ((result11 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 349;
                     return;
                  }
               }
               case 276 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 350;
                     return;
                  }
               }
               case 277 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 165;
                     return;
                  } else {
                     step = 166;
                     return;
                  }
               }
               case 278 -> {
                  var result11 = saved11.getBlock();
                  var result12 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result11);
                  if (result12) {
                     step = 351;
                     return;
                  } else {
                     step = 352;
                     return;
                  }
               }
               case 279 -> {
                  var result10 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result10 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result10;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 99;
                     return;
                  }
               }
               case 280 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 353;
                     return;
                  }
               }
               case 281 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 354;
                     return;
                  } else {
                     step = 355;
                     return;
                  }
               }
               case 282 -> {
                  var result11 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 356;
                     return;
                  }
               }
               case 283 -> {
                  var result11 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 171;
                     return;
                  }
               }
               case 284 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 357;
                     return;
                  }
               }
               case 285 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result11;
                     saved11 = nextValue0;
                     step = 358;
                     return;
                  }
               }
               case 286 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 359;
                     return;
                  } else {
                     step = 360;
                     return;
                  }
               }
               case 287 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 361;
                     return;
                  }
               }
               case 288 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 362;
                     return;
                  }
               }
               case 289 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 363;
                     return;
                  } else {
                     step = 364;
                     return;
                  }
               }
               case 290 -> {
                  var result11 = saved13.getValue();
                  if ((result11 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result11;
                     saved15 = nextValue0;
                     step = 365;
                     return;
                  }
               }
               case 291 -> {
                  var result11 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 366;
                     return;
                  }
               }
               case 292 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 367;
                     return;
                  } else {
                     step = 368;
                     return;
                  }
               }
               case 293 -> {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               case 294 -> {
                  var result12 = ((java.util.Map) saved8).put(chunkPos, saved19);
                  finished = true; return;
               }
               case 295 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result12;
                     saved11 = nextValue0;
                     step = 369;
                     return;
                  }
               }
               case 296 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 370;
                     return;
                  } else {
                     step = 371;
                     return;
                  }
               }
               case 297 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 372;
                     return;
                  }
               }
               case 298 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 373;
                     return;
                  }
               }
               case 299 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 374;
                     return;
                  }
               }
               case 300 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 375;
                     return;
                  } else {
                     step = 376;
                     return;
                  }
               }
               case 301 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 377;
                     return;
                  } else {
                     step = 378;
                     return;
                  }
               }
               case 302 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 379;
                     return;
                  }
               }
               case 303 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 380;
                     return;
                  }
               }
               case 304 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 381;
                     return;
                  } else {
                     step = 382;
                     return;
                  }
               }
               case 305 -> {
                  var result10 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result10 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result10 * -(16)));
                     int nextValue1 = result10;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 383;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 306 -> {
                  var field5 = SusChunkFinderModule.this.hits;
                  if ((field5 != null)) {
                     java.util.Map nextValue0 = field5;
                     saved8 = nextValue0;
                     step = 385;
                     return;
                  } else {
                     step = 18;
                     return;
                  }
               }
               case 307 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 386;
                     return;
                  } else {
                     step = 387;
                     return;
                  }
               }
               case 308 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 388;
                     return;
                  }
               }
               case 309 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 389;
                     return;
                  }
               }
               case 310 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 390;
                     return;
                  } else {
                     step = 391;
                     return;
                  }
               }
               case 311 -> {
                  var result10 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result10 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result10 * -(16)));
                     int nextValue1 = result10;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 392;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 312 -> {
                  var field5 = SusChunkFinderModule.this.hits;
                  if ((field5 != null)) {
                     java.util.Map nextValue0 = field5;
                     saved8 = nextValue0;
                     step = 394;
                     return;
                  } else {
                     step = 18;
                     return;
                  }
               }
               case 313 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 395;
                     return;
                  } else {
                     step = 396;
                     return;
                  }
               }
               case 314 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 397;
                     return;
                  }
               }
               case 315 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 398;
                     return;
                  }
               }
               case 316 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 399;
                     return;
                  } else {
                     step = 400;
                     return;
                  }
               }
               case 317 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 401;
                     return;
                  }
               }
               case 318 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 402;
                     return;
                  }
               }
               case 319 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 191;
                     return;
                  } else {
                     step = 192;
                     return;
                  }
               }
               case 320 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 403;
                     return;
                  } else {
                     step = 404;
                     return;
                  }
               }
               case 321 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 405;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance4() {
            switch (step) {
               case 322 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 406;
                     return;
                  } else {
                     step = 407;
                     return;
                  }
               }
               case 323 -> {
                  var result11 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 408;
                     return;
                  }
               }
               case 324 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 409;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 325 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 410;
                     return;
                  }
               }
               case 326 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 411;
                     return;
                  } else {
                     step = 412;
                     return;
                  }
               }
               case 327 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 413;
                     return;
                  }
               }
               case 328 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 414;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 329 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 415;
                     return;
                  }
               }
               case 330 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 416;
                     return;
                  } else {
                     step = 417;
                     return;
                  }
               }
               case 331 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 418;
                     return;
                  }
               }
               case 332 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 419;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 333 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result12;
                     saved11 = nextValue0;
                     step = 420;
                     return;
                  }
               }
               case 334 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 421;
                     return;
                  } else {
                     step = 422;
                     return;
                  }
               }
               case 335 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 423;
                     return;
                  }
               }
               case 336 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 424;
                     return;
                  }
               }
               case 337 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 425;
                     return;
                  }
               }
               case 338 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 426;
                     return;
                  } else {
                     step = 427;
                     return;
                  }
               }
               case 339 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 428;
                     return;
                  } else {
                     step = 429;
                     return;
                  }
               }
               case 340 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 430;
                     return;
                  }
               }
               case 341 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 431;
                     return;
                  }
               }
               case 342 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 432;
                     return;
                  } else {
                     step = 433;
                     return;
                  }
               }
               case 343 -> {
                  var result10 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result10 * 16) <= 32)) {
                     int nextValue0 = (32 + (result10 * -(16)));
                     int nextValue1 = result10;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 434;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 344 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 436;
                     return;
                  } else {
                     step = 437;
                     return;
                  }
               }
               case 345 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 438;
                     return;
                  }
               }
               case 346 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 439;
                     return;
                  }
               }
               case 347 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 440;
                     return;
                  } else {
                     step = 441;
                     return;
                  }
               }
               case 348 -> {
                  var result10 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result10 * 16) <= 32)) {
                     int nextValue0 = (32 + (result10 * -(16)));
                     int nextValue1 = result10;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 442;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 349 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 444;
                     return;
                  } else {
                     step = 445;
                     return;
                  }
               }
               case 350 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 446;
                     return;
                  }
               }
               case 351 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 447;
                     return;
                  }
               }
               case 352 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 448;
                     return;
                  } else {
                     step = 449;
                     return;
                  }
               }
               case 353 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 450;
                     return;
                  }
               }
               case 354 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 451;
                     return;
                  }
               }
               case 355 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 216;
                     return;
                  } else {
                     step = 217;
                     return;
                  }
               }
               case 356 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 452;
                     return;
                  } else {
                     step = 453;
                     return;
                  }
               }
               case 357 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 454;
                     return;
                  }
               }
               case 358 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 455;
                     return;
                  } else {
                     step = 456;
                     return;
                  }
               }
               case 359 -> {
                  var result11 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 457;
                     return;
                  }
               }
               case 360 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 458;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 361 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 459;
                     return;
                  }
               }
               case 362 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 460;
                     return;
                  } else {
                     step = 461;
                     return;
                  }
               }
               case 363 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 462;
                     return;
                  }
               }
               case 364 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 463;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 365 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 464;
                     return;
                  }
               }
               case 366 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 465;
                     return;
                  } else {
                     step = 466;
                     return;
                  }
               }
               case 367 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result11;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 467;
                     return;
                  }
               }
               case 368 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 468;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 369 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 469;
                     return;
                  } else {
                     step = 470;
                     return;
                  }
               }
               case 370 -> {
                  var result12 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 471;
                     return;
                  }
               }
               case 371 -> {
                  var result12 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 472;
                     return;
                  }
               }
               case 372 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 470;
                     return;
                  }
               }
               case 373 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 473;
                     return;
                  }
               }
               case 374 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 474;
                     return;
                  } else {
                     step = 475;
                     return;
                  }
               }
               case 375 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 177;
                     return;
                  }
               }
               case 376 -> {
                  var result13 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 88;
                     return;
                  }
               }
               case 377 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result12;
                     saved11 = nextValue0;
                     step = 476;
                     return;
                  }
               }
               case 378 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 477;
                     return;
                  } else {
                     step = 478;
                     return;
                  }
               }
               case 379 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 479;
                     return;
                  }
               }
               case 380 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 480;
                     return;
                  }
               }
               case 381 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 481;
                     return;
                  }
               }
               case 382 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 482;
                     return;
                  } else {
                     step = 483;
                     return;
                  }
               }
               case 383 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 484;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 384 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 305;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 385 -> {
                  var result10 = ((java.util.Map) saved8).remove(chunkPos);
                  finished = true; return;
               }
               case 386 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 485;
                     return;
                  }
               }
               case 387 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 486;
                     return;
                  } else {
                     step = 487;
                     return;
                  }
               }
               case 388 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 488;
                     return;
                  }
               }
               case 389 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 489;
                     return;
                  }
               }
               case 390 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 490;
                     return;
                  }
               }
               case 391 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 491;
                     return;
                  } else {
                     step = 492;
                     return;
                  }
               }
               case 392 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 493;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 393 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 311;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 394 -> {
                  var result10 = ((java.util.Map) saved8).remove(chunkPos);
                  finished = true; return;
               }
               case 395 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 494;
                     return;
                  }
               }
               case 396 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 495;
                     return;
                  } else {
                     step = 496;
                     return;
                  }
               }
               case 397 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 497;
                     return;
                  }
               }
               case 398 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 498;
                     return;
                  }
               }
               case 399 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 499;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance5() {
            switch (step) {
               case 400 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 500;
                     return;
                  } else {
                     step = 501;
                     return;
                  }
               }
               case 401 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 502;
                     return;
                  } else {
                     step = 503;
                     return;
                  }
               }
               case 402 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 504;
                     return;
                  }
               }
               case 403 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 505;
                     return;
                  }
               }
               case 404 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 506;
                     return;
                  } else {
                     step = 507;
                     return;
                  }
               }
               case 405 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 508;
                     return;
                  }
               }
               case 406 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 509;
                     return;
                  }
               }
               case 407 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 251;
                     return;
                  } else {
                     step = 252;
                     return;
                  }
               }
               case 408 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 510;
                     return;
                  } else {
                     step = 511;
                     return;
                  }
               }
               case 409 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 153;
                     return;
                  }
               }
               case 410 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 512;
                     return;
                  }
               }
               case 411 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 513;
                     return;
                  }
               }
               case 412 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 254;
                     return;
                  } else {
                     step = 255;
                     return;
                  }
               }
               case 413 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 514;
                     return;
                  } else {
                     step = 515;
                     return;
                  }
               }
               case 414 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 152;
                     return;
                  }
               }
               case 415 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 516;
                     return;
                  }
               }
               case 416 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 517;
                     return;
                  }
               }
               case 417 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 257;
                     return;
                  } else {
                     step = 258;
                     return;
                  }
               }
               case 418 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 518;
                     return;
                  } else {
                     step = 519;
                     return;
                  }
               }
               case 419 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 153;
                     return;
                  }
               }
               case 420 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 520;
                     return;
                  } else {
                     step = 521;
                     return;
                  }
               }
               case 421 -> {
                  var result12 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 522;
                     return;
                  }
               }
               case 422 -> {
                  var result12 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 523;
                     return;
                  }
               }
               case 423 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 521;
                     return;
                  }
               }
               case 424 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 524;
                     return;
                  }
               }
               case 425 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 525;
                     return;
                  } else {
                     step = 526;
                     return;
                  }
               }
               case 426 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 202;
                     return;
                  }
               }
               case 427 -> {
                  var result13 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 98;
                     return;
                  }
               }
               case 428 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result12;
                     saved11 = nextValue0;
                     step = 527;
                     return;
                  }
               }
               case 429 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 528;
                     return;
                  } else {
                     step = 529;
                     return;
                  }
               }
               case 430 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 530;
                     return;
                  }
               }
               case 431 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 531;
                     return;
                  }
               }
               case 432 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 532;
                     return;
                  }
               }
               case 433 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 533;
                     return;
                  } else {
                     step = 534;
                     return;
                  }
               }
               case 434 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 535;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 435 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 343;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 436 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 536;
                     return;
                  }
               }
               case 437 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 537;
                     return;
                  } else {
                     step = 538;
                     return;
                  }
               }
               case 438 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 539;
                     return;
                  }
               }
               case 439 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 540;
                     return;
                  }
               }
               case 440 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 541;
                     return;
                  }
               }
               case 441 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 542;
                     return;
                  } else {
                     step = 543;
                     return;
                  }
               }
               case 442 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 544;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 443 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 348;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 444 -> {
                  var result12 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 545;
                     return;
                  }
               }
               case 445 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 546;
                     return;
                  } else {
                     step = 547;
                     return;
                  }
               }
               case 446 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 548;
                     return;
                  }
               }
               case 447 -> {
                  var result13 = saved13.getValue();
                  if ((result13 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result13;
                     saved15 = nextValue0;
                     step = 549;
                     return;
                  }
               }
               case 448 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 550;
                     return;
                  }
               }
               case 449 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 551;
                     return;
                  } else {
                     step = 552;
                     return;
                  }
               }
               case 450 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 553;
                     return;
                  } else {
                     step = 554;
                     return;
                  }
               }
               case 451 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 555;
                     return;
                  }
               }
               case 452 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 556;
                     return;
                  }
               }
               case 453 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 557;
                     return;
                  } else {
                     step = 558;
                     return;
                  }
               }
               case 454 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 559;
                     return;
                  }
               }
               case 455 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 560;
                     return;
                  }
               }
               case 456 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 285;
                     return;
                  } else {
                     step = 286;
                     return;
                  }
               }
               case 457 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 561;
                     return;
                  } else {
                     step = 562;
                     return;
                  }
               }
               case 458 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 172;
                     return;
                  }
               }
               case 459 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 563;
                     return;
                  }
               }
               case 460 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 564;
                     return;
                  }
               }
               case 461 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 288;
                     return;
                  } else {
                     step = 289;
                     return;
                  }
               }
               case 462 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 565;
                     return;
                  } else {
                     step = 566;
                     return;
                  }
               }
               case 463 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 171;
                     return;
                  }
               }
               case 464 -> {
                  var result12 = ((java.lang.Integer) saved15).intValue();
                  if ((result12 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 567;
                     return;
                  }
               }
               case 465 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 568;
                     return;
                  }
               }
               case 466 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 291;
                     return;
                  } else {
                     step = 292;
                     return;
                  }
               }
               case 467 -> {
                  var result12 = saved11.getBlock();
                  var result13 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result12);
                  if (result13) {
                     step = 569;
                     return;
                  } else {
                     step = 570;
                     return;
                  }
               }
               case 468 -> {
                  var result11 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result11 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result11;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 172;
                     return;
                  }
               }
               case 469 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 571;
                     return;
                  }
               }
               case 470 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 295;
                     return;
                  } else {
                     step = 296;
                     return;
                  }
               }
               case 471 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 572;
                     return;
                  } else {
                     step = 573;
                     return;
                  }
               }
               case 472 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 574;
                     return;
                  } else {
                     step = 307;
                     return;
                  }
               }
               case 473 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 573;
                     return;
                  }
               }
               case 474 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 575;
                     return;
                  }
               }
               case 475 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 299;
                     return;
                  } else {
                     step = 300;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance6() {
            switch (step) {
               case 476 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 576;
                     return;
                  } else {
                     step = 577;
                     return;
                  }
               }
               case 477 -> {
                  var result12 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 578;
                     return;
                  }
               }
               case 478 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 579;
                     return;
                  } else {
                     step = 580;
                     return;
                  }
               }
               case 479 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 577;
                     return;
                  }
               }
               case 480 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 581;
                     return;
                  }
               }
               case 481 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 582;
                     return;
                  } else {
                     step = 583;
                     return;
                  }
               }
               case 482 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 232;
                     return;
                  }
               }
               case 483 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 584;
                     return;
                  } else {
                     step = 585;
                     return;
                  }
               }
               case 484 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 586;
                     return;
                  }
               }
               case 485 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 587;
                     return;
                  } else {
                     step = 588;
                     return;
                  }
               }
               case 486 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 589;
                     return;
                  }
               }
               case 487 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 590;
                     return;
                  } else {
                     step = 591;
                     return;
                  }
               }
               case 488 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 588;
                     return;
                  }
               }
               case 489 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 592;
                     return;
                  }
               }
               case 490 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 593;
                     return;
                  } else {
                     step = 594;
                     return;
                  }
               }
               case 491 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 238;
                     return;
                  }
               }
               case 492 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 595;
                     return;
                  } else {
                     step = 596;
                     return;
                  }
               }
               case 493 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 597;
                     return;
                  }
               }
               case 494 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 598;
                     return;
                  } else {
                     step = 599;
                     return;
                  }
               }
               case 495 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 600;
                     return;
                  }
               }
               case 496 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 601;
                     return;
                  } else {
                     step = 591;
                     return;
                  }
               }
               case 497 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 599;
                     return;
                  }
               }
               case 498 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 602;
                     return;
                  }
               }
               case 499 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 603;
                     return;
                  } else {
                     step = 604;
                     return;
                  }
               }
               case 500 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 244;
                     return;
                  }
               }
               case 501 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 605;
                     return;
                  } else {
                     step = 596;
                     return;
                  }
               }
               case 502 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 606;
                     return;
                  }
               }
               case 503 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 607;
                     return;
                  } else {
                     step = 608;
                     return;
                  }
               }
               case 504 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 609;
                     return;
                  }
               }
               case 505 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 610;
                     return;
                  }
               }
               case 506 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 611;
                     return;
                  }
               }
               case 507 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 612;
                     return;
                  } else {
                     step = 613;
                     return;
                  }
               }
               case 508 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 614;
                     return;
                  } else {
                     step = 615;
                     return;
                  }
               }
               case 509 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 616;
                     return;
                  }
               }
               case 510 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 617;
                     return;
                  }
               }
               case 511 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 618;
                     return;
                  } else {
                     step = 619;
                     return;
                  }
               }
               case 512 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 620;
                     return;
                  } else {
                     step = 621;
                     return;
                  }
               }
               case 513 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 622;
                     return;
                  }
               }
               case 514 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 623;
                     return;
                  }
               }
               case 515 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 624;
                     return;
                  } else {
                     step = 625;
                     return;
                  }
               }
               case 516 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 626;
                     return;
                  } else {
                     step = 627;
                     return;
                  }
               }
               case 517 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 628;
                     return;
                  }
               }
               case 518 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 629;
                     return;
                  }
               }
               case 519 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 630;
                     return;
                  } else {
                     step = 631;
                     return;
                  }
               }
               case 520 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 632;
                     return;
                  }
               }
               case 521 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 333;
                     return;
                  } else {
                     step = 334;
                     return;
                  }
               }
               case 522 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 633;
                     return;
                  } else {
                     step = 634;
                     return;
                  }
               }
               case 523 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 635;
                     return;
                  } else {
                     step = 344;
                     return;
                  }
               }
               case 524 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 634;
                     return;
                  }
               }
               case 525 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 636;
                     return;
                  }
               }
               case 526 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 337;
                     return;
                  } else {
                     step = 338;
                     return;
                  }
               }
               case 527 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 637;
                     return;
                  } else {
                     step = 638;
                     return;
                  }
               }
               case 528 -> {
                  var result12 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 639;
                     return;
                  }
               }
               case 529 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 640;
                     return;
                  } else {
                     step = 641;
                     return;
                  }
               }
               case 530 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 638;
                     return;
                  }
               }
               case 531 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 642;
                     return;
                  }
               }
               case 532 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 643;
                     return;
                  } else {
                     step = 644;
                     return;
                  }
               }
               case 533 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 266;
                     return;
                  }
               }
               case 534 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 645;
                     return;
                  } else {
                     step = 646;
                     return;
                  }
               }
               case 535 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 647;
                     return;
                  }
               }
               case 536 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 648;
                     return;
                  } else {
                     step = 649;
                     return;
                  }
               }
               case 537 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 650;
                     return;
                  }
               }
               case 538 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 651;
                     return;
                  } else {
                     step = 652;
                     return;
                  }
               }
               case 539 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 649;
                     return;
                  }
               }
               case 540 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 653;
                     return;
                  }
               }
               case 541 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 654;
                     return;
                  } else {
                     step = 655;
                     return;
                  }
               }
               case 542 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 272;
                     return;
                  }
               }
               case 543 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 656;
                     return;
                  } else {
                     step = 657;
                     return;
                  }
               }
               case 544 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 658;
                     return;
                  }
               }
               case 545 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 659;
                     return;
                  } else {
                     step = 660;
                     return;
                  }
               }
               case 546 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result12;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 661;
                     return;
                  }
               }
               case 547 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 662;
                     return;
                  } else {
                     step = 652;
                     return;
                  }
               }
               case 548 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 660;
                     return;
                  }
               }
               case 549 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 663;
                     return;
                  }
               }
               case 550 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 664;
                     return;
                  } else {
                     step = 665;
                     return;
                  }
               }
               case 551 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 278;
                     return;
                  }
               }
               case 552 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 666;
                     return;
                  } else {
                     step = 657;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance7() {
            switch (step) {
               case 553 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 667;
                     return;
                  }
               }
               case 554 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 668;
                     return;
                  } else {
                     step = 669;
                     return;
                  }
               }
               case 555 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 670;
                     return;
                  }
               }
               case 556 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 671;
                     return;
                  }
               }
               case 557 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 672;
                     return;
                  }
               }
               case 558 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 673;
                     return;
                  } else {
                     step = 674;
                     return;
                  }
               }
               case 559 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 675;
                     return;
                  } else {
                     step = 676;
                     return;
                  }
               }
               case 560 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 677;
                     return;
                  }
               }
               case 561 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 678;
                     return;
                  }
               }
               case 562 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 679;
                     return;
                  } else {
                     step = 680;
                     return;
                  }
               }
               case 563 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 681;
                     return;
                  } else {
                     step = 682;
                     return;
                  }
               }
               case 564 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 683;
                     return;
                  }
               }
               case 565 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 684;
                     return;
                  }
               }
               case 566 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 685;
                     return;
                  } else {
                     step = 686;
                     return;
                  }
               }
               case 567 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 687;
                     return;
                  } else {
                     step = 688;
                     return;
                  }
               }
               case 568 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 689;
                     return;
                  }
               }
               case 569 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 690;
                     return;
                  }
               }
               case 570 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 691;
                     return;
                  } else {
                     step = 692;
                     return;
                  }
               }
               case 571 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 693;
                     return;
                  }
               }
               case 572 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 694;
                     return;
                  }
               }
               case 573 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 695;
                     return;
                  } else {
                     step = 696;
                     return;
                  }
               }
               case 574 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 697;
                     return;
                  }
               }
               case 575 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 698;
                     return;
                  }
               }
               case 576 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 699;
                     return;
                  }
               }
               case 577 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 377;
                     return;
                  } else {
                     step = 378;
                     return;
                  }
               }
               case 578 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 700;
                     return;
                  } else {
                     step = 701;
                     return;
                  }
               }
               case 579 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 702;
                     return;
                  }
               }
               case 580 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 703;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 581 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 701;
                     return;
                  }
               }
               case 582 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 704;
                     return;
                  }
               }
               case 583 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 381;
                     return;
                  } else {
                     step = 382;
                     return;
                  }
               }
               case 584 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 89;
                     return;
                  }
               }
               case 585 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 705;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 586 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 706;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 587 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 707;
                     return;
                  }
               }
               case 588 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 386;
                     return;
                  } else {
                     step = 387;
                     return;
                  }
               }
               case 589 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 708;
                     return;
                  } else {
                     step = 709;
                     return;
                  }
               }
               case 590 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 472;
                     return;
                  }
               }
               case 591 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 710;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 592 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 709;
                     return;
                  }
               }
               case 593 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 711;
                     return;
                  }
               }
               case 594 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 390;
                     return;
                  } else {
                     step = 391;
                     return;
                  }
               }
               case 595 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 88;
                     return;
                  }
               }
               case 596 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 712;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 597 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 713;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 598 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 714;
                     return;
                  }
               }
               case 599 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 395;
                     return;
                  } else {
                     step = 396;
                     return;
                  }
               }
               case 600 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 715;
                     return;
                  } else {
                     step = 716;
                     return;
                  }
               }
               case 601 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 702;
                     return;
                  }
               }
               case 602 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 716;
                     return;
                  }
               }
               case 603 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 717;
                     return;
                  }
               }
               case 604 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 399;
                     return;
                  } else {
                     step = 400;
                     return;
                  }
               }
               case 605 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 89;
                     return;
                  }
               }
               case 606 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 718;
                     return;
                  } else {
                     step = 719;
                     return;
                  }
               }
               case 607 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 720;
                     return;
                  }
               }
               case 608 -> {
                  var result13 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 721;
                     return;
                  }
               }
               case 609 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 719;
                     return;
                  }
               }
               case 610 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 722;
                     return;
                  }
               }
               case 611 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 723;
                     return;
                  } else {
                     step = 724;
                     return;
                  }
               }
               case 612 -> {
                  var result14 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 320;
                     return;
                  }
               }
               case 613 -> {
                  var result14 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 152;
                     return;
                  }
               }
               case 614 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 725;
                     return;
                  }
               }
               case 615 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 726;
                     return;
                  } else {
                     step = 727;
                     return;
                  }
               }
               case 616 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 728;
                     return;
                  }
               }
               case 617 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 729;
                     return;
                  }
               }
               case 618 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 730;
                     return;
                  }
               }
               case 619 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 731;
                     return;
                  } else {
                     step = 732;
                     return;
                  }
               }
               case 620 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 733;
                     return;
                  }
               }
               case 621 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 734;
                     return;
                  } else {
                     step = 735;
                     return;
                  }
               }
               case 622 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 736;
                     return;
                  }
               }
               case 623 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 737;
                     return;
                  }
               }
               case 624 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 738;
                     return;
                  }
               }
               case 625 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 739;
                     return;
                  } else {
                     step = 740;
                     return;
                  }
               }
               case 626 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 741;
                     return;
                  }
               }
               case 627 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 742;
                     return;
                  } else {
                     step = 743;
                     return;
                  }
               }
               case 628 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 744;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance8() {
            switch (step) {
               case 629 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 745;
                     return;
                  }
               }
               case 630 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 746;
                     return;
                  }
               }
               case 631 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 747;
                     return;
                  } else {
                     step = 748;
                     return;
                  }
               }
               case 632 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 749;
                     return;
                  }
               }
               case 633 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 750;
                     return;
                  }
               }
               case 634 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 751;
                     return;
                  } else {
                     step = 752;
                     return;
                  }
               }
               case 635 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 753;
                     return;
                  }
               }
               case 636 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 754;
                     return;
                  }
               }
               case 637 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 755;
                     return;
                  }
               }
               case 638 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 428;
                     return;
                  } else {
                     step = 429;
                     return;
                  }
               }
               case 639 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 756;
                     return;
                  } else {
                     step = 757;
                     return;
                  }
               }
               case 640 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 758;
                     return;
                  }
               }
               case 641 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 759;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 642 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 757;
                     return;
                  }
               }
               case 643 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 760;
                     return;
                  }
               }
               case 644 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 432;
                     return;
                  } else {
                     step = 433;
                     return;
                  }
               }
               case 645 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 99;
                     return;
                  }
               }
               case 646 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 761;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 647 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 762;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 648 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 763;
                     return;
                  }
               }
               case 649 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 436;
                     return;
                  } else {
                     step = 437;
                     return;
                  }
               }
               case 650 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 764;
                     return;
                  } else {
                     step = 765;
                     return;
                  }
               }
               case 651 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 523;
                     return;
                  }
               }
               case 652 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 766;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 653 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 765;
                     return;
                  }
               }
               case 654 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 767;
                     return;
                  }
               }
               case 655 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 440;
                     return;
                  } else {
                     step = 441;
                     return;
                  }
               }
               case 656 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 98;
                     return;
                  }
               }
               case 657 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 768;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 658 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 769;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 659 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 770;
                     return;
                  }
               }
               case 660 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 444;
                     return;
                  } else {
                     step = 445;
                     return;
                  }
               }
               case 661 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 771;
                     return;
                  } else {
                     step = 772;
                     return;
                  }
               }
               case 662 -> {
                  var result12 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result12 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result12;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 758;
                     return;
                  }
               }
               case 663 -> {
                  var result14 = ((java.lang.Integer) saved15).intValue();
                  if ((result14 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 772;
                     return;
                  }
               }
               case 664 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 773;
                     return;
                  }
               }
               case 665 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 448;
                     return;
                  } else {
                     step = 449;
                     return;
                  }
               }
               case 666 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 99;
                     return;
                  }
               }
               case 667 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 774;
                     return;
                  } else {
                     step = 775;
                     return;
                  }
               }
               case 668 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 776;
                     return;
                  }
               }
               case 669 -> {
                  var result13 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 777;
                     return;
                  }
               }
               case 670 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 775;
                     return;
                  }
               }
               case 671 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 778;
                     return;
                  }
               }
               case 672 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 779;
                     return;
                  } else {
                     step = 780;
                     return;
                  }
               }
               case 673 -> {
                  var result14 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 356;
                     return;
                  }
               }
               case 674 -> {
                  var result14 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 171;
                     return;
                  }
               }
               case 675 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 781;
                     return;
                  }
               }
               case 676 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 782;
                     return;
                  } else {
                     step = 783;
                     return;
                  }
               }
               case 677 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 784;
                     return;
                  }
               }
               case 678 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 785;
                     return;
                  }
               }
               case 679 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 786;
                     return;
                  }
               }
               case 680 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 787;
                     return;
                  } else {
                     step = 788;
                     return;
                  }
               }
               case 681 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 789;
                     return;
                  }
               }
               case 682 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 790;
                     return;
                  } else {
                     step = 791;
                     return;
                  }
               }
               case 683 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 792;
                     return;
                  }
               }
               case 684 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 793;
                     return;
                  }
               }
               case 685 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 794;
                     return;
                  }
               }
               case 686 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 795;
                     return;
                  } else {
                     step = 796;
                     return;
                  }
               }
               case 687 -> {
                  var result13 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 797;
                     return;
                  }
               }
               case 688 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved16 = nextValue0;
                     step = 798;
                     return;
                  } else {
                     step = 799;
                     return;
                  }
               }
               case 689 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 800;
                     return;
                  }
               }
               case 690 -> {
                  var result14 = saved13.getValue();
                  if ((result14 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result14;
                     saved15 = nextValue0;
                     step = 801;
                     return;
                  }
               }
               case 691 -> {
                  var result14 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 802;
                     return;
                  }
               }
               case 692 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 803;
                     return;
                  } else {
                     step = 804;
                     return;
                  }
               }
               case 693 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 805;
                     return;
                  }
               }
               case 694 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 806;
                     return;
                  }
               }
               case 695 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 807;
                     return;
                  }
               }
               case 696 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 808;
                     return;
                  } else {
                     step = 809;
                     return;
                  }
               }
               case 697 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 810;
                     return;
                  }
               }
               case 698 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 811;
                     return;
                  }
               }
               case 699 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 812;
                     return;
                  }
               }
               case 700 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 813;
                     return;
                  }
               }
               case 701 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 814;
                     return;
                  } else {
                     step = 815;
                     return;
                  }
               }
               case 702 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 816;
                     return;
                  } else {
                     step = 313;
                     return;
                  }
               }
               case 703 -> {
                  var result12 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result12 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result12 * -(16)));
                     int nextValue1 = result12;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 817;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance9() {
            switch (step) {
               case 704 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 819;
                     return;
                  }
               }
               case 705 -> {
                  var result13 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result13 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result13 * -(16)));
                     int nextValue1 = result13;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 820;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 706 -> {
                  var result11 = saved10.isEmpty();
                  if (!(result11)) {
                     step = 822;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 707 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 823;
                     return;
                  }
               }
               case 708 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 824;
                     return;
                  }
               }
               case 709 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 825;
                     return;
                  } else {
                     step = 826;
                     return;
                  }
               }
               case 710 -> {
                  var result12 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result12 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result12 * -(16)));
                     int nextValue1 = result12;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 827;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 711 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 829;
                     return;
                  }
               }
               case 712 -> {
                  var result13 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result13 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result13 * -(16)));
                     int nextValue1 = result13;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 830;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 713 -> {
                  var result11 = saved10.isEmpty();
                  if (!(result11)) {
                     step = 832;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 714 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 833;
                     return;
                  }
               }
               case 715 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 834;
                     return;
                  }
               }
               case 716 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 835;
                     return;
                  } else {
                     step = 836;
                     return;
                  }
               }
               case 717 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 837;
                     return;
                  }
               }
               case 718 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 838;
                     return;
                  }
               }
               case 719 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 502;
                     return;
                  } else {
                     step = 503;
                     return;
                  }
               }
               case 720 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 839;
                     return;
                  } else {
                     step = 840;
                     return;
                  }
               }
               case 721 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 841;
                     return;
                  } else {
                     step = 512;
                     return;
                  }
               }
               case 722 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 840;
                     return;
                  }
               }
               case 723 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 842;
                     return;
                  }
               }
               case 724 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 506;
                     return;
                  } else {
                     step = 507;
                     return;
                  }
               }
               case 725 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 843;
                     return;
                  } else {
                     step = 844;
                     return;
                  }
               }
               case 726 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 845;
                     return;
                  }
               }
               case 727 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 846;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 728 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 844;
                     return;
                  }
               }
               case 729 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 847;
                     return;
                  }
               }
               case 730 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 848;
                     return;
                  } else {
                     step = 849;
                     return;
                  }
               }
               case 731 -> {
                  var result14 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 408;
                     return;
                  }
               }
               case 732 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 850;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 733 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 851;
                     return;
                  } else {
                     step = 852;
                     return;
                  }
               }
               case 734 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 853;
                     return;
                  }
               }
               case 735 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 854;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 736 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 852;
                     return;
                  }
               }
               case 737 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 855;
                     return;
                  }
               }
               case 738 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 856;
                     return;
                  } else {
                     step = 857;
                     return;
                  }
               }
               case 739 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 413;
                     return;
                  }
               }
               case 740 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 858;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 741 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 859;
                     return;
                  } else {
                     step = 860;
                     return;
                  }
               }
               case 742 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 861;
                     return;
                  }
               }
               case 743 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 862;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 744 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 860;
                     return;
                  }
               }
               case 745 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 863;
                     return;
                  }
               }
               case 746 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 864;
                     return;
                  } else {
                     step = 865;
                     return;
                  }
               }
               case 747 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 418;
                     return;
                  }
               }
               case 748 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 866;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 749 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 867;
                     return;
                  }
               }
               case 750 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 868;
                     return;
                  }
               }
               case 751 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 869;
                     return;
                  }
               }
               case 752 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 870;
                     return;
                  } else {
                     step = 871;
                     return;
                  }
               }
               case 753 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 872;
                     return;
                  }
               }
               case 754 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 873;
                     return;
                  }
               }
               case 755 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 874;
                     return;
                  }
               }
               case 756 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 875;
                     return;
                  }
               }
               case 757 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 876;
                     return;
                  } else {
                     step = 877;
                     return;
                  }
               }
               case 758 -> {
                  var result13 = saved11.getBlock();
                  var result14 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result13);
                  if (result14) {
                     step = 878;
                     return;
                  } else {
                     step = 349;
                     return;
                  }
               }
               case 759 -> {
                  var result12 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result12 * 16) <= 32)) {
                     int nextValue0 = (32 + (result12 * -(16)));
                     int nextValue1 = result12;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 879;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 760 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 881;
                     return;
                  }
               }
               case 761 -> {
                  var result13 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result13 * 16) <= 32)) {
                     int nextValue0 = (32 + (result13 * -(16)));
                     int nextValue1 = result13;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 882;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 762 -> {
                  var result11 = saved10.isEmpty();
                  if (!(result11)) {
                     step = 884;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 763 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 885;
                     return;
                  }
               }
               case 764 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 886;
                     return;
                  }
               }
               case 765 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 887;
                     return;
                  } else {
                     step = 888;
                     return;
                  }
               }
               case 766 -> {
                  var result12 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result12 * 16) <= 32)) {
                     int nextValue0 = (32 + (result12 * -(16)));
                     int nextValue1 = result12;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 889;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 767 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 891;
                     return;
                  }
               }
               case 768 -> {
                  var result13 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result13 * 16) <= 32)) {
                     int nextValue0 = (32 + (result13 * -(16)));
                     int nextValue1 = result13;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 892;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 769 -> {
                  var result11 = saved10.isEmpty();
                  if (!(result11)) {
                     step = 894;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 770 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 895;
                     return;
                  }
               }
               case 771 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 896;
                     return;
                  }
               }
               case 772 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 897;
                     return;
                  } else {
                     step = 898;
                     return;
                  }
               }
               case 773 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 899;
                     return;
                  }
               }
               case 774 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 900;
                     return;
                  }
               }
               case 775 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 553;
                     return;
                  } else {
                     step = 554;
                     return;
                  }
               }
               case 776 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 901;
                     return;
                  } else {
                     step = 902;
                     return;
                  }
               }
               case 777 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 903;
                     return;
                  } else {
                     step = 563;
                     return;
                  }
               }
               case 778 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 902;
                     return;
                  }
               }
               case 779 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 904;
                     return;
                  }
               }
               case 780 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 557;
                     return;
                  } else {
                     step = 558;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance10() {
            switch (step) {
               case 781 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 905;
                     return;
                  } else {
                     step = 906;
                     return;
                  }
               }
               case 782 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 907;
                     return;
                  }
               }
               case 783 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 908;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 784 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 906;
                     return;
                  }
               }
               case 785 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 909;
                     return;
                  }
               }
               case 786 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 910;
                     return;
                  } else {
                     step = 911;
                     return;
                  }
               }
               case 787 -> {
                  var result14 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 457;
                     return;
                  }
               }
               case 788 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 912;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 789 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 913;
                     return;
                  } else {
                     step = 914;
                     return;
                  }
               }
               case 790 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 915;
                     return;
                  }
               }
               case 791 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 916;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 792 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 914;
                     return;
                  }
               }
               case 793 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 917;
                     return;
                  }
               }
               case 794 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 918;
                     return;
                  } else {
                     step = 919;
                     return;
                  }
               }
               case 795 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 462;
                     return;
                  }
               }
               case 796 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 920;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 797 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 921;
                     return;
                  } else {
                     step = 922;
                     return;
                  }
               }
               case 798 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result13;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 923;
                     return;
                  }
               }
               case 799 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 924;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 800 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 922;
                     return;
                  }
               }
               case 801 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 925;
                     return;
                  }
               }
               case 802 -> {
                  var result15 = saved11.getBlock();
                  var result16 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result15);
                  if (result16) {
                     step = 926;
                     return;
                  } else {
                     step = 927;
                     return;
                  }
               }
               case 803 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result14;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 467;
                     return;
                  }
               }
               case 804 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 928;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 805 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 470;
                     return;
                  }
               }
               case 806 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 929;
                     return;
                  }
               }
               case 807 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 930;
                     return;
                  } else {
                     step = 931;
                     return;
                  }
               }
               case 808 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 471;
                     return;
                  }
               }
               case 809 -> {
                  var result15 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 472;
                     return;
                  }
               }
               case 810 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 932;
                     return;
                  }
               }
               case 811 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 931;
                     return;
                  }
               }
               case 812 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 933;
                     return;
                  }
               }
               case 813 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 934;
                     return;
                  }
               }
               case 814 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 935;
                     return;
                  }
               }
               case 815 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 936;
                     return;
                  } else {
                     step = 937;
                     return;
                  }
               }
               case 816 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 938;
                     return;
                  }
               }
               case 817 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 939;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 818 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 703;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 819 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 940;
                     return;
                  }
               }
               case 820 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 941;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 821 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 705;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 822 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result12 = saved10.hasAny(callback2);
                  if (result12) {
                     step = 942;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 823 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 943;
                     return;
                  }
               }
               case 824 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 944;
                     return;
                  }
               }
               case 825 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 945;
                     return;
                  }
               }
               case 826 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 946;
                     return;
                  } else {
                     step = 947;
                     return;
                  }
               }
               case 827 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 948;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 828 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 710;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 829 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 949;
                     return;
                  }
               }
               case 830 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 950;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 831 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 712;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 832 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result12 = saved10.hasAny(callback2);
                  if (result12) {
                     step = 951;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 833 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 952;
                     return;
                  }
               }
               case 834 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 953;
                     return;
                  }
               }
               case 835 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 954;
                     return;
                  }
               }
               case 836 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 955;
                     return;
                  } else {
                     step = 956;
                     return;
                  }
               }
               case 837 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 957;
                     return;
                  }
               }
               case 838 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 958;
                     return;
                  }
               }
               case 839 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 959;
                     return;
                  }
               }
               case 840 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 960;
                     return;
                  } else {
                     step = 961;
                     return;
                  }
               }
               case 841 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 962;
                     return;
                  }
               }
               case 842 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 963;
                     return;
                  }
               }
               case 843 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 964;
                     return;
                  }
               }
               case 844 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 614;
                     return;
                  } else {
                     step = 615;
                     return;
                  }
               }
               case 845 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 965;
                     return;
                  } else {
                     step = 966;
                     return;
                  }
               }
               case 846 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 967;
                     return;
                  }
               }
               case 847 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 966;
                     return;
                  }
               }
               case 848 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 968;
                     return;
                  }
               }
               case 849 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 618;
                     return;
                  } else {
                     step = 619;
                     return;
                  }
               }
               case 850 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 153;
                     return;
                  }
               }
               case 851 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 969;
                     return;
                  }
               }
               case 852 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 620;
                     return;
                  } else {
                     step = 621;
                     return;
                  }
               }
               case 853 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 970;
                     return;
                  } else {
                     step = 971;
                     return;
                  }
               }
               case 854 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 721;
                     return;
                  }
               }
               case 855 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 971;
                     return;
                  }
               }
               case 856 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 972;
                     return;
                  }
               }
               case 857 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 624;
                     return;
                  } else {
                     step = 625;
                     return;
                  }
               }
               case 858 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 152;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance11() {
            switch (step) {
               case 859 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 973;
                     return;
                  }
               }
               case 860 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 626;
                     return;
                  } else {
                     step = 627;
                     return;
                  }
               }
               case 861 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 974;
                     return;
                  } else {
                     step = 975;
                     return;
                  }
               }
               case 862 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 967;
                     return;
                  }
               }
               case 863 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 975;
                     return;
                  }
               }
               case 864 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 976;
                     return;
                  }
               }
               case 865 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 630;
                     return;
                  } else {
                     step = 631;
                     return;
                  }
               }
               case 866 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 153;
                     return;
                  }
               }
               case 867 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 521;
                     return;
                  }
               }
               case 868 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 977;
                     return;
                  }
               }
               case 869 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 978;
                     return;
                  } else {
                     step = 979;
                     return;
                  }
               }
               case 870 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 522;
                     return;
                  }
               }
               case 871 -> {
                  var result15 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 523;
                     return;
                  }
               }
               case 872 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 980;
                     return;
                  }
               }
               case 873 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 979;
                     return;
                  }
               }
               case 874 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 981;
                     return;
                  }
               }
               case 875 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 982;
                     return;
                  }
               }
               case 876 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 983;
                     return;
                  }
               }
               case 877 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 984;
                     return;
                  } else {
                     step = 985;
                     return;
                  }
               }
               case 878 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 986;
                     return;
                  }
               }
               case 879 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 987;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 880 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 759;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 881 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 988;
                     return;
                  }
               }
               case 882 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 989;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 883 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 761;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 884 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result12 = saved10.hasAny(callback2);
                  if (result12) {
                     step = 990;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 885 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 991;
                     return;
                  }
               }
               case 886 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 992;
                     return;
                  }
               }
               case 887 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 993;
                     return;
                  }
               }
               case 888 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 994;
                     return;
                  } else {
                     step = 995;
                     return;
                  }
               }
               case 889 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 996;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 890 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 766;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 891 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 997;
                     return;
                  }
               }
               case 892 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 998;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 893 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 768;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 894 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result12 = saved10.hasAny(callback2);
                  if (result12) {
                     step = 999;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 895 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1000;
                     return;
                  }
               }
               case 896 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 1001;
                     return;
                  }
               }
               case 897 -> {
                  var result15 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 1002;
                     return;
                  }
               }
               case 898 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1003;
                     return;
                  } else {
                     step = 1004;
                     return;
                  }
               }
               case 899 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1005;
                     return;
                  }
               }
               case 900 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1006;
                     return;
                  }
               }
               case 901 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1007;
                     return;
                  }
               }
               case 902 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1008;
                     return;
                  } else {
                     step = 1009;
                     return;
                  }
               }
               case 903 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1010;
                     return;
                  }
               }
               case 904 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1011;
                     return;
                  }
               }
               case 905 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1012;
                     return;
                  }
               }
               case 906 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 675;
                     return;
                  } else {
                     step = 676;
                     return;
                  }
               }
               case 907 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 1013;
                     return;
                  } else {
                     step = 1014;
                     return;
                  }
               }
               case 908 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 1015;
                     return;
                  }
               }
               case 909 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1014;
                     return;
                  }
               }
               case 910 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 1016;
                     return;
                  }
               }
               case 911 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 679;
                     return;
                  } else {
                     step = 680;
                     return;
                  }
               }
               case 912 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 172;
                     return;
                  }
               }
               case 913 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1017;
                     return;
                  }
               }
               case 914 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 681;
                     return;
                  } else {
                     step = 682;
                     return;
                  }
               }
               case 915 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 1018;
                     return;
                  } else {
                     step = 1019;
                     return;
                  }
               }
               case 916 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 777;
                     return;
                  }
               }
               case 917 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1019;
                     return;
                  }
               }
               case 918 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 1020;
                     return;
                  }
               }
               case 919 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 685;
                     return;
                  } else {
                     step = 686;
                     return;
                  }
               }
               case 920 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 171;
                     return;
                  }
               }
               case 921 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1021;
                     return;
                  }
               }
               case 922 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 687;
                     return;
                  } else {
                     step = 688;
                     return;
                  }
               }
               case 923 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 1022;
                     return;
                  } else {
                     step = 1023;
                     return;
                  }
               }
               case 924 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 1015;
                     return;
                  }
               }
               case 925 -> {
                  var result15 = ((java.lang.Integer) saved15).intValue();
                  if ((result15 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1023;
                     return;
                  }
               }
               case 926 -> {
                  var field5 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field5 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field5;
                     saved13 = nextValue0;
                     step = 1024;
                     return;
                  }
               }
               case 927 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 691;
                     return;
                  } else {
                     step = 692;
                     return;
                  }
               }
               case 928 -> {
                  var result14 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result14 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result14;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 172;
                     return;
                  }
               }
               case 929 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 573;
                     return;
                  }
               }
               case 930 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1025;
                     return;
                  }
               }
               case 931 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 695;
                     return;
                  } else {
                     step = 696;
                     return;
                  }
               }
               case 932 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 307;
                     return;
                  }
               }
               case 933 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 577;
                     return;
                  }
               }
               case 934 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1026;
                     return;
                  }
               }
               case 935 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1027;
                     return;
                  } else {
                     step = 1028;
                     return;
                  }
               }
               case 936 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 578;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance12() {
            switch (step) {
               case 937 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 1029;
                     return;
                  } else {
                     step = 1030;
                     return;
                  }
               }
               case 938 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 1031;
                     return;
                  }
               }
               case 939 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1032;
                     return;
                  }
               }
               case 940 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1028;
                     return;
                  }
               }
               case 941 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1033;
                     return;
                  }
               }
               case 942 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1034;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1035;
                     return;
                  }
               }
               case 943 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 588;
                     return;
                  }
               }
               case 944 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1036;
                     return;
                  }
               }
               case 945 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1037;
                     return;
                  } else {
                     step = 1038;
                     return;
                  }
               }
               case 946 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 589;
                     return;
                  }
               }
               case 947 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1039;
                     return;
                  } else {
                     step = 1040;
                     return;
                  }
               }
               case 948 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1041;
                     return;
                  }
               }
               case 949 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1038;
                     return;
                  }
               }
               case 950 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1042;
                     return;
                  }
               }
               case 951 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1043;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1044;
                     return;
                  }
               }
               case 952 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 599;
                     return;
                  }
               }
               case 953 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1045;
                     return;
                  }
               }
               case 954 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1046;
                     return;
                  } else {
                     step = 1047;
                     return;
                  }
               }
               case 955 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 600;
                     return;
                  }
               }
               case 956 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1048;
                     return;
                  } else {
                     step = 1040;
                     return;
                  }
               }
               case 957 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1047;
                     return;
                  }
               }
               case 958 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1049;
                     return;
                  }
               }
               case 959 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1050;
                     return;
                  }
               }
               case 960 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 1051;
                     return;
                  }
               }
               case 961 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1052;
                     return;
                  } else {
                     step = 1053;
                     return;
                  }
               }
               case 962 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1054;
                     return;
                  }
               }
               case 963 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1055;
                     return;
                  }
               }
               case 964 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1056;
                     return;
                  }
               }
               case 965 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1057;
                     return;
                  }
               }
               case 966 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1058;
                     return;
                  } else {
                     step = 1059;
                     return;
                  }
               }
               case 967 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 1060;
                     return;
                  } else {
                     step = 516;
                     return;
                  }
               }
               case 968 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1061;
                     return;
                  }
               }
               case 969 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1062;
                     return;
                  }
               }
               case 970 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1063;
                     return;
                  }
               }
               case 971 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1064;
                     return;
                  } else {
                     step = 1065;
                     return;
                  }
               }
               case 972 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1066;
                     return;
                  }
               }
               case 973 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1067;
                     return;
                  }
               }
               case 974 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1068;
                     return;
                  }
               }
               case 975 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1069;
                     return;
                  } else {
                     step = 1070;
                     return;
                  }
               }
               case 976 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1071;
                     return;
                  }
               }
               case 977 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 634;
                     return;
                  }
               }
               case 978 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1072;
                     return;
                  }
               }
               case 979 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 751;
                     return;
                  } else {
                     step = 752;
                     return;
                  }
               }
               case 980 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 344;
                     return;
                  }
               }
               case 981 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 638;
                     return;
                  }
               }
               case 982 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1073;
                     return;
                  }
               }
               case 983 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1074;
                     return;
                  } else {
                     step = 1075;
                     return;
                  }
               }
               case 984 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 639;
                     return;
                  }
               }
               case 985 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 1076;
                     return;
                  } else {
                     step = 1077;
                     return;
                  }
               }
               case 986 -> {
                  var result15 = saved13.getValue();
                  if ((result15 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result15;
                     saved15 = nextValue0;
                     step = 1078;
                     return;
                  }
               }
               case 987 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1079;
                     return;
                  }
               }
               case 988 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1075;
                     return;
                  }
               }
               case 989 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1080;
                     return;
                  }
               }
               case 990 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1081;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1082;
                     return;
                  }
               }
               case 991 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 649;
                     return;
                  }
               }
               case 992 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1083;
                     return;
                  }
               }
               case 993 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1084;
                     return;
                  } else {
                     step = 1085;
                     return;
                  }
               }
               case 994 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 650;
                     return;
                  }
               }
               case 995 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1086;
                     return;
                  } else {
                     step = 1087;
                     return;
                  }
               }
               case 996 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1088;
                     return;
                  }
               }
               case 997 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1085;
                     return;
                  }
               }
               case 998 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1089;
                     return;
                  }
               }
               case 999 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1090;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1091;
                     return;
                  }
               }
               case 1000 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 660;
                     return;
                  }
               }
               case 1001 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1092;
                     return;
                  }
               }
               case 1002 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1093;
                     return;
                  } else {
                     step = 1094;
                     return;
                  }
               }
               case 1003 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result15;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 661;
                     return;
                  }
               }
               case 1004 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1095;
                     return;
                  } else {
                     step = 1087;
                     return;
                  }
               }
               case 1005 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1094;
                     return;
                  }
               }
               case 1006 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1096;
                     return;
                  }
               }
               case 1007 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1097;
                     return;
                  }
               }
               case 1008 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 1098;
                     return;
                  }
               }
               case 1009 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1099;
                     return;
                  } else {
                     step = 1100;
                     return;
                  }
               }
               case 1010 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1101;
                     return;
                  }
               }
               case 1011 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1102;
                     return;
                  }
               }
               case 1012 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1103;
                     return;
                  }
               }
               case 1013 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1104;
                     return;
                  }
               }
               case 1014 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1105;
                     return;
                  } else {
                     step = 1106;
                     return;
                  }
               }
               case 1015 -> {
                  var result14 = saved11.getBlock();
                  var result15 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result14);
                  if (result15) {
                     step = 1107;
                     return;
                  } else {
                     step = 567;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance13() {
            switch (step) {
               case 1016 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1108;
                     return;
                  }
               }
               case 1017 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1109;
                     return;
                  }
               }
               case 1018 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1110;
                     return;
                  }
               }
               case 1019 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1111;
                     return;
                  } else {
                     step = 1112;
                     return;
                  }
               }
               case 1020 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1113;
                     return;
                  }
               }
               case 1021 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1114;
                     return;
                  }
               }
               case 1022 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1115;
                     return;
                  }
               }
               case 1023 -> {
                  if (((0 + 1) != 16)) {
                     int nextValue0 = (0 + 1);
                     saved14 = nextValue0;
                     step = 1116;
                     return;
                  } else {
                     step = 1117;
                     return;
                  }
               }
               case 1024 -> {
                  var result17 = saved13.getValue();
                  if ((result17 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result17;
                     saved15 = nextValue0;
                     step = 1118;
                     return;
                  }
               }
               case 1025 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1119;
                     return;
                  }
               }
               case 1026 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 701;
                     return;
                  }
               }
               case 1027 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1120;
                     return;
                  }
               }
               case 1028 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 814;
                     return;
                  } else {
                     step = 815;
                     return;
                  }
               }
               case 1029 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 702;
                     return;
                  }
               }
               case 1030 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 1121;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 1031 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1122;
                     return;
                  }
               }
               case 1032 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1123;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1033 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1124;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1034 -> {
                  if ((14 < saved7)) {
                     step = 1125;
                     return;
                  } else {
                     step = 1126;
                     return;
                  }
               }
               case 1035 -> {
                  if ((14 < saved7)) {
                     step = 1127;
                     return;
                  } else {
                     step = 1128;
                     return;
                  }
               }
               case 1036 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 709;
                     return;
                  }
               }
               case 1037 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1129;
                     return;
                  }
               }
               case 1038 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 825;
                     return;
                  } else {
                     step = 826;
                     return;
                  }
               }
               case 1039 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 472;
                     return;
                  }
               }
               case 1040 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 1130;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 1041 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1131;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1042 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1132;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1043 -> {
                  if ((14 < saved7)) {
                     step = 1133;
                     return;
                  } else {
                     step = 1134;
                     return;
                  }
               }
               case 1044 -> {
                  if ((14 < saved7)) {
                     step = 1135;
                     return;
                  } else {
                     step = 1136;
                     return;
                  }
               }
               case 1045 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 716;
                     return;
                  }
               }
               case 1046 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1137;
                     return;
                  }
               }
               case 1047 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 835;
                     return;
                  } else {
                     step = 836;
                     return;
                  }
               }
               case 1048 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 702;
                     return;
                  }
               }
               case 1049 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 719;
                     return;
                  }
               }
               case 1050 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1138;
                     return;
                  }
               }
               case 1051 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1139;
                     return;
                  } else {
                     step = 1140;
                     return;
                  }
               }
               case 1052 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 720;
                     return;
                  }
               }
               case 1053 -> {
                  var result16 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 721;
                     return;
                  }
               }
               case 1054 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1141;
                     return;
                  }
               }
               case 1055 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1140;
                     return;
                  }
               }
               case 1056 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1142;
                     return;
                  }
               }
               case 1057 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1143;
                     return;
                  }
               }
               case 1058 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 1144;
                     return;
                  }
               }
               case 1059 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1145;
                     return;
                  } else {
                     step = 1146;
                     return;
                  }
               }
               case 1060 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1147;
                     return;
                  }
               }
               case 1061 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1148;
                     return;
                  }
               }
               case 1062 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1149;
                     return;
                  }
               }
               case 1063 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1150;
                     return;
                  }
               }
               case 1064 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 1151;
                     return;
                  }
               }
               case 1065 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1152;
                     return;
                  } else {
                     step = 1153;
                     return;
                  }
               }
               case 1066 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1154;
                     return;
                  }
               }
               case 1067 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1155;
                     return;
                  }
               }
               case 1068 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1156;
                     return;
                  }
               }
               case 1069 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 1157;
                     return;
                  }
               }
               case 1070 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1158;
                     return;
                  } else {
                     step = 1159;
                     return;
                  }
               }
               case 1071 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1160;
                     return;
                  }
               }
               case 1072 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1161;
                     return;
                  }
               }
               case 1073 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 757;
                     return;
                  }
               }
               case 1074 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1162;
                     return;
                  }
               }
               case 1075 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 876;
                     return;
                  } else {
                     step = 877;
                     return;
                  }
               }
               case 1076 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 758;
                     return;
                  }
               }
               case 1077 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 1163;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 1078 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1164;
                     return;
                  }
               }
               case 1079 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1165;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1080 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1166;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1081 -> {
                  if ((14 < saved7)) {
                     step = 1167;
                     return;
                  } else {
                     step = 1168;
                     return;
                  }
               }
               case 1082 -> {
                  if ((14 < saved7)) {
                     step = 1169;
                     return;
                  } else {
                     step = 1170;
                     return;
                  }
               }
               case 1083 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 765;
                     return;
                  }
               }
               case 1084 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1171;
                     return;
                  }
               }
               case 1085 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 887;
                     return;
                  } else {
                     step = 888;
                     return;
                  }
               }
               case 1086 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 523;
                     return;
                  }
               }
               case 1087 -> {
                  var length3 = saved6.length;
                  if (((0 + 1) < length3)) {
                     int nextValue0 = (0 + 1);
                     saved9 = nextValue0;
                     step = 1172;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 1088 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1173;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1089 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1174;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 1090 -> {
                  if ((14 < saved7)) {
                     step = 1175;
                     return;
                  } else {
                     step = 1176;
                     return;
                  }
               }
               case 1091 -> {
                  if ((14 < saved7)) {
                     step = 1177;
                     return;
                  } else {
                     step = 1178;
                     return;
                  }
               }
               case 1092 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 772;
                     return;
                  }
               }
               case 1093 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1179;
                     return;
                  }
               }
               case 1094 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 897;
                     return;
                  } else {
                     step = 898;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance14() {
            switch (step) {
               case 1095 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 758;
                     return;
                  }
               }
               case 1096 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 775;
                     return;
                  }
               }
               case 1097 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1180;
                     return;
                  }
               }
               case 1098 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1181;
                     return;
                  } else {
                     step = 1182;
                     return;
                  }
               }
               case 1099 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 776;
                     return;
                  }
               }
               case 1100 -> {
                  var result16 = saved10.getBlockState((int) (0L), (0 + 1), 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     int nextValue1 = (0 + 1);
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved4 = nextValue0;
                     saved12 = nextValue1;
                     saved11 = nextValue2;
                     step = 777;
                     return;
                  }
               }
               case 1101 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1183;
                     return;
                  }
               }
               case 1102 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1182;
                     return;
                  }
               }
               case 1103 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1184;
                     return;
                  }
               }
               case 1104 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1185;
                     return;
                  }
               }
               case 1105 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 1186;
                     return;
                  }
               }
               case 1106 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1187;
                     return;
                  } else {
                     step = 1188;
                     return;
                  }
               }
               case 1107 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1189;
                     return;
                  }
               }
               case 1108 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1190;
                     return;
                  }
               }
               case 1109 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1191;
                     return;
                  }
               }
               case 1110 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1192;
                     return;
                  }
               }
               case 1111 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 1193;
                     return;
                  }
               }
               case 1112 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1194;
                     return;
                  } else {
                     step = 1195;
                     return;
                  }
               }
               case 1113 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1196;
                     return;
                  }
               }
               case 1114 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1197;
                     return;
                  }
               }
               case 1115 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1198;
                     return;
                  }
               }
               case 1116 -> {
                  var result16 = saved10.getBlockState((int) (Integer.toUnsignedLong(saved14)), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 1199;
                     return;
                  }
               }
               case 1117 -> {
                  if (((saved16 + 1) != 16)) {
                     int nextValue0 = (saved16 + 1);
                     saved16 = nextValue0;
                     step = 1200;
                     return;
                  } else {
                     step = 1201;
                     return;
                  }
               }
               case 1118 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1202;
                     return;
                  }
               }
               case 1119 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1203;
                     return;
                  }
               }
               case 1120 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1204;
                     return;
                  }
               }
               case 1121 -> {
                  var result15 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result15 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result15 * -(16)));
                     int nextValue1 = result15;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 1205;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1122 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 313;
                     return;
                  }
               }
               case 1123 -> {
                  var result13 = saved10.isEmpty();
                  if (!(result13)) {
                     step = 1207;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1124 -> {
                  var result14 = saved10.isEmpty();
                  if (!(result14)) {
                     step = 1208;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1125 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 116;
                     return;
                  }
               }
               case 1126 -> {
                  if ((0 <= saved7)) {
                     step = 1209;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 1127 -> {
                  if ((saved4 <= 15)) {
                     step = 1210;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 1128 -> {
                  if ((saved4 <= saved7)) {
                     step = 409;
                     return;
                  } else {
                     step = 384;
                     return;
                  }
               }
               case 1129 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1211;
                     return;
                  }
               }
               case 1130 -> {
                  var result15 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result15 * 16) <= saved5)) {
                     int nextValue0 = (saved5 + (result15 * -(16)));
                     int nextValue1 = result15;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 1212;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1131 -> {
                  var result13 = saved10.isEmpty();
                  if (!(result13)) {
                     step = 1214;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1132 -> {
                  var result14 = saved10.isEmpty();
                  if (!(result14)) {
                     step = 1215;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1133 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 116;
                     return;
                  }
               }
               case 1134 -> {
                  if ((0 <= saved7)) {
                     step = 1216;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 1135 -> {
                  if ((saved4 <= 15)) {
                     step = 414;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 1136 -> {
                  if ((saved4 <= saved7)) {
                     step = 419;
                     return;
                  } else {
                     step = 393;
                     return;
                  }
               }
               case 1137 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1217;
                     return;
                  }
               }
               case 1138 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 840;
                     return;
                  }
               }
               case 1139 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1218;
                     return;
                  }
               }
               case 1140 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 960;
                     return;
                  } else {
                     step = 961;
                     return;
                  }
               }
               case 1141 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 512;
                     return;
                  }
               }
               case 1142 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 844;
                     return;
                  }
               }
               case 1143 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1219;
                     return;
                  }
               }
               case 1144 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1220;
                     return;
                  } else {
                     step = 1221;
                     return;
                  }
               }
               case 1145 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 845;
                     return;
                  }
               }
               case 1146 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 1222;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1147 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1223;
                     return;
                  }
               }
               case 1148 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1221;
                     return;
                  }
               }
               case 1149 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 852;
                     return;
                  }
               }
               case 1150 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1224;
                     return;
                  }
               }
               case 1151 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1225;
                     return;
                  } else {
                     step = 1226;
                     return;
                  }
               }
               case 1152 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 853;
                     return;
                  }
               }
               case 1153 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1227;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1154 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1226;
                     return;
                  }
               }
               case 1155 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 860;
                     return;
                  }
               }
               case 1156 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1228;
                     return;
                  }
               }
               case 1157 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1229;
                     return;
                  } else {
                     step = 1230;
                     return;
                  }
               }
               case 1158 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 861;
                     return;
                  }
               }
               case 1159 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1231;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1160 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1230;
                     return;
                  }
               }
               case 1161 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1232;
                     return;
                  }
               }
               case 1162 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1233;
                     return;
                  }
               }
               case 1163 -> {
                  var result15 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result15 * 16) <= 32)) {
                     int nextValue0 = (32 + (result15 * -(16)));
                     int nextValue1 = result15;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 1234;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1164 -> {
                  var result16 = ((java.lang.Integer) saved15).intValue();
                  if ((result16 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 349;
                     return;
                  }
               }
               case 1165 -> {
                  var result13 = saved10.isEmpty();
                  if (!(result13)) {
                     step = 1236;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1166 -> {
                  var result14 = saved10.isEmpty();
                  if (!(result14)) {
                     step = 1237;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1167 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 130;
                     return;
                  }
               }
               case 1168 -> {
                  if ((0 <= saved7)) {
                     step = 1238;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 1169 -> {
                  if ((saved4 <= 15)) {
                     step = 1239;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 1170 -> {
                  if ((saved4 <= saved7)) {
                     step = 458;
                     return;
                  } else {
                     step = 435;
                     return;
                  }
               }
               case 1171 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1240;
                     return;
                  }
               }
               case 1172 -> {
                  var result15 = chunk.sectionIndexToCoord((int) (Integer.toUnsignedLong(saved9)));
                  if (((result15 * 16) <= 32)) {
                     int nextValue0 = (32 + (result15 * -(16)));
                     int nextValue1 = result15;
                     saved7 = nextValue0;
                     saved4 = nextValue1;
                     step = 1241;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1173 -> {
                  var result13 = saved10.isEmpty();
                  if (!(result13)) {
                     step = 1243;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1174 -> {
                  var result14 = saved10.isEmpty();
                  if (!(result14)) {
                     step = 1244;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance15() {
            switch (step) {
               case 1175 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 130;
                     return;
                  }
               }
               case 1176 -> {
                  if ((0 <= saved7)) {
                     step = 1245;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 1177 -> {
                  if ((saved4 <= 15)) {
                     step = 463;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 1178 -> {
                  if ((saved4 <= saved7)) {
                     step = 468;
                     return;
                  } else {
                     step = 443;
                     return;
                  }
               }
               case 1179 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1246;
                     return;
                  }
               }
               case 1180 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 902;
                     return;
                  }
               }
               case 1181 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1247;
                     return;
                  }
               }
               case 1182 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1008;
                     return;
                  } else {
                     step = 1009;
                     return;
                  }
               }
               case 1183 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 563;
                     return;
                  }
               }
               case 1184 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 906;
                     return;
                  }
               }
               case 1185 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1248;
                     return;
                  }
               }
               case 1186 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1249;
                     return;
                  } else {
                     step = 1250;
                     return;
                  }
               }
               case 1187 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved16;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved17 = nextValue0;
                     saved11 = nextValue1;
                     step = 907;
                     return;
                  }
               }
               case 1188 -> {
                  if ((0 < saved7)) {
                     int nextValue0 = (0 + 1);
                     saved4 = nextValue0;
                     step = 1251;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1189 -> {
                  var result16 = saved13.getValue();
                  if ((result16 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result16;
                     saved15 = nextValue0;
                     step = 1252;
                     return;
                  }
               }
               case 1190 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1250;
                     return;
                  }
               }
               case 1191 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 914;
                     return;
                  }
               }
               case 1192 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1253;
                     return;
                  }
               }
               case 1193 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1254;
                     return;
                  } else {
                     step = 1255;
                     return;
                  }
               }
               case 1194 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 915;
                     return;
                  }
               }
               case 1195 -> {
                  if ((saved4 < 15)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1256;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1196 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1255;
                     return;
                  }
               }
               case 1197 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 922;
                     return;
                  }
               }
               case 1198 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1257;
                     return;
                  }
               }
               case 1199 -> {
                  var result17 = saved11.getBlock();
                  var result18 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result17);
                  if (result18) {
                     step = 1258;
                     return;
                  } else {
                     step = 1259;
                     return;
                  }
               }
               case 1200 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, saved16);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     int nextValue1 = saved16;
                     net.minecraft.block.BlockState nextValue2 = result16;
                     saved12 = nextValue0;
                     saved17 = nextValue1;
                     saved11 = nextValue2;
                     step = 923;
                     return;
                  }
               }
               case 1201 -> {
                  if ((saved4 < saved7)) {
                     int nextValue0 = (saved4 + 1);
                     saved4 = nextValue0;
                     step = 1260;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1202 -> {
                  var result18 = ((java.lang.Integer) saved15).intValue();
                  if ((result18 <= (0 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (0 + 1);
                     saved18 = nextValue0;
                     step = 1259;
                     return;
                  }
               }
               case 1203 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 931;
                     return;
                  }
               }
               case 1204 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1261;
                     return;
                  }
               }
               case 1205 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 1262;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1206 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 1121;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 1207 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result14 = saved10.hasAny(callback2);
                  if (result14) {
                     step = 1263;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1208 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result15 = saved10.hasAny(callback2);
                  if (result15) {
                     step = 1264;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1209 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 151;
                     return;
                  }
               }
               case 1210 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 152;
                     return;
                  }
               }
               case 1211 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1265;
                     return;
                  }
               }
               case 1212 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 1266;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1213 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 1130;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 1214 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result14 = saved10.hasAny(callback2);
                  if (result14) {
                     step = 1267;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1215 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result15 = saved10.hasAny(callback2);
                  if (result15) {
                     step = 1268;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1216 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 151;
                     return;
                  }
               }
               case 1217 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1269;
                     return;
                  }
               }
               case 1218 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1270;
                     return;
                  }
               }
               case 1219 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 966;
                     return;
                  }
               }
               case 1220 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1271;
                     return;
                  }
               }
               case 1221 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1058;
                     return;
                  } else {
                     step = 1059;
                     return;
                  }
               }
               case 1222 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 967;
                     return;
                  }
               }
               case 1223 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1272;
                     return;
                  }
               }
               case 1224 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 971;
                     return;
                  }
               }
               case 1225 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1273;
                     return;
                  }
               }
               case 1226 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1064;
                     return;
                  } else {
                     step = 1065;
                     return;
                  }
               }
               case 1227 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 721;
                     return;
                  }
               }
               case 1228 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 975;
                     return;
                  }
               }
               case 1229 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1274;
                     return;
                  }
               }
               case 1230 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1069;
                     return;
                  } else {
                     step = 1070;
                     return;
                  }
               }
               case 1231 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 967;
                     return;
                  }
               }
               case 1232 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 979;
                     return;
                  }
               }
               case 1233 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1275;
                     return;
                  }
               }
               case 1234 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 1276;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1235 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 1163;
                     return;
                  } else {
                     step = 306;
                     return;
                  }
               }
               case 1236 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result14 = saved10.hasAny(callback2);
                  if (result14) {
                     step = 1277;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1237 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result15 = saved10.hasAny(callback2);
                  if (result15) {
                     step = 1278;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1238 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 170;
                     return;
                  }
               }
               case 1239 -> {
                  var result13 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result13;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 171;
                     return;
                  }
               }
               case 1240 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1279;
                     return;
                  }
               }
               case 1241 -> {
                  if ((saved3 <= ((saved4 * 16) + 15))) {
                     step = 1280;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1242 -> {
                  var length4 = saved6.length;
                  if (((saved9 + 1) < length4)) {
                     int nextValue0 = (saved9 + 1);
                     saved9 = nextValue0;
                     step = 1172;
                     return;
                  } else {
                     step = 312;
                     return;
                  }
               }
               case 1243 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result14 = saved10.hasAny(callback2);
                  if (result14) {
                     step = 1281;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1244 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result15 = saved10.hasAny(callback2);
                  if (result15) {
                     step = 1282;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 1245 -> {
                  var result13 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result13 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result13;
                     saved11 = nextValue0;
                     step = 170;
                     return;
                  }
               }
               case 1246 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1283;
                     return;
                  }
               }
               case 1247 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1284;
                     return;
                  }
               }
               case 1248 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1014;
                     return;
                  }
               }
               case 1249 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1285;
                     return;
                  }
               }
               case 1250 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1105;
                     return;
                  } else {
                     step = 1106;
                     return;
                  }
               }
               case 1251 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 1015;
                     return;
                  }
               }
               case 1252 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1286;
                     return;
                  }
               }
               case 1253 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1019;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance16() {
            switch (step) {
               case 1254 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1287;
                     return;
                  }
               }
               case 1255 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1111;
                     return;
                  } else {
                     step = 1112;
                     return;
                  }
               }
               case 1256 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 777;
                     return;
                  }
               }
               case 1257 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1023;
                     return;
                  }
               }
               case 1258 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1288;
                     return;
                  }
               }
               case 1259 -> {
                  if (((saved14 + 1) != 16)) {
                     int nextValue0 = (saved14 + 1);
                     saved14 = nextValue0;
                     step = 1116;
                     return;
                  } else {
                     step = 1117;
                     return;
                  }
               }
               case 1260 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 1015;
                     return;
                  }
               }
               case 1261 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1028;
                     return;
                  }
               }
               case 1262 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1289;
                     return;
                  }
               }
               case 1263 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1290;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1291;
                     return;
                  }
               }
               case 1264 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1292;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1293;
                     return;
                  }
               }
               case 1265 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1038;
                     return;
                  }
               }
               case 1266 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1294;
                     return;
                  }
               }
               case 1267 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1295;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1296;
                     return;
                  }
               }
               case 1268 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1297;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1298;
                     return;
                  }
               }
               case 1269 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1047;
                     return;
                  }
               }
               case 1270 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1299;
                     return;
                  }
               }
               case 1271 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1300;
                     return;
                  }
               }
               case 1272 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 516;
                     return;
                  }
               }
               case 1273 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1301;
                     return;
                  }
               }
               case 1274 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1302;
                     return;
                  }
               }
               case 1275 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1075;
                     return;
                  }
               }
               case 1276 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1303;
                     return;
                  }
               }
               case 1277 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1304;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1305;
                     return;
                  }
               }
               case 1278 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1306;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1307;
                     return;
                  }
               }
               case 1279 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1085;
                     return;
                  }
               }
               case 1280 -> {
                  var length4 = saved6.length;
                  if ((length4 <= saved9)) {
                     step = 25;
                     return;
                  } else {
                     step = 1308;
                     return;
                  }
               }
               case 1281 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1309;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1310;
                     return;
                  }
               }
               case 1282 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1311;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1312;
                     return;
                  }
               }
               case 1283 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1094;
                     return;
                  }
               }
               case 1284 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1313;
                     return;
                  }
               }
               case 1285 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1314;
                     return;
                  }
               }
               case 1286 -> {
                  var result17 = ((java.lang.Integer) saved15).intValue();
                  if ((result17 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 567;
                     return;
                  }
               }
               case 1287 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1315;
                     return;
                  }
               }
               case 1288 -> {
                  var result19 = saved13.getValue();
                  if ((result19 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result19;
                     saved15 = nextValue0;
                     step = 1316;
                     return;
                  }
               }
               case 1289 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1317;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1290 -> {
                  if ((14 < saved7)) {
                     step = 1318;
                     return;
                  } else {
                     step = 1319;
                     return;
                  }
               }
               case 1291 -> {
                  if ((14 < saved7)) {
                     step = 1320;
                     return;
                  } else {
                     step = 1321;
                     return;
                  }
               }
               case 1292 -> {
                  if ((14 < saved7)) {
                     step = 1322;
                     return;
                  } else {
                     step = 1323;
                     return;
                  }
               }
               case 1293 -> {
                  if ((14 < saved7)) {
                     step = 1324;
                     return;
                  } else {
                     step = 1325;
                     return;
                  }
               }
               case 1294 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1326;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1295 -> {
                  if ((14 < saved7)) {
                     step = 1327;
                     return;
                  } else {
                     step = 1328;
                     return;
                  }
               }
               case 1296 -> {
                  if ((14 < saved7)) {
                     step = 1329;
                     return;
                  } else {
                     step = 1330;
                     return;
                  }
               }
               case 1297 -> {
                  if ((14 < saved7)) {
                     step = 1331;
                     return;
                  } else {
                     step = 1332;
                     return;
                  }
               }
               case 1298 -> {
                  if ((14 < saved7)) {
                     step = 1333;
                     return;
                  } else {
                     step = 1334;
                     return;
                  }
               }
               case 1299 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1140;
                     return;
                  }
               }
               case 1300 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1335;
                     return;
                  }
               }
               case 1301 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1336;
                     return;
                  }
               }
               case 1302 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1337;
                     return;
                  }
               }
               case 1303 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1338;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1304 -> {
                  if ((14 < saved7)) {
                     step = 1339;
                     return;
                  } else {
                     step = 1340;
                     return;
                  }
               }
               case 1305 -> {
                  if ((14 < saved7)) {
                     step = 1341;
                     return;
                  } else {
                     step = 1342;
                     return;
                  }
               }
               case 1306 -> {
                  if ((14 < saved7)) {
                     step = 1343;
                     return;
                  } else {
                     step = 1344;
                     return;
                  }
               }
               case 1307 -> {
                  if ((14 < saved7)) {
                     step = 1345;
                     return;
                  } else {
                     step = 1346;
                     return;
                  }
               }
               case 1308 -> {
                  var element2 = saved6[saved9];
                  if ((element2 != null)) {
                     net.minecraft.world.chunk.ChunkSection nextValue0 = element2;
                     saved10 = nextValue0;
                     step = 1347;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1309 -> {
                  if ((14 < saved7)) {
                     step = 1348;
                     return;
                  } else {
                     step = 1349;
                     return;
                  }
               }
               case 1310 -> {
                  if ((14 < saved7)) {
                     step = 1350;
                     return;
                  } else {
                     step = 1351;
                     return;
                  }
               }
               case 1311 -> {
                  if ((14 < saved7)) {
                     step = 1352;
                     return;
                  } else {
                     step = 1353;
                     return;
                  }
               }
               case 1312 -> {
                  if ((14 < saved7)) {
                     step = 1354;
                     return;
                  } else {
                     step = 1355;
                     return;
                  }
               }
               case 1313 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1182;
                     return;
                  }
               }
               case 1314 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1356;
                     return;
                  }
               }
               case 1315 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1357;
                     return;
                  }
               }
               case 1316 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1358;
                     return;
                  }
               }
               case 1317 -> {
                  var result16 = saved10.isEmpty();
                  if (!(result16)) {
                     step = 1359;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1318 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1360;
                     return;
                  }
               }
               case 1319 -> {
                  if ((0 <= saved7)) {
                     step = 1361;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1320 -> {
                  if ((saved4 <= 15)) {
                     step = 1362;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1321 -> {
                  if ((saved4 <= saved7)) {
                     step = 846;
                     return;
                  } else {
                     step = 818;
                     return;
                  }
               }
               case 1322 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 116;
                     return;
                  }
               }
               case 1323 -> {
                  if ((0 <= saved7)) {
                     step = 1363;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1324 -> {
                  if ((saved4 <= 15)) {
                     step = 1364;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1325 -> {
                  if ((saved4 <= saved7)) {
                     step = 850;
                     return;
                  } else {
                     step = 821;
                     return;
                  }
               }
               case 1326 -> {
                  var result16 = saved10.isEmpty();
                  if (!(result16)) {
                     step = 1365;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1327 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1360;
                     return;
                  }
               }
               case 1328 -> {
                  if ((0 <= saved7)) {
                     step = 1366;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1329 -> {
                  if ((saved4 <= 15)) {
                     step = 854;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1330 -> {
                  if ((saved4 <= saved7)) {
                     step = 862;
                     return;
                  } else {
                     step = 828;
                     return;
                  }
               }
               case 1331 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 116;
                     return;
                  }
               }
               case 1332 -> {
                  if ((0 <= saved7)) {
                     step = 1367;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1333 -> {
                  if ((saved4 <= 15)) {
                     step = 858;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1334 -> {
                  if ((saved4 <= saved7)) {
                     step = 866;
                     return;
                  } else {
                     step = 831;
                     return;
                  }
               }
               case 1335 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1221;
                     return;
                  }
               }
               case 1336 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1226;
                     return;
                  }
               }
               case 1337 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1230;
                     return;
                  }
               }
               case 1338 -> {
                  var result16 = saved10.isEmpty();
                  if (!(result16)) {
                     step = 1368;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1339 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1369;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance17() {
            switch (step) {
               case 1340 -> {
                  if ((0 <= saved7)) {
                     step = 1370;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1341 -> {
                  if ((saved4 <= 15)) {
                     step = 1371;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1342 -> {
                  if ((saved4 <= saved7)) {
                     step = 908;
                     return;
                  } else {
                     step = 880;
                     return;
                  }
               }
               case 1343 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 130;
                     return;
                  }
               }
               case 1344 -> {
                  if ((0 <= saved7)) {
                     step = 1372;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1345 -> {
                  if ((saved4 <= 15)) {
                     step = 1373;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1346 -> {
                  if ((saved4 <= saved7)) {
                     step = 912;
                     return;
                  } else {
                     step = 883;
                     return;
                  }
               }
               case 1347 -> {
                  var result16 = saved10.isEmpty();
                  if (!(result16)) {
                     step = 1374;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1348 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1369;
                     return;
                  }
               }
               case 1349 -> {
                  if ((0 <= saved7)) {
                     step = 1375;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1350 -> {
                  if ((saved4 <= 15)) {
                     step = 916;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1351 -> {
                  if ((saved4 <= saved7)) {
                     step = 924;
                     return;
                  } else {
                     step = 890;
                     return;
                  }
               }
               case 1352 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 130;
                     return;
                  }
               }
               case 1353 -> {
                  if ((0 <= saved7)) {
                     step = 1376;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 1354 -> {
                  if ((saved4 <= 15)) {
                     step = 920;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 1355 -> {
                  if ((saved4 <= saved7)) {
                     step = 928;
                     return;
                  } else {
                     step = 893;
                     return;
                  }
               }
               case 1356 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1250;
                     return;
                  }
               }
               case 1357 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1255;
                     return;
                  }
               }
               case 1358 -> {
                  var result20 = ((java.lang.Integer) saved15).intValue();
                  if ((result20 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 1259;
                     return;
                  }
               }
               case 1359 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result17 = saved10.hasAny(callback2);
                  if (result17) {
                     step = 1377;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1360 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1378;
                     return;
                  } else {
                     step = 401;
                     return;
                  }
               }
               case 1361 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1379;
                     return;
                  }
               }
               case 1362 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 721;
                     return;
                  }
               }
               case 1363 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 151;
                     return;
                  }
               }
               case 1364 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 152;
                     return;
                  }
               }
               case 1365 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result17 = saved10.hasAny(callback2);
                  if (result17) {
                     step = 1380;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1366 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1379;
                     return;
                  }
               }
               case 1367 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 151;
                     return;
                  }
               }
               case 1368 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result17 = saved10.hasAny(callback2);
                  if (result17) {
                     step = 1381;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1369 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1382;
                     return;
                  } else {
                     step = 450;
                     return;
                  }
               }
               case 1370 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1383;
                     return;
                  }
               }
               case 1371 -> {
                  var result15 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result15;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 777;
                     return;
                  }
               }
               case 1372 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 170;
                     return;
                  }
               }
               case 1373 -> {
                  var result16 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result16;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 171;
                     return;
                  }
               }
               case 1374 -> {
                  var array2 = new java.lang.Object[0];
                  var callback2 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$scanChunk$1(((net.minecraft.block.BlockState) parameter0)); } };
                  var result17 = saved10.hasAny(callback2);
                  if (result17) {
                     step = 1384;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1375 -> {
                  var result15 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result15 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result15;
                     saved11 = nextValue0;
                     step = 1383;
                     return;
                  }
               }
               case 1376 -> {
                  var result16 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result16 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result16;
                     saved11 = nextValue0;
                     step = 170;
                     return;
                  }
               }
               case 1377 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1385;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1386;
                     return;
                  }
               }
               case 1378 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1387;
                     return;
                  }
               }
               case 1379 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1388;
                     return;
                  } else {
                     step = 508;
                     return;
                  }
               }
               case 1380 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1389;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1390;
                     return;
                  }
               }
               case 1381 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1391;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1392;
                     return;
                  }
               }
               case 1382 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1393;
                     return;
                  }
               }
               case 1383 -> {
                  var result16 = saved11.getBlock();
                  var result17 = com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result16);
                  if (result17) {
                     step = 1394;
                     return;
                  } else {
                     step = 559;
                     return;
                  }
               }
               case 1384 -> {
                  if (((saved3 + (saved4 * -(16))) < 1)) {
                     step = 1395;
                     return;
                  } else {
                     int nextValue0 = (saved3 + (saved4 * -(16)));
                     saved4 = nextValue0;
                     step = 1396;
                     return;
                  }
               }
               case 1385 -> {
                  if ((14 < saved7)) {
                     step = 1397;
                     return;
                  } else {
                     step = 1398;
                     return;
                  }
               }
               case 1386 -> {
                  if ((14 < saved7)) {
                     step = 1399;
                     return;
                  } else {
                     step = 1400;
                     return;
                  }
               }
               case 1387 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1401;
                     return;
                  }
               }
               case 1388 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1402;
                     return;
                  }
               }
               case 1389 -> {
                  if ((14 < saved7)) {
                     step = 1403;
                     return;
                  } else {
                     step = 1404;
                     return;
                  }
               }
               case 1390 -> {
                  if ((14 < saved7)) {
                     step = 1405;
                     return;
                  } else {
                     step = 1406;
                     return;
                  }
               }
               case 1391 -> {
                  if ((14 < saved7)) {
                     step = 1407;
                     return;
                  } else {
                     step = 1408;
                     return;
                  }
               }
               case 1392 -> {
                  if ((14 < saved7)) {
                     step = 1409;
                     return;
                  } else {
                     step = 1410;
                     return;
                  }
               }
               case 1393 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1411;
                     return;
                  }
               }
               case 1394 -> {
                  var field6 = SusChunkFinderModule.this.amethystThreshold;
                  if ((field6 == null)) {
                     step = 105;
                     return;
                  } else {
                     com.zenya.setting.Setting nextValue0 = field6;
                     saved13 = nextValue0;
                     step = 1412;
                     return;
                  }
               }
               case 1395 -> {
                  if ((14 < saved7)) {
                     step = 1413;
                     return;
                  } else {
                     step = 1414;
                     return;
                  }
               }
               case 1396 -> {
                  if ((14 < saved7)) {
                     step = 1415;
                     return;
                  } else {
                     step = 1416;
                     return;
                  }
               }
               case 1397 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1360;
                     return;
                  }
               }
               case 1398 -> {
                  if ((0 <= saved7)) {
                     step = 1417;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1399 -> {
                  if ((saved4 <= 15)) {
                     step = 1418;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1400 -> {
                  if ((saved4 <= saved7)) {
                     step = 1222;
                     return;
                  } else {
                     step = 1206;
                     return;
                  }
               }
               case 1401 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1419;
                     return;
                  }
               }
               case 1402 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1420;
                     return;
                  }
               }
               case 1403 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1360;
                     return;
                  }
               }
               case 1404 -> {
                  if ((0 <= saved7)) {
                     step = 1421;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1405 -> {
                  if ((saved4 <= 15)) {
                     step = 1227;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1406 -> {
                  if ((saved4 <= saved7)) {
                     step = 1231;
                     return;
                  } else {
                     step = 1213;
                     return;
                  }
               }
               case 1407 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1369;
                     return;
                  }
               }
               case 1408 -> {
                  if ((0 <= saved7)) {
                     step = 1422;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1409 -> {
                  if ((saved4 <= 15)) {
                     step = 1423;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1410 -> {
                  if ((saved4 <= saved7)) {
                     step = 1251;
                     return;
                  } else {
                     step = 1235;
                     return;
                  }
               }
               case 1411 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1424;
                     return;
                  }
               }
               case 1412 -> {
                  var result18 = saved13.getValue();
                  if ((result18 == null)) {
                     step = 134;
                     return;
                  } else {
                     java.lang.Object nextValue0 = result18;
                     saved15 = nextValue0;
                     step = 1425;
                     return;
                  }
               }
               case 1413 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1369;
                     return;
                  }
               }
               case 1414 -> {
                  if ((0 <= saved7)) {
                     step = 1426;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1415 -> {
                  if ((saved4 <= 15)) {
                     step = 1256;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1416 -> {
                  if ((saved4 <= saved7)) {
                     step = 1260;
                     return;
                  } else {
                     step = 1242;
                     return;
                  }
               }
               case 1417 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1379;
                     return;
                  }
               }
               case 1418 -> {
                  var result18 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result18;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 721;
                     return;
                  }
               }
               case 1419 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 401;
                     return;
                  }
               }
               case 1420 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1427;
                     return;
                  }
               }
               case 1421 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1379;
                     return;
                  }
               }
               case 1422 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1383;
                     return;
                  }
               }
               case 1423 -> {
                  var result18 = saved10.getBlockState((int) (0L), saved4, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     int nextValue0 = saved4;
                     net.minecraft.block.BlockState nextValue1 = result18;
                     saved12 = nextValue0;
                     saved11 = nextValue1;
                     step = 777;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void advance18() {
            switch (step) {
               case 1424 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 450;
                     return;
                  }
               }
               case 1425 -> {
                  if (!((saved15 == null || saved15 instanceof java.lang.Integer))) {
                     step = 173;
                     return;
                  } else {
                     step = 1428;
                     return;
                  }
               }
               case 1426 -> {
                  var result18 = saved10.getBlockState((int) (0L), 0, 0);
                  if ((result18 == null)) {
                     step = 70;
                     return;
                  } else {
                     net.minecraft.block.BlockState nextValue0 = result18;
                     saved11 = nextValue0;
                     step = 1383;
                     return;
                  }
               }
               case 1427 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 508;
                     return;
                  }
               }
               case 1428 -> {
                  var result19 = ((java.lang.Integer) saved15).intValue();
                  if ((result19 <= (saved18 + 1))) {
                     step = 224;
                     return;
                  } else {
                     int nextValue0 = (saved18 + 1);
                     saved18 = nextValue0;
                     step = 559;
                     return;
                  }
               }
               default -> throw new IllegalStateException("Invalid control-flow partition");
            }
         }
         void run() {
            while (!finished) {
               if (step <= 85) advance0();
               else if (step <= 165) advance1();
               else if (step <= 244) advance2();
               else if (step <= 321) advance3();
               else if (step <= 399) advance4();
               else if (step <= 475) advance5();
               else if (step <= 552) advance6();
               else if (step <= 628) advance7();
               else if (step <= 703) advance8();
               else if (step <= 780) advance9();
               else if (step <= 858) advance10();
               else if (step <= 936) advance11();
               else if (step <= 1015) advance12();
               else if (step <= 1094) advance13();
               else if (step <= 1174) advance14();
               else if (step <= 1253) advance15();
               else if (step <= 1339) advance16();
               else if (step <= 1423) advance17();
               else if (step <= 1428) advance18();
               else throw new IllegalStateException("Invalid control-flow state");
            }
         }
      }
      new Execution().run();
   }

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      var field1 = com.zenya.module.donut.SusChunkFinderModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.world;
         if ((field2 == null)) {
            return;
         } else {
            var field3 = com.zenya.module.donut.SusChunkFinderModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var field4 = field3.player;
               if ((field4 == null)) {
                  return;
               } else {
                  var field5 = this.hits;
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
                           var field6 = this.overlayColor;
                           if ((field6 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result4 = field6.getValue();
                              if ((result4 != null)) {
                                 if (!((result4 == null || result4 instanceof java.awt.Color))) {
                                    throw new java.lang.ClassCastException("");
                                 } else {
                                    var field7 = this.alpha;
                                    if ((field7 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var result5 = field7.getValue();
                                       if ((result5 != null)) {
                                          if (!((result5 == null || result5 instanceof java.lang.Integer))) {
                                             throw new java.lang.ClassCastException("");
                                          } else {
                                             var result6 = ((java.lang.Integer) result5).intValue();
                                             var selected1 = ((result6 < 255) ? result6 : 255);
                                             var selected2 = ((0 < selected1) ? selected1 : 0);
                                             var result7 = ((java.awt.Color) result4).getRed();
                                             var result8 = ((java.awt.Color) result4).getGreen();
                                             var result9 = ((java.awt.Color) result4).getBlue();
                                             var color1 = new java.awt.Color(result7, result8, result9, selected2);
                                             var hashSet1 = new java.util.HashSet();
                                             var field8 = this.hits;
                                             var array1 = new java.lang.Object[1];
                                             array1[0] = hashSet1;
                                             var captured1 = ((java.util.Set) hashSet1);
                                             var callback1 = new java.util.function.BiConsumer() { public void accept(java.lang.Object parameter0, java.lang.Object parameter1) { com.zenya.module.donut.SusChunkFinderModule.lambda$onRender$2(captured1, ((net.minecraft.util.math.ChunkPos) parameter0), ((com.zenya.module.donut.SusChunkFinderModule.HitType) parameter1)); } };
                                             if ((field8 != null)) {
                                                field8.forEach(callback1);
                                                var result10 = this.buildTriangulatedBaseArea(((java.util.Set) hashSet1));
                                                var result11 = com.zenya.render.WorldRenderer.beginWorldBatch(matrices);
                                                if ((result3 != null)) {
                                                   var field9 = result3.y;
                                                   if ((result10 != null)) {
                                                      var result12 = result10.iterator();
                                                      if ((result12 != null)) {
                                                         Object iteration1_1 = result3;
                                                         repeat1: while (true) {
                                                            var result13 = result12.hasNext();
                                                            if (!(result13)) {
                                                               if ((result11 != null)) {
                                                                  result11.flush();
                                                                  return;
                                                               } else {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               }
                                                            } else {
                                                               var result14 = result12.next();
                                                               if ((result14 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  if (!((result14 == null || result14 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                     throw new java.lang.ClassCastException("");
                                                                  } else {
                                                                     var result15 = ((net.minecraft.util.math.ChunkPos) result14).getStartX();
                                                                     var field10 = ((net.minecraft.util.math.Vec3d) iteration1_1).x;
                                                                     var result16 = ((net.minecraft.util.math.ChunkPos) result14).getStartZ();
                                                                     var field11 = ((net.minecraft.util.math.Vec3d) iteration1_1).z;
                                                                     if ((result11 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        result11.renderFilledBox(((double) (result15) - field10), (63.0 - field9), ((double) (result16) - field11), (((double) (result15) - field10) + 16.0), (0.12 + (63.0 - field9)), (((double) (result16) - field11) + 16.0), color1);
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
                                       } else {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       }
                                    }
                                 }
                              } else {
                                 var field7 = this.alpha;
                                 if ((field7 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    var result5 = field7.getValue();
                                    if ((result5 != null)) {
                                       if (!((result5 == null || result5 instanceof java.lang.Integer))) {
                                          throw new java.lang.ClassCastException("");
                                       } else {
                                          var result6 = ((java.lang.Integer) result5).intValue();
                                          var selected1 = ((result6 < 255) ? result6 : 255);
                                          var selected2 = ((0 < selected1) ? selected1 : 0);
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
         }
      }
   }

   private Set<ChunkPos> buildTriangulatedBaseArea(Set<ChunkPos> geodes) {
      var hashSet1 = new java.util.HashSet(((java.util.Collection) geodes));
      if ((geodes != null)) {
         var result1 = geodes.isEmpty();
         if (result1) {
            return ((java.util.Set) hashSet1);
         } else {
            var result2 = com.zenya.module.donut.SusChunkFinderModule.collapseConnectedGeodeChunks(geodes);
            if ((result2 != null)) {
               var result3 = result2.keySet();
               var arrayList1 = new java.util.ArrayList(((java.util.Collection) result3));
               var field1 = this.simulationDistance;
               if ((field1 != null)) {
                  var result4 = field1.getValue();
                  if ((result4 == null)) {
                     throw new java.lang.NullPointerException("object reference is null");
                  } else {
                     if (!((result4 == null || result4 instanceof java.lang.Integer))) {
                        throw new java.lang.ClassCastException("");
                     } else {
                        var result5 = ((java.lang.Integer) result4).intValue();
                        var field2 = this.overlapSensitivity;
                        if ((field2 != null)) {
                           var result6 = field2.getValue();
                           if ((result6 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              if (!((result6 == null || result6 instanceof java.lang.Integer))) {
                                 throw new java.lang.ClassCastException("");
                              } else {
                                 var result7 = ((java.lang.Integer) result6).intValue();
                                 var result8 = com.zenya.module.donut.SusChunkFinderModule.groupGeodesByProximity(((java.util.List) arrayList1), result5);
                                 if ((result8 != null)) {
                                    var result9 = result8.iterator();
                                    if ((result9 != null)) {
                                       var selected1 = ((2 < result7) ? result7 : 2);
                                       repeat1: while (true) {
                                          var result10 = result9.hasNext();
                                          if (!(result10)) {
                                             return ((java.util.Set) hashSet1);
                                          } else {
                                             var result11 = result9.next();
                                             if ((result11 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                if (!((result11 == null || result11 instanceof java.util.List))) {
                                                   throw new java.lang.ClassCastException("");
                                                } else {
                                                   var result12 = ((java.util.List) result11).size();
                                                   if ((selected1 <= result12)) {
                                                      var hashMap1 = new java.util.HashMap();
                                                      var result13 = ((java.util.List) result11).iterator();
                                                      if ((result13 == null)) {
                                                         throw new java.lang.NullPointerException("object reference is null");
                                                      } else {
                                                         repeat2: while (true) {
                                                            var result14 = result13.hasNext();
                                                            if (!(result14)) {
                                                               var result15 = ((java.util.Map) hashMap1).values();
                                                               if ((result15 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result16 = result15.stream();
                                                                  var array1 = new java.lang.Object[0];
                                                                  var callback1 = new java.util.function.ToIntFunction() { public int applyAsInt(java.lang.Object parameter0) { return (((java.lang.Integer) parameter0)).intValue(); } };
                                                                  if ((result16 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result17 = result16.mapToInt(callback1);
                                                                     if ((result17 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result18 = result17.max();
                                                                        if ((result18 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result19 = result18.orElse(0);
                                                                           if ((1 < result19)) {
                                                                              var selected2 = ((selected1 <= result19) ? selected1 : result19);
                                                                              var hashSet2 = new java.util.HashSet();
                                                                              var array2 = new java.lang.Object[2];
                                                                              array2[0] = java.lang.Integer.valueOf((int) (Integer.toUnsignedLong(selected2)));
                                                                              array2[1] = hashSet2;
                                                                              var captured1 = ((java.lang.Integer) java.lang.Integer.valueOf((int) (Integer.toUnsignedLong(selected2)))).intValue();
                                                                              var captured2 = ((java.util.Set) hashSet2);
                                                                              var callback2 = new java.util.function.BiConsumer() { public void accept(java.lang.Object parameter0, java.lang.Object parameter1) { com.zenya.module.donut.SusChunkFinderModule.lambda$buildTriangulatedBaseArea$3(captured1, captured2, ((net.minecraft.util.math.ChunkPos) parameter0), ((java.lang.Integer) parameter1)); } };
                                                                              ((java.util.Map) hashMap1).forEach(callback2);
                                                                              var result20 = ((java.util.List) result11).iterator();
                                                                              if ((result20 == null)) {
                                                                                 throw new java.lang.NullPointerException("object reference is null");
                                                                              } else {
                                                                                 repeat3: while (true) {
                                                                                    var result21 = result20.hasNext();
                                                                                    if (!(result21)) {
                                                                                       var result22 = ((java.util.Set) hashSet1).addAll(((java.util.Collection) hashSet2));
                                                                                       continue repeat1;
                                                                                    } else {
                                                                                       var result22 = result20.next();
                                                                                       if ((result22 != null)) {
                                                                                          if (!((result22 == null || result22 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                                             throw new java.lang.ClassCastException("");
                                                                                          } else {
                                                                                             var result23 = ((java.util.Set) hashSet2).stream();
                                                                                             var array3 = new java.lang.Object[2];
                                                                                             array3[0] = result22;
                                                                                             array3[1] = java.lang.Integer.valueOf(result5);
                                                                                             var captured3 = ((net.minecraft.util.math.ChunkPos) result22);
                                                                                             var captured4 = ((java.lang.Integer) java.lang.Integer.valueOf(result5)).intValue();
                                                                                             var callback3 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$buildTriangulatedBaseArea$4(captured3, captured4, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                                                                             if ((result23 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                var result24 = result23.anyMatch(callback3);
                                                                                                if (result24) {
                                                                                                   var result25 = java.util.Set.of();
                                                                                                   var result26 = ((java.util.Map) result2).getOrDefault(result22, result25);
                                                                                                   if ((result26 != null)) {
                                                                                                      if (!((result26 == null || result26 instanceof java.util.Collection))) {
                                                                                                         throw new java.lang.ClassCastException("");
                                                                                                      } else {
                                                                                                         var result27 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) result26));
                                                                                                         continue repeat3;
                                                                                                      }
                                                                                                   } else {
                                                                                                      var result27 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) result26));
                                                                                                      continue repeat3;
                                                                                                   }
                                                                                                } else {
                                                                                                   continue repeat3;
                                                                                                }
                                                                                             }
                                                                                          }
                                                                                       } else {
                                                                                          var result23 = ((java.util.Set) hashSet2).stream();
                                                                                          var array3 = new java.lang.Object[2];
                                                                                          array3[0] = result22;
                                                                                          array3[1] = java.lang.Integer.valueOf(result5);
                                                                                          var captured3 = ((net.minecraft.util.math.ChunkPos) result22);
                                                                                          var captured4 = ((java.lang.Integer) java.lang.Integer.valueOf(result5)).intValue();
                                                                                          var callback3 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$buildTriangulatedBaseArea$4(captured3, captured4, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                                                                          if ((result23 == null)) {
                                                                                             throw new java.lang.NullPointerException("object reference is null");
                                                                                          } else {
                                                                                             var result24 = result23.anyMatch(callback3);
                                                                                             if (result24) {
                                                                                                var result25 = java.util.Set.of();
                                                                                                var result26 = ((java.util.Map) result2).getOrDefault(result22, result25);
                                                                                                if ((result26 != null)) {
                                                                                                   if (!((result26 == null || result26 instanceof java.util.Collection))) {
                                                                                                      throw new java.lang.ClassCastException("");
                                                                                                   } else {
                                                                                                      var result27 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) result26));
                                                                                                      continue repeat3;
                                                                                                   }
                                                                                                } else {
                                                                                                   var result27 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) result26));
                                                                                                   continue repeat3;
                                                                                                }
                                                                                             } else {
                                                                                                continue repeat3;
                                                                                             }
                                                                                          }
                                                                                       }
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
                                                            } else {
                                                               var result15 = result13.next();
                                                               if ((result15 != null)) {
                                                                  if (!((result15 == null || result15 instanceof net.minecraft.util.math.ChunkPos))) {
                                                                     throw new java.lang.ClassCastException("");
                                                                  } else {
                                                                     if ((-(1) < result5)) {
                                                                        int iteration3_1 = ((result5 * 2) + 1);
                                                                        long iteration3_2 = Integer.toUnsignedLong(-(result5));
                                                                        int iteration3_3 = -(result5);
                                                                        repeat3: while (true) {
                                                                           if ((result15 == null)) {
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           } else {
                                                                              var field3 = ((net.minecraft.util.math.ChunkPos) result15).x;
                                                                              var field4 = ((net.minecraft.util.math.ChunkPos) result15).z;
                                                                              var chunkPos1 = new net.minecraft.util.math.ChunkPos((field3 + (int) (iteration3_2)), (field4 + iteration3_3));
                                                                              var array1 = new java.lang.Object[0];
                                                                              var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                              var result16 = ((java.util.Map) hashMap1).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                              iteration3_1 = (iteration3_1 + -(1));
                                                                              iteration3_3 = (iteration3_3 + 1);
                                                                              if ((iteration3_1 != 0)) {
                                                                                 continue repeat3;
                                                                              } else {
                                                                                 var saved1 = (int) (iteration3_2);
                                                                                 iteration3_2 = (long) ((saved1 + 1));
                                                                                 iteration3_1 = ((result5 * 2) + 1);
                                                                                 iteration3_3 = -(result5);
                                                                                 if ((saved1 != result5)) {
                                                                                    continue repeat3;
                                                                                 } else {
                                                                                    continue repeat2;
                                                                                 }
                                                                              }
                                                                           }
                                                                        }
                                                                     } else {
                                                                        continue repeat2;
                                                                     }
                                                                  }
                                                               } else {
                                                                  if ((-(1) < result5)) {
                                                                     int iteration3_1 = ((result5 * 2) + 1);
                                                                     long iteration3_2 = Integer.toUnsignedLong(-(result5));
                                                                     int iteration3_3 = -(result5);
                                                                     repeat3: while (true) {
                                                                        if ((result15 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var field3 = ((net.minecraft.util.math.ChunkPos) result15).x;
                                                                           var field4 = ((net.minecraft.util.math.ChunkPos) result15).z;
                                                                           var chunkPos1 = new net.minecraft.util.math.ChunkPos((field3 + (int) (iteration3_2)), (field4 + iteration3_3));
                                                                           var array1 = new java.lang.Object[0];
                                                                           var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                           var result16 = ((java.util.Map) hashMap1).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                           iteration3_1 = (iteration3_1 + -(1));
                                                                           iteration3_3 = (iteration3_3 + 1);
                                                                           if ((iteration3_1 != 0)) {
                                                                              continue repeat3;
                                                                           } else {
                                                                              var saved1 = (int) (iteration3_2);
                                                                              iteration3_2 = (long) ((saved1 + 1));
                                                                              iteration3_1 = ((result5 * 2) + 1);
                                                                              iteration3_3 = -(result5);
                                                                              if ((saved1 != result5)) {
                                                                                 continue repeat3;
                                                                              } else {
                                                                                 continue repeat2;
                                                                              }
                                                                           }
                                                                        }
                                                                     }
                                                                  } else {
                                                                     continue repeat2;
                                                                  }
                                                               }
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

   private static List<List<ChunkPos>> groupGeodesByProximity(List<ChunkPos> geodes, int simDistance) {
      var arrayList1 = new java.util.ArrayList();
      var hashSet1 = new java.util.HashSet(((java.util.Collection) geodes));
      repeat1: while (true) {
         var result1 = ((java.util.Set) hashSet1).isEmpty();
         if (result1) {
            return ((java.util.List) arrayList1);
         } else {
            var result2 = ((java.util.Set) hashSet1).iterator();
            if ((result2 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var result3 = result2.next();
               if ((result3 != null)) {
                  if (!((result3 == null || result3 instanceof net.minecraft.util.math.ChunkPos))) {
                     throw new java.lang.ClassCastException("");
                  } else {
                     var result4 = ((java.util.Set) hashSet1).remove(result3);
                     var arrayList2 = new java.util.ArrayList();
                     var arrayDeque1 = new java.util.ArrayDeque();
                     var result5 = ((java.util.ArrayDeque) arrayDeque1).add(result3);
                     repeat2: while (true) {
                        var result6 = arrayDeque1.isEmpty();
                        if (result6) {
                           var result7 = ((java.util.List) arrayList1).add(arrayList2);
                           continue repeat1;
                        } else {
                           var result7 = arrayDeque1.removeFirst();
                           if ((result7 != null)) {
                              if (!((result7 == null || result7 instanceof net.minecraft.util.math.ChunkPos))) {
                                 throw new java.lang.ClassCastException("");
                              } else {
                                 var result8 = ((java.util.List) arrayList2).add(result7);
                                 var arrayList3 = new java.util.ArrayList();
                                 var result9 = ((java.util.Set) hashSet1).iterator();
                                 if ((result9 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    repeat3: while (true) {
                                       var result10 = result9.hasNext();
                                       if (!(result10)) {
                                          var result11 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) arrayList3));
                                          var result12 = arrayDeque1.addAll(((java.util.Collection) arrayList3));
                                          continue repeat2;
                                       } else {
                                          var result11 = result9.next();
                                          if ((result11 != null)) {
                                             if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                                throw new java.lang.ClassCastException("");
                                             } else {
                                                if ((result7 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                                   if ((result11 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                      var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                      var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                      if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                         var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                         if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                            var result12 = ((java.util.List) arrayList3).add(result11);
                                                            continue repeat3;
                                                         } else {
                                                            continue repeat3;
                                                         }
                                                      } else {
                                                         continue repeat3;
                                                      }
                                                   }
                                                }
                                             }
                                          } else {
                                             if ((result7 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                                if ((result11 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                   var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                   var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                   if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                      var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                      if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                         var result12 = ((java.util.List) arrayList3).add(result11);
                                                         continue repeat3;
                                                      } else {
                                                         continue repeat3;
                                                      }
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           } else {
                              var result8 = ((java.util.List) arrayList2).add(result7);
                              var arrayList3 = new java.util.ArrayList();
                              var result9 = ((java.util.Set) hashSet1).iterator();
                              if ((result9 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 repeat3: while (true) {
                                    var result10 = result9.hasNext();
                                    if (!(result10)) {
                                       var result11 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) arrayList3));
                                       var result12 = arrayDeque1.addAll(((java.util.Collection) arrayList3));
                                       continue repeat2;
                                    } else {
                                       var result11 = result9.next();
                                       if ((result11 != null)) {
                                          if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                             throw new java.lang.ClassCastException("");
                                          } else {
                                             if ((result7 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                                if ((result11 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                   var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                   var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                   if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                      var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                      if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                         var result12 = ((java.util.List) arrayList3).add(result11);
                                                         continue repeat3;
                                                      } else {
                                                         continue repeat3;
                                                      }
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                }
                                             }
                                          }
                                       } else {
                                          if ((result7 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                             if ((result11 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                   var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                   if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                      var result12 = ((java.util.List) arrayList3).add(result11);
                                                      continue repeat3;
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                } else {
                                                   continue repeat3;
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
                  }
               } else {
                  var result4 = ((java.util.Set) hashSet1).remove(result3);
                  var arrayList2 = new java.util.ArrayList();
                  var arrayDeque1 = new java.util.ArrayDeque();
                  var result5 = ((java.util.ArrayDeque) arrayDeque1).add(result3);
                  repeat2: while (true) {
                     var result6 = arrayDeque1.isEmpty();
                     if (result6) {
                        var result7 = ((java.util.List) arrayList1).add(arrayList2);
                        continue repeat1;
                     } else {
                        var result7 = arrayDeque1.removeFirst();
                        if ((result7 != null)) {
                           if (!((result7 == null || result7 instanceof net.minecraft.util.math.ChunkPos))) {
                              throw new java.lang.ClassCastException("");
                           } else {
                              var result8 = ((java.util.List) arrayList2).add(result7);
                              var arrayList3 = new java.util.ArrayList();
                              var result9 = ((java.util.Set) hashSet1).iterator();
                              if ((result9 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 repeat3: while (true) {
                                    var result10 = result9.hasNext();
                                    if (!(result10)) {
                                       var result11 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) arrayList3));
                                       var result12 = arrayDeque1.addAll(((java.util.Collection) arrayList3));
                                       continue repeat2;
                                    } else {
                                       var result11 = result9.next();
                                       if ((result11 != null)) {
                                          if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                             throw new java.lang.ClassCastException("");
                                          } else {
                                             if ((result7 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                                if ((result11 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                   var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                   var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                   if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                      var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                      if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                         var result12 = ((java.util.List) arrayList3).add(result11);
                                                         continue repeat3;
                                                      } else {
                                                         continue repeat3;
                                                      }
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                }
                                             }
                                          }
                                       } else {
                                          if ((result7 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                             if ((result11 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                   var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                   if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                      var result12 = ((java.util.List) arrayList3).add(result11);
                                                      continue repeat3;
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                } else {
                                                   continue repeat3;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        } else {
                           var result8 = ((java.util.List) arrayList2).add(result7);
                           var arrayList3 = new java.util.ArrayList();
                           var result9 = ((java.util.Set) hashSet1).iterator();
                           if ((result9 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              repeat3: while (true) {
                                 var result10 = result9.hasNext();
                                 if (!(result10)) {
                                    var result11 = ((java.util.Set) hashSet1).removeAll(((java.util.Collection) arrayList3));
                                    var result12 = arrayDeque1.addAll(((java.util.Collection) arrayList3));
                                    continue repeat2;
                                 } else {
                                    var result11 = result9.next();
                                    if ((result11 != null)) {
                                       if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                          throw new java.lang.ClassCastException("");
                                       } else {
                                          if ((result7 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                             if ((result11 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                                var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                                var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                                if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                   var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                   if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                      var result12 = ((java.util.List) arrayList3).add(result11);
                                                      continue repeat3;
                                                   } else {
                                                      continue repeat3;
                                                   }
                                                } else {
                                                   continue repeat3;
                                                }
                                             }
                                          }
                                       }
                                    } else {
                                       if ((result7 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var field1 = ((net.minecraft.util.math.ChunkPos) result7).x;
                                          if ((result11 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var field2 = ((net.minecraft.util.math.ChunkPos) result11).x;
                                             var field3 = ((net.minecraft.util.math.ChunkPos) result7).z;
                                             var field4 = ((net.minecraft.util.math.ChunkPos) result11).z;
                                             if ((0.0 <= (((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))))) {
                                                var root1 = Math.sqrt((((double) ((field3 - field4)) * (double) ((field3 - field4))) + ((double) ((field1 - field2)) * (double) ((field1 - field2)))));
                                                if ((root1 <= ((double) (simDistance) * 2.5))) {
                                                   var result12 = ((java.util.List) arrayList3).add(result11);
                                                   continue repeat3;
                                                } else {
                                                   continue repeat3;
                                                }
                                             } else {
                                                continue repeat3;
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
               }
            }
         }
      }
   }

   private static Map<ChunkPos, Set<ChunkPos>> collapseConnectedGeodeChunks(Set<ChunkPos> chunks) {
      java.util.HashMap saved1 = null;
      java.util.ArrayDeque saved2 = null;
      java.util.HashSet saved3 = null;
      java.util.Iterator saved4 = null;
      java.lang.Object saved5 = null;
      long saved6 = 0L;
      long saved7 = 0L;
      int saved8 = 0;
      java.util.HashSet saved9 = null;
      java.util.function.Function saved10 = null;
      int saved11 = 0;
      int saved12 = 0;
      int saved13 = 0;
      int saved14 = 0;
      net.minecraft.util.math.ChunkPos saved15 = null;
      long saved16 = 0L;
      int step = 0;
      dispatch: while (true) {
         switch (step) {
            case 0 -> {
               var hashSet1 = new java.util.HashSet(((java.util.Collection) chunks));
               var hashMap1 = new java.util.HashMap();
               var arrayDeque1 = new java.util.ArrayDeque();
               var result1 = ((java.util.Set) hashSet1).isEmpty();
               if (result1) {
                  java.util.HashMap nextValue0 = hashMap1;
                  saved1 = nextValue0;
                  step = 1;
                  continue dispatch;
               } else {
                  java.util.ArrayDeque nextValue0 = arrayDeque1;
                  java.util.HashMap nextValue1 = hashMap1;
                  java.util.HashSet nextValue2 = hashSet1;
                  saved2 = nextValue0;
                  saved1 = nextValue1;
                  saved3 = nextValue2;
                  step = 2;
                  continue dispatch;
               }
            }
            case 1 -> {
               return ((java.util.Map) saved1);
            }
            case 2 -> {
               var result2 = ((java.util.Set) saved3).iterator();
               if ((result2 == null)) {
                  step = 3;
                  continue dispatch;
               } else {
                  java.util.Iterator nextValue0 = result2;
                  saved4 = nextValue0;
                  step = 4;
                  continue dispatch;
               }
            }
            case 3 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 4 -> {
               var result3 = saved4.next();
               if ((result3 != null)) {
                  java.lang.Object nextValue0 = result3;
                  saved5 = nextValue0;
                  step = 5;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result3;
                  saved5 = nextValue0;
                  step = 6;
                  continue dispatch;
               }
            }
            case 5 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 6;
                  continue dispatch;
               }
            }
            case 6 -> {
               var result4 = ((java.util.Set) saved3).remove(saved5);
               var result5 = ((java.util.ArrayDeque) saved2).add(saved5);
               var hashSet2 = new java.util.HashSet();
               var result6 = saved2.isEmpty();
               if (result6) {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet2;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 8;
                  continue dispatch;
               } else {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet2;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 9;
                  continue dispatch;
               }
            }
            case 7 -> {
               throw new java.lang.ClassCastException("");
            }
            case 8 -> {
               var rounded1 = com.zenya.util.ClientMath.roundToLong(((double) (saved7) / (double) (saved8)));
               var rounded2 = com.zenya.util.ClientMath.roundToLong(((double) (saved6) / (double) (saved8)));
               var chunkPos1 = new net.minecraft.util.math.ChunkPos((int) (rounded1), (int) (rounded2));
               var array1 = new java.lang.Object[0];
               var callback1 = new java.util.function.Function() { public java.lang.Object apply(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$collapseConnectedGeodeChunks$5(((net.minecraft.util.math.ChunkPos) parameter0)); } };
               var result7 = ((java.util.Map) saved1).computeIfAbsent(chunkPos1, callback1);
               if ((result7 == null)) {
                  step = 10;
                  continue dispatch;
               } else {
                  java.util.function.Function nextValue0 = callback1;
                  java.lang.Object nextValue1 = result7;
                  saved10 = nextValue0;
                  saved5 = nextValue1;
                  step = 11;
                  continue dispatch;
               }
            }
            case 9 -> {
               var result7 = saved2.removeFirst();
               if ((result7 != null)) {
                  java.lang.Object nextValue0 = result7;
                  saved5 = nextValue0;
                  step = 12;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result7;
                  saved5 = nextValue0;
                  step = 13;
                  continue dispatch;
               }
            }
            case 10 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 11 -> {
               if (!((saved5 == null || saved5 instanceof java.util.Set))) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 14;
                  continue dispatch;
               }
            }
            case 12 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 13;
                  continue dispatch;
               }
            }
            case 13 -> {
               var result8 = ((java.util.Set) saved9).add(saved5);
               if ((saved5 == null)) {
                  step = 16;
                  continue dispatch;
               } else {
                  step = 17;
                  continue dispatch;
               }
            }
            case 14 -> {
               var result8 = ((java.util.Set) saved5).addAll(((java.util.Collection) saved9));
               var result9 = ((java.util.Set) saved3).isEmpty();
               if (result9) {
                  step = 1;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 15 -> {
               throw new java.lang.ClassCastException("");
            }
            case 16 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 17 -> {
               var field1 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field2 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 19;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 20;
                  continue dispatch;
               }
            }
            case 18 -> {
               var result10 = ((java.util.Set) saved3).iterator();
               if ((result10 == null)) {
                  step = 3;
                  continue dispatch;
               } else {
                  java.util.Iterator nextValue0 = result10;
                  saved4 = nextValue0;
                  step = 21;
                  continue dispatch;
               }
            }
            case 19 -> {
               var field3 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field4 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               var chunkPos1 = new net.minecraft.util.math.ChunkPos((field3 + saved11), (field4 + saved12));
               var result9 = ((java.util.Set) saved3).remove(chunkPos1);
               if (result9) {
                  int nextValue0 = (field3 + saved11);
                  int nextValue1 = (field4 + saved12);
                  net.minecraft.util.math.ChunkPos nextValue2 = chunkPos1;
                  saved13 = nextValue0;
                  saved14 = nextValue1;
                  saved15 = nextValue2;
                  step = 22;
                  continue dispatch;
               } else {
                  int nextValue0 = (field3 + saved11);
                  int nextValue1 = (field4 + saved12);
                  saved13 = nextValue0;
                  saved14 = nextValue1;
                  step = 23;
                  continue dispatch;
               }
            }
            case 20 -> {
               if ((saved11 != 0)) {
                  step = 19;
                  continue dispatch;
               } else {
                  step = 24;
                  continue dispatch;
               }
            }
            case 21 -> {
               var result11 = saved4.next();
               if ((result11 != null)) {
                  java.lang.Object nextValue0 = result11;
                  saved5 = nextValue0;
                  step = 25;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result11;
                  saved5 = nextValue0;
                  step = 26;
                  continue dispatch;
               }
            }
            case 22 -> {
               ((java.util.ArrayDeque) saved2).addLast(saved15);
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 27;
                  continue dispatch;
               } else {
                  step = 28;
                  continue dispatch;
               }
            }
            case 23 -> {
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 27;
                  continue dispatch;
               } else {
                  step = 28;
                  continue dispatch;
               }
            }
            case 24 -> {
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 29;
                  continue dispatch;
               } else {
                  step = 30;
                  continue dispatch;
               }
            }
            case 25 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 26;
                  continue dispatch;
               }
            }
            case 26 -> {
               var result12 = ((java.util.Set) saved3).remove(saved5);
               var result13 = ((java.util.ArrayDeque) saved2).add(saved5);
               var hashSet3 = new java.util.HashSet();
               var result14 = saved2.isEmpty();
               if (result14) {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet3;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 31;
                  continue dispatch;
               } else {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet3;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 32;
                  continue dispatch;
               }
            }
            case 27 -> {
               if ((saved12 != 0)) {
                  step = 19;
                  continue dispatch;
               } else {
                  step = 33;
                  continue dispatch;
               }
            }
            case 28 -> {
               if (((saved11 + 1) != 2)) {
                  int nextValue0 = (saved11 + 1);
                  saved11 = nextValue0;
                  step = 34;
                  continue dispatch;
               } else {
                  step = 35;
                  continue dispatch;
               }
            }
            case 29 -> {
               if ((saved12 != 0)) {
                  step = 19;
                  continue dispatch;
               } else {
                  step = 20;
                  continue dispatch;
               }
            }
            case 30 -> {
               if (((saved11 + 1) != 2)) {
                  int nextValue0 = (saved11 + 1);
                  saved11 = nextValue0;
                  step = 36;
                  continue dispatch;
               } else {
                  step = 37;
                  continue dispatch;
               }
            }
            case 31 -> {
               var rounded3 = com.zenya.util.ClientMath.roundToLong(((double) (saved7) / (double) (saved8)));
               var rounded4 = com.zenya.util.ClientMath.roundToLong(((double) (saved6) / (double) (saved8)));
               var chunkPos2 = new net.minecraft.util.math.ChunkPos((int) (rounded3), (int) (rounded4));
               var array2 = new java.lang.Object[0];
               var callback2 = new java.util.function.Function() { public java.lang.Object apply(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$collapseConnectedGeodeChunks$5(((net.minecraft.util.math.ChunkPos) parameter0)); } };
               var result15 = ((java.util.Map) saved1).computeIfAbsent(chunkPos2, callback2);
               if ((result15 == null)) {
                  step = 10;
                  continue dispatch;
               } else {
                  java.util.function.Function nextValue0 = callback2;
                  long nextValue1 = rounded4;
                  java.lang.Object nextValue2 = result15;
                  saved10 = nextValue0;
                  saved16 = nextValue1;
                  saved5 = nextValue2;
                  step = 38;
                  continue dispatch;
               }
            }
            case 32 -> {
               var result15 = saved2.removeFirst();
               if ((result15 != null)) {
                  java.lang.Object nextValue0 = result15;
                  saved5 = nextValue0;
                  step = 39;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result15;
                  saved5 = nextValue0;
                  step = 40;
                  continue dispatch;
               }
            }
            case 33 -> {
               if ((saved11 != 0)) {
                  step = 19;
                  continue dispatch;
               } else {
                  step = 23;
                  continue dispatch;
               }
            }
            case 34 -> {
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 19;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 33;
                  continue dispatch;
               }
            }
            case 35 -> {
               var result10 = saved2.isEmpty();
               if (result10) {
                  step = 8;
                  continue dispatch;
               } else {
                  step = 41;
                  continue dispatch;
               }
            }
            case 36 -> {
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 19;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 20;
                  continue dispatch;
               }
            }
            case 37 -> {
               var result9 = saved2.isEmpty();
               if (result9) {
                  step = 8;
                  continue dispatch;
               } else {
                  step = 9;
                  continue dispatch;
               }
            }
            case 38 -> {
               if (!((saved5 == null || saved5 instanceof java.util.Set))) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 42;
                  continue dispatch;
               }
            }
            case 39 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 40;
                  continue dispatch;
               }
            }
            case 40 -> {
               var result16 = ((java.util.Set) saved9).add(saved5);
               if ((saved5 == null)) {
                  step = 16;
                  continue dispatch;
               } else {
                  step = 43;
                  continue dispatch;
               }
            }
            case 41 -> {
               var result11 = saved2.removeFirst();
               if ((result11 != null)) {
                  java.lang.Object nextValue0 = result11;
                  saved5 = nextValue0;
                  step = 44;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result11;
                  saved5 = nextValue0;
                  step = 45;
                  continue dispatch;
               }
            }
            case 42 -> {
               var result16 = ((java.util.Set) saved5).addAll(((java.util.Collection) saved9));
               var result17 = ((java.util.Set) saved3).isEmpty();
               if (result17) {
                  step = 1;
                  continue dispatch;
               } else {
                  step = 46;
                  continue dispatch;
               }
            }
            case 43 -> {
               var field1 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field2 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 48;
                  continue dispatch;
               }
            }
            case 44 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 45;
                  continue dispatch;
               }
            }
            case 45 -> {
               var result12 = ((java.util.Set) saved9).add(saved5);
               if ((saved5 == null)) {
                  step = 16;
                  continue dispatch;
               } else {
                  step = 49;
                  continue dispatch;
               }
            }
            case 46 -> {
               var result18 = ((java.util.Set) saved3).iterator();
               if ((result18 == null)) {
                  step = 3;
                  continue dispatch;
               } else {
                  java.util.Iterator nextValue0 = result18;
                  saved4 = nextValue0;
                  step = 50;
                  continue dispatch;
               }
            }
            case 47 -> {
               var field3 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field4 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               var chunkPos2 = new net.minecraft.util.math.ChunkPos((field3 + saved11), (field4 + saved12));
               var result17 = ((java.util.Set) saved3).remove(chunkPos2);
               if (result17) {
                  int nextValue0 = (field3 + saved11);
                  int nextValue1 = (field4 + saved12);
                  net.minecraft.util.math.ChunkPos nextValue2 = chunkPos2;
                  saved13 = nextValue0;
                  saved14 = nextValue1;
                  saved15 = nextValue2;
                  step = 51;
                  continue dispatch;
               } else {
                  int nextValue0 = (field3 + saved11);
                  int nextValue1 = (field4 + saved12);
                  saved13 = nextValue0;
                  saved14 = nextValue1;
                  step = 52;
                  continue dispatch;
               }
            }
            case 48 -> {
               if ((saved11 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 53;
                  continue dispatch;
               }
            }
            case 49 -> {
               var field5 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field6 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field6);
                  long nextValue3 = (saved7 + field5);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 19;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field6);
                  long nextValue3 = (saved7 + field5);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 33;
                  continue dispatch;
               }
            }
            case 50 -> {
               var result19 = saved4.next();
               if ((result19 != null)) {
                  java.lang.Object nextValue0 = result19;
                  saved5 = nextValue0;
                  step = 54;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result19;
                  saved5 = nextValue0;
                  step = 55;
                  continue dispatch;
               }
            }
            case 51 -> {
               ((java.util.ArrayDeque) saved2).addLast(saved15);
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 56;
                  continue dispatch;
               } else {
                  step = 57;
                  continue dispatch;
               }
            }
            case 52 -> {
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 56;
                  continue dispatch;
               } else {
                  step = 57;
                  continue dispatch;
               }
            }
            case 53 -> {
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 58;
                  continue dispatch;
               } else {
                  step = 59;
                  continue dispatch;
               }
            }
            case 54 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 7;
                  continue dispatch;
               } else {
                  step = 55;
                  continue dispatch;
               }
            }
            case 55 -> {
               var result20 = ((java.util.Set) saved3).remove(saved5);
               var result21 = ((java.util.ArrayDeque) saved2).add(saved5);
               var hashSet4 = new java.util.HashSet();
               var result22 = saved2.isEmpty();
               if (result22) {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet4;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 31;
                  continue dispatch;
               } else {
                  long nextValue0 = 0L;
                  long nextValue1 = 0L;
                  int nextValue2 = 0;
                  java.util.HashSet nextValue3 = hashSet4;
                  saved6 = nextValue0;
                  saved7 = nextValue1;
                  saved8 = nextValue2;
                  saved9 = nextValue3;
                  step = 60;
                  continue dispatch;
               }
            }
            case 56 -> {
               if ((saved12 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 61;
                  continue dispatch;
               }
            }
            case 57 -> {
               if (((saved11 + 1) != 2)) {
                  int nextValue0 = (saved11 + 1);
                  saved11 = nextValue0;
                  step = 62;
                  continue dispatch;
               } else {
                  step = 63;
                  continue dispatch;
               }
            }
            case 58 -> {
               if ((saved12 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 48;
                  continue dispatch;
               }
            }
            case 59 -> {
               if (((saved11 + 1) != 2)) {
                  int nextValue0 = (saved11 + 1);
                  saved11 = nextValue0;
                  step = 64;
                  continue dispatch;
               } else {
                  step = 65;
                  continue dispatch;
               }
            }
            case 60 -> {
               var result23 = saved2.removeFirst();
               if ((result23 != null)) {
                  java.lang.Object nextValue0 = result23;
                  saved5 = nextValue0;
                  step = 66;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result23;
                  saved5 = nextValue0;
                  step = 67;
                  continue dispatch;
               }
            }
            case 61 -> {
               if ((saved11 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 52;
                  continue dispatch;
               }
            }
            case 62 -> {
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 61;
                  continue dispatch;
               }
            }
            case 63 -> {
               var result18 = saved2.isEmpty();
               if (result18) {
                  step = 31;
                  continue dispatch;
               } else {
                  step = 68;
                  continue dispatch;
               }
            }
            case 64 -> {
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 48;
                  continue dispatch;
               }
            }
            case 65 -> {
               var result17 = saved2.isEmpty();
               if (result17) {
                  step = 31;
                  continue dispatch;
               } else {
                  step = 32;
                  continue dispatch;
               }
            }
            case 66 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 67;
                  continue dispatch;
               }
            }
            case 67 -> {
               var result24 = ((java.util.Set) saved9).add(saved5);
               if ((saved5 == null)) {
                  step = 16;
                  continue dispatch;
               } else {
                  step = 69;
                  continue dispatch;
               }
            }
            case 68 -> {
               var result19 = saved2.removeFirst();
               if ((result19 != null)) {
                  java.lang.Object nextValue0 = result19;
                  saved5 = nextValue0;
                  step = 70;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result19;
                  saved5 = nextValue0;
                  step = 71;
                  continue dispatch;
               }
            }
            case 69 -> {
               var field1 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field2 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field2);
                  long nextValue3 = (saved7 + field1);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 72;
                  continue dispatch;
               }
            }
            case 70 -> {
               if (!((saved5 == null || saved5 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 71;
                  continue dispatch;
               }
            }
            case 71 -> {
               var result20 = ((java.util.Set) saved9).add(saved5);
               if ((saved5 == null)) {
                  step = 16;
                  continue dispatch;
               } else {
                  step = 73;
                  continue dispatch;
               }
            }
            case 72 -> {
               if ((saved11 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 74;
                  continue dispatch;
               }
            }
            case 73 -> {
               var field5 = ((net.minecraft.util.math.ChunkPos) saved5).x;
               var field6 = ((net.minecraft.util.math.ChunkPos) saved5).z;
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field6);
                  long nextValue3 = (saved7 + field5);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  int nextValue1 = -(1);
                  long nextValue2 = (saved6 + field6);
                  long nextValue3 = (saved7 + field5);
                  int nextValue4 = (saved8 + 1);
                  saved11 = nextValue0;
                  saved12 = nextValue1;
                  saved6 = nextValue2;
                  saved7 = nextValue3;
                  saved8 = nextValue4;
                  step = 61;
                  continue dispatch;
               }
            }
            case 74 -> {
               if (((saved12 + 1) != 2)) {
                  int nextValue0 = (saved12 + 1);
                  saved12 = nextValue0;
                  step = 75;
                  continue dispatch;
               } else {
                  step = 76;
                  continue dispatch;
               }
            }
            case 75 -> {
               if ((saved12 != 0)) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 72;
                  continue dispatch;
               }
            }
            case 76 -> {
               if (((saved11 + 1) != 2)) {
                  int nextValue0 = (saved11 + 1);
                  saved11 = nextValue0;
                  step = 77;
                  continue dispatch;
               } else {
                  step = 78;
                  continue dispatch;
               }
            }
            case 77 -> {
               if ((-(1) != 0)) {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 47;
                  continue dispatch;
               } else {
                  int nextValue0 = -(1);
                  saved12 = nextValue0;
                  step = 72;
                  continue dispatch;
               }
            }
            case 78 -> {
               var result25 = saved2.isEmpty();
               if (result25) {
                  step = 31;
                  continue dispatch;
               } else {
                  step = 60;
                  continue dispatch;
               }
            }
            default -> throw new IllegalStateException("Invalid control-flow state");
         }
      }
   }

   private void cleanUpFarHits() {
      var field1 = com.zenya.module.donut.SusChunkFinderModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var field2 = field1.player;
      if ((field2 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = field2.getChunkPos();
      var field3 = this.scanRadius;
      if ((field3 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result2 = field3.getValue();
      if ((result2 != null)) {
         if (!((result2 == null || result2 instanceof java.lang.Integer))) {
            throw new java.lang.ClassCastException("");
         }
         var result3 = ((java.lang.Integer) result2).intValue();
         var field4 = com.zenya.module.donut.SusChunkFinderModule.AMETHYST_RENDER_DISTANCE_CHUNKS;
         var selected1 = ((result3 < field4) ? result3 : field4);
         var field5 = this.hits;
         if ((field5 != null)) {
            var result4 = field5.keySet();
            var array1 = new java.lang.Object[2];
            array1[0] = result1;
            array1[1] = java.lang.Integer.valueOf((selected1 + 2));
            var captured1 = result1;
            var captured2 = ((java.lang.Integer) java.lang.Integer.valueOf((selected1 + 2))).intValue();
            var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.donut.SusChunkFinderModule.lambda$cleanUpFarHits$6(captured1, captured2, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
            if ((result4 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            }
            var result5 = result4.removeIf(callback1);
            return;
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static boolean isWithinAmethystRenderDistance(ChunkPos center, int dx, int dz) {
      if ((center == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = center.getStartX();
      var result2 = center.getStartZ();
      var field1 = center.x;
      var field2 = center.z;
      return (((-((!(Double.isNaN(((((((double) ((field2 + dz)) * 16.0) + 8.0) - ((double) (result2) + 8.0)) * ((((double) ((field2 + dz)) * 16.0) + 8.0) - ((double) (result2) + 8.0))) + (((((double) ((field1 + dx)) * 16.0) + 8.0) - ((double) (result1) + 8.0)) * ((((double) ((field1 + dx)) * 16.0) + 8.0) - ((double) (result1) + 8.0)))))) ? 1 : 0)) & -(((((((((double) ((field2 + dz)) * 16.0) + 8.0) - ((double) (result2) + 8.0)) * ((((double) ((field2 + dz)) * 16.0) + 8.0) - ((double) (result2) + 8.0))) + (((((double) ((field1 + dx)) * 16.0) + 8.0) - ((double) (result1) + 8.0)) * ((((double) ((field1 + dx)) * 16.0) + 8.0) - ((double) (result1) + 8.0)))) <= 90000.0) ? 1 : 0))) & 1) != 0);
   }

   private void clear() {
      this.hits.clear();
      this.scanned.clear();
      this.queued.clear();
      this.scanQueue.clear();
      this.rescanTimer = 0;
   }

   private static boolean lambda$cleanUpFarHits$6(ChunkPos center, int maxDistance, ChunkPos pos) {
      if ((pos != null)) {
         var field1 = pos.x;
         if ((center != null)) {
            var field2 = center.x;
            var selected1 = ((field2 < field1) ? (field1 - field2) : (field2 - field1));
            if ((maxDistance < selected1)) {
               return true;
            }
            var field3 = pos.z;
            var field4 = center.z;
            var selected2 = ((field4 < field3) ? (field3 - field4) : (field4 - field3));
            return !(Integer.compareUnsigned(selected2, maxDistance) <= 0);
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static Set lambda$collapseConnectedGeodeChunks$5(ChunkPos ignored) {
      return new java.util.HashSet();
   }

   private static boolean lambda$buildTriangulatedBaseArea$4(ChunkPos origin, int simDistance, ChunkPos candidate) {
      if ((candidate != null)) {
         var field1 = candidate.x;
         if ((origin != null)) {
            var field2 = origin.x;
            var selected1 = ((field2 < field1) ? (field1 - field2) : (field2 - field1));
            if ((simDistance < selected1)) {
               return false;
            }
            var field3 = candidate.z;
            var field4 = origin.z;
            var selected2 = ((field4 < field3) ? (field3 - field4) : (field4 - field3));
            return Integer.compareUnsigned(selected2, simDistance) <= 0;
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static void lambda$buildTriangulatedBaseArea$3(int voteThreshold, Set overlapChunks, ChunkPos candidate, Integer count) {
      if ((count != null)) {
         var result1 = count.intValue();
         if ((voteThreshold <= result1)) {
            if ((overlapChunks == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            }
            var result2 = ((java.util.Set) overlapChunks).add(candidate);
            return;
         } else {
            return;
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   private static void lambda$onRender$2(Set geodes, ChunkPos pos, SusChunkFinderModule.HitType type) {
      var field1 = com.zenya.module.donut.SusChunkFinderModule.HitType.AMETHYST;
      if ((type == field1)) {
         if ((geodes == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         }
         var result1 = ((java.util.Set) geodes).add(pos);
         return;
      } else {
         return;
      }
   }

   private static boolean lambda$scanChunk$1(BlockState state) {
      var result1 = state.getBlock();
      return com.zenya.module.donut.SusChunkFinderModule.isAmethyst(result1);
   }

   private static long lambda$enqueueChunks$0(ChunkPos center, ChunkPos pos) {
      if ((pos != null)) {
         var field1 = pos.x;
         if ((center != null)) {
            var field2 = center.x;
            var field3 = pos.z;
            var field4 = center.z;
            return (((long) ((field3 - field4)) * (long) ((field3 - field4))) + ((long) ((field1 - field2)) * (long) ((field1 - field2))));
         }
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }

   static {
      AMETHYST_RENDER_DISTANCE_CHUNKS = 19;
   }

   private static enum HitType {
      AMETHYST;
   }
}
