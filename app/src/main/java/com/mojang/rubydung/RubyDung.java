package com.mojang.rubydung;

import com.mojang.rubydung.level.Chunk;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelRenderer;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.swing.JOptionPane;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class RubyDung implements Runnable {
    private static final boolean FULLSCREEN_MODE = false;

    private int width;

    private int height;

    private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);

    private Timer timer = new Timer(60.0F);

    private Level level;

    private LevelRenderer levelRenderer;

    private Player player;

    public void init() throws LWJGLException, IOException {
        int col = 920330;
        float fr = 0.5F;
        float fg = 0.8F;
        float fb = 1.0F;
        this.fogColor.put(new float[] { (col >> 16 & 0xFF) / 255.0F, (col >> 8 & 0xFF) / 255.0F, (col & 0xFF) / 255.0F, 1.0F });
        this.fogColor.flip();
        Display.setDisplayMode(new DisplayMode(1024, 768));
        Display.create();
        Keyboard.create();
        Mouse.create();
        this.width = Display.getDisplayMode().getWidth();
        this.height = Display.getDisplayMode().getHeight();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(fr, fg, fb, 0.0F);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        this.level = new Level(256, 256, 64);
        this.levelRenderer = new LevelRenderer(this.level);
        this.player = new Player(this.level);
        Mouse.setGrabbed(true);
    }

    public void destroy(){
        this.level.save();
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    public void run() {
        try {
            init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString(), "Failed to start RubyDung", 0);
            System.exit(0);
        }
        long lastTime = System.currentTimeMillis();
        int frames = 0;
        try {
            while(!Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && !Display.isCloseRequested()){
                this.timer.advanceTime();
                for(int i = 0; i < this.timer.ticks; i++)
                    tick();
                render(this.timer.a);
                frames++;
                while(System.currentTimeMillis() >= lastTime + 1000L){
                    System.out.println(String.valueOf(frames) + " fps, " + Chunk.updates);
                    Chunk.updates = 0;
                    lastTime += 1000L;
                    frames = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    public void tick(){
        this.player.tick();
    }

    private void moveCameraToPlayer(float a) {
        GL11.glTranslatef(0.0F, 0.0F, -0.3F);
        GL11.glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
        float x = this.player.xo + (this.player.x - this.player.xo) * a;
        float y = this.player.yo + (this.player.y - this.player.yo) * a;
        float z = this.player.zo + (this.player.z - this.player.zo) * a;
        GL11.glTranslatef(-x, -y, -z);
    }

    private void setupCamera(float a) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(70.0F, this.width / this.height, 0.05F, 1000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        moveCameraToPlayer(a);
    }

    private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);

    private void setupPickCamera(float a, int x, int y) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        this.viewportBuffer.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, this.viewportBuffer);
        this.viewportBuffer.flip();
        this.viewportBuffer.limit(16);
        GLU.gluPickMatrix(x, y, 5.0F, 5.0F, this.viewportBuffer);
        GLU.gluPerspective(70.0F, this.width / this.height, 0.05F, 1000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        moveCameraToPlayer(a);
    }

    private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);

    private HitResult hitResult = null;

    private void pick(float a) {
        this.selectBuffer.clear();
        GL11.glSelectBuffer(this.selectBuffer);
        GL11.glRenderMode(GL11.GL_SELECT);
        setupPickCamera(a, this.width / 2, this.height / 2);
        this.levelRenderer.pick(this.player);
        int hits = GL11.glRenderMode(GL11.GL_RENDER);
        this.selectBuffer.flip();
        this.selectBuffer.limit(this.selectBuffer.capacity());
        long closest = 0L;
        int[] names = new int[10];
        int hitNameCount = 0;
        for (int i = 0; i < hits; i++) {
            int nameCount = this.selectBuffer.get();
            long minZ = this.selectBuffer.get();
            this.selectBuffer.get();
            long dist = minZ;
            if (dist < closest || i == 0) {
                closest = dist;
                hitNameCount = nameCount;
                for (int j = 0; j < nameCount; j++)
                names[j] = this.selectBuffer.get(); 
            } else {
                for (int j = 0; j < nameCount; j++)
                this.selectBuffer.get(); 
            }
        }
        if (hitNameCount > 0) {
            this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
        } else {
            this.hitResult = null;
        }
    }

    public void render(float a){
        float xo = Mouse.getDX();
        float yo = Mouse.getDY();
        this.player.turn(xo, yo);
        pick(a);
        while (Mouse.next()) {
            if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState())
                if (this.hitResult != null)
                    this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);  
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState())
                if (this.hitResult != null) {
                    int x = this.hitResult.x;
                    int y = this.hitResult.y;
                    int z = this.hitResult.z;
                    if (this.hitResult.f == 0)
                        y--; 
                    if (this.hitResult.f == 1)
                        y++; 
                    if (this.hitResult.f == 2)
                        z--; 
                    if (this.hitResult.f == 3)
                        z++; 
                    if (this.hitResult.f == 4)
                        x--; 
                    if (this.hitResult.f == 5)
                        x++; 
                    this.level.setTile(x, y, z, 1);
                }
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && Keyboard.getEventKeyState())
                this.level.save();
        }
        GL11.glClear(16640);
        setupCamera(a);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_VIEWPORT_BIT);
        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.2F);
        GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor);
        GL11.glDisable(GL11.GL_FOG);
        this.levelRenderer.render(this.player, 0);
        GL11.glEnable(GL11.GL_FOG);
        this.levelRenderer.render(this.player, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (this.hitResult != null)
            this.levelRenderer.renderHit(this.hitResult);
        GL11.glDisable(GL11.GL_FOG);
        Display.update();
    }

    public static void checkError() {
        int e = GL11.glGetError();
        if (e != 0)
          throw new IllegalStateException(GLU.gluErrorString(e)); 
      }

    public static void main(String[] args) throws LWJGLException{
        (new Thread(new RubyDung())).start();
    }
}
