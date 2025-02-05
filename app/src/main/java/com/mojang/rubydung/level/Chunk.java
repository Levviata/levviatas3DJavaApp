package com.mojang.rubydung.level;

import com.mojang.rubydung.Textures;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.opengl.GL11;

public class Chunk {
    public AABB aabb;
  
    public final Level level;
    
    public final int x0;
    
    public final int y0;
    
    public final int z0;
    
    public final int x1;
    
    public final int y1;
    
    public final int z1;

    private boolean dirty = true;

    private int lists = -1;

    private static int texture = Textures.loadTexture("/terrain.png", GL11.GL_NEAREST);

    private static Tesselator t = new Tesselator();

    public static int rebuiltThisFrame = 0;
  
    public static int updates = 0;

    public Chunk(Level level, int x0, int y0, int z0, int x1, int y1, int z1){
        this.level = level;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.aabb = new AABB(x0, y0, z0, x1, y1, z1);
        this.lists = GL11.glGenLists(2);
    }

    private void rebuild(int layer){
        if (rebuiltThisFrame == 2)
            return;
        this.dirty = false;
        updates++;
        rebuiltThisFrame++;
        GL11.glNewList(this.lists + layer, GL11.GL_COMPILE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        t.init();
        int tiles = 0;
        for (int x = this.x0; x < this.x1; x++) {
            for (int y = this.y0; y < this.y1; y++) {
                for (int z = this.z0; z < this.z1; z++) {
                    if (this.level.isTile(x, y, z)) {
                        int tex = (y == this.level.depth * 2 / 3) ? 0 : 1;
                        tiles++;
                        if (tex == 0) {
                            Tile.rock.render(t, this.level, layer, x, y, z);
                        } else {
                            Tile.grass.render(t, this.level, layer, x, y, z);
                        }
                    }
                }
            }
        }
        t.flush();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEndList();
    }

    public void render(int layer){
        if(this.dirty){
            rebuild(0);
            rebuild(1);
        }
        GL11.glCallList(this.lists + layer);
    }

    public void setDirty(){
        this.dirty = true;
    }
}
