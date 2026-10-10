package com.zenya.module.smps;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.setting.Setting;
import java.awt.Color;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class OceanBaseSignalsModule extends Module {
   private static final double PLATE_Y = 63.0;
   private static final int MAX_RANGE_CHUNKS = 16;
   private final Setting<Integer> entitySensitivity;
   private final Setting<Integer> scanRadius;
   private final Setting<Color> scanColor;
   private final Setting<Color> flagColor;
   private final Map<ChunkPos, Integer> entityCounts;
   private final Set<ChunkPos> oceanChunks;
   private final Set<ChunkPos> notifiedChunks;
   private int scanTicks;

   public OceanBaseSignalsModule() {
      super("Ocean Base Signals", Category.SMPS);
      Setting<Integer> setting1 = new com.zenya.setting.Setting<>("Entity Sensitivity", java.lang.Integer.valueOf(8), java.lang.Integer.valueOf(3), java.lang.Integer.valueOf(48));
      this.entitySensitivity = setting1;
      Setting<Integer> setting2 = new com.zenya.setting.Setting<>("Scan Radius", java.lang.Integer.valueOf(10), java.lang.Integer.valueOf(2), java.lang.Integer.valueOf(16));
      this.scanRadius = setting2;
      var color1 = new java.awt.Color(30, 138, 255, 42);
      Setting<Color> setting3 = new com.zenya.setting.Setting<>("Ocean Scan Color", color1);
      this.scanColor = setting3;
      var color2 = new java.awt.Color(255, 68, 35, 125);
      Setting<Color> setting4 = new com.zenya.setting.Setting<>("Flag Color", color2);
      this.flagColor = setting4;
      Map<ChunkPos, Integer> hashMap1 = new java.util.HashMap<>();
      this.entityCounts = hashMap1;
      Set<ChunkPos> hashSet1 = new java.util.HashSet<>();
      this.oceanChunks = hashSet1;
      Set<ChunkPos> hashSet2 = new java.util.HashSet<>();
      this.notifiedChunks = hashSet2;
      this.setDescription("Flags dense visible non-item entity clusters beneath ocean columns.");
      this.addSetting(this.entitySensitivity);
      this.addSetting(this.scanRadius);
      this.addSetting(this.scanColor);
      this.addSetting(this.flagColor);
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
      var field1 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.world;
         if ((field2 == null)) {
            return;
         } else {
            var field3 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var field4 = field3.player;
               if ((field4 == null)) {
                  return;
               } else {
                  var field5 = this.scanTicks;
                  this.scanTicks = (field5 + 1);
                  if ((8 < field5)) {
                     this.scanTicks = 0;
                     this.refreshOceanChunks();
                     var field6 = this.entityCounts;
                     if ((field6 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     } else {
                        field6.clear();
                        var field7 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                        if ((field7 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           var field8 = field7.player;
                           if ((field8 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result1 = field8.getChunkPos();
                              var field9 = this.scanRadius;
                              if ((field9 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 var result2 = field9.getValue();
                                 if ((result2 != null)) {
                                    if (!((result2 == null || result2 instanceof java.lang.Integer))) {
                                       throw new java.lang.ClassCastException("");
                                    } else {
                                       var result3 = ((java.lang.Integer) result2).intValue();
                                       var field10 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                       if ((field10 != null)) {
                                          var field11 = field10.world;
                                          if ((field11 != null)) {
                                             var result4 = field11.getEntities();
                                             if ((result4 != null)) {
                                                var result5 = result4.iterator();
                                                if ((result5 != null)) {
                                                   Object iteration1_1 = result1;
                                                   repeat1: while (true) {
                                                      var result6 = result5.hasNext();
                                                      if (!(result6)) {
                                                         this.notifyNewSignals();
                                                         return;
                                                      } else {
                                                         var result7 = result5.next();
                                                         if ((result7 != null)) {
                                                            if (!((result7 == null || result7 instanceof net.minecraft.entity.Entity))) {
                                                               throw new java.lang.ClassCastException("");
                                                            } else {
                                                               var field12 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                               if ((field12 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var field13 = field12.player;
                                                                  if (!((result7 == field13))) {
                                                                     if ((result7 == null)) {
                                                                        var result8 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                        var chunkPos1 = new net.minecraft.util.math.ChunkPos(result8);
                                                                        var field14 = chunkPos1.x;
                                                                        if ((iteration1_1 != null)) {
                                                                           var field15 = ((net.minecraft.util.math.ChunkPos) iteration1_1).x;
                                                                           var selected1 = ((field15 < field14) ? (field14 - field15) : (field15 - field14));
                                                                           if ((selected1 <= result3)) {
                                                                              var field16 = chunkPos1.z;
                                                                              var field17 = ((net.minecraft.util.math.ChunkPos) iteration1_1).z;
                                                                              var selected2 = ((field17 < field16) ? (field16 - field17) : (field17 - field16));
                                                                              if (Integer.compareUnsigned(selected2, result3) <= 0) {
                                                                                 var field18 = this.oceanChunks;
                                                                                 if ((field18 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    var result9 = ((java.util.Set) field18).contains(chunkPos1);
                                                                                    if (result9) {
                                                                                       var result10 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                                       var result11 = this.isBelowOceanColumn(result10);
                                                                                       if (result11) {
                                                                                          var field19 = this.entityCounts;
                                                                                          var array1 = new java.lang.Object[0];
                                                                                          var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                                          if ((field19 == null)) {
                                                                                             throw new java.lang.NullPointerException("object reference is null");
                                                                                          } else {
                                                                                             var result12 = ((java.util.Map) field19).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                                             continue repeat1;
                                                                                          }
                                                                                       } else {
                                                                                          continue repeat1;
                                                                                       }
                                                                                    } else {
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
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        }
                                                                     } else {
                                                                        if (!((result7 == null || result7 instanceof net.minecraft.entity.ItemEntity))) {
                                                                           var result8 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                           var chunkPos1 = new net.minecraft.util.math.ChunkPos(result8);
                                                                           var field14 = chunkPos1.x;
                                                                           if ((iteration1_1 != null)) {
                                                                              var field15 = ((net.minecraft.util.math.ChunkPos) iteration1_1).x;
                                                                              var selected1 = ((field15 < field14) ? (field14 - field15) : (field15 - field14));
                                                                              if ((selected1 <= result3)) {
                                                                                 var field16 = chunkPos1.z;
                                                                                 var field17 = ((net.minecraft.util.math.ChunkPos) iteration1_1).z;
                                                                                 var selected2 = ((field17 < field16) ? (field16 - field17) : (field17 - field16));
                                                                                 if (Integer.compareUnsigned(selected2, result3) <= 0) {
                                                                                    var field18 = this.oceanChunks;
                                                                                    if ((field18 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var result9 = ((java.util.Set) field18).contains(chunkPos1);
                                                                                       if (result9) {
                                                                                          var result10 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                                          var result11 = this.isBelowOceanColumn(result10);
                                                                                          if (result11) {
                                                                                             var field19 = this.entityCounts;
                                                                                             var array1 = new java.lang.Object[0];
                                                                                             var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                                             if ((field19 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                var result12 = ((java.util.Map) field19).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                                                continue repeat1;
                                                                                             }
                                                                                          } else {
                                                                                             continue repeat1;
                                                                                          }
                                                                                       } else {
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
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           }
                                                                        } else {
                                                                           continue repeat1;
                                                                        }
                                                                     }
                                                                  } else {
                                                                     continue repeat1;
                                                                  }
                                                               }
                                                            }
                                                         } else {
                                                            var field12 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                            if ((field12 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var field13 = field12.player;
                                                               if (!((result7 == field13))) {
                                                                  if ((result7 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     if (!((result7 == null || result7 instanceof net.minecraft.entity.ItemEntity))) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        continue repeat1;
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
                  } else {
                     var field6 = this.entityCounts;
                     if ((field6 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     } else {
                        field6.clear();
                        var field7 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                        if ((field7 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           var field8 = field7.player;
                           if ((field8 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result1 = field8.getChunkPos();
                              var field9 = this.scanRadius;
                              if ((field9 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 var result2 = field9.getValue();
                                 if ((result2 != null)) {
                                    if (!((result2 == null || result2 instanceof java.lang.Integer))) {
                                       throw new java.lang.ClassCastException("");
                                    } else {
                                       var result3 = ((java.lang.Integer) result2).intValue();
                                       var field10 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                       if ((field10 != null)) {
                                          var field11 = field10.world;
                                          if ((field11 != null)) {
                                             var result4 = field11.getEntities();
                                             if ((result4 != null)) {
                                                var result5 = result4.iterator();
                                                if ((result5 != null)) {
                                                   Object iteration1_1 = result1;
                                                   repeat1: while (true) {
                                                      var result6 = result5.hasNext();
                                                      if (!(result6)) {
                                                         this.notifyNewSignals();
                                                         return;
                                                      } else {
                                                         var result7 = result5.next();
                                                         if ((result7 != null)) {
                                                            if (!((result7 == null || result7 instanceof net.minecraft.entity.Entity))) {
                                                               throw new java.lang.ClassCastException("");
                                                            } else {
                                                               var field12 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                               if ((field12 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var field13 = field12.player;
                                                                  if (!((result7 == field13))) {
                                                                     if ((result7 == null)) {
                                                                        var result8 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                        var chunkPos1 = new net.minecraft.util.math.ChunkPos(result8);
                                                                        var field14 = chunkPos1.x;
                                                                        if ((iteration1_1 != null)) {
                                                                           var field15 = ((net.minecraft.util.math.ChunkPos) iteration1_1).x;
                                                                           var selected1 = ((field15 < field14) ? (field14 - field15) : (field15 - field14));
                                                                           if ((selected1 <= result3)) {
                                                                              var field16 = chunkPos1.z;
                                                                              var field17 = ((net.minecraft.util.math.ChunkPos) iteration1_1).z;
                                                                              var selected2 = ((field17 < field16) ? (field16 - field17) : (field17 - field16));
                                                                              if (Integer.compareUnsigned(selected2, result3) <= 0) {
                                                                                 var field18 = this.oceanChunks;
                                                                                 if ((field18 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    var result9 = ((java.util.Set) field18).contains(chunkPos1);
                                                                                    if (result9) {
                                                                                       var result10 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                                       var result11 = this.isBelowOceanColumn(result10);
                                                                                       if (result11) {
                                                                                          var field19 = this.entityCounts;
                                                                                          var array1 = new java.lang.Object[0];
                                                                                          var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                                          if ((field19 == null)) {
                                                                                             throw new java.lang.NullPointerException("object reference is null");
                                                                                          } else {
                                                                                             var result12 = ((java.util.Map) field19).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                                             continue repeat1;
                                                                                          }
                                                                                       } else {
                                                                                          continue repeat1;
                                                                                       }
                                                                                    } else {
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
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        }
                                                                     } else {
                                                                        if (!((result7 == null || result7 instanceof net.minecraft.entity.ItemEntity))) {
                                                                           var result8 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                           var chunkPos1 = new net.minecraft.util.math.ChunkPos(result8);
                                                                           var field14 = chunkPos1.x;
                                                                           if ((iteration1_1 != null)) {
                                                                              var field15 = ((net.minecraft.util.math.ChunkPos) iteration1_1).x;
                                                                              var selected1 = ((field15 < field14) ? (field14 - field15) : (field15 - field14));
                                                                              if ((selected1 <= result3)) {
                                                                                 var field16 = chunkPos1.z;
                                                                                 var field17 = ((net.minecraft.util.math.ChunkPos) iteration1_1).z;
                                                                                 var selected2 = ((field17 < field16) ? (field16 - field17) : (field17 - field16));
                                                                                 if (Integer.compareUnsigned(selected2, result3) <= 0) {
                                                                                    var field18 = this.oceanChunks;
                                                                                    if ((field18 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var result9 = ((java.util.Set) field18).contains(chunkPos1);
                                                                                       if (result9) {
                                                                                          var result10 = ((net.minecraft.entity.Entity) result7).getBlockPos();
                                                                                          var result11 = this.isBelowOceanColumn(result10);
                                                                                          if (result11) {
                                                                                             var field19 = this.entityCounts;
                                                                                             var array1 = new java.lang.Object[0];
                                                                                             var callback1 = new java.util.function.BiFunction() { public java.lang.Object apply(java.lang.Object parameter0, java.lang.Object parameter1) { return java.lang.Integer.sum(((java.lang.Integer) parameter0).intValue(), ((java.lang.Integer) parameter1).intValue()); } };
                                                                                             if ((field19 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                var result12 = ((java.util.Map) field19).merge(chunkPos1, java.lang.Integer.valueOf(1), callback1);
                                                                                                continue repeat1;
                                                                                             }
                                                                                          } else {
                                                                                             continue repeat1;
                                                                                          }
                                                                                       } else {
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
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           }
                                                                        } else {
                                                                           continue repeat1;
                                                                        }
                                                                     }
                                                                  } else {
                                                                     continue repeat1;
                                                                  }
                                                               }
                                                            }
                                                         } else {
                                                            var field12 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                            if ((field12 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var field13 = field12.player;
                                                               if (!((result7 == field13))) {
                                                                  if ((result7 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     if (!((result7 == null || result7 instanceof net.minecraft.entity.ItemEntity))) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        continue repeat1;
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
   }

   private void refreshOceanChunks() {
      var field1 = this.oceanChunks;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         field1.clear();
         var field2 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
         if ((field2 == null)) {
            throw new java.lang.NullPointerException("object reference is null");
         } else {
            var field3 = field2.player;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var result1 = field3.getChunkPos();
               var field4 = this.scanRadius;
               if ((field4 == null)) {
                  throw new java.lang.NullPointerException("object reference is null");
               } else {
                  var result2 = field4.getValue();
                  if ((result2 != null)) {
                     if (!((result2 == null || result2 instanceof java.lang.Integer))) {
                        throw new java.lang.ClassCastException("");
                     } else {
                        var result3 = ((java.lang.Integer) result2).intValue();
                        if ((result1 != null)) {
                           var field5 = result1.x;
                           int iteration1_1 = (field5 - result3);
                           Object iteration1_2 = result1;
                           repeat1: while (true) {
                              var field6 = ((net.minecraft.util.math.ChunkPos) iteration1_2).x;
                              if (((field6 + result3) < iteration1_1)) {
                                 var field7 = this.notifiedChunks;
                                 var array1 = new java.lang.Object[2];
                                 array1[0] = iteration1_2;
                                 iteration1_2 = java.lang.Integer.valueOf((int) (Integer.toUnsignedLong(result3)));
                                 array1[1] = iteration1_2;
                                 var captured1 = ((net.minecraft.util.math.ChunkPos) iteration1_2);
                                 var captured2 = ((java.lang.Integer) iteration1_2).intValue();
                                 var callback1 = new java.util.function.Predicate() { public boolean test(java.lang.Object parameter0) { return com.zenya.module.smps.OceanBaseSignalsModule.lambda$refreshOceanChunks$0(captured1, captured2, ((net.minecraft.util.math.ChunkPos) parameter0)); } };
                                 iteration1_2 = callback1;
                                 if ((field7 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    var result4 = field7.removeIf(((java.util.function.Predicate) iteration1_2));
                                    return;
                                 }
                              } else {
                                 var field7 = ((net.minecraft.util.math.ChunkPos) iteration1_2).z;
                                 int iteration2_1 = (field7 - result3);
                                 repeat2: while (true) {
                                    var field8 = ((net.minecraft.util.math.ChunkPos) iteration1_2).z;
                                    if (((field8 + result3) < iteration2_1)) {
                                       iteration1_1 = (iteration1_1 + 1);
                                       continue repeat1;
                                    } else {
                                       var field9 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                       if ((field9 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var field10 = field9.world;
                                          if ((field10 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var result4 = field10.getChunkManager();
                                             if ((result4 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var saved1 = iteration2_1;
                                                var result5 = result4.getWorldChunk((int) (Integer.toUnsignedLong(iteration1_1)), saved1, false);
                                                if ((result5 != null)) {
                                                   var saved2 = iteration2_1;
                                                   var saved3 = iteration1_1;
                                                   var chunkPos1 = new net.minecraft.util.math.ChunkPos(saved3, saved2);
                                                   var field11 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                   if ((field11 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var field12 = field11.world;
                                                      var field13 = net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES;
                                                      var result6 = chunkPos1.getCenterX();
                                                      var result7 = chunkPos1.getCenterZ();
                                                      if ((field12 == null)) {
                                                         throw new java.lang.NullPointerException("object reference is null");
                                                      } else {
                                                         var result8 = field12.getTopY(field13, result6, result7);
                                                         var result9 = chunkPos1.getCenterX();
                                                         var result10 = chunkPos1.getCenterZ();
                                                         var blockPos1 = new net.minecraft.util.math.BlockPos(result9, result8, result10);
                                                         var field14 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
                                                         if ((field14 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var field15 = field14.world;
                                                            if ((field15 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result11 = field15.getBiome(blockPos1);
                                                               var field16 = net.minecraft.registry.tag.BiomeTags.IS_OCEAN;
                                                               if ((result11 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result12 = result11.isIn(field16);
                                                                  if (result12) {
                                                                     var field17 = this.oceanChunks;
                                                                     if ((field17 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result13 = ((java.util.Set) field17).add(chunkPos1);
                                                                        iteration2_1 = (iteration2_1 + 1);
                                                                        continue repeat2;
                                                                     }
                                                                  } else {
                                                                     iteration2_1 = (iteration2_1 + 1);
                                                                     continue repeat2;
                                                                  }
                                                               }
                                                            }
                                                         }
                                                      }
                                                   }
                                                } else {
                                                   iteration2_1 = (iteration2_1 + 1);
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

   private boolean isBelowOceanColumn(BlockPos pos) {
      var field1 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
      if ((field1 != null)) {
         var field2 = field1.world;
         var field3 = net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES;
         if ((pos != null)) {
            var result1 = pos.getX();
            var result2 = pos.getZ();
            if ((field2 != null)) {
               var result3 = field2.getTopY(field3, result1, result2);
               var result4 = pos.getY();
               if (((result3 + -(2)) <= result4)) {
                  return false;
               }
               var result5 = pos.getX();
               var result6 = pos.getZ();
               var blockPos1 = new net.minecraft.util.math.BlockPos(result5, result3, result6);
               var field4 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
               if ((field4 != null)) {
                  var field5 = field4.world;
                  if ((field5 != null)) {
                     var result7 = field5.getBiome(blockPos1);
                     var field6 = net.minecraft.registry.tag.BiomeTags.IS_OCEAN;
                     if ((result7 != null)) {
                        return result7.isIn(field6);
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
   }

   private void notifyNewSignals() {
      var field1 = this.entitySensitivity;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var result1 = field1.getValue();
         if ((result1 != null)) {
            if (!((result1 == null || result1 instanceof java.lang.Integer))) {
               throw new java.lang.ClassCastException("");
            } else {
               var result2 = ((java.lang.Integer) result1).intValue();
               var field2 = this.entityCounts;
               if ((field2 != null)) {
                  var result3 = field2.entrySet();
                  if ((result3 != null)) {
                     var result4 = result3.iterator();
                     if ((result4 != null)) {
                        repeat1: while (true) {
                           var result5 = result4.hasNext();
                           if (!(result5)) {
                              return;
                           } else {
                              var result6 = result4.next();
                              if ((result6 == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 if (!((result6 == null || result6 instanceof java.util.Map.Entry))) {
                                    throw new java.lang.ClassCastException("");
                                 } else {
                                    var result7 = ((java.util.Map.Entry) result6).getValue();
                                    if ((result7 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       if (!((result7 == null || result7 instanceof java.lang.Integer))) {
                                          throw new java.lang.ClassCastException("");
                                       } else {
                                          var result8 = ((java.lang.Integer) result7).intValue();
                                          if ((result2 <= result8)) {
                                             var field3 = this.notifiedChunks;
                                             var result9 = ((java.util.Map.Entry) result6).getKey();
                                             if ((result9 != null)) {
                                                if (!((result9 == null || result9 instanceof net.minecraft.util.math.ChunkPos))) {
                                                   throw new java.lang.ClassCastException("");
                                                } else {
                                                   if ((field3 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var result10 = ((java.util.Set) field3).add(result9);
                                                      if (result10) {
                                                         var result11 = ((java.util.Map.Entry) result6).getKey();
                                                         if ((result11 != null)) {
                                                            if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                                               throw new java.lang.ClassCastException("");
                                                            } else {
                                                               var stringBuilder1 = new java.lang.StringBuilder();
                                                               var result12 = stringBuilder1.append("Probable base around X: ");
                                                               if ((result11 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result13 = ((net.minecraft.util.math.ChunkPos) result11).getCenterX();
                                                                  if ((result12 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result14 = result12.append(result13);
                                                                     if ((result14 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result15 = result14.append(" Z: ");
                                                                        var result16 = ((net.minecraft.util.math.ChunkPos) result11).getCenterZ();
                                                                        if ((result15 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result17 = result15.append(result16);
                                                                           if ((result17 == null)) {
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           } else {
                                                                              var result18 = result17.toString();
                                                                              com.zenya.module.ModuleUtils.toast("Ocean Base Signals", result18);
                                                                              continue repeat1;
                                                                           }
                                                                        }
                                                                     }
                                                                  }
                                                               }
                                                            }
                                                         } else {
                                                            var stringBuilder1 = new java.lang.StringBuilder();
                                                            var result12 = stringBuilder1.append("Probable base around X: ");
                                                            if ((result11 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result13 = ((net.minecraft.util.math.ChunkPos) result11).getCenterX();
                                                               if ((result12 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result14 = result12.append(result13);
                                                                  if ((result14 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result15 = result14.append(" Z: ");
                                                                     var result16 = ((net.minecraft.util.math.ChunkPos) result11).getCenterZ();
                                                                     if ((result15 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result17 = result15.append(result16);
                                                                        if ((result17 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result18 = result17.toString();
                                                                           com.zenya.module.ModuleUtils.toast("Ocean Base Signals", result18);
                                                                           continue repeat1;
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
                                             } else {
                                                if ((field3 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result10 = ((java.util.Set) field3).add(result9);
                                                   if (result10) {
                                                      var result11 = ((java.util.Map.Entry) result6).getKey();
                                                      if ((result11 != null)) {
                                                         if (!((result11 == null || result11 instanceof net.minecraft.util.math.ChunkPos))) {
                                                            throw new java.lang.ClassCastException("");
                                                         } else {
                                                            var stringBuilder1 = new java.lang.StringBuilder();
                                                            var result12 = stringBuilder1.append("Probable base around X: ");
                                                            if ((result11 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result13 = ((net.minecraft.util.math.ChunkPos) result11).getCenterX();
                                                               if ((result12 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result14 = result12.append(result13);
                                                                  if ((result14 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result15 = result14.append(" Z: ");
                                                                     var result16 = ((net.minecraft.util.math.ChunkPos) result11).getCenterZ();
                                                                     if ((result15 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result17 = result15.append(result16);
                                                                        if ((result17 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result18 = result17.toString();
                                                                           com.zenya.module.ModuleUtils.toast("Ocean Base Signals", result18);
                                                                           continue repeat1;
                                                                        }
                                                                     }
                                                                  }
                                                               }
                                                            }
                                                         }
                                                      } else {
                                                         var stringBuilder1 = new java.lang.StringBuilder();
                                                         var result12 = stringBuilder1.append("Probable base around X: ");
                                                         if ((result11 == null)) {
                                                            throw new java.lang.NullPointerException("object reference is null");
                                                         } else {
                                                            var result13 = ((net.minecraft.util.math.ChunkPos) result11).getCenterX();
                                                            if ((result12 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               var result14 = result12.append(result13);
                                                               if ((result14 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var result15 = result14.append(" Z: ");
                                                                  var result16 = ((net.minecraft.util.math.ChunkPos) result11).getCenterZ();
                                                                  if ((result15 == null)) {
                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                  } else {
                                                                     var result17 = result15.append(result16);
                                                                     if ((result17 == null)) {
                                                                        throw new java.lang.NullPointerException("object reference is null");
                                                                     } else {
                                                                        var result18 = result17.toString();
                                                                        com.zenya.module.ModuleUtils.toast("Ocean Base Signals", result18);
                                                                        continue repeat1;
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
                                          } else {
                                             continue repeat1;
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

   @Override
   public void onRender(MatrixStack matrices, float tickDelta) {
      net.minecraft.client.MinecraftClient saved1 = null;
      java.util.Map saved2 = null;
      net.minecraft.client.render.Camera saved3 = null;
      net.minecraft.util.math.Vec3d saved4 = null;
      com.zenya.setting.Setting saved5 = null;
      java.lang.Object saved6 = null;
      int saved7 = 0;
      com.zenya.setting.Setting saved8 = null;
      java.lang.Object saved9 = null;
      double saved10 = 0.0;
      com.zenya.render.WorldRenderer.WorldBatch saved11 = null;
      java.util.Set saved12 = null;
      double saved13 = 0.0;
      double saved14 = 0.0;
      double saved15 = 0.0;
      java.util.Iterator saved16 = null;
      float saved17 = 0.0F;
      java.util.Map saved18 = null;
      java.lang.Object saved19 = null;
      java.util.Set saved20 = null;
      float saved21 = 0.0F;
      java.util.Iterator saved22 = null;
      int saved23 = 0;
      int saved24 = 0;
      double saved25 = 0.0;
      double saved26 = 0.0;
      java.lang.Object saved27 = null;
      int saved28 = 0;
      int saved29 = 0;
      int saved30 = 0;
      int saved31 = 0;
      double saved32 = 0.0;
      java.awt.Color saved33 = null;
      double saved34 = 0.0;
      long saved35 = 0L;
      long saved36 = 0L;
      long saved37 = 0L;
      long saved38 = 0L;
      float saved39 = 0.0F;
      int step = 0;
      dispatch: while (true) {
         switch (step) {
            case 0 -> {
               var field1 = com.zenya.module.smps.OceanBaseSignalsModule.mc;
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
               var field3 = this.entityCounts;
               if ((field3 == null)) {
                  step = 5;
                  continue dispatch;
               } else {
                  java.util.Map nextValue0 = field3;
                  saved2 = nextValue0;
                  step = 6;
                  continue dispatch;
               }
            }
            case 5 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 6 -> {
               var result1 = saved2.isEmpty();
               if (result1) {
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
               var result2 = com.zenya.render.WorldRenderer.getCamera();
               if ((result2 == null)) {
                  step = 9;
                  continue dispatch;
               } else {
                  net.minecraft.client.render.Camera nextValue0 = result2;
                  saved3 = nextValue0;
                  step = 10;
                  continue dispatch;
               }
            }
            case 9 -> {
               return;
            }
            case 10 -> {
               var result3 = com.zenya.render.WorldRenderer.getCameraPos(saved3);
               var field4 = this.entitySensitivity;
               if ((field4 == null)) {
                  step = 11;
                  continue dispatch;
               } else {
                  net.minecraft.util.math.Vec3d nextValue0 = result3;
                  com.zenya.setting.Setting nextValue1 = field4;
                  saved4 = nextValue0;
                  saved5 = nextValue1;
                  step = 12;
                  continue dispatch;
               }
            }
            case 11 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 12 -> {
               var result4 = saved5.getValue();
               if ((result4 != null)) {
                  java.lang.Object nextValue0 = result4;
                  saved6 = nextValue0;
                  step = 13;
                  continue dispatch;
               } else {
                  step = 14;
                  continue dispatch;
               }
            }
            case 13 -> {
               if (!((saved6 == null || saved6 instanceof java.lang.Integer))) {
                  step = 15;
                  continue dispatch;
               } else {
                  step = 16;
                  continue dispatch;
               }
            }
            case 14 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 15 -> {
               throw new java.lang.ClassCastException("");
            }
            case 16 -> {
               var result5 = ((java.lang.Integer) saved6).intValue();
               var field5 = this.scanColor;
               if ((field5 != null)) {
                  int nextValue0 = result5;
                  com.zenya.setting.Setting nextValue1 = field5;
                  saved7 = nextValue0;
                  saved5 = nextValue1;
                  step = 17;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 17 -> {
               var result6 = saved5.getValue();
               if ((result6 != null)) {
                  java.lang.Object nextValue0 = result6;
                  saved6 = nextValue0;
                  step = 19;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result6;
                  saved6 = nextValue0;
                  step = 20;
                  continue dispatch;
               }
            }
            case 18 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 19 -> {
               if (!((saved6 == null || saved6 instanceof java.awt.Color))) {
                  step = 21;
                  continue dispatch;
               } else {
                  step = 20;
                  continue dispatch;
               }
            }
            case 20 -> {
               var field6 = this.flagColor;
               if ((field6 != null)) {
                  com.zenya.setting.Setting nextValue0 = field6;
                  saved8 = nextValue0;
                  step = 22;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 21 -> {
               throw new java.lang.ClassCastException("");
            }
            case 22 -> {
               var result7 = saved8.getValue();
               if ((result7 != null)) {
                  java.lang.Object nextValue0 = result7;
                  saved9 = nextValue0;
                  step = 23;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result7;
                  saved9 = nextValue0;
                  step = 24;
                  continue dispatch;
               }
            }
            case 23 -> {
               if (!((saved9 == null || saved9 instanceof java.awt.Color))) {
                  step = 25;
                  continue dispatch;
               } else {
                  step = 24;
                  continue dispatch;
               }
            }
            case 24 -> {
               var result8 = com.zenya.render.WorldRenderer.beginWorldBatch(matrices);
               var result9 = java.lang.System.currentTimeMillis();
               var numeric1 = Math.sin(((((double) ((result9 % 1200)) * 3.141592653589793) + ((double) ((result9 % 1200)) * 3.141592653589793)) / 1200.0));
               var field7 = this.oceanChunks;
               if ((field7 != null)) {
                  double nextValue0 = numeric1;
                  com.zenya.render.WorldRenderer.WorldBatch nextValue1 = result8;
                  java.util.Set nextValue2 = field7;
                  saved10 = nextValue0;
                  saved11 = nextValue1;
                  saved12 = nextValue2;
                  step = 26;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 25 -> {
               throw new java.lang.ClassCastException("");
            }
            case 26 -> {
               var result10 = saved12.iterator();
               if ((result10 != null)) {
                  double nextValue0 = 63.0;
                  double nextValue1 = 16.0;
                  double nextValue2 = 0.05;
                  java.util.Iterator nextValue3 = result10;
                  saved13 = nextValue0;
                  saved14 = nextValue1;
                  saved15 = nextValue2;
                  saved16 = nextValue3;
                  step = 27;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 27 -> {
               var result11 = saved16.hasNext();
               if (!(result11)) {
                  step = 28;
                  continue dispatch;
               } else {
                  float nextValue0 = (((float) (saved10) * 0.45F) + 0.55F);
                  saved17 = nextValue0;
                  step = 29;
                  continue dispatch;
               }
            }
            case 28 -> {
               var field8 = this.entityCounts;
               if ((field8 != null)) {
                  java.util.Map nextValue0 = field8;
                  saved18 = nextValue0;
                  step = 30;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 29 -> {
               var result12 = saved16.next();
               if ((result12 == null)) {
                  step = 31;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result12;
                  saved19 = nextValue0;
                  step = 32;
                  continue dispatch;
               }
            }
            case 30 -> {
               var result12 = saved18.entrySet();
               if ((result12 != null)) {
                  java.util.Set nextValue0 = result12;
                  saved20 = nextValue0;
                  step = 33;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 31 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 32 -> {
               if (!((saved19 == null || saved19 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 34;
                  continue dispatch;
               } else {
                  step = 35;
                  continue dispatch;
               }
            }
            case 33 -> {
               var result13 = saved20.iterator();
               if ((result13 != null)) {
                  double nextValue0 = 0.12;
                  float nextValue1 = 1.0F;
                  float nextValue2 = 8e+01F;
                  java.util.Iterator nextValue3 = result13;
                  saved10 = nextValue0;
                  saved17 = nextValue1;
                  saved21 = nextValue2;
                  saved22 = nextValue3;
                  step = 36;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 34 -> {
               throw new java.lang.ClassCastException("");
            }
            case 35 -> {
               var result13 = ((net.minecraft.util.math.ChunkPos) saved19).getStartX();
               if ((saved4 == null)) {
                  step = 37;
                  continue dispatch;
               } else {
                  int nextValue0 = result13;
                  saved23 = nextValue0;
                  step = 38;
                  continue dispatch;
               }
            }
            case 36 -> {
               var result14 = saved22.hasNext();
               if (!(result14)) {
                  step = 39;
                  continue dispatch;
               } else {
                  step = 40;
                  continue dispatch;
               }
            }
            case 37 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 38 -> {
               var field8 = saved4.x;
               var result14 = ((net.minecraft.util.math.ChunkPos) saved19).getStartZ();
               var field9 = saved4.z;
               var field10 = saved4.y;
               if ((saved6 == null)) {
                  step = 41;
                  continue dispatch;
               } else {
                  double nextValue0 = field8;
                  int nextValue1 = result14;
                  double nextValue2 = field10;
                  double nextValue3 = field9;
                  saved10 = nextValue0;
                  saved24 = nextValue1;
                  saved25 = nextValue2;
                  saved26 = nextValue3;
                  step = 42;
                  continue dispatch;
               }
            }
            case 39 -> {
               return;
            }
            case 40 -> {
               var result15 = saved22.next();
               if ((result15 == null)) {
                  step = 43;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result15;
                  saved27 = nextValue0;
                  step = 44;
                  continue dispatch;
               }
            }
            case 41 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 42 -> {
               var result15 = ((java.awt.Color) saved6).getRed();
               var result16 = ((java.awt.Color) saved6).getGreen();
               var result17 = ((java.awt.Color) saved6).getBlue();
               var result18 = ((java.awt.Color) saved6).getAlpha();
               var rounded1 = com.zenya.util.ClientMath.roundToInt(((float) (result18) * saved17));
               if ((rounded1 < 9)) {
                  int nextValue0 = result17;
                  int nextValue1 = result15;
                  int nextValue2 = result16;
                  saved28 = nextValue0;
                  saved29 = nextValue1;
                  saved30 = nextValue2;
                  step = 45;
                  continue dispatch;
               } else {
                  int nextValue0 = result17;
                  int nextValue1 = rounded1;
                  int nextValue2 = result15;
                  int nextValue3 = result16;
                  saved28 = nextValue0;
                  saved31 = nextValue1;
                  saved29 = nextValue2;
                  saved30 = nextValue3;
                  step = 46;
                  continue dispatch;
               }
            }
            case 43 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 44 -> {
               if (!((saved27 == null || saved27 instanceof java.util.Map.Entry))) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 48;
                  continue dispatch;
               }
            }
            case 45 -> {
               var color1 = new java.awt.Color(saved29, saved30, saved28, 8);
               if ((saved11 == null)) {
                  step = 49;
                  continue dispatch;
               } else {
                  double nextValue0 = (saved13 - saved25);
                  double nextValue1 = ((saved13 - saved25) + saved15);
                  java.awt.Color nextValue2 = color1;
                  saved25 = nextValue0;
                  saved32 = nextValue1;
                  saved33 = nextValue2;
                  step = 50;
                  continue dispatch;
               }
            }
            case 46 -> {
               var color1 = new java.awt.Color(saved29, saved30, saved28, saved31);
               if ((saved11 == null)) {
                  step = 49;
                  continue dispatch;
               } else {
                  double nextValue0 = (saved13 - saved25);
                  double nextValue1 = ((saved13 - saved25) + saved15);
                  java.awt.Color nextValue2 = color1;
                  saved25 = nextValue0;
                  saved32 = nextValue1;
                  saved33 = nextValue2;
                  step = 50;
                  continue dispatch;
               }
            }
            case 47 -> {
               throw new java.lang.ClassCastException("");
            }
            case 48 -> {
               var result16 = ((java.util.Map.Entry) saved27).getValue();
               if ((result16 == null)) {
                  step = 51;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result16;
                  saved19 = nextValue0;
                  step = 52;
                  continue dispatch;
               }
            }
            case 49 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 50 -> {
               saved11.renderFilledBox(((double) (saved23) - saved10), saved25, ((double) (saved24) - saved26), (((double) (saved23) - saved10) + saved14), saved32, (((double) (saved24) - saved26) + saved14), saved33);
               var result19 = saved16.hasNext();
               if (!(result19)) {
                  double nextValue0 = ((double) (saved24) - saved26);
                  double nextValue1 = (((double) (saved23) - saved10) + saved14);
                  saved26 = nextValue0;
                  saved34 = nextValue1;
                  step = 53;
                  continue dispatch;
               } else {
                  long nextValue0 = (Double.doubleToRawLongBits(saved25) >>> 32);
                  long nextValue1 = (Double.doubleToRawLongBits(((double) (saved24) - saved26)) >>> 32);
                  long nextValue2 = (Double.doubleToRawLongBits((((double) (saved23) - saved10) + saved14)) >>> 32);
                  long nextValue3 = (Double.doubleToRawLongBits(saved32) >>> 32);
                  saved35 = nextValue0;
                  saved36 = nextValue1;
                  saved37 = nextValue2;
                  saved38 = nextValue3;
                  step = 54;
                  continue dispatch;
               }
            }
            case 51 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 52 -> {
               if (!((saved19 == null || saved19 instanceof java.lang.Integer))) {
                  step = 55;
                  continue dispatch;
               } else {
                  step = 56;
                  continue dispatch;
               }
            }
            case 53 -> {
               var field11 = this.entityCounts;
               if ((field11 != null)) {
                  java.util.Map nextValue0 = field11;
                  saved18 = nextValue0;
                  step = 57;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 54 -> {
               var result20 = saved16.next();
               if ((result20 == null)) {
                  step = 31;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result20;
                  saved19 = nextValue0;
                  step = 58;
                  continue dispatch;
               }
            }
            case 55 -> {
               throw new java.lang.ClassCastException("");
            }
            case 56 -> {
               var result17 = ((java.lang.Integer) saved19).intValue();
               if ((saved7 <= result17)) {
                  int nextValue0 = result17;
                  saved23 = nextValue0;
                  step = 59;
                  continue dispatch;
               } else {
                  step = 60;
                  continue dispatch;
               }
            }
            case 57 -> {
               var result20 = saved18.entrySet();
               if ((result20 != null)) {
                  java.util.Set nextValue0 = result20;
                  saved20 = nextValue0;
                  step = 61;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 58 -> {
               if (!((saved19 == null || saved19 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 34;
                  continue dispatch;
               } else {
                  step = 62;
                  continue dispatch;
               }
            }
            case 59 -> {
               if ((saved17 <= ((float) ((saved23 - saved7)) / (float) (saved7)))) {
                  step = 63;
                  continue dispatch;
               } else {
                  float nextValue0 = ((float) ((saved23 - saved7)) / (float) (saved7));
                  saved39 = nextValue0;
                  step = 64;
                  continue dispatch;
               }
            }
            case 60 -> {
               var result18 = saved22.hasNext();
               if (!(result18)) {
                  step = 39;
                  continue dispatch;
               } else {
                  step = 40;
                  continue dispatch;
               }
            }
            case 61 -> {
               var result21 = saved20.iterator();
               if ((result21 != null)) {
                  double nextValue0 = 0.12;
                  float nextValue1 = 1.0F;
                  float nextValue2 = 8e+01F;
                  java.util.Iterator nextValue3 = result21;
                  saved10 = nextValue0;
                  saved17 = nextValue1;
                  saved21 = nextValue2;
                  saved22 = nextValue3;
                  step = 65;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 62 -> {
               var result21 = ((net.minecraft.util.math.ChunkPos) saved19).getStartX();
               if ((saved4 == null)) {
                  step = 37;
                  continue dispatch;
               } else {
                  int nextValue0 = result21;
                  saved23 = nextValue0;
                  step = 66;
                  continue dispatch;
               }
            }
            case 63 -> {
               if ((saved9 == null)) {
                  step = 67;
                  continue dispatch;
               } else {
                  float nextValue0 = saved17;
                  saved39 = nextValue0;
                  step = 68;
                  continue dispatch;
               }
            }
            case 64 -> {
               if ((saved9 == null)) {
                  step = 67;
                  continue dispatch;
               } else {
                  step = 68;
                  continue dispatch;
               }
            }
            case 65 -> {
               var result22 = saved22.hasNext();
               if (!(result22)) {
                  step = 69;
                  continue dispatch;
               } else {
                  step = 70;
                  continue dispatch;
               }
            }
            case 66 -> {
               var field11 = saved4.x;
               var result22 = ((net.minecraft.util.math.ChunkPos) saved19).getStartZ();
               var field12 = saved4.z;
               var field13 = saved4.y;
               if ((saved6 == null)) {
                  step = 41;
                  continue dispatch;
               } else {
                  double nextValue0 = field11;
                  int nextValue1 = result22;
                  double nextValue2 = field13;
                  double nextValue3 = field12;
                  saved10 = nextValue0;
                  saved24 = nextValue1;
                  saved25 = nextValue2;
                  saved26 = nextValue3;
                  step = 71;
                  continue dispatch;
               }
            }
            case 67 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 68 -> {
               var result18 = ((java.awt.Color) saved9).getRed();
               var result19 = ((java.awt.Color) saved9).getGreen();
               var result20 = ((java.awt.Color) saved9).getBlue();
               var result21 = ((java.awt.Color) saved9).getAlpha();
               var rounded1 = com.zenya.util.ClientMath.roundToInt((saved39 * saved21));
               if ((219 < (rounded1 + result21))) {
                  int nextValue0 = result18;
                  int nextValue1 = result19;
                  int nextValue2 = result20;
                  saved23 = nextValue0;
                  saved24 = nextValue1;
                  saved29 = nextValue2;
                  step = 72;
                  continue dispatch;
               } else {
                  int nextValue0 = (rounded1 + result21);
                  int nextValue1 = result18;
                  int nextValue2 = result19;
                  int nextValue3 = result20;
                  saved28 = nextValue0;
                  saved23 = nextValue1;
                  saved24 = nextValue2;
                  saved29 = nextValue3;
                  step = 73;
                  continue dispatch;
               }
            }
            case 69 -> {
               if ((saved11 != null)) {
                  step = 74;
                  continue dispatch;
               } else {
                  step = 18;
                  continue dispatch;
               }
            }
            case 70 -> {
               var result23 = saved22.next();
               if ((result23 == null)) {
                  step = 43;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result23;
                  saved27 = nextValue0;
                  step = 75;
                  continue dispatch;
               }
            }
            case 71 -> {
               var result23 = ((java.awt.Color) saved6).getRed();
               var result24 = ((java.awt.Color) saved6).getGreen();
               var result25 = ((java.awt.Color) saved6).getBlue();
               var result26 = ((java.awt.Color) saved6).getAlpha();
               var rounded2 = com.zenya.util.ClientMath.roundToInt(((float) (result26) * saved17));
               if ((rounded2 < 9)) {
                  int nextValue0 = result25;
                  int nextValue1 = result23;
                  int nextValue2 = result24;
                  saved28 = nextValue0;
                  saved29 = nextValue1;
                  saved30 = nextValue2;
                  step = 76;
                  continue dispatch;
               } else {
                  int nextValue0 = result25;
                  int nextValue1 = rounded2;
                  int nextValue2 = result23;
                  int nextValue3 = result24;
                  saved28 = nextValue0;
                  saved31 = nextValue1;
                  saved29 = nextValue2;
                  saved30 = nextValue3;
                  step = 77;
                  continue dispatch;
               }
            }
            case 72 -> {
               var color1 = new java.awt.Color(saved23, saved24, saved29, 220);
               var result22 = ((java.util.Map.Entry) saved27).getKey();
               if ((result22 == null)) {
                  step = 78;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result22;
                  java.awt.Color nextValue1 = color1;
                  saved27 = nextValue0;
                  saved33 = nextValue1;
                  step = 79;
                  continue dispatch;
               }
            }
            case 73 -> {
               var color1 = new java.awt.Color(saved23, saved24, saved29, saved28);
               var result22 = ((java.util.Map.Entry) saved27).getKey();
               if ((result22 == null)) {
                  step = 78;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result22;
                  java.awt.Color nextValue1 = color1;
                  saved27 = nextValue0;
                  saved33 = nextValue1;
                  step = 79;
                  continue dispatch;
               }
            }
            case 74 -> {
               saved11.flush();
               return;
            }
            case 75 -> {
               if (!((saved27 == null || saved27 instanceof java.util.Map.Entry))) {
                  step = 47;
                  continue dispatch;
               } else {
                  step = 80;
                  continue dispatch;
               }
            }
            case 76 -> {
               var color2 = new java.awt.Color(saved29, saved30, saved28, 8);
               if ((saved11 == null)) {
                  step = 49;
                  continue dispatch;
               } else {
                  double nextValue0 = (saved13 - saved25);
                  double nextValue1 = ((saved13 - saved25) + saved15);
                  java.awt.Color nextValue2 = color2;
                  saved25 = nextValue0;
                  saved32 = nextValue1;
                  saved33 = nextValue2;
                  step = 50;
                  continue dispatch;
               }
            }
            case 77 -> {
               var color2 = new java.awt.Color(saved29, saved30, saved28, saved31);
               if ((saved11 == null)) {
                  step = 49;
                  continue dispatch;
               } else {
                  double nextValue0 = (saved13 - saved25);
                  double nextValue1 = ((saved13 - saved25) + saved15);
                  java.awt.Color nextValue2 = color2;
                  saved25 = nextValue0;
                  saved32 = nextValue1;
                  saved33 = nextValue2;
                  step = 50;
                  continue dispatch;
               }
            }
            case 78 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 79 -> {
               if (!((saved27 == null || saved27 instanceof net.minecraft.util.math.ChunkPos))) {
                  step = 81;
                  continue dispatch;
               } else {
                  step = 82;
                  continue dispatch;
               }
            }
            case 80 -> {
               var result24 = ((java.util.Map.Entry) saved27).getValue();
               if ((result24 == null)) {
                  step = 51;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result24;
                  saved19 = nextValue0;
                  step = 83;
                  continue dispatch;
               }
            }
            case 81 -> {
               throw new java.lang.ClassCastException("");
            }
            case 82 -> {
               var result23 = ((net.minecraft.util.math.ChunkPos) saved27).getStartX();
               if ((saved4 == null)) {
                  step = 84;
                  continue dispatch;
               } else {
                  int nextValue0 = result23;
                  saved23 = nextValue0;
                  step = 85;
                  continue dispatch;
               }
            }
            case 83 -> {
               if (!((saved19 == null || saved19 instanceof java.lang.Integer))) {
                  step = 55;
                  continue dispatch;
               } else {
                  step = 86;
                  continue dispatch;
               }
            }
            case 84 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 85 -> {
               var field9 = saved4.x;
               var result24 = ((net.minecraft.util.math.ChunkPos) saved27).getStartZ();
               var field10 = saved4.z;
               var field11 = saved4.y;
               if ((saved11 == null)) {
                  step = 87;
                  continue dispatch;
               } else {
                  double nextValue0 = field9;
                  int nextValue1 = result24;
                  double nextValue2 = (saved13 - field11);
                  double nextValue3 = field10;
                  double nextValue4 = ((saved13 - field11) + saved10);
                  saved15 = nextValue0;
                  saved24 = nextValue1;
                  saved25 = nextValue2;
                  saved26 = nextValue3;
                  saved32 = nextValue4;
                  step = 88;
                  continue dispatch;
               }
            }
            case 86 -> {
               var result25 = ((java.lang.Integer) saved19).intValue();
               if ((saved7 <= result25)) {
                  int nextValue0 = result25;
                  long nextValue1 = (Double.doubleToRawLongBits(saved25) >>> 32);
                  long nextValue2 = (Double.doubleToRawLongBits(saved26) >>> 32);
                  long nextValue3 = (Double.doubleToRawLongBits(saved34) >>> 32);
                  long nextValue4 = (Double.doubleToRawLongBits(saved32) >>> 32);
                  saved23 = nextValue0;
                  saved35 = nextValue1;
                  saved36 = nextValue2;
                  saved37 = nextValue3;
                  saved38 = nextValue4;
                  step = 89;
                  continue dispatch;
               } else {
                  step = 90;
                  continue dispatch;
               }
            }
            case 87 -> {
               throw new java.lang.NullPointerException("object reference is null");
            }
            case 88 -> {
               saved11.renderFilledBox(((double) (saved23) - saved15), saved25, ((double) (saved24) - saved26), (((double) (saved23) - saved15) + saved14), saved32, (((double) (saved24) - saved26) + saved14), saved33);
               var result25 = saved22.hasNext();
               if (!(result25)) {
                  step = 69;
                  continue dispatch;
               } else {
                  double nextValue0 = ((double) (saved24) - saved26);
                  double nextValue1 = (((double) (saved23) - saved15) + saved14);
                  saved26 = nextValue0;
                  saved34 = nextValue1;
                  step = 70;
                  continue dispatch;
               }
            }
            case 89 -> {
               if ((saved17 <= ((float) ((saved23 - saved7)) / (float) (saved7)))) {
                  step = 91;
                  continue dispatch;
               } else {
                  float nextValue0 = ((float) ((saved23 - saved7)) / (float) (saved7));
                  saved39 = nextValue0;
                  step = 92;
                  continue dispatch;
               }
            }
            case 90 -> {
               var result26 = saved22.hasNext();
               if (!(result26)) {
                  step = 69;
                  continue dispatch;
               } else {
                  step = 70;
                  continue dispatch;
               }
            }
            case 91 -> {
               if ((saved9 == null)) {
                  step = 67;
                  continue dispatch;
               } else {
                  float nextValue0 = saved17;
                  saved39 = nextValue0;
                  step = 93;
                  continue dispatch;
               }
            }
            case 92 -> {
               if ((saved9 == null)) {
                  step = 67;
                  continue dispatch;
               } else {
                  step = 93;
                  continue dispatch;
               }
            }
            case 93 -> {
               var result26 = ((java.awt.Color) saved9).getRed();
               var result27 = ((java.awt.Color) saved9).getGreen();
               var result28 = ((java.awt.Color) saved9).getBlue();
               var result29 = ((java.awt.Color) saved9).getAlpha();
               var rounded2 = com.zenya.util.ClientMath.roundToInt((saved39 * saved21));
               if ((219 < (rounded2 + result29))) {
                  int nextValue0 = result26;
                  int nextValue1 = result27;
                  int nextValue2 = result28;
                  saved23 = nextValue0;
                  saved24 = nextValue1;
                  saved29 = nextValue2;
                  step = 94;
                  continue dispatch;
               } else {
                  int nextValue0 = (rounded2 + result29);
                  int nextValue1 = result26;
                  int nextValue2 = result27;
                  int nextValue3 = result28;
                  saved28 = nextValue0;
                  saved23 = nextValue1;
                  saved24 = nextValue2;
                  saved29 = nextValue3;
                  step = 95;
                  continue dispatch;
               }
            }
            case 94 -> {
               var color2 = new java.awt.Color(saved23, saved24, saved29, 220);
               var result30 = ((java.util.Map.Entry) saved27).getKey();
               if ((result30 == null)) {
                  step = 78;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result30;
                  java.awt.Color nextValue1 = color2;
                  saved27 = nextValue0;
                  saved33 = nextValue1;
                  step = 79;
                  continue dispatch;
               }
            }
            case 95 -> {
               var color2 = new java.awt.Color(saved23, saved24, saved29, saved28);
               var result30 = ((java.util.Map.Entry) saved27).getKey();
               if ((result30 == null)) {
                  step = 78;
                  continue dispatch;
               } else {
                  java.lang.Object nextValue0 = result30;
                  java.awt.Color nextValue1 = color2;
                  saved27 = nextValue0;
                  saved33 = nextValue1;
                  step = 79;
                  continue dispatch;
               }
            }
            default -> throw new IllegalStateException("Invalid control-flow state");
         }
      }
   }

   private void clear() {
      this.entityCounts.clear();
      this.oceanChunks.clear();
      this.notifiedChunks.clear();
      this.scanTicks = 0;
   }

   private static boolean lambda$refreshOceanChunks$0(ChunkPos center, int radius, ChunkPos chunk) {
      if ((chunk != null)) {
         var field1 = chunk.x;
         if ((center != null)) {
            var field2 = center.x;
            var selected1 = ((field2 < field1) ? (field1 - field2) : (field2 - field1));
            if (((radius + 2) < selected1)) {
               return true;
            }
            var field3 = chunk.z;
            var field4 = center.z;
            var selected2 = ((field4 < field3) ? (field3 - field4) : (field4 - field3));
            return !(Integer.compareUnsigned(selected2, (radius + 2)) <= 0);
         } else {
            throw new java.lang.NullPointerException("object reference is null");
         }
      } else {
         throw new java.lang.NullPointerException("object reference is null");
      }
   }
}
