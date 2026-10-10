package com.zenya.module.smps;

import com.zenya.module.Category;
import com.zenya.module.Module;
import com.zenya.setting.ModeSetting;
import com.zenya.setting.Setting;
import java.util.Set;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public final class StashNotifierModule extends Module {
   private final Setting<Integer> minimumStorageCount;
   private final Setting<Integer> minimumDistance;
   private final Setting<Boolean> criticalSpawner;
   private final Setting<Boolean> disconnectOnFind;
   private final Setting<Boolean> notifications;
   private final ModeSetting notificationMode;
   private final Set<ChunkPos> processedChunks;

   public StashNotifierModule() {
      super("Stash Notifier", Category.SMPS);
      Setting<Integer> setting1 = new com.zenya.setting.Setting<>("Min Storage Count", java.lang.Integer.valueOf(4), java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(500));
      this.minimumStorageCount = setting1;
      Setting<Integer> setting2 = new com.zenya.setting.Setting<>("Min Distance", java.lang.Integer.valueOf(0), java.lang.Integer.valueOf(0), java.lang.Integer.valueOf(10000));
      this.minimumDistance = setting2;
      Setting<Boolean> setting3 = new com.zenya.setting.Setting<>("Critical Spawner", java.lang.Boolean.valueOf(true));
      this.criticalSpawner = setting3;
      Setting<Boolean> setting4 = new com.zenya.setting.Setting<>("Disconnect on Find", java.lang.Boolean.valueOf(false));
      this.disconnectOnFind = setting4;
      Setting<Boolean> setting5 = new com.zenya.setting.Setting<>("Notifications", java.lang.Boolean.valueOf(true));
      this.notifications = setting5;
      var array1 = new java.lang.String[3];
      array1[0] = "Chat";
      array1[1] = "Toast";
      array1[2] = "Both";
      ModeSetting modeSetting1 = new com.zenya.setting.ModeSetting("Notification Mode", "Chat", array1);
      this.notificationMode = modeSetting1;
      Set<ChunkPos> hashSet1 = new java.util.HashSet<>();
      this.processedChunks = hashSet1;
      this.setDescription("Notifies when loaded chunks contain many storage blocks or a critical spawner.");
      this.addSetting(this.minimumStorageCount);
      this.addSetting(this.minimumDistance);
      this.addSetting(this.criticalSpawner);
      this.addSetting(this.disconnectOnFind);
      this.addSetting(this.notifications);
      this.addSetting(this.notificationMode);
   }

   @Override
   public void onEnable() {
      this.processedChunks.clear();
   }

   @Override
   public void onDisable() {
      this.processedChunks.clear();
   }

   @Override
   public void onWorldChange() {
      this.processedChunks.clear();
   }

   @Override
   public void onTick() {
      var field1 = com.zenya.module.smps.StashNotifierModule.mc;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      } else {
         var field2 = field1.world;
         if ((field2 != null)) {
            var field3 = com.zenya.module.smps.StashNotifierModule.mc;
            if ((field3 == null)) {
               throw new java.lang.NullPointerException("object reference is null");
            } else {
               var field4 = field3.player;
               if ((field4 != null)) {
                  var field5 = com.zenya.module.smps.StashNotifierModule.mc;
                  if ((field5 == null)) {
                     throw new java.lang.NullPointerException("object reference is null");
                  } else {
                     var field6 = field5.player;
                     if ((field6 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     } else {
                        var result1 = field6.getChunkPos();
                        var result2 = com.zenya.module.ModuleUtils.viewDistanceChunks();
                        if ((result1 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           var field7 = result1.x;
                           int iteration1_1 = (field7 - result2);
                           repeat1: while (true) {
                              var field8 = result1.x;
                              if (((field8 + result2) < iteration1_1)) {
                                 return;
                              } else {
                                 var field9 = result1.z;
                                 int iteration2_1 = (field9 - result2);
                                 repeat2: while (true) {
                                    var field10 = result1.z;
                                    if (((field10 + result2) < iteration2_1)) {
                                       iteration1_1 = (iteration1_1 + 1);
                                       continue repeat1;
                                    } else {
                                       var saved1 = iteration1_1;
                                       var chunkPos1 = new net.minecraft.util.math.ChunkPos(saved1, iteration2_1);
                                       var field11 = this.processedChunks;
                                       if ((field11 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          var result3 = ((java.util.Set) field11).add(chunkPos1);
                                          if (result3) {
                                             var field12 = com.zenya.module.smps.StashNotifierModule.mc;
                                             if ((field12 == null)) {
                                                throw new java.lang.NullPointerException("object reference is null");
                                             } else {
                                                var field13 = field12.world;
                                                if ((field13 == null)) {
                                                   throw new java.lang.NullPointerException("object reference is null");
                                                } else {
                                                   var result4 = field13.getChunkManager();
                                                   if ((result4 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      var saved2 = iteration2_1;
                                                      var result5 = result4.getWorldChunk(iteration1_1, saved2, false);
                                                      if ((result5 != null)) {
                                                         this.scanChunk(result5, chunkPos1);
                                                         iteration2_1 = (iteration2_1 + 1);
                                                         continue repeat2;
                                                      } else {
                                                         iteration2_1 = (iteration2_1 + 1);
                                                         continue repeat2;
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
                  }
               } else {
                  return;
               }
            }
         } else {
            return;
         }
      }
   }

   private void scanChunk(WorldChunk chunk, ChunkPos chunkPos) {
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
                  boolean iteration1_1 = false;
                  Object iteration1_2 = result3;
                  int iteration1_3 = 0;
                  repeat1: while (true) {
                     var result4 = ((java.util.Iterator) iteration1_2).hasNext();
                     if (!(result4)) {
                        iteration1_2 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((iteration1_2 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        } else {
                           iteration1_2 = ((net.minecraft.client.MinecraftClient) iteration1_2).player;
                           if ((iteration1_2 == null)) {
                              throw new java.lang.NullPointerException("object reference is null");
                           } else {
                              var result5 = ((net.minecraft.client.network.ClientPlayerEntity) iteration1_2).getX();
                              if ((chunkPos == null)) {
                                 throw new java.lang.NullPointerException("object reference is null");
                              } else {
                                 var result6 = chunkPos.getCenterX();
                                 iteration1_2 = com.zenya.module.smps.StashNotifierModule.mc;
                                 if ((iteration1_2 == null)) {
                                    throw new java.lang.NullPointerException("object reference is null");
                                 } else {
                                    iteration1_2 = ((net.minecraft.client.MinecraftClient) iteration1_2).player;
                                    if ((iteration1_2 == null)) {
                                       throw new java.lang.NullPointerException("object reference is null");
                                    } else {
                                       var result7 = ((net.minecraft.client.network.ClientPlayerEntity) iteration1_2).getZ();
                                       var result8 = chunkPos.getCenterZ();
                                       iteration1_2 = this.minimumDistance;
                                       if ((iteration1_2 == null)) {
                                          throw new java.lang.NullPointerException("object reference is null");
                                       } else {
                                          iteration1_2 = ((com.zenya.setting.Setting) iteration1_2).getValue();
                                          if ((iteration1_2 == null)) {
                                             throw new java.lang.NullPointerException("object reference is null");
                                          } else {
                                             var saved1 = (iteration1_2 == null || iteration1_2 instanceof java.lang.Integer);
                                             if (saved1) {
                                                var result10 = ((java.lang.Integer) iteration1_2).intValue();
                                                var root1 = Math.sqrt((((result7 - (double) (result8)) * (result7 - (double) (result8))) + ((result5 - (double) (result6)) * (result5 - (double) (result6)))));
                                                if ((root1 < (double) (result10))) {
                                                   return;
                                                } else {
                                                   iteration1_2 = this.criticalSpawner;
                                                   if ((iteration1_2 == null)) {
                                                      throw new java.lang.NullPointerException("object reference is null");
                                                   } else {
                                                      iteration1_2 = ((com.zenya.setting.Setting) iteration1_2).getValue();
                                                      if ((iteration1_2 == null)) {
                                                         throw new java.lang.NullPointerException("object reference is null");
                                                      } else {
                                                         var saved2 = (iteration1_2 == null || iteration1_2 instanceof java.lang.Boolean);
                                                         if (saved2) {
                                                            var result12 = ((java.lang.Boolean) iteration1_2).booleanValue();
                                                            iteration1_2 = this.minimumStorageCount;
                                                            if ((iteration1_2 == null)) {
                                                               throw new java.lang.NullPointerException("object reference is null");
                                                            } else {
                                                               iteration1_2 = ((com.zenya.setting.Setting) iteration1_2).getValue();
                                                               if ((iteration1_2 == null)) {
                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                               } else {
                                                                  var saved3 = (iteration1_2 == null || iteration1_2 instanceof java.lang.Integer);
                                                                  if (saved3) {
                                                                     var result14 = ((java.lang.Integer) iteration1_2).intValue();
                                                                     if ((iteration1_3 < result14)) {
                                                                        if (!(result12)) {
                                                                           return;
                                                                        } else {
                                                                           if (!(iteration1_1)) {
                                                                              return;
                                                                           } else {
                                                                              var stringBuilder1 = new java.lang.StringBuilder();
                                                                              var result15 = stringBuilder1.append("Stash with \u00a7e");
                                                                              if ((result15 == null)) {
                                                                                 throw new java.lang.NullPointerException("object reference is null");
                                                                              } else {
                                                                                 var result16 = result15.append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                 if ((result16 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    var result17 = result16.append("\u00a7r storages at X: ");
                                                                                    iteration1_3 = chunkPos.getCenterX();
                                                                                    if ((result17 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       iteration1_2 = result17.append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                       if ((iteration1_2 == null)) {
                                                                                          throw new java.lang.NullPointerException("object reference is null");
                                                                                       } else {
                                                                                          iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(" Y: ");
                                                                                          var field8 = com.zenya.module.smps.StashNotifierModule.mc;
                                                                                          if ((field8 == null)) {
                                                                                             throw new java.lang.NullPointerException("object reference is null");
                                                                                          } else {
                                                                                             var field9 = field8.player;
                                                                                             if ((field9 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                iteration1_3 = field9.getBlockY();
                                                                                                if ((iteration1_2 == null)) {
                                                                                                   throw new java.lang.NullPointerException("object reference is null");
                                                                                                } else {
                                                                                                   iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                                   if ((iteration1_2 == null)) {
                                                                                                      throw new java.lang.NullPointerException("object reference is null");
                                                                                                   } else {
                                                                                                      iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(" Z: ");
                                                                                                      iteration1_3 = chunkPos.getCenterZ();
                                                                                                      if ((iteration1_2 == null)) {
                                                                                                         throw new java.lang.NullPointerException("object reference is null");
                                                                                                      } else {
                                                                                                         iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                                         var selected1 = ((result12 && iteration1_1) ? 18841L : 25L);
                                                                                                         var text1 = (selected1 == 25L ? "" : " \u00a7c(Spawner!)");
                                                                                                         if ((iteration1_2 == null)) {
                                                                                                            throw new java.lang.NullPointerException("object reference is null");
                                                                                                         } else {
                                                                                                            iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(text1);
                                                                                                            if ((iteration1_2 == null)) {
                                                                                                               throw new java.lang.NullPointerException("object reference is null");
                                                                                                            } else {
                                                                                                               iteration1_2 = ((java.lang.StringBuilder) iteration1_2).toString();
                                                                                                               this.notify(((java.lang.String) iteration1_2));
                                                                                                               iteration1_2 = this.disconnectOnFind;
                                                                                                               if ((iteration1_2 == null)) {
                                                                                                                  throw new java.lang.NullPointerException("object reference is null");
                                                                                                               } else {
                                                                                                                  iteration1_2 = ((com.zenya.setting.Setting) iteration1_2).getValue();
                                                                                                                  if ((iteration1_2 == null)) {
                                                                                                                     throw new java.lang.NullPointerException("object reference is null");
                                                                                                                  } else {
                                                                                                                     var saved4 = (iteration1_2 == null || iteration1_2 instanceof java.lang.Boolean);
                                                                                                                     if (saved4) {
                                                                                                                        var result29 = ((java.lang.Boolean) iteration1_2).booleanValue();
                                                                                                                        if (!(result29)) {
                                                                                                                           return;
                                                                                                                        } else {
                                                                                                                           iteration1_2 = "Stash found";
                                                                                                                           com.zenya.module.ModuleUtils.disconnect(((java.lang.String) iteration1_2));
                                                                                                                           return;
                                                                                                                        }
                                                                                                                     } else {
                                                                                                                        throw new java.lang.ClassCastException("");
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
                                                                        }
                                                                     } else {
                                                                        var stringBuilder1 = new java.lang.StringBuilder();
                                                                        var result15 = stringBuilder1.append("Stash with \u00a7e");
                                                                        if ((result15 == null)) {
                                                                           throw new java.lang.NullPointerException("object reference is null");
                                                                        } else {
                                                                           var result16 = result15.append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                           if ((result16 == null)) {
                                                                              throw new java.lang.NullPointerException("object reference is null");
                                                                           } else {
                                                                              var result17 = result16.append("\u00a7r storages at X: ");
                                                                              iteration1_3 = chunkPos.getCenterX();
                                                                              if ((result17 == null)) {
                                                                                 throw new java.lang.NullPointerException("object reference is null");
                                                                              } else {
                                                                                 iteration1_2 = result17.append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                 if ((iteration1_2 == null)) {
                                                                                    throw new java.lang.NullPointerException("object reference is null");
                                                                                 } else {
                                                                                    iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(" Y: ");
                                                                                    var field8 = com.zenya.module.smps.StashNotifierModule.mc;
                                                                                    if ((field8 == null)) {
                                                                                       throw new java.lang.NullPointerException("object reference is null");
                                                                                    } else {
                                                                                       var field9 = field8.player;
                                                                                       if ((field9 == null)) {
                                                                                          throw new java.lang.NullPointerException("object reference is null");
                                                                                       } else {
                                                                                          iteration1_3 = field9.getBlockY();
                                                                                          if ((iteration1_2 == null)) {
                                                                                             throw new java.lang.NullPointerException("object reference is null");
                                                                                          } else {
                                                                                             iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                             if ((iteration1_2 == null)) {
                                                                                                throw new java.lang.NullPointerException("object reference is null");
                                                                                             } else {
                                                                                                iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(" Z: ");
                                                                                                iteration1_3 = chunkPos.getCenterZ();
                                                                                                if ((iteration1_2 == null)) {
                                                                                                   throw new java.lang.NullPointerException("object reference is null");
                                                                                                } else {
                                                                                                   iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append((int) (Integer.toUnsignedLong(iteration1_3)));
                                                                                                   var selected1 = ((result12 && iteration1_1) ? 18841L : 25L);
                                                                                                   var text1 = (selected1 == 25L ? "" : " \u00a7c(Spawner!)");
                                                                                                   if ((iteration1_2 == null)) {
                                                                                                      throw new java.lang.NullPointerException("object reference is null");
                                                                                                   } else {
                                                                                                      iteration1_2 = ((java.lang.StringBuilder) iteration1_2).append(text1);
                                                                                                      if ((iteration1_2 == null)) {
                                                                                                         throw new java.lang.NullPointerException("object reference is null");
                                                                                                      } else {
                                                                                                         iteration1_2 = ((java.lang.StringBuilder) iteration1_2).toString();
                                                                                                         this.notify(((java.lang.String) iteration1_2));
                                                                                                         iteration1_2 = this.disconnectOnFind;
                                                                                                         if ((iteration1_2 == null)) {
                                                                                                            throw new java.lang.NullPointerException("object reference is null");
                                                                                                         } else {
                                                                                                            iteration1_2 = ((com.zenya.setting.Setting) iteration1_2).getValue();
                                                                                                            if ((iteration1_2 == null)) {
                                                                                                               throw new java.lang.NullPointerException("object reference is null");
                                                                                                            } else {
                                                                                                               var saved4 = (iteration1_2 == null || iteration1_2 instanceof java.lang.Boolean);
                                                                                                               if (saved4) {
                                                                                                                  var result29 = ((java.lang.Boolean) iteration1_2).booleanValue();
                                                                                                                  if (!(result29)) {
                                                                                                                     return;
                                                                                                                  } else {
                                                                                                                     iteration1_2 = "Stash found";
                                                                                                                     com.zenya.module.ModuleUtils.disconnect(((java.lang.String) iteration1_2));
                                                                                                                     return;
                                                                                                                  }
                                                                                                               } else {
                                                                                                                  throw new java.lang.ClassCastException("");
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
                                                                  } else {
                                                                     throw new java.lang.ClassCastException("");
                                                                  }
                                                               }
                                                            }
                                                         } else {
                                                            throw new java.lang.ClassCastException("");
                                                         }
                                                      }
                                                   }
                                                }
                                             } else {
                                                throw new java.lang.ClassCastException("");
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     } else {
                        var result5 = ((java.util.Iterator) iteration1_2).next();
                        if ((result5 != null)) {
                           if (!((result5 == null || result5 instanceof net.minecraft.block.entity.BlockEntity))) {
                              throw new java.lang.ClassCastException("");
                           } else {
                              var result6 = com.zenya.module.ModuleUtils.isStorage(((net.minecraft.block.entity.BlockEntity) result5));
                              iteration1_3 = ((iteration1_3 + 1) - (!(result6) ? 1 : 0));
                              if ((result5 != null)) {
                                 if ((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity)) {
                                    iteration1_1 = true;
                                 } else {
                                 }
                                 iteration1_3 = (iteration1_3 + ((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity) ? 1 : 0));
                                 continue repeat1;
                              } else {
                                 continue repeat1;
                              }
                           }
                        } else {
                           var result6 = com.zenya.module.ModuleUtils.isStorage(((net.minecraft.block.entity.BlockEntity) result5));
                           iteration1_3 = ((iteration1_3 + 1) - (!(result6) ? 1 : 0));
                           if ((result5 != null)) {
                              if ((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity)) {
                                 iteration1_1 = true;
                              } else {
                              }
                              iteration1_3 = (iteration1_3 + ((result5 == null || result5 instanceof net.minecraft.block.entity.MobSpawnerBlockEntity) ? 1 : 0));
                              continue repeat1;
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

   private void notify(String message) {
      var field1 = this.notifications;
      if ((field1 == null)) {
         throw new java.lang.NullPointerException("object reference is null");
      }
      var result1 = field1.getValue();
      if ((result1 != null)) {
         if (!((result1 == null || result1 instanceof java.lang.Boolean))) {
            throw new java.lang.ClassCastException("");
         }
         var result2 = ((java.lang.Boolean) result1).booleanValue();
         if (!(result2)) {
            return;
         }
         var field2 = this.notificationMode;
         if ((field2 != null)) {
            var result3 = field2.is("Chat");
            if (!(result3)) {
               var field3 = this.notificationMode;
               if ((field3 == null)) {
                  throw new java.lang.NullPointerException("object reference is null");
               }
               var result4 = field3.is("Both");
               if (result4) {
                  com.zenya.module.ModuleUtils.chat("Stash Notifier", message);
                  var field4 = this.notificationMode;
                  if ((field4 != null)) {
                     var result5 = field4.is("Toast");
                     if (!(result5)) {
                        var field5 = this.notificationMode;
                        if ((field5 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        }
                        var result6 = field5.is("Both");
                        if (!(result6)) {
                           return;
                        }
                        var field6 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field6 != null)) {
                           var result7 = field6.getToastManager();
                           if ((result7 == null)) {
                              return;
                           }
                           var field7 = com.zenya.module.smps.StashNotifierModule.mc;
                           if ((field7 != null)) {
                              var result8 = field7.getToastManager();
                              var field8 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                              var result9 = net.minecraft.text.Text.literal("Stash Notifier");
                              if ((message != null)) {
                                 var result10 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                                 if ((result10 != null)) {
                                    var result11 = result10.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                    if ((result11 != null)) {
                                       var result12 = result11.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                       var result13 = net.minecraft.text.Text.literal(result12);
                                       net.minecraft.client.toast.SystemToast.show(result8, field8, ((net.minecraft.text.Text) result9), ((net.minecraft.text.Text) result13));
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
                        var field5 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field5 != null)) {
                           var result6 = field5.getToastManager();
                           if ((result6 == null)) {
                              return;
                           }
                           var field6 = com.zenya.module.smps.StashNotifierModule.mc;
                           if ((field6 != null)) {
                              var result7 = field6.getToastManager();
                              var field7 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                              var result8 = net.minecraft.text.Text.literal("Stash Notifier");
                              if ((message != null)) {
                                 var result9 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                                 if ((result9 != null)) {
                                    var result10 = result9.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                    if ((result10 != null)) {
                                       var result11 = result10.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                       var result12 = net.minecraft.text.Text.literal(result11);
                                       net.minecraft.client.toast.SystemToast.show(result7, field7, ((net.minecraft.text.Text) result8), ((net.minecraft.text.Text) result12));
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
                     }
                  } else {
                     throw new java.lang.NullPointerException("object reference is null");
                  }
               } else {
                  var field4 = this.notificationMode;
                  if ((field4 != null)) {
                     var result5 = field4.is("Toast");
                     if (!(result5)) {
                        var field5 = this.notificationMode;
                        if ((field5 == null)) {
                           throw new java.lang.NullPointerException("object reference is null");
                        }
                        var result6 = field5.is("Both");
                        if (!(result6)) {
                           return;
                        }
                        var field6 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field6 != null)) {
                           var result7 = field6.getToastManager();
                           if ((result7 == null)) {
                              return;
                           }
                           var field7 = com.zenya.module.smps.StashNotifierModule.mc;
                           if ((field7 != null)) {
                              var result8 = field7.getToastManager();
                              var field8 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                              var result9 = net.minecraft.text.Text.literal("Stash Notifier");
                              if ((message != null)) {
                                 var result10 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                                 if ((result10 != null)) {
                                    var result11 = result10.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                    if ((result11 != null)) {
                                       var result12 = result11.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                       var result13 = net.minecraft.text.Text.literal(result12);
                                       net.minecraft.client.toast.SystemToast.show(result8, field8, ((net.minecraft.text.Text) result9), ((net.minecraft.text.Text) result13));
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
                        var field5 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field5 != null)) {
                           var result6 = field5.getToastManager();
                           if ((result6 == null)) {
                              return;
                           }
                           var field6 = com.zenya.module.smps.StashNotifierModule.mc;
                           if ((field6 != null)) {
                              var result7 = field6.getToastManager();
                              var field7 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                              var result8 = net.minecraft.text.Text.literal("Stash Notifier");
                              if ((message != null)) {
                                 var result9 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                                 if ((result9 != null)) {
                                    var result10 = result9.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                    if ((result10 != null)) {
                                       var result11 = result10.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                       var result12 = net.minecraft.text.Text.literal(result11);
                                       net.minecraft.client.toast.SystemToast.show(result7, field7, ((net.minecraft.text.Text) result8), ((net.minecraft.text.Text) result12));
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
                     }
                  } else {
                     throw new java.lang.NullPointerException("object reference is null");
                  }
               }
            } else {
               com.zenya.module.ModuleUtils.chat("Stash Notifier", message);
               var field3 = this.notificationMode;
               if ((field3 != null)) {
                  var result4 = field3.is("Toast");
                  if (!(result4)) {
                     var field4 = this.notificationMode;
                     if ((field4 == null)) {
                        throw new java.lang.NullPointerException("object reference is null");
                     }
                     var result5 = field4.is("Both");
                     if (!(result5)) {
                        return;
                     }
                     var field5 = com.zenya.module.smps.StashNotifierModule.mc;
                     if ((field5 != null)) {
                        var result6 = field5.getToastManager();
                        if ((result6 == null)) {
                           return;
                        }
                        var field6 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field6 != null)) {
                           var result7 = field6.getToastManager();
                           var field7 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                           var result8 = net.minecraft.text.Text.literal("Stash Notifier");
                           if ((message != null)) {
                              var result9 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                              if ((result9 != null)) {
                                 var result10 = result9.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                 if ((result10 != null)) {
                                    var result11 = result10.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                    var result12 = net.minecraft.text.Text.literal(result11);
                                    net.minecraft.client.toast.SystemToast.show(result7, field7, ((net.minecraft.text.Text) result8), ((net.minecraft.text.Text) result12));
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
                     var field4 = com.zenya.module.smps.StashNotifierModule.mc;
                     if ((field4 != null)) {
                        var result5 = field4.getToastManager();
                        if ((result5 == null)) {
                           return;
                        }
                        var field5 = com.zenya.module.smps.StashNotifierModule.mc;
                        if ((field5 != null)) {
                           var result6 = field5.getToastManager();
                           var field6 = net.minecraft.client.toast.SystemToast.Type.PERIODIC_NOTIFICATION;
                           var result7 = net.minecraft.text.Text.literal("Stash Notifier");
                           if ((message != null)) {
                              var result8 = message.replace(((java.lang.CharSequence) "\u00a7e"), ((java.lang.CharSequence) ""));
                              if ((result8 != null)) {
                                 var result9 = result8.replace(((java.lang.CharSequence) "\u00a7r"), ((java.lang.CharSequence) ""));
                                 if ((result9 != null)) {
                                    var result10 = result9.replace(((java.lang.CharSequence) "\u00a7c"), ((java.lang.CharSequence) ""));
                                    var result11 = net.minecraft.text.Text.literal(result10);
                                    net.minecraft.client.toast.SystemToast.show(result6, field6, ((net.minecraft.text.Text) result7), ((net.minecraft.text.Text) result11));
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
                  }
               } else {
                  throw new java.lang.NullPointerException("object reference is null");
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
