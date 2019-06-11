package jboy;

import java.awt.Color;

import PicoEngine.Window;

public class GPU {
    Window win = new Window(160, 144, 1, 1, "jBoy");

    public int[] vram = new int[8192];
    public int[] oam = new int[160];
    public int[][][] tilemap = new int[512][8][8];

    int curline = 0;
    int curscan = 0;
    int linemode = 2;
    int modeclocks = 0;
    int yscrl = 0;
    int xscrl = 0;
    int raster = 0;
    int ints = 0;

    int lcdon = 0;
    int bgon = 0;
    int objon = 0;
    int winon = 0;

    int objsize = 0;

    int bgtilebase = 0x0000;
    int bgmapbase = 0x1800;
    int wintilebase = 0x1800;

    int[] scanrow = new int[160];

    private class Palette {
        int[] bg = new int[4];
        int[] obj0 = new int[4];
        int[] obj1 = new int[4];
    }

    private class Object {
        int y = -16;
        int x = -8;

        int tile = 0;
        int palette = 0;

        int xflip = 0;
        int yflip = 0;

        int prio = 0;

        int num;

        public Object(int num) {
            this.num = num;
        }
    }

    Palette palette = new Palette();
    Object[] objdata = new Object[40];

    public void reset() {
        for(int i=0; i<8192; i++) {
            vram[i] = 0;
        }
        for(int i=0; i<160; i++) {
            oam[i] = 0;
        }
        for(int i=0; i<4; i++) {
            palette.bg[i] = 255;
            palette.obj0[i] = 255;
            palette.obj1[i] = 255;
        }
        for(int i=0;i<512;i++){
            for(int j=0;j<8;j++){
                for(int k=0;k<8;k++){
                    tilemap[i][j][k] = 0;
                }
            }
        }

        System.out.println("Starting GC");
        win.autoConfig(Color.gray);

        curline=0;
        curscan=0;
        linemode=2;
        modeclocks=0;
        yscrl=0;
        xscrl=0;
        raster=0;
        ints = 0;

        lcdon = 0;
        bgon = 0;
        objon = 0;
        winon = 0;

        objsize = 0;
        for(int i=0; i<160; i++){
            scanrow[i] = 0;
        }

        for(int i=0; i<40; i++){
            objdata[i] = new Object(i);
        }

        // Set to values expected by BIOS, to start
        bgtilebase = 0x0000;
        bgmapbase = 0x1800;
        wintilebase = 0x1800;

        System.out.println("GPU reset");
    }

    public int rb(int addr) {
        return 0;
    }

    public void updateTile(int addr, int val) {

    }

    public void updateOAM(int addr, int val) {

    }

    public void wb(int addr, int val) {

    }
}